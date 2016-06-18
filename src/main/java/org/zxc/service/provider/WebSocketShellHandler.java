package org.zxc.service.provider;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.zxc.service.context.SessionState;
import org.zxc.service.domain.ChannelMessage;
import org.zxc.service.domain.Shell;
import org.zxc.service.service.ShellService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

/**
 * 接受shell命令的websocket消息处理器
 * @author david
 * 2016年6月11日
 */
@Component
public class WebSocketShellHandler extends TextWebSocketHandler{
	
	private static final Logger LOG = Logger.getLogger(WebSocketShellHandler.class);

	@Autowired
	private ShellService shellService;
	/**
	 *创建shell的url路由表达式 ".*create$"
	 */
	private static final String CREATE = ".*create$";
	
	/**
	 * 发送命令的uri路由表达式 ".*send$"
	 */
	private static final String SEND = ".*send$";
	
	/**
	 *关闭shell的url路由表达式 ".*exit$"
	 */
	private static final String EXIT = ".*exit$";
	
	@Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {		
		String uri = session.getUri().getPath();
		if(uri.matches(CREATE)){
			create(session, message);
		}else if(uri.matches(SEND)){
			send(session,message);
		}else if(uri.matches(EXIT)){
			exit(session,message);
		}
    }
	
	/**
	 * 通过通信中的信息创建shell会话，将创建的状态返回给客户端
	 * @param session
	 * @param message 为{@link org.zxc.service.domain.Shell}的json序列化对象
	 */
	private void create(WebSocketSession session, TextMessage message){		
		String frame = message.getPayload();
		ObjectMapper mapper = new ObjectMapper();
		Shell shell = null;
		TextMessage replayMessage  = null;
		try {
			shell = mapper.readValue(frame, Shell.class);
		} catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
			replayMessage = new TextMessage(e1.getMessage());
		} 
		
		if(replayMessage == null){
			try {
				shellService.create(shell);
				shellService.connectChannel(shell.getSessionId(),session);
//				replayMessage = new TextMessage(SessionState.SUCCESS.getState());
			} catch (JSchException | IOException e1) {
				LOG.error(e1.getMessage(), e1);
				replayMessage = new TextMessage(e1.getMessage());
				try {
					session.sendMessage(replayMessage);
				} catch (IOException e) {
					LOG.error(e1.getMessage(), e);	
				}
			} 
		}
	}
	
	/**
	 * 通过通信中的信息获取shell，关闭shell的会话，包括session,channle以及shell的管道
	 * @param webSocketSession
	 * @param message {@link ChannelMessage}的json序列化对象
	 */
	private void send(WebSocketSession webSocketSession, TextMessage message){
		String channelMsg = message.getPayload();
		TextMessage replayMessage  = null;
		try {
			ChannelMessage msg = new ObjectMapper().readValue(channelMsg, ChannelMessage.class);
			shellService.process(msg.getChannelName(), msg.getContent());
		}  catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
			replayMessage = new TextMessage(e1.getMessage());
		}
		if(replayMessage != null){
			try {
				webSocketSession.sendMessage(replayMessage);
			} catch (IOException e) {			
				LOG.error(e.getMessage(), e);
			}
		}
	}	
	
	/**
	 * 通过通信中的信息获取执行命令，发送到channel对应的shell中执行
	 * @param webSocketSession
	 * @param message {@link ChannelMessage}的json序列化对象
	 */
	private void exit(WebSocketSession webSocketSession, TextMessage message){
		String channelMsg = message.getPayload();
		TextMessage replayMessage  = null;
		try {
			ChannelMessage msg = new ObjectMapper().readValue(channelMsg, ChannelMessage.class);
			shellService.process(msg.getChannelName(), msg.getContent());
			shellService.destory(msg.getChannelName());
		}  catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
			replayMessage = new TextMessage(e1.getMessage());
		}
		if(replayMessage != null){
			try {
				webSocketSession.sendMessage(replayMessage);
			} catch (IOException e) {			
				LOG.error(e.getMessage(), e);
			}
		}
	}	
}
