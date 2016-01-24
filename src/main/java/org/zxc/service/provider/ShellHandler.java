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
	
	private static final int BYTE_LENGTH = 8096;
	
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
		byte[] results = null;
		try {
			ChannelMessage msg = new ObjectMapper().readValue(channelMsg, ChannelMessage.class);
			ShellSession shell = SESSION_MAP.get(msg.getChannelName());
			results = shell.exeCmd(msg.getContent());
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			results = e1.getMessage().getBytes();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			results = e1.getMessage().getBytes();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			results = e1.getMessage().getBytes();
		}
		int num = results.length / BYTE_LENGTH;
		int mod = results.length % BYTE_LENGTH;
		for(int i = 0 ; i < num;i++){			
			sendMessage(session,results,i*BYTE_LENGTH,BYTE_LENGTH);			
		}		
		sendMessage(session,results,num*BYTE_LENGTH,mod);
	}	
	
	private void sendMessage(WebSocketSession session,byte[] results,int start,int length){
		ByteBuffer buffer =ByteBuffer.allocate(length);
		buffer.put(results,start*BYTE_LENGTH,start*BYTE_LENGTH+length-1);
		TextMessage replayMessage = new TextMessage(buffer.array());
//		BinaryMessage replayMessage = new BinaryMessage(buffer);
		try {
			session.sendMessage(replayMessage);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
}
