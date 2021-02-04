package ambush;

import java.net.InetSocketAddress;

import ambush.handler.BootstrapChildInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AmbushMain {
	
	private final EventLoopGroup group = new NioEventLoopGroup();
	private Channel channel;
	
	public ChannelFuture start(InetSocketAddress address) {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(group)
			.channel(NioServerSocketChannel.class)
			.childHandler(createInitializer());
		ChannelFuture bindFuture = bootstrap.bind(address);
		bindFuture.syncUninterruptibly();
		channel = bindFuture.channel();
		
		return bindFuture;
	}
	
	protected ChannelInitializer<Channel> createInitializer() {
		return new BootstrapChildInitializer();
	}
	
	private void destroy() {
		if(channel != null)
			channel.close();
		
		group.shutdownGracefully();
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Give port number");
			System.exit(-1);
		}
		
		int port = Integer.parseInt(args[0]);
		final AmbushMain endpoint = new AmbushMain();
		ChannelFuture startFuture = endpoint.start(new InetSocketAddress(port));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override 
			public void run() {
				endpoint.destroy();
			}
		});
		
		startFuture.channel().closeFuture().syncUninterruptibly();
	}

}
