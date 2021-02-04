package ambush.user;

import java.util.HashMap;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class ChannelGroupManager {
	
	//synchronizing
	private static ChannelGroupManager instance;
	
	private final HashMap<String,ChannelGroup> channelGroupSet = new HashMap<>();
	
	private ChannelGroupManager() {
	}
	
	public static ChannelGroupManager getInstance() {
		if(instance == null) {
			synchronized(ChannelGroupManager.class) {
				if(instance== null) 
					instance = new ChannelGroupManager();
			}
		}
		return instance;
	}
	
	public boolean addGroup(String name) {
		if(channelGroupSet.containsKey(name)) {
			return false;
		} else {
			channelGroupSet.put(name, new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE));
			return true;
		}
	}
	
	public void removeGroup(String name) {
		channelGroupSet.remove(name);
	}
	
	public void sendMessage(String groupName, TextWebSocketFrame msg) {
		channelGroupSet.get(groupName).writeAndFlush(msg.retain());
	}
}
