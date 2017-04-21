package com.mawujun.navi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.mawujun.ems.MainActivity;
import com.mawujun.ems.R;
import com.mawujun.mobile.gps.model.BaseMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by mawujun on 2017/4/21.
 */

public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {

    //private Context context = MainApplication.getAppContext();

    //这里是断线要进行的操作
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("断线了。---------");
    }

    //这里是出现异常的话要进行的操作
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("出现异常了。。。。。。。。。。。。。");
        cause.printStackTrace();
    }

    //这里是接受服务端发送过来的消息
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        //http://www.cnblogs.com/chaoxiyouda/p/5432216.html有如何接受通知的内容
        Log.w("--------------test","从后台接收到数据:"+baseMsg.toString());
    }
}
