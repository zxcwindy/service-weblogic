package org.zxc.service.provider;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ShellHandler  extends TextWebSocketHandler{

	@Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
		System.out.println("111"+message.getPayload());
		TextMessage replayMessage = new TextMessage("sfdasdfsa中文");
		try {
			session.sendMessage(replayMessage);
		} catch (IOException e) {			
			e.printStackTrace();
		}
    }
}
