package com.loar.downview;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class FileBean implements Parcelable {
    /**
     * 主键
     */
    private int id;
    /**
     * 文件URL
     */
    private String downpath;

    private String title;
    private String name;

    private boolean isVisibility;

    private Bitmap icon;

    private int threadid;
    /**
     * 已经下载的量
     */
    private long downLength;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 开始下载时间
     */
    private long downloadBeginTime;
    /**
     * 下载完成时间
     */
    private long downloadCompleteTime;

    private long downloadLastTime;
    /**
     * 文件保存的完整路劲
     */
    private String savePath;
    /**
     * 状态
     */
    private int state = DownloadStatus.PENDING;
    /**
     * 消息(失败的时候)
     */
    private String msg;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 文件原始ID
     */
    private String fileOriginKey;
    /**
     * 额外信息
     */
    private String extraInfo;

    /**
     * md5
     */
    private String md5;

    /**
     * 是否支持断点续传
     */
    private boolean isSupportBreak;

    public FileBean() {
    }

    protected FileBean(Parcel in) {
        id = in.readInt();
        downpath = in.readString();
        title = in.readString();
        name = in.readString();
        isVisibility = in.readByte() != 0;
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        threadid = in.readInt();
        downLength = in.readLong();
        fileSize = in.readLong();
        downloadBeginTime = in.readLong();
        downloadCompleteTime = in.readLong();
        downloadLastTime = in.readLong();
        savePath = in.readString();
        state = in.readInt();
        msg = in.readString();
        fileType = in.readString();
        fileOriginKey = in.readString();
        extraInfo = in.readString();
        md5 = in.readString();
        isSupportBreak = in.readByte() != 0;
    }

    public static final Creator<FileBean> CREATOR = new Creator<FileBean>() {
        @Override
        public FileBean createFromParcel(Parcel in) {
            return new FileBean(in);
        }

        @Override
        public FileBean[] newArray(int size) {
            return new FileBean[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDownpath() {
        return downpath;
    }

    public void setDownpath(String downpath) {
        this.downpath = downpath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThreadid() {
        return threadid;
    }

    public void setThreadid(int threadid) {
        this.threadid = threadid;
    }

    public long getDownLength() {
        return downLength;
    }

    public void setDownLength(long downLength) {
        this.downLength = downLength;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getDownloadBeginTime() {
        return downloadBeginTime;
    }

    public void setDownloadBeginTime(long downloadBeginTime) {
        this.downloadBeginTime = downloadBeginTime;
    }

    public long getDownloadCompleteTime() {
        return downloadCompleteTime;
    }

    public void setDownloadCompleteTime(long downloadCompleteTime) {
        this.downloadCompleteTime = downloadCompleteTime;
    }

    public long getDownloadLastTime() {
        return downloadLastTime;
    }

    public void setDownloadLastTime(long downloadLastTime) {
        this.downloadLastTime = downloadLastTime;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileOriginKey() {
        return fileOriginKey;
    }

    public void setFileOriginKey(String fileOriginKey) {
        this.fileOriginKey = fileOriginKey;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public boolean isVisibility() {
        return isVisibility;
    }

    public void setVisibility(boolean visibility) {
        isVisibility = visibility;
    }

    public boolean isSupportBreak() {
        return isSupportBreak;
    }

    public void setSupportBreak(boolean supportBreak) {
        isSupportBreak = supportBreak;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "id=" + id +
                ", downpath='" + downpath + '\'' +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", isVisibility=" + isVisibility +
                ", icon=" + icon +
                ", threadid=" + threadid +
                ", downLength=" + downLength +
                ", fileSize=" + fileSize +
                ", downloadBeginTime=" + downloadBeginTime +
                ", downloadCompleteTime=" + downloadCompleteTime +
                ", downloadLastTime=" + downloadLastTime +
                ", savePath='" + savePath + '\'' +
                ", state=" + state +
                ", msg='" + msg + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileOriginKey='" + fileOriginKey + '\'' +
                ", extraInfo='" + extraInfo + '\'' +
                ", md5='" + md5 + '\'' +
                ", isSupportBreak=" + isSupportBreak +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(downpath);
        parcel.writeString(title);
        parcel.writeString(name);
        parcel.writeByte((byte) (isVisibility ? 1 : 0));
        parcel.writeParcelable(icon, i);
        parcel.writeInt(threadid);
        parcel.writeLong(downLength);
        parcel.writeLong(fileSize);
        parcel.writeLong(downloadBeginTime);
        parcel.writeLong(downloadCompleteTime);
        parcel.writeLong(downloadLastTime);
        parcel.writeString(savePath);
        parcel.writeInt(state);
        parcel.writeString(msg);
        parcel.writeString(fileType);
        parcel.writeString(fileOriginKey);
        parcel.writeString(extraInfo);
        parcel.writeString(md5);
        parcel.writeByte((byte) (isSupportBreak ? 1 : 0));
    }
}
