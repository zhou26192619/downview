package com.loar.downview;

/**
 * 下载状态
 *
 * @author justsy
 */
public class DownloadStatus {
    /**
     * 进行中
     */
    public static final int PROCESSING = 2;
    /**
     * 失败
     */
    public static final int FAILURE = 16;

    /**
     * 暂停
     */
    public static final int STOP = 4;

    /**
     * 完成
     */
    public static final int COMPLETE = 8;
    /**
     * 未开始 准备阶段
     */
    public static final int PENDING = 1;
}
