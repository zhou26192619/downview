1、使用现有ui
/**
     *
     * @param context
     * @param url  下载地址
     * @param title 显示的标题
     * @param icon 图标
     * @param path 保存路径
     * @param name 保存文件名
     * @param md5 校验码
     * @param isVisibility 下载过程是否显示到UI上
     * @param isSupportBreak 是否支持断点续传
     */
DownloadProgressService.start(Context context, @NonNull String url, String title,@Nullable Bitmap icon,
                             @NonNull String path, @Nullable String name,@Nullable String md5, boolean isVisibility, boolean isSupportBreak);

下载结束后，结果通过广播返回 ACTION_DOWNLOAD_COMPLETE = "com.justsy.download.complete";
可以拿到(FileBean)intent.getParcelableExtra(DownloadProgressService.KEY_RESULT)对象

2、不使用现有ui
FileDownloadThread t = new FileDownloadThread(this, title, url, path, name, icon, md5, handler, isVisibility, isSupportBreak);
ThreadPoolFactory.addTask(t);
在handler中进行下载进度回调

end