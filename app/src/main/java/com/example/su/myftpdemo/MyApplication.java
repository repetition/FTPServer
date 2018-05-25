package com.example.su.myftpdemo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mListener!=null) {
                mListener.handleMessage(msg);
            }
            System.out.println("mListener 地址值==== " + mListener);
        }
    };


    private static HandlerListener mListener;

    public static void setOnHandlerListener(HandlerListener listener) {
        mListener = listener;
    }

    public static HandlerListener getListener() {
        return mListener;
    }

    public interface HandlerListener {
        void handleMessage(Message msg);
    }
}
