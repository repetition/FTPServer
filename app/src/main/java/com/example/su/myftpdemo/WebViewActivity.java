package com.example.su.myftpdemo;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = WebViewActivity.class.getName();
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        init();
    }

    private void init() {
        mWebView = findViewById(R.id.webView);
        mWebView.clearCache(true);
        mWebView.clearHistory();

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setDomStorageEnabled(true);//开启DOM storage API功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.e(TAG, "onReceivedSslError sslError=" + error.toString());
                handler.proceed();
               /* if (error.getPrimaryError() == android.net.http.SslError.SSL_INVALID) {// 校验过程遇到了bug
                    handler.proceed();
                } else {
                    handler.cancel();
                }*/
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                Log.i(TAG,url);

                Log.i(TAG,CookieStr+"");

            }
        });

       // mWebView.loadUrl("http://u.gmcc.net/MYservers/openurl.jsp?id=%E5%9F%B9%E8%AE%AD%E5%AD%A6%E9%99%A2%E6%B1%87%E8%8B%B1%E6%A5%BC%E4%BA%8C%E6%A5%BC%E8%AF%BE%E5%AE%A42");
        mWebView.loadUrl("http://10.10.9.234/res/resources/publish/manager/project/programHtml/");


    }
}
