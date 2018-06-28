package com.example.su.myftpdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    private static String TAG = BootReceiver.class.getName();
    private SharedPreferences.Editor editor;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MyApplication.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /* 服务开机自启动 */
                    Intent ftpIntent = new Intent(context, FTPServer.class);
                    Bundle bundle = new Bundle();
                    String localIpAddress = Utils.getLocalIpAddress();
                    bundle.putString("id", localIpAddress);
                    ftpIntent.putExtras(bundle);
                    ComponentName componentName = context.startService(ftpIntent);

                    Toast.makeText(context, "服务启动！" + localIpAddress, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "服务启动 | " + localIpAddress + "========" + componentName);

                    editor = context.getSharedPreferences("conf", Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isRun",true);
                    editor.commit();
                }
            }, 2000L);
        }
        Toast.makeText(context, "已经开机，正在启动服务！", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "已经开机，正在启动服务！");
    }

}
