package com.mawujun.navi;




import android.util.Log;

import com.mawujun.mobile.gps.model.BaseMsg;
import com.mawujun.mobile.gps.model.ConnectMsg;
import com.mawujun.mobile.gps.model.Constants;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import static android.R.attr.password;

/**
 * Created by mawujun on 2017/4/21.
 */

public class NettyClientBootstrap {
    private static final NettyClientBootstrap bootstrap = new NettyClientBootstrap();
    private int port = 9091;
    private String host = "122.227.163.82";

    public boolean isConnected() {
        if (socketChannel != null && socketChannel.isOpen()) {
            return true;
        } else {
            return false;
        }
    }



    private boolean connected=false;
    //private ServerInfoModel serverInfoModel = new ServerInfoModelImpl();
    private SocketChannel socketChannel;
    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);

    public static NettyClientBootstrap getInstance() {
        return bootstrap;
    }

    public void push(BaseMsg msg){
        socketChannel.writeAndFlush(msg);
    }
    public boolean connected(String sessionId) {
        if (socketChannel != null && socketChannel.isOpen()) {
            System.out.println("已经连接");
            //listener.onExist();
            return true;
        } else {
            Constants.setClientId(sessionId);// TODO: 2016/2/23
            System.out.println("长链接开始");
            Log.w("--------------test","长链接开始");
            if (startConnect()) {
                connected=true;

                ConnectMsg connectMsg = new ConnectMsg();// TODO: 2016/2/23
                connectMsg.setClientId(sessionId);
                socketChannel.writeAndFlush(connectMsg);
                System.out.println("长链接成功");
                Log.w("--------------test","长链接成功");
                //listener.onSuccess();
                return true;
            } else {
                System.out.println("长链接失败...");
                Log.w("--------------test","长链接失败");
                //listener.onFailure();
                return false;
            }
        }
    }

    private Boolean startConnect() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
//        if (serverInfoModel.load() != null) {
//            host = serverInfoModel.load().getServerIp();
//        }
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(20, 10, 0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new NettyClientHandler());
            }
        });
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                System.out.println("connect server  成功---------");
                return true;
            } else {
                System.out.println("connect server  失败---------");
                return false;
            }
        } catch (Exception e) {
            System.out.println("无法连接----------------");
            return false;
        }
    }

    /**
     * 关闭通道
     */
    public void closeChannel() {
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    /**
     * @return 返回通道连接状态
     */
    public boolean isOpen() {
        if (socketChannel != null) {
            System.out.println(socketChannel.isOpen());
            return socketChannel.isOpen();
        }
        return false;
    }

    public interface OnConnectListener {

        void onExist();

        void onSuccess();

        void onFailure();
    }
}
