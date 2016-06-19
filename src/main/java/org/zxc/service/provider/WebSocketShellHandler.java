package org.zxc.service.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.fasterxml.jackson.core.type.TypeReference;
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
	 *创建shell的类型 "create"
	 */
	private static final String CREATE = "create";
	
	/**
	 * 发送命令的类型 "send"
	 */
	private static final String SEND = "send";
	
	/**
	 *关闭shell的类型"exit"
	 */
	private static final String EXIT = "exit";
	
	/**
	 * remoteshell的uri表达式".*remoteshell$"
	 */
	private static final String REMOTE_SHELL = ".*remoteshell$";
	
	/**
	 * 客户端发送消息体的通用转换规则
	 * <pre>
	 * {"type":"create","content":"{\"host\":.....}"}
	 * {"type":"send","content":"{\"sessionId\":.....}"}
	 * {"type":"exit","content":"{\"sessionId\":.....}"}
	 */
	private static final TypeReference<HashMap<String,String>> TYPE_REF = new TypeReference<HashMap<String,String>>() {};
	
	@Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {		
		String uri = session.getUri().getPath();
		if(uri.matches(REMOTE_SHELL)){
			process(session,message);
		}
    }
	
	/**
	 * 根据message的消息体中type的类型，判断是否创建、执行命令或者退出shell
	 * <p>type类型分为<ul>
	 * <li>create 创建
	 * <li> send 执行命令
	 * <li> exit 退出shell
	 * @param session
	 * @param message
	 */
	private void process(WebSocketSession session, TextMessage message) {
		String frame = message.getPayload();
		try {
			Map<String,String> frameMap = new ObjectMapper().readValue(frame, TYPE_REF);
			String type = frameMap.get("type") ;
			String content = frameMap.get("content") ;
			if(CREATE.equals(type)){
				create(session, content);
			}else if(SEND.equals(type)){
				send(session,content);
			}else if(EXIT.equals(type)){
				exit(session,content);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

	/**
	 * 通过通信中的信息创建shell会话，将创建的状态返回给客户端
	 * @param session
	 * @param message 为{@link org.zxc.service.domain.Shell}的json序列化对象
	 */
	private void create(WebSocketSession session, String frame){		
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
	private void send(WebSocketSession webSocketSession, String channelMsg){
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
	private void exit(WebSocketSession webSocketSession, String channelMsg){
		try {
			ChannelMessage msg = new ObjectMapper().readValue(channelMsg, ChannelMessage.class);
			shellService.process(msg.getChannelName(), msg.getContent());
			shellService.destory(msg.getChannelName());
			webSocketSession.close();
		}  catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
		}
	}	
}
