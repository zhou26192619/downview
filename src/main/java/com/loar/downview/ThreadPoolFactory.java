package com.loar.downview;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFactory {

    private static Map<String, FileDownloadThread> downloadMap;

    /**
     * 线程池最大线程数
     */
    public static final int MAX_THREAD_COUNT = 10;

    private static ExecutorService executor;

    private static FileService fileService;

    /**
     * 异常重试间隔
     */
    private static int timeRepeat = 15000;

    static {
        executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
    }

    private static FileService getFileService(Context context) {
        if (fileService == null) {
            fileService = new FileService(context);
        }

        return fileService;
    }

    /**
     * 添加一个线程
     *
     * @param task 任务
     * @return true=任务添加;false=任务未添加
     */
    public static boolean addTask(FileDownloadThread task) {
        if (!contains(task.getFileBean().getDownpath())) {
            getDownloadMap().put(task.getFileBean().getDownpath(), task);
            executor.execute(task);
            return true;
        }
        return false;
    }

    /**
     * 判断下载任务列表中是否包含指定URL的下载任务
     *
     * @return
     */
    public static boolean contains(String url) {
        FileDownloadThread th = getDownloadMap().get(url);
        return th != null && !(th.getFileBean().getState() == DownloadStatus.COMPLETE
                || th.getFileBean().getState() == DownloadStatus.FAILURE);
    }

    /**
     * 退出任务，并从下载任务列表中删除.
     *
     * @param url      要删除的任务的URL
     * @param holdFile 是否需要保留文件
     */
    public static void exit(String url, boolean holdFile) {
        if (contains(url)) {
            FileDownloadThread task = getDownloadMap().get(url);
            if (task != null) {
                task.exit();
                getDownloadMap().remove(url);
                if (!holdFile) {
                    if (task.getSaveFile() != null && task.getSaveFile().exists()) {
                        task.getSaveFile().delete();
                    }
                }
            }
        }
    }

    /**
     * 全部退出
     *
     * @param holdFile 是否需要保留文件
     */
    public static void exitAll(boolean holdFile) {
        Set<String> dlmp = getDownloadMap().keySet();
        Iterator<String> it = dlmp != null ? dlmp.iterator() : null;
        if (it != null) {
            while (it.hasNext()) {
                exit(it.next(), holdFile);
            }
        }
    }

    /**
     * 从sqlite中查找，重启全部暂停、失败的线程
     *
     * @param context
     */
    public static void restartAll(Context context) {
        restart(DownloadStatus.STOP, context, null);
        restart(DownloadStatus.FAILURE, context, null);
    }

    /**
     * 根据状态从sqlite中查找，重启线程
     *
     * @param state
     * @param context
     */
    public static void restart(int state, Context context, Handler handler) {
//        List<FileBean> list = getFileService(context).getdFileBeans(String.valueOf(state));
//        if (!list.isEmpty()) {
//            for (FileBean fb : list) {
//                addTask(new FileDownloadThread(context, fb, handler));
//            }
//        }
    }

    /**
     * 下载异常任务,异常任务只状态是1 但实际并未下载的任务
     * 加锁避免数据库数据与正在执行的任务不一致
     */
    public static synchronized void restartUnfinished(Context context) {
//        List<FileBean> list = fileService.getdFileBeans("1");
//        if (downloadMap == null) {
//            downloadMap = new HashMap<>();
//        }
//
//        if (list != null && list.size() > 0) {
//            for (int i = 0; i < list.size(); i++) {
//                FileBean fb = list.get(i);
//
//                Log.i("alltask", fb.toString());
//
//                if (!downloadMap.containsKey(fb.getDownpath())) {
//                    FileDownloadThread task = new FileDownloadThread(context, fb, null);
//                    ThreadPoolFactory.addTask(task);
//
//                    Log.i("restarttask", fb.toString());
//                }
//            }
//        }
    }

    public static int getTimeRepeat() {
        return timeRepeat;
    }

    public static void setTimeRepeat(int timeRepeat) {
        ThreadPoolFactory.timeRepeat = timeRepeat;
    }

    /**
     * 任务集合
     *
     * @return
     */
    private static synchronized Map<String, FileDownloadThread> getDownloadMap() {
        if (downloadMap == null) {
            downloadMap = new HashMap<>();
        }
        return downloadMap;
    }
}
