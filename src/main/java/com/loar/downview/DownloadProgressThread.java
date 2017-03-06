package com.loar.downview;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 下载查看进度
 * Created by Justsy on 2016/11/22.
 */

public class DownloadProgressThread extends Thread {

    private final Context context;
    private CopyOnWriteArrayList<DownloadProgressService.ProgressBean> progresses;
    private boolean flag = true;

    public DownloadProgressThread(Context context, CopyOnWriteArrayList<DownloadProgressService.ProgressBean> progresses) {
        this.context = context;
        this.progresses = progresses;
    }

    @Override
    public void run() {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            while (flag) {
                for (DownloadProgressService.ProgressBean progress : progresses) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(progress.downId);
                    Cursor c = downloadManager.query(query);
                    if (c != null && c.moveToFirst()) {
                        progress.filePath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        progress.reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                        progress.title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        progress.fileUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        progress.max = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int pg = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        if (progress.progress != pg) {
                            progress.progress = pg;
                            progress.lastTime = System.currentTimeMillis();
                        }
                        progress.status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (System.currentTimeMillis() - progress.lastTime > DownloadProgressService.TIME_FAIL_DURATION) {
                            //下载超时
                            downloadManager.remove(progress.downId);
                            progress.status = DownloadManager.STATUS_FAILED;
                            progress.reason = "下载超时";
                        }
                    }
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSelf() {
        flag = false;
    }

}
