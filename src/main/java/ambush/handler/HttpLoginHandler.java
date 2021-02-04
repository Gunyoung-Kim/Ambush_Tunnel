package ambush.handler;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpLoginHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private static final File LOGIN;
	
	static {
		URL location = HttpLoginHandler.class
				.getProtectionDomain().getCodeSource().getLocation();
		try {
			String path = location.toURI() + "login.html";
			path = !path.contains("file:") ? path : path.substring(5);
			LOGIN = new File(path);
		} catch(URISyntaxException e) {
			throw new IllegalStateException("Unable to locate index.html", e);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		if(msg.uri().equals("")) {
			if(HttpUtil.is100ContinueExpected(msg)) {
				send100Continue(ctx);
			}
			RandomAccessFile file = new RandomAccessFile(LOGIN, "r");
			HttpResponse response = new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML);
			boolean keepAlive = HttpUtil.isKeepAlive(msg);
			if(keepAlive) {
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			ctx.write(response);
			
			//save resources by zero copy
			ctx.write(new DefaultFileRegion(file.getChannel(),0, file.length()));
			
			// if pipeline has sslhandler then use this
			//ctx.write(new ChunkedNioFile(file.getChannel()));
			
			ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			if(!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else {
			ctx.fireChannelRead(msg.retain());
		}
	}
	
	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		ctx.writeAndFlush(response);
	}
	
	@Override 
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
