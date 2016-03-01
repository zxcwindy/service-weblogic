package org.zxc.service.provider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.zxc.service.context.SessionState;
import org.zxc.service.context.ShellSession;
import org.zxc.service.domain.ChannelMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShellHandler  extends TextWebSocketHandler{
	
	private static final  Map<String,ShellSession> SESSION_MAP = new HashMap<String,ShellSession>(); 

	private static final String CREATE = ".*create$";
	
	private static final String SEND = ".*send$";
	
	@Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {		
		String uri = session.getUri().getPath();
		if(uri.matches(CREATE)){
			create(session, message);
		}else if(uri.matches(SEND)){
			send(session,message);
		}
    }
	
	public void create(WebSocketSession session, TextMessage message){		
		String channelName = message.getPayload();
		TextMessage replayMessage = new TextMessage(SessionState.EXISTS.getState());;
		if(SESSION_MAP.get(channelName) == null){
			synchronized (SESSION_MAP) {
				if(SESSION_MAP.get(channelName) == null){
					SESSION_MAP.put(channelName, new ShellSession());			
					replayMessage = new TextMessage(SessionState.SUCCESS.getState());
				}
			}
		}
		try {
			session.sendMessage(replayMessage);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	public void send(WebSocketSession session, TextMessage message){
		String channelMsg = message.getPayload();
		try {
			ChannelMessage msg = new ObjectMapper().readValue(channelMsg, ChannelMessage.class);
			ShellSession shell = SESSION_MAP.get(msg.getChannelName());
			shell.exeCmd(msg.getContent(),session);
		} catch (JsonParseException e1) {
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}	
}
