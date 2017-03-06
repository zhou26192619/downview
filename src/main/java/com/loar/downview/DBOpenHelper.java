package com.loar.downview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "eric.db";
    private static final String TB_DOWNLOAD = "filedownlog";
    private static final int VERSION = 1;

    public DBOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_DOWNLOAD
                + " (id integer primary key autoincrement, "
                + "downpath varchar(100), "
                + "name varchar(100), "
                + "title varchar(100), "
                + "threadid INTEGER, "
                + "downLength BIGINT, "
                + "fileSize BIGINT,"
                + DownColumns.IS_VISIBILITY + " BOOLEAN,"
                + DownColumns.IS_SUPPORT_BREAK + " BOOLEAN,"
                + "downloadBeginTime BIGINT,"
                + "downloadCompleteTime BIGINT,"
                + "downloadLastTime BIGINT,"
                + "savePath varchar(200),"
                + "icon BLOB,"
                + "state INTEGER,"
                + "msg varchar(100),"
                + "fileType varchar(50),"
                + "fileOriginKey varchar(50),"
                + "extraInfo varchar(1000),"
                + "md5 varchar(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS filedownlog");
        onCreate(db);
    }

    public interface DownColumns extends BaseColumns {
        String DOWN_PATH = "downpath";
        String NAME = "name";
        String TITLE = "title";
        String THREAD_ID = "threadid";
        String DOWN_LENGTH = "downLength";
        String FILE_SIZE = "fileSize";
        String BEGIN_TIME = "downloadBeginTime";
        String COMPLETE_TIME = "downloadCompleteTime";
        String LAST_TIME = "downloadLastTime";
        String SAVE_PATH = "savePath";
        String ICON = "icon";
        String STATE = "state";
        String IS_VISIBILITY = "isVisibility";
        String MSG = "msg";
        String FILE_TYPE = "fileType";
        String FILE_ORIGIN_KEY = "fileOriginKey";
        String EXTRA_INFO = "extraInfo";
        String MD5 = "md5";
        String IS_SUPPORT_BREAK = "isSupportBreak";

    }
}
