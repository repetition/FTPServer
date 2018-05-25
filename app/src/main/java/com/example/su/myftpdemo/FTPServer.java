package com.example.su.myftpdemo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;

public class FTPServer extends Service {

    private static final String TAG = "FTPServer";
    private static String hostip = ""; // 本机IP
    private static final int PORT = 8090;
    // sd卡目录
    @SuppressLint("SdCardPath")
    private static final String dirname = "/mnt/sdcard/ftp";
    // ftp服务器配置文件路径
    private static final String filename = dirname + "/users.properties";
    private FtpServer mFtpServer = null;
    private static Handler mHandler = new Handler();
    private String ip;
    private String CHANNEL_ID = "com.example.su.myftpdemo";
    private CharSequence CHANNEL_NAME = "FTPServer";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent intent1 = new Intent(this, FTPServer.class);

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }*/
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0, intent1, 0);
        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle("FTPServerRunning")
                .setContentText("FTPServerRunning...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(notificationIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noti.setChannelId(CHANNEL_ID);
        }
        startForeground(123456, noti.build());

        Bundle bundle = intent.getExtras();
        ip = bundle.getString("ip");

        startFtpServer(ip);
        Log.d(TAG, "ip" + ip + "port:" + PORT);
        MyApplication.getHandler().sendEmptyMessage(1);
        Log.d(TAG, "已经启动！------------------------------------------------------------------------");
        Log.d(TAG, "onStartCommand");
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Thread() {
            @Override
            public void run() {
                super.run();
                stopFtpServer(ip);
            }
        }.start();
        stopForeground(true);//取消最高级进程
        MyApplication.getHandler().sendEmptyMessage(2);
        Log.d(TAG, "onDestroy");
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();

        SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("conf", Context.MODE_PRIVATE).edit();
        editor.putBoolean("isRun", false);
        editor.commit();

    }


    /**
     * 开启FTP服务器
     *
     * @param hostip 本机ip
     */
    private void startFtpServer(String hostIp) {
        FtpServerFactory serverFactory = new FtpServerFactory();
        // 设置监听IP和端口号
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(PORT);
        factory.setServerAddress(hostIp);
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File files = new File(filename);
        //设置配置文件
        userManagerFactory.setFile(files);
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        serverFactory.addListener("default", factory.createListener());
        // start the server
        mFtpServer = serverFactory.createServer();
        try {
            mFtpServer.start();
            Log.d(TAG, "开启了FTP服务器  ip = " + hostip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭FTP服务器
     */
    private void stopFtpServer(String ip) {
        if (mFtpServer != null) {
            mFtpServer.stop();
            mFtpServer = null;
            mHandler.sendEmptyMessage(0x0002);
            Log.d(TAG, "关闭了FTP服务器 ip = " + ip);
        } else {
            mHandler.sendEmptyMessage(0x0004);
        }
    }

}
