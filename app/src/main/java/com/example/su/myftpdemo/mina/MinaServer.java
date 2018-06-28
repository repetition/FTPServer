package com.example.su.myftpdemo.mina;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class MinaServer {
    private static final Logger log = LoggerFactory.getLogger(MinaServer.class);
    private IoAcceptor ioAcceptor;

    public static void main(String[] args) {
    }

    public void startServer() {
        log.info("start...");
        ioAcceptor = new NioSocketAcceptor();
        ioAcceptor.getSessionConfig().setReadBufferSize(2048 * 2);
        ioAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
/*        DefaultIoFilterChainBuilder filterChain = ioAcceptor.getFilterChain();
        TextLineCodecFactory factory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        factory.setDecoderMaxLineLength(Integer.MAX_VALUE);
        factory.setEncoderMaxLineLength(Integer.MAX_VALUE);
        filterChain.addLast("executor", new ExecutorFilter());
        filterChain.addLast("codec", new ProtocolCodecFilter(factory));*/
        ioAcceptor.setHandler(new MinServerHandler());
        // filterChain.addLast("logging", new LoggingFilter());
        try {
            ioAcceptor.bind(new InetSocketAddress(7007));
            log.info("start success");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("start fail");
        }
    }

    public void stopServer() {
        if (null != ioAcceptor) {
            ioAcceptor.unbind();
            ioAcceptor = null;
        }
    }

}
