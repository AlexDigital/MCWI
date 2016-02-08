package de.alexdigital.mcwi.web;

import com.sun.scenario.Settings;
import de.alexdigital.mcwi.McWebinterface;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class WebServer {

    @Getter
    @Setter
    private int port;

    public WebServer(int port) {
        this.setPort(port);
    }

    @Setter
    private Logger logger;

    private Channel channel;

    @Getter
    private SocketIO socketIO;

    public void start() {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            socketIO = new SocketIO("localhost", port + 1);
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(
                                        new StringEncoder(Charset.forName("UTF-8")),
                                        new LineBasedFrameDecoder(2048),
                                        new StringDecoder(Charset.forName("UTF-8")),
                                        new WebClientHandler());
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 50)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                this.channel = bootstrap.bind(port).sync().channel();
                this.logger.info("Webserver started");

                socketIO.start();
                this.logger.info("Socket.IO-Server started");

                this.channel.closeFuture().addListener(f -> this.logger.info("Webserver closed")).sync();
                this.logger.info("Socket.IO-Server closed");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    public void stop() {
        this.channel.close();
        socketIO.stop();
    }

}
