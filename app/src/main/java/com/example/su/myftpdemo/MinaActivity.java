package com.example.su.myftpdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.su.myftpdemo.mina.MinaServer;

public class MinaActivity extends AppCompatActivity {

    public static Handler mHandler = new Handler();
    private Button mBTStart;
    public static TextView mTVRec;
    private boolean isStart = false;
    private MinaServer server;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mina);
        mBTStart = findViewById(R.id.bt_start);
        mTVRec = findViewById(R.id.tv_Rec);

        mBTStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    startServer();
                    mBTStart.setText("关闭");
                } else {
                    stopServer();
                    mBTStart.setText("开启");
                }
            }
        });
    }


    public void startServer() {
        new Thread() {
            @Override
            public void run() {
                server = new MinaServer();
                server.startServer();
                isStart = true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTVRec.setText("服务启动成功。端口7007");
                        Toast.makeText(MinaActivity.this, "服务启动成功。端口7007", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.start();
    }

    public void stopServer() {
        new Thread() {
            @Override
            public void run() {
                if (null != server) {
                    server.stopServer();
                    isStart = false;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTVRec.setText("服务关闭");
                        Toast.makeText(MinaActivity.this, "服务关闭成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.start();
    }
}
