package com.example.su.myftpdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static boolean isStartService = false;

    static {
        BasicConfigurator.configure();
    }

    private Button mButton, btn_stop, btn_confg, bt_read, mBTWebView, mBTServer;
    private EditText mEditText;
    private TextView mtv_Read_confg, mTv_ip;
    private static final String TAG = "FtpServerService";
    private static String hostip = ""; // 本机IP
    private static final int PORT = 8090;
    // sd卡目录
    @SuppressLint("SdCardPath")
    private static final String dirname = "/mnt/sdcard/ftp";
    // ftp服务器配置文件路径
    private static final String filename = dirname + "/users.properties";
    private FtpServer mFtpServer = null;

    private Intent intent;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.btn);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_confg = findViewById(R.id.btn_config);
        mBTWebView = findViewById(R.id.btn_webView);
        mEditText = (EditText) findViewById(R.id.et);
        bt_read = findViewById(R.id.btn_read_config);
        mtv_Read_confg = findViewById(R.id.tv_read_conf);
        mTv_ip = findViewById(R.id.tv_ip);
        mBTServer = findViewById(R.id.btn_server);

        mBTWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startService(intent);
                mButton.setEnabled(false);
                btn_stop.setEnabled(true);
                mTv_ip.setText("远程访问地址：ftp//" + getLocalIpAddress() + ":8090");
                isStartService = true;
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.stopService(intent);
                btn_stop.setEnabled(false);
                mButton.setEnabled(true);
                isStartService = false;
            }
        });
        mBTServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MinaActivity.class);
                startActivity(intent);
            }
        });

        btn_confg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建服务器配置文件
                request();
            }
        });

        bt_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(dirname + "/users.properties");
                    InputStreamReader reader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    mtv_Read_confg.setText(builder.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        MyApplication.setOnHandlerListener(new MyApplication.HandlerListener() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        mButton.setEnabled(false);
                        btn_stop.setEnabled(true);
                        Toast.makeText(MainActivity.this, "开启了FTP服务器  ip = " + hostip, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mButton.setEnabled(true);
                        btn_stop.setEnabled(false);
                        mTv_ip.setText("远程访问地址：ftp//" + getLocalIpAddress() + ":8090");
                        Toast.makeText(MainActivity.this, "关闭了FTP服务器  ip = " + hostip, Toast.LENGTH_SHORT).show();
                        break;

                    case 3:

                        break;
                }
            }
        });


        intent = new Intent(MainActivity.this, FTPServer.class);
        Bundle bundle = new Bundle();
        bundle.putString("ip", getLocalIpAddress());
        intent.putExtras(bundle);

        SharedPreferences preferences = this.getSharedPreferences("conf", MODE_PRIVATE);
        boolean isRun = preferences.getBoolean("isRun", false);
        if (isRun) {
            btn_stop.setEnabled(true);
            mButton.setEnabled(false);

        }
        boolean isWrite = preferences.getBoolean("isWrite", false);
        if (!isWrite) {
            request();
        }
    }

    /**
     * 创建服务器配置文件
     */
    private void createDirsFiles() throws IOException {
        File dir = new File(dirname);
        if (!dir.exists()) {
            dir.mkdir();
        }
        FileOutputStream fos = null;
        //    String tmp = getString(R.string.users);

        String tmp = Config.UserConfig;
        File sourceFile = new File(dirname + "/users.properties");
        fos = new FileOutputStream(sourceFile);
        fos.write(tmp.getBytes());
        fos.flush();
        if (fos != null) {
            fos.close();
        }
    }


    /**
     * 获取本机ip
     */
    public static String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = Isipv4(sAddr);
                        if (isIPv4) {
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean Isipv4(String ipv4) {
        if (ipv4 == null || ipv4.length() == 0) {
            return false;//字符串为空或者空串
        }
        String[] parts = ipv4.split("\\.");//因为java doc里已经说明, split的参数是reg, 即正则表达式, 如果用"|"分割, 则需使用"\\|"
        if (parts.length != 4) {
            return false;//分割开的数组根本就不是4个数字
        }
        for (int i = 0; i < parts.length; i++) {
            try {
                int n = Integer.parseInt(parts[i]);
                if (n < 0 || n > 255) {
                    return false;//数字不在正确范围内
                }
            } catch (NumberFormatException e) {
                return false;//转换数字不正确
            }
        }
        return true;
    }


    public void request() {
        /**
         * 第 1 步: 检查是否有相应的权限
         */
        boolean isAllGranted = checkPermissionAllGranted(
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );

        // 如果这3个权限全都拥有, 则直接执行备份代码
        if (isAllGranted) {
            try {
                createDirsFiles();
                Toast.makeText(MainActivity.this, "写入配置！", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = this.getSharedPreferences("conf", MODE_PRIVATE).edit();
                editor.putBoolean("isWrite", true);
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        requestPermission(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, MY_PERMISSION_REQUEST_CODE);
    }


    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {

        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    // 只要有一个权限没有被授予, 则直接返回 false
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * 检查是否拥有指定的所有权限
     */
    private void requestPermission(String[] permissions, int code) {

        if (Build.VERSION.SDK_INT >= 23) {
            // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
            this.requestPermissions(
                    permissions,
                    code
            );
        }
    }


    /**
     * 第 3 步: 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码

                try {
                    createDirsFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                Toast.makeText(MainActivity.this, "请打开权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
