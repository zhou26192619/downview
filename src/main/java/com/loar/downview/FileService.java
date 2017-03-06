package com.loar.downview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载文件的数据库信息
 */
public class FileService {
    private DBOpenHelper openHelper;

    public FileService(Context context) {
        openHelper = new DBOpenHelper(context);
    }

    /**
     * 根据文件URL获取线程和对应线程下载的长度
     *
     * @param path 文件URL
     * @return 线程和对应线程下载长度MAP; 其中KEY=线程序列, VALUE=线程下载的长度
     */
    public Map<Integer, Integer[]> getData(String path) {
        Map<Integer, Integer[]> data = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openHelper.getReadableDatabase();
            cursor = db.rawQuery("select threadid, downLength, fileSize from filedownlog where downpath=?",
                    new String[]{path});

            data = new HashMap<>();

            while (cursor.moveToNext()) {
                data.put(cursor.getInt(0), new Integer[]{cursor.getInt(1), cursor.getInt(2)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return data;
    }

    public void closeDB(SQLiteDatabase db) {
        if (openHelper.getReadableDatabase() != null) {
            openHelper.getReadableDatabase().close();
        }
    }

    /**
     * 根据文件URL获取文件信息
     *
     * @param path 文件URL
     * @return 文件信息Bean
     */
    public FileBean getFileBean(String path) {
        FileBean bean = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = openHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from filedownlog where downpath=?", new String[]{path});
            if (cursor.moveToNext()) {
                bean = getBean(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bean;
    }

    /**
     * 根据状态查询
     *
     * @return FileBean集合
     */
    public List<FileBean> getFileBeans(String state) {
        List<FileBean> list = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = openHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from filedownlog where state=? ", new String[]{state});
            list = new ArrayList<FileBean>();

            while (cursor.moveToNext()) {
                FileBean bean = getBean(cursor);
                list.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * 保存文件下载程度
     *
     * @param path 文件URL
     * @param map  KEY=线程序列, VALUE=下载长度
     */
    public void save(String path, Map<Integer, Integer> map) {
        SQLiteDatabase db = null;
        try {
            if (map == null || map.isEmpty()) {
                return;
            } else if (getFileBean(path) != null) {
                // 已添加存在的，直接返回
                return;
            }

            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                db.execSQL("insert into filedownlog(downpath, threadid, downLength) values(?,?,?)", new Object[]{
                        path, entry.getKey(), entry.getValue()});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 新增加的下载任务,数据保存
     *
     * @param path           文件下载路径
     * @param threadId       线程ID(自己分配, 非系统分配)
     * @param downloadedSize 已经下载的量(新增加的下载任务, 初始为0即可)
     * @param fileSize       文件大小
     */
    public void save(String path, int threadId, long downloadedSize, long fileSize) {
        SQLiteDatabase db = null;
        try {
            if (getFileBean(path) != null) {
                return;
            }

            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("insert into filedownlog(downpath, threadid, downLength, fileSize) values(?,?,?,?)",
                    new Object[]{path, threadId, downloadedSize, fileSize});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 新增加的下载任务,数据保存
     *
     * @param fileBean 需要保存的文件Bean
     */
    public void save(FileBean fileBean) {
        SQLiteDatabase db = null;
        try {
            if (fileBean == null) {
                return;
            }
            db = openHelper.getWritableDatabase();
            db.beginTransaction();
            if (getFileBean(fileBean.getDownpath()) == null) {
                insertFileBean(fileBean, db);
            } else {
                updateFileBean(fileBean, db);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public void insertFileBean(FileBean fileBean, SQLiteDatabase db) {
        try {
            if (fileBean == null) {
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (fileBean.getIcon() != null) {
                fileBean.getIcon().compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            db.execSQL("insert into filedownlog ( downloadBeginTime, "
                            + "downpath, "
                            + "name, "
                            + "title, "
                            + "downloadCompleteTime, "
                            + "downloadLastTime, "
                            + "downLength, "
                            + "fileSize, "
                            + "icon, "
                            + "threadid, "
                            + "savePath, "
                            + "state, "
                            + "msg, "
                            + "fileType, "
                            + "fileOriginKey, "
                            + "extraInfo, "
                            + DBOpenHelper.DownColumns.IS_VISIBILITY + ", "
                            + DBOpenHelper.DownColumns.IS_SUPPORT_BREAK + ", "
                            + "md5)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{
                            fileBean.getDownloadBeginTime(),
                            fileBean.getDownpath(),
                            fileBean.getName(),
                            fileBean.getTitle(),
                            fileBean.getDownloadCompleteTime(),
                            fileBean.getDownloadLastTime(),
                            fileBean.getDownLength(),
                            fileBean.getFileSize(),
                            out.toByteArray(),
                            fileBean.getThreadid(),
                            fileBean.getSavePath() == null ? "" : fileBean.getSavePath(),
                            fileBean.getState(),
                            fileBean.getMsg() == null ? "" : fileBean.getMsg(),
                            fileBean.getFileType() == null ? "" : fileBean.getFileType(),
                            fileBean.getFileOriginKey() == null ? "" : fileBean.getFileOriginKey(),
                            fileBean.getExtraInfo() == null ? "" : fileBean.getExtraInfo(),
                            fileBean.isVisibility(),
                            fileBean.isSupportBreak(),
                            fileBean.getMd5() == null ? "" : fileBean.getMd5()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void updateFileBean(FileBean fileBean, SQLiteDatabase db) {
        try {
            if (fileBean == null) {
                return;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (fileBean.getIcon() != null) {
                fileBean.getIcon().compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            db.execSQL(
                    "update filedownlog set " +
                            DBOpenHelper.DownColumns.BEGIN_TIME + "=?, " +
                            DBOpenHelper.DownColumns.COMPLETE_TIME + "=?, " +
                            DBOpenHelper.DownColumns.LAST_TIME + "=?, " +
                            DBOpenHelper.DownColumns.ICON + "=?, " +
                            DBOpenHelper.DownColumns.NAME + "=?, " +
                            DBOpenHelper.DownColumns.TITLE + "=?, " +
                            DBOpenHelper.DownColumns.THREAD_ID + "=?, " +
                            DBOpenHelper.DownColumns.SAVE_PATH + "=?, " +
                            DBOpenHelper.DownColumns.STATE + "=?, " +
                            DBOpenHelper.DownColumns.MSG + "=?, " +
                            DBOpenHelper.DownColumns.FILE_TYPE + "=?, " +
                            DBOpenHelper.DownColumns.FILE_ORIGIN_KEY + "=?, " +
                            DBOpenHelper.DownColumns.EXTRA_INFO + "=?, " +
                            DBOpenHelper.DownColumns.MD5 + "=?, " +
                            DBOpenHelper.DownColumns.DOWN_LENGTH + "=?, " +
                            DBOpenHelper.DownColumns.FILE_SIZE + "=?, " +
                            DBOpenHelper.DownColumns.IS_SUPPORT_BREAK + "=?, " +
                            DBOpenHelper.DownColumns.IS_VISIBILITY + "=? " +
                            "where " + DBOpenHelper.DownColumns.DOWN_PATH + "=?",
                    new Object[]{
                            fileBean.getDownloadBeginTime(),
                            fileBean.getDownloadCompleteTime(),
                            fileBean.getDownloadLastTime(),
                            out.toByteArray(),
                            fileBean.getName(),
                            fileBean.getTitle(),
                            fileBean.getThreadid(),
                            fileBean.getSavePath() == null ? "" : fileBean.getSavePath(),
                            fileBean.getState(),
                            fileBean.getMsg() == null ? "" : fileBean.getMsg(),
                            fileBean.getFileType() == null ? "" : fileBean.getFileType(),
                            fileBean.getFileOriginKey() == null ? "" : fileBean.getFileOriginKey(),
                            fileBean.getExtraInfo() == null ? "" : fileBean.getExtraInfo(),
                            fileBean.getMd5() == null ? "" : fileBean.getMd5(),
                            fileBean.getDownLength(),
                            fileBean.getFileSize(),
                            fileBean.isSupportBreak(),
                            fileBean.isVisibility(),
                            fileBean.getDownpath() == null ? "" : fileBean.getDownpath()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 更新下载进度
     *
     * @param path     文件下载路径
     * @param threadId 线程ID(自己分配, 非系统分配)
     * @param pos      下载到的位置
     */
    public void updateTyc(String path, int threadId, long pos) {
        SQLiteDatabase db = null;
        try {
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("update filedownlog set downLength=? where downpath=? and threadid=?", new Object[]{pos, path,
                    threadId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 更新下载进度
     *
     * @param path     文件下载路径
     * @param threadId 线程ID(自己分配, 非系统分配)
     * @param pos      下载到的位置
     * @param state    状态
     */
    public void updateTyc(String path, int threadId, long pos, int state) {
        SQLiteDatabase db = null;
        try {
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("update filedownlog set downLength=?, state=? where downpath=? and threadid=?", new Object[]{
                    pos, state, path, threadId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 更新下载进度
     *
     * @param path          文件下载路径
     * @param threadId      线程ID(自己分配, 非系统分配)
     * @param pos           下载到的位置
     * @param state         状态
     * @param exceptionType 异常
     */
    public void updateTyc(String path, int threadId, long pos, int state, FileDownloadException.FileDownloadExceptionType exceptionType) {
        SQLiteDatabase db = null;
        try {
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            if (exceptionType != null) {
                db.execSQL("update filedownlog set downLength=?, state=?, msg=? where downpath=? and threadid=?",
                        new Object[]{pos, state, exceptionType.toString(), path, threadId});
            } else {
                db.execSQL("update filedownlog set downLength=?, state=? where downpath=? and threadid=?",
                        new Object[]{pos, state, path, threadId});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 根据文件URL，以及线程序列更新已经下载的长度
     *
     * @param path 文件URL
     * @param map  KEY=线程序列,VALUE=已经下载的长度
     */
    public void update(String path, Map<Integer, Integer> map) {
        SQLiteDatabase db = null;
        try {
            if (map == null || map.isEmpty()) {
                return;
            }

            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                db.execSQL("update filedownlog set downLength=? where downpath=? and threadid=?",
                        new Object[]{entry.getValue(), path, entry.getKey()});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 根据文件URL删除数据
     *
     * @param path 文件URL
     */
    public void delete(String path) {
        SQLiteDatabase db = null;
        try {
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 根据文件URL删除数据
     *
     * @param path 文件URL
     */
    public void updateDlCompleteTime(String dlCompleteTime, String path) {
        SQLiteDatabase db = null;
        try {
            db = openHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("update filedownlog set downloadCompleteTime = ? where downpath = ?", new Object[]{
                    dlCompleteTime, path});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    private FileBean getBean(Cursor cursor) {
        FileBean bean = null;
        if (cursor != null) {
            bean = new FileBean();
            bean.setId(cursor.getInt(cursor.getColumnIndex("id")));
            bean.setDownpath(cursor.getString(cursor.getColumnIndex("downpath")));
            bean.setMd5(cursor.getString(cursor.getColumnIndex("md5")));
            bean.setName(cursor.getString(cursor.getColumnIndex("name")));
            bean.setState(cursor.getInt(cursor.getColumnIndex("state")));
            bean.setSavePath(cursor.getString(cursor.getColumnIndex("savePath")));
            bean.setThreadid(cursor.getInt(cursor.getColumnIndex("threadid")));
            bean.setDownLength(cursor.getInt(cursor.getColumnIndex("downLength")));
            bean.setFileSize(cursor.getInt(cursor.getColumnIndex("fileSize")));
            bean.setDownloadBeginTime(cursor.getLong(cursor.getColumnIndex("downloadBeginTime")));
            bean.setDownloadCompleteTime(cursor.getLong(cursor.getColumnIndex("downloadCompleteTime")));
            bean.setDownloadLastTime(cursor.getLong(cursor.getColumnIndex("downloadLastTime")));
            bean.setMsg(cursor.getString(cursor.getColumnIndex("msg")));
            bean.setFileType(cursor.getString(cursor.getColumnIndex("fileType")));
            bean.setFileOriginKey(cursor.getString(cursor.getColumnIndex("fileOriginKey")));
            bean.setExtraInfo(cursor.getString(cursor.getColumnIndex("extraInfo")));
            bean.setSupportBreak(cursor.getInt(cursor.getColumnIndex(DBOpenHelper.DownColumns.IS_VISIBILITY)) > 0);
            bean.setSupportBreak(cursor.getInt(cursor.getColumnIndex(DBOpenHelper.DownColumns.IS_SUPPORT_BREAK)) > 0);
            byte[] in = cursor.getBlob(cursor.getColumnIndex("icon"));
            bean.setIcon(BitmapFactory.decodeByteArray(in, 0, in.length));
        }
        return bean;
    }
}
