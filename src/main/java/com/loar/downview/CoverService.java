package com.loar.downview;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

/**
 * 提示窗、遮盖层等一切的基类
 * Created by Justsy on 2016/6/6.
 */
public abstract class CoverService extends Service {

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    protected View coverView;

    @Override
    public void onCreate() {
        super.onCreate();
        floatWindow();
    }

    private void floatWindow() {
        try {
            coverView = inflateView();
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
            wmParams = inflateLayoutParams();
            //添加mFloatLayout
            mWindowManager.addView(coverView, wmParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract WindowManager.LayoutParams inflateLayoutParams();

    abstract View inflateView();

    protected View getChildViewById(int id) {
        return coverView.findViewById(id);
    }

    public View getRootView() {
        return coverView;
    }

    /**
     * 处理视图内容
     *
     * @param intent
     */
    protected abstract void dealView(Intent intent) throws Exception;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent!=null){
                if (mWindowManager != null && coverView != null && coverView.getParent() == null) {
                    mWindowManager.addView(coverView, wmParams);
                }
                dealView(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null && coverView != null) {
            mWindowManager.removeViewImmediate(coverView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
