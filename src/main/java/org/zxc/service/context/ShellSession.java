package org.zxc.service.context;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.zxc.service.provider.ShellHandler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ShellSession {
	
	private static final Logger LOG = Logger.getLogger(ShellSession.class);
	
	public static final int BYTE_LENGTH = 8096;
	
	private static final String EOF = "";
	
	private JSch jsch = new JSch();

	String host = "darkstar";
	String user = "david";
	String passwd = "taohaoziji";

	Session session = null;
	Channel channel = null;
	
	InputStream in =  new ByteArrayInputStream(new byte[8092]);
	OutputStream out = new ByteArrayOutputStream();

	public ShellSession() {
		try {
			session = jsch.getSession(user, host, 22);
			session.setPassword(passwd);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000); // making a connection with timeout.
//			channel = session.openChannel("shell");
//			channel.setInputStream(in);
			
//			channel.connect(3000);
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * 在服务器端执行shell命令，将执行结果返回到客户端的通道中
	 * @param cmd 执行的命令
	 * @param websocketSession 对应输入的websession
	 * @return 命令是否执行成功
	 */
	public boolean exeCmd(String cmd,WebSocketSession websocketSession){
		boolean execSuccess = true;
		BufferedReader br = null;
        InputStreamReader isr = null;
	     try {
	        Channel channel = session.openChannel("exec");
	        LOG.info("cmd:" + cmd);
	        ((ChannelExec)channel).setCommand(cmd);
	        isr = new InputStreamReader(channel.getInputStream(),"utf-8");
	        br = new BufferedReader(isr); 
	        channel.connect();
	        String str = null;
	        byte[] tempByte = null;
	        while((str = br.readLine()) != null){
	        	tempByte = (str+"\n").getBytes();
	        	sendMessage(websocketSession,tempByte);
	        }
	        sendMessage(websocketSession,EOF.getBytes());
	        channel.disconnect();
	     }catch(IOException ioX){
	    	 execSuccess = false;
	       ioX.printStackTrace();
	     } catch(JSchException jschX){
	    	 execSuccess = false;
	    	 jschX.printStackTrace();
	     }finally{
	    	 if(br != null){
	    		 try {
					br.close();
				} catch (IOException e) {
					execSuccess = false;
					e.printStackTrace();
				}
	    	 }
	    	 if(isr != null){
	    		 try {
					isr.close();
				} catch (IOException e) {
					execSuccess = false;
					e.printStackTrace();
				}
	    	 }
	     }
	     return execSuccess;
	}	
	
	/**
	 * 向客户端发送执行结果，发送的结果有websocket的长度限制
	 * @param websocketSession
	 * @param messageByte 当前字节流
	 */
	public void sendMessage(WebSocketSession websocketSession,byte[] messageByte){
		int num = messageByte.length / BYTE_LENGTH;
		int mod = messageByte.length % BYTE_LENGTH;
		for(int i = 0 ; i < num;i++){			
			sendMessage(websocketSession,messageByte,i*BYTE_LENGTH,BYTE_LENGTH);			
		}		
		sendMessage(websocketSession,messageByte,num*BYTE_LENGTH,mod);		
	}
	
	/**
	 * 向客户端发送执行结果，发送指定字节长度的结果有websocket的长度限制
	 * @param session
	 * @param results 当前行的字节流
	 * @param start 字节流的开始位置
	 * @param length 字节流发送的长度
	 */
	private void sendMessage(WebSocketSession session,byte[] results,int start,int length){
		try {
			ByteBuffer buffer =ByteBuffer.allocate(length);
			buffer.put(results,start*BYTE_LENGTH,start*BYTE_LENGTH+length-1);
			TextMessage replayMessage = new TextMessage(buffer.array());
			session.sendMessage(replayMessage);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}
