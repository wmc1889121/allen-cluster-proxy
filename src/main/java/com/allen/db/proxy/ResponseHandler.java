package com.allen.db.proxy;

import com.allen.protocol.client.AllenClient;
import com.allen.protocol.entity.NettyMessage;
import com.allen.protocol.handler.AllenReader;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@ChannelHandler.Sharable
public class ResponseHandler extends AllenReader {
    private final AllenClient client;

    @Override
    protected void channelRead(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        client.response(msg.getHeader().getRequestId(), msg);
    }
}
