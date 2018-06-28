package com.example.su.myftpdemo.mina;

import android.widget.Toast;

import com.example.su.myftpdemo.MainActivity;
import com.example.su.myftpdemo.MinaActivity;
import com.example.su.myftpdemo.MyApplication;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MinServerHandler extends IoHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MinServerHandler.class);
    private static Map<String, IoSession> ioSessionMap = new HashMap<>();

    public boolean isAdd = false;

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        log.info(session.getRemoteAddress().toString() + " - sessionCreated");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        log.info(session.getRemoteAddress().toString() + " - sessionOpened");
        log.info(session.getId() + "");

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        log.info(session.getRemoteAddress() + " - sessionClosed");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        log.info(session.getRemoteAddress().toString() + " - sessionIdle");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        log.info(session.getRemoteAddress() + " - exceptionCaught");

        printText("异常："+cause.getMessage());
    }


    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        log.info(session.getRemoteAddress() + " - messageReceived");
        if (message instanceof IoBuffer) {

            IoBuffer ioBuffer = (IoBuffer) message;
            InputStream inputStream = ioBuffer.asInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,Charset.forName("gbk")));
            StringBuilder builder = new StringBuilder();
            String len;
            while ((len = br.readLine()) != null) {
                builder.append(len);
            }
            printText("收到："+builder.toString());
            log.info(session.getRemoteAddress() + " | messageReceived :"+builder.toString());
        }

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
        log.info(session.getRemoteAddress().toString() + " - messageSent");
    }


    public void printText(final String text) {
        MinaActivity.mHandler.post(new Runnable() {
            @Override
            public void run() {
                MinaActivity.mTVRec.setText(text);
                Toast.makeText(MyApplication.getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
