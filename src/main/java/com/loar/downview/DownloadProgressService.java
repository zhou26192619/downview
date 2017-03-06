package com.loar.downview;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 下载进度显示服务
 * Created by Justsy on 2016/6/6.
 */
public class DownloadProgressService extends CoverService {

    public static final String ACTION_DOWNLOAD_COMPLETE = "com.loar.downview.complete";
    public static final String KEY_RESULT = "result";
    public static final String KEY_URI = "url";
    public static final String KEY_PATH = "path";
    public static final String KEY_ICON = "icon";
    public static final String KEY_NAME = "name";
    public static final String KEY_MD5 = "md5";
    public static final String KEY_TITLE = "tvTitle";
    public static final String KEY_VISIBILITY = "visibility";
    public static final String KEY_IS_SUPPORT_BREAK = "isSupportBreak";

    public static final long TIME_FAIL_DURATION = 1000L * 60 * 4;

    private TextView tvTitle;
    private ProgressBar pb;
    private RelativeLayout rlRoot;
    private TextView tvPercent;
    private ImageView ivIcon;

    private final CopyOnWriteArrayList<FileDownloadThread> tasks = new CopyOnWriteArrayList<>();
    private int index;
    private long lastTime = System.currentTimeMillis();

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                FileBean fileBean = msg.getData().getParcelable(FileDownloadThread.FILE_BEAN);
                if (fileBean == null) return;

                if (DownloadStatus.COMPLETE == fileBean.getState() || DownloadStatus.FAILURE == fileBean.getState()) {
                    deleteTask(fileBean);
                    //通知下载结束
                    Intent in = new Intent(DownloadProgressService.ACTION_DOWNLOAD_COMPLETE);
                    in.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    in.putExtra(KEY_RESULT, fileBean);
                    sendBroadcast(in);

                    if (DownloadStatus.FAILURE == fileBean.getState()) {
                        Toast.makeText(getApplicationContext(), fileBean.getExtraInfo() + "，" + fileBean.getTitle() + "下载失败！", Toast.LENGTH_SHORT).show();
                    } else if (DownloadStatus.COMPLETE == fileBean.getState()) {
                        Toast.makeText(getApplicationContext(), fileBean.getTitle() + "下载成功！", Toast.LENGTH_SHORT).show();
                    }
                    if (tasks.isEmpty()) {
                        stopSelf();
                        return;
                    }
                }
                FileDownloadThread currentTask = takeCurrentTask();
//                if (fileBean.getDownpath().equals(currentTask.getFileBean().getDownpath())) {
                pb.setMax(100);
                tvTitle.setText(currentTask.getFileBean().getTitle());
                int p = (int) (currentTask.getFileBean().getDownLength() * 100F / currentTask.getFileBean().getFileSize());
                tvPercent.setText("已下载" + p + "%");
                pb.setProgress(p);
                tvTitle.setText(currentTask.getFileBean().getTitle());
                ivIcon.setImageBitmap(currentTask.getFileBean().getIcon());
//                }
                if (isTopApp()){
                    rlRoot.setVisibility(View.VISIBLE);
                }else{
                    rlRoot.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 判断自身应用是否是顶层应用
     * @return
     */
    public boolean isTopApp() {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                //前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(getApplicationContext().getPackageName())) {
                            return true;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            if (taskInfo != null) {
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equals(getApplicationContext().getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }



    /**
     * 获取当前的任务
     *
     * @return
     */
    private FileDownloadThread takeCurrentTask() {
        index = ((index >= tasks.size()) ? 0 : index);
        if (System.currentTimeMillis() - lastTime > 2000) {//切换显示间隔
            lastTime = System.currentTimeMillis();
            index = ((index + 1 >= tasks.size()) ? 0 : index + 1);
        }
        return tasks.get(index);
    }

    private void deleteTask(FileBean fileBean) {
//        synchronized (tasks) {
//            Iterator<FileDownloadThread> it = tasks.iterator();
//            while (it.hasNext()) {
//                FileDownloadThread task = it.next();
//                if (task.getFileBean().getDownpath().equals(fileBean.getDownpath())) {
//                    it.remove();
//                }
//            }
//        }
        for (FileDownloadThread task : tasks) {
            if (task.getFileBean().getDownpath().equals(fileBean.getDownpath())) {
                tasks.remove(task);
            }
        }
    }

    private void addTask(FileDownloadThread thread) {
//        synchronized (tasks) {
        if (!isContainTask(thread.getFileBean())) {
            tasks.add(thread);
        }
//        }
    }

    private boolean isContainTask(FileBean fileBean) {
        for (FileDownloadThread task : tasks) {
            if (task.getFileBean().getDownpath().equals(fileBean.getDownpath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    WindowManager.LayoutParams inflateLayoutParams() {
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        return wmParams;
    }

    @Override
    View inflateView() {
        View view = View.inflate(getApplicationContext(), R.layout.dialog_download_progress, null);
        rlRoot = (RelativeLayout) view.findViewById(R.id.root);
        tvTitle = (TextView) view.findViewById(R.id.title);
        tvPercent = (TextView) view.findViewById(R.id.percent);
        ivIcon = (ImageView) view.findViewById(R.id.icon);
        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    protected void dealView(Intent intent) throws Exception {
        String url = intent.getStringExtra(KEY_URI);
        String md5 = intent.getStringExtra(KEY_MD5);
        String title = intent.getStringExtra(KEY_TITLE);
        String name = intent.getStringExtra(KEY_NAME);
        String path = intent.getStringExtra(KEY_PATH);
        Bitmap icon = intent.getParcelableExtra(KEY_ICON);
        boolean isVisibility = intent.getBooleanExtra(KEY_VISIBILITY, true);
        boolean isSupportBreak = intent.getBooleanExtra(KEY_IS_SUPPORT_BREAK, true);
        FileDownloadThread t = new FileDownloadThread(this, title, url, path, name, icon, md5, handler, isVisibility, isSupportBreak);
        if (isVisibility) {
            addTask(t);
            tvTitle.setText(title);
            ivIcon.setImageBitmap(icon);
        }
        ThreadPoolFactory.addTask(t);
    }

    /**
     * @param context
     * @param url            下载地址
     * @param title          显示的标题
     * @param icon           图标
     * @param path           保存路径
     * @param name           保存文件名
     * @param md5            校验码
     * @param isVisibility   是否显示
     * @param isSupportBreak 是否支持断点续传
     */
    public static void start(Context context, @NonNull String url, String title, @Nullable Bitmap icon,
                             @NonNull String path, @Nullable String name, @Nullable String md5, boolean isVisibility, boolean isSupportBreak) {
        Intent in = new Intent(context, DownloadProgressService.class);
        in.putExtra(KEY_TITLE, title);
        in.putExtra(KEY_PATH, path);
        in.putExtra(KEY_ICON, icon);
        in.putExtra(KEY_MD5, md5);
        in.putExtra(KEY_NAME, name);
        in.putExtra(KEY_URI, url);
        in.putExtra(KEY_VISIBILITY, isVisibility);
        in.putExtra(KEY_IS_SUPPORT_BREAK, isSupportBreak);
        context.startService(in);
    }


    public static class ProgressBean {
        public long downId;
        public int max;
        public int progress;
        public String title;
        public String md5;
        public String fileUri;
        public String filePath;
        public String reason;
        public int status;
        public long lastTime;//最后下载的时间
    }
}
