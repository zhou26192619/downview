package com.loar.downview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileDownloadThread extends Thread {

    public static final String FILE_BEAN = "fileBean";
    private static final String TAG = "FileDownloadThread";
    private FileService fileService;
    private Handler handler;
    private FileBean fileBean;
    //所要保存的文件
    private File saveFile;
    //下载过的文件
    private File formerFile;

    private boolean isExit = false;

    /**
     * 异常次数
     */
    private int exceptionCount = 0;

    /**
     * @param title
     * @param url     下载路径
     * @param path    保存路径
     * @param name    文件名
     * @param md5
     * @param handler 下载回调
     */
    public FileDownloadThread(Context context, String title, String url, String path, String name, Bitmap icon, String md5, Handler handler, boolean isVisibility, boolean isSupportBreak) {
        fileService = new FileService(context);
        this.handler = handler;
        if (name == null) {
            name = url.substring(url.lastIndexOf('/') + 1);
        }
        fileBean = new FileBean();
        fileBean.setDownpath(url);
        fileBean.setTitle(title);
        fileBean.setSavePath(path);
        fileBean.setName(name);
        fileBean.setMd5(md5);
        fileBean.setVisibility(isVisibility);
        fileBean.setSupportBreak(isSupportBreak);
        fileBean.setIcon(icon);
        fileBean.setDownloadBeginTime(System.currentTimeMillis());
        try {
            saveFile = new File(path, name);
            FileBean formerBean = fileService.getFileBean(url);
            if (formerBean != null) {
                formerFile = new File(formerBean.getSavePath(), formerBean.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileDownloadThread(Context context, @NonNull FileBean fileBean, Handler handler) {
        this.fileBean = fileBean;
        this.handler = handler;
        try {
            saveFile = new File(fileBean.getSavePath(), fileBean.getName());
            fileService = new FileService(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件长度
     *
     * @throws Exception
     */
    public void searchFileLength() throws Exception {
        URL downloadUrl = new URL(fileBean.getDownpath());
        HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
        conn.setConnectTimeout(CONNECTION_TIME_OUT);
        conn.setRequestMethod(CONNECTION_METHOD);
        conn.setRequestProperty("Accept", CONNECTION_ACCEPT);
        conn.setRequestProperty("Accept-Language", CONNECTION_ACCEPT_LANGUAGE);
        conn.setRequestProperty("Referer", fileBean.getDownpath());
        conn.setRequestProperty("Charset", CONNECTION_ChARSET);
        conn.setRequestProperty("User-Agent", CONNECTION_USER_AGENT);
        conn.setRequestProperty("Connection", CONNECTION_CONNECTION);
        conn.connect();

        if (conn.getResponseCode() == 200) {// 响应成功
            //初始化文件长度
            fileBean.setFileSize(conn.getContentLength());
            if (fileBean.getFileSize() <= 0) {
                throw new FileDownloadException(FileDownloadException.FileDownloadExceptionType.SERVER_ERROR);
            }
        } else {
            print("服务器响应错误:" + conn.getResponseCode() + "," + conn.getResponseMessage());
            throw new FileDownloadException(FileDownloadException.FileDownloadExceptionType.SERVER_ERROR);
        }
    }

    @Override
    public void run() {
        RandomAccessFile randOut = null;
        InputStream inStream = null;
        try {
            //文件前期判断
            if (formerFile != null && formerFile.exists()) {
                //如果以前下载过文件，则比较文件是否相同（比较md5值）
                if (checkMd5(formerFile, fileBean.getMd5())) {
                    //如果相同，拷贝文件到指定目录
                    if (!formerFile.getAbsolutePath().equals(saveFile.getAbsolutePath())) {
                        copyFile(formerFile, saveFile);
                    }
                    fileBean.setState(DownloadStatus.COMPLETE);
                    fileBean.setDownLength(formerFile.length());
                    fileBean.setFileSize(formerFile.length());
                    sendMsg();
                    return;
                }
            }
            if (saveFile != null && saveFile.exists()) {
                //保存的路径下有该文件，则比较文件是否相同（比较md5值）
                if (checkMd5(saveFile, fileBean.getMd5())) {
                    //如果相同,直接返回
                    fileBean.setState(DownloadStatus.COMPLETE);
                    fileBean.setDownLength(saveFile.length());
                    fileBean.setFileSize(saveFile.length());
                    sendMsg();
                    return;
                }
            }
            //如果文件不存在
            if (saveFile != null && !saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            searchFileLength();
            long startPos = 0;
            if (fileBean.isSupportBreak()) {
                startPos = saveFile.length();
            }
            long endPos = fileBean.getFileSize();
            fileBean.setDownLength(startPos);
            fileBean.setState(DownloadStatus.PROCESSING);
            Log.e(TAG, fileBean.toString());

            //创建文件
            randOut = new RandomAccessFile(saveFile, "rwd");
            URL url = new URL(fileBean.getDownpath());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(CONNECTION_TIME_OUT);
            http.setRequestMethod(CONNECTION_METHOD);
            http.setRequestProperty("Accept", CONNECTION_ACCEPT);
            http.setRequestProperty("Accept-Language", CONNECTION_ACCEPT_LANGUAGE);
            http.setRequestProperty("Charset", CONNECTION_ChARSET);
            http.setRequestProperty("User-Agent", CONNECTION_USER_AGENT);
            http.setRequestProperty("Connection", CONNECTION_CONNECTION);
            http.setRequestProperty("Referer", fileBean.getDownpath());
            http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            inStream = http.getInputStream();

            byte[] buffer = new byte[1024 * 8];
            int offset;
            long dance = 0;
            randOut.seek(startPos);
            while (!isExit && (offset = inStream.read(buffer, 0, 1024 * 8)) != -1) {
                randOut.write(buffer, 0, offset);
                startPos += offset;
                dance += offset;
                fileBean.setDownLength(startPos);
                fileBean.setDownloadLastTime(System.currentTimeMillis());
                if (dance > 1024L * 128) {//回调间隔
                    dance = 0;
                    sendMsg();
                }
            }
            if (isExit) {
                fileBean.setState(DownloadStatus.STOP);
            }
            //下载完成
            if (fileBean.getFileSize() == fileBean.getDownLength()) {
                fileBean.setDownloadCompleteTime(System.currentTimeMillis());
                // MD5校验
                if (TextUtils.isEmpty(fileBean.getMd5()) || checkMd5(saveFile, fileBean.getMd5())) {
                    fileBean.setState(DownloadStatus.COMPLETE);
                } else {
                    fileBean.setState(DownloadStatus.FAILURE);
                    fileBean.setExtraInfo(FileDownloadException.FileDownloadExceptionType.MD5_ERROR.getMsg());
                    saveFile.delete();
                }
            }
            sendMsg();
            Log.e(TAG, fileBean.toString());
            //保存文件进度
            fileService.save(fileBean);
        } catch (Exception e) {
            e.printStackTrace();
            reTry(e);
        } finally {
            try {
                if (randOut != null) {
                    randOut.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMsg() {
        if (handler != null) {
            Message msg = Message.obtain();
            msg.getData().putParcelable(FILE_BEAN, fileBean);
            handler.sendMessage(msg);
        }
    }

    private static void copyFile(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * MD5校验
     *
     * @param md5
     * @throws FileDownloadException
     */
    private boolean checkMd5(File file, String md5) {
        if (TextUtils.isEmpty(md5)) {
            return false;
        }
        //MD5校验
        String md5Str = md5Hex(file);
        if (TextUtils.isEmpty(md5Str)) {
            return false;
        }
        return md5.equals(md5Str);
    }

    /**
     * 重试
     *
     * @param e
     */
    protected void reTry(Exception e) {
        exceptionCount++;
        if (exceptionCount < 5) {
            try {
                Thread.sleep(ThreadPoolFactory.getTimeRepeat());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            run();
        } else {
            //下载失败
            fileBean.setState(DownloadStatus.FAILURE);
            if (e instanceof FileDownloadException) {
                fileBean.setExtraInfo(((FileDownloadException) e).getExceptionType().getMsg());
            } else {
                fileBean.setExtraInfo(FileDownloadException.FileDownloadExceptionType.NET_EXCEPTION.getMsg());
            }
            sendMsg();
            //保存文件进度
            fileService.save(fileBean);
        }
    }

    public final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public String md5Hex(File file) {
        BufferedInputStream in = null;
        try {
            if (file == null || !file.exists() || file.length() <= 0) {
                return null;
            }

            try {
                long length = file.length();
                int size = (int) Math.min(65536, length);
                byte[] buf = new byte[size];
                MessageDigest digest = MessageDigest.getInstance("MD5");
                in = new BufferedInputStream(new FileInputStream(file), buf.length);
                int num_read;
                while ((num_read = in.read(buf)) != -1) {
                    digest.update(buf, 0, num_read);
                }
                return new String(encode(digest.digest()));
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public char[] encode(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = hexChar[(0xF0 & data[i]) >>> 4];
            out[j++] = hexChar[0x0F & data[i]];
        }

        return out;
    }

    private static void print(String msg) {
        Log.i(TAG, msg);
    }

    private final int CONNECTION_TIME_OUT = 5 * 1000;
    private final String CONNECTION_METHOD = "GET";
    private final String CONNECTION_ChARSET = "UTF-8";
    private final String CONNECTION_ACCEPT = "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*";
    private final String CONNECTION_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";
    private final String CONNECTION_CONNECTION = "Keep-Alive";
    private final String CONNECTION_ACCEPT_LANGUAGE = "zh-CN";

    /**
     * 获取HttpURLConnection返回的头部信息
     *
     * @param http
     * @return
     */
    public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0; ; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null)
                break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }

    /**
     * 打印HttpURLConnection头部信息
     *
     * @param http
     */
    public static void printResponseHeader(HttpURLConnection http) {
        Map<String, String> header = getHttpResponseHeader(http);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey() + ":" : "";
            print(key + entry.getValue());
        }
    }

    public File getSaveFile() {
        return saveFile;
    }

    public FileBean getFileBean() {
        return fileBean;
    }

    public void exit() {
        isExit = true;
    }
}
