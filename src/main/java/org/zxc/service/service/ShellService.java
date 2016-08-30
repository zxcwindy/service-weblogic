package org.zxc.service.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.zxc.service.domain.Shell;
import org.zxc.service.exception.ShellException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * 负责当前容器中所有shell会话的创建，channel连接，销毁和查询以及命令查询
 * @author david
 * 2016年6月11日
 */
@Service
public class ShellService {
	
	private static final Logger LOG = Logger.getLogger(ShellService.class);
	
	/**
	 * 用sessionid做为key,shell做为value，保存所有的shell会话的引用
	 */
	private static final ConcurrentHashMap<String,Shell> SHELL_MAP= new ConcurrentHashMap<String,Shell>();
	
	/**
	 * 换行符
	 */
	private static final byte[] NEW_LINE = "\n".getBytes();
	
	/**
	 * 一次发送到客户端的字节长度
	 */
	private static final int BYTE_LENGTH = 8096;

	/**
	 * 使用JSch创建一个shell会话，打开一个类型为"shell"的channel，并注册到全局会话管理中
	 * <p>当sessionid在全局会话管理中已经存在时，不会进行创建动作。
	 * <p>此方法并不会调用{@link Channel#connect()}方法，真正开启shell操作时，需要通过
	 * <p>{@link Shell#getChannel()}调用{@link Channel#connect(int)}开启连接
	 * @param shell 需要被创建的shell对象
	 * @return 完成session和channel初始化的shell
	 * @throws JSchException 
	 * @throws IOException 
	 */
	public Shell create(final Shell shell) throws JSchException, IOException{
		if(SHELL_MAP.get(shell.getSessionId()) == null){
			Session session = initSession(shell);
			Channel channel = null;
			try {
				channel = initChannel(shell, session);
				shell.setSession(session);
				shell.setChannel(channel);
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
				destory(shell.getSessionId());
				throw e;
			}
			SHELL_MAP.put(shell.getSessionId(), shell);
		}
		return shell;
	}
	
	/**
	 * 将shell的session会话、channel、以及输入输出流关闭
	 * <p>并从全局管理中移除
	 * @param sessionId 需要被销毁的shell的sessionid
	 * @return 完成资源释放后的shell
	 */
	public Shell destory(String sessionId){
		Shell shell = SHELL_MAP.get(sessionId);
		if(shell.getChannel() != null){
			try {
				if(shell.getChannel().getOutputStream() != null){
					shell.getChannel().getOutputStream().close();
				}
				
				if(shell.getChannel().getInputStream() != null){
					shell.getChannel().getInputStream().close();
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			}
			shell.getChannel().disconnect();
		}
		
		if(shell.getSession() != null){
			shell.getSession().disconnect();
		}
		
		return SHELL_MAP.remove(shell.getSessionId());
	}
	
	/**
	 * 通过sessionid获取shell
	 * @param sessionId
	 * @return shell对象
	 */
	public Shell getShell(String sessionId){
		Shell shell = SHELL_MAP.get(sessionId);
		if(shell == null){
			throw new ShellException(sessionId + "会话不存在");
		}
		return shell;
	}
	
	/**
	 * 启动sessionid对应的channel连接，超时时间为3000
	 * @param sessionId
	 * @throws JSchException 
	 */
	public void connectChannel(String sessionId,final WebSocketSession websocketSession) throws JSchException{
		final Shell shell = getShell(sessionId);
		if(!shell.getChannel().isConnected()){
			try {
				shell.getChannel().connect(3000);
				new Thread(new Runnable(){
					public void run(){
					    try(final BufferedReader br = new BufferedReader(new InputStreamReader(shell.getIs()));){
					    	 String str = null;
						        byte[] tempByte = null;
						        while((str = br.readLine()) != null){
						        	tempByte = (str).getBytes();
						        	sendMessage(websocketSession,ArrayUtils.addAll(tempByte, NEW_LINE));
						        }
						} catch (IOException e){
							LOG.error(e.getMessage(),e);
						} 
					    LOG.info(shell.getSessionId()+"会话结束");
					} }).start();
				process(sessionId, "");
			} catch (JSchException e) {
				LOG.error(e.getMessage(),e);
				throw e;
			}
		}
	}

	/**
	 * 从根据sessionid找到shell对象，并中获取输入流和输出流，将命令放入输入流中执行
	 * <p>执行结果返回到输出流中，并通过websocket输出到客户端
	 * @param sessionId
	 * @param cmd
	 */
	public void process(String sessionId,String cmd){
		Shell shell = SHELL_MAP.get(sessionId);
		try {
			OutputStream os = shell.getChannel().getOutputStream();
			os.write(ArrayUtils.addAll(cmd.getBytes(), NEW_LINE));
			os.flush();
			os.write(NEW_LINE);
			os.flush();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 初始化session，关闭StrictHostKeyChecking，超时时间为30秒
	 * @param shell 需要被创建的shell对象
	 * @return 初始化成功的session
	 * @throws JSchException  当jsch session连接过程中出现错误时，抛出此异常
	 */
	private Session initSession(Shell shell) throws JSchException{
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(shell.getUserName(), shell.getHost(), shell.getPort());
			session.setPassword(shell.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(30000);
		} catch (JSchException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
		return session;
	}
	
	/**
	 * 初始化类型为"shell"的channel，设置channel的输出流为 {@link java.io.PipedInputStream} 输入流为 {@link java.io.PipedOutputStream}
	 * @param shell 需要创建shell对象
	 * @return channel 根据session创建的channel
	 * @throws JSchException 当jsch连接过程中出现错误时，抛出此异常
	 * @throws IOException channel设置输入输出流错误时，抛出此异常
	 */
	private Channel initChannel(final Shell shell,Session session) throws JSchException, IOException{
		Channel channel = null;
		PipedInputStream channelInputStream = new PipedInputStream();
		PipedOutputStream channelOutputStream = new PipedOutputStream();
		shell.setOs(channelOutputStream);
		shell.setIs(channelInputStream);
		try {
			channel = session.openChannel("shell");
			shell.setChannel(channel);
			
			channel.setInputStream(new PipedInputStream(channelOutputStream));
			channel.setOutputStream(new PipedOutputStream(channelInputStream));
		} catch (JSchException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
		return channel;
	}
	
	/**
	 * 向客户端发送执行结果，发送的结果有websocket的长度限制
	 * @param websocketSession
	 * @param messageByte 当前字节流
	 */
	private void sendMessage(WebSocketSession websocketSession,byte[] messageByte){
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
			buffer.put(results,start*BYTE_LENGTH,start*BYTE_LENGTH+length);
			TextMessage replayMessage = new TextMessage(buffer.array());
			session.sendMessage(replayMessage);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}
