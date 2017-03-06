package com.loar.downview;

public class FileDownloadException extends Exception {

    private FileDownloadExceptionType type;

    public FileDownloadException(FileDownloadExceptionType exceptionType) {
        super(exceptionType.toString());
        this.type = exceptionType;
    }

    public FileDownloadExceptionType getExceptionType() {
        return this.type;
    }

    public enum FileDownloadExceptionType {

        NET_EXCEPTION(1, "网络异常"), SERVER_ERROR(2, "服务器响应异常"), INNER_EXCEPTION(3, "系统内部错误"), MD5_ERROR(4, "MD5校验失败");

        private int type;
        private String msg;

        FileDownloadExceptionType(int type, String msg) {
            this.type = type;
            this.msg = msg;
        }

        public int getType() {
            return this.type;
        }

        public String getMsg() {
            return this.msg;
        }

        @Override
        public String toString() {
            return getType() + ":" + getMsg();
        }

        public boolean equals(FileDownloadExceptionType type) {
            if (type == null)
                return false;
            return type.getType() == getType();
        }
    }
}
