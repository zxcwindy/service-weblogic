package org.zxc.service.domain;

import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

/**
 * shell对象，包含sessioinid,主机，用户名，密码，端口，输入输出流信息
 * @author david
 * 2016年6月11日
 */
public class Shell{
	/**
	 * shell会话id
	 */
	private String sessionId;
	
	/**
	 * 主机用户名
	 */
	private String userName;
	
	/**
	 * 主机用户密码
	 */
	private String password;
	
	/**
	 * 主机ip或域名
	 */
	private String host;
	
	/**
	 * 默认端口：22
	 */
	private int port = 22;
	
	/**
	 * shell的session会话
	 */
	@JsonIgnore
	private Session session;
	
	/**
	 * session对应的channel
	 */
	@JsonIgnore
	private Channel channel;
	
	/**
	 * shell输出流
	 */
	@JsonIgnore
	private InputStream is;
	
	/**
	 * shell 输入流
	 */
	@JsonIgnore
	private OutputStream os;
	
	/**
	 * shell会话的字符集，默认为utf-8
	 */
	private String lang =  "utf-8";
	
	public Shell(){}
	
	public Shell(String sessionId){
		this.sessionId = sessionId;
	}

	/**
	 * 构造一个shell对象
	 * @param sessionId 会话id
	 * @param userName 用户名
	 * @param password 密码
	 * @param host 主机
	 * @param port 端口
	 */
	public Shell(String sessionId, String userName, String password,
			String host, int port) {
		this.sessionId = sessionId;
		this.userName = userName;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	/**
	 * 默认端口为22的shell
	 * @param sessionId
	 * @param userName
	 * @param password
	 * @param host
	 */
	public Shell(String sessionId, String userName, String password, String host) {
		this(sessionId,userName,password,host,22);
	}

	/**
	 * 获取sessionId
	 * @return sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * 获取用户名
	 * @return 用户名
	 */
	public String getUserName() {
		return userName;
	}

	
	/**
	 * 设置用户密码
	 * @param password 密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 获取密码
	 * @return 密码
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 获取主机
	 * @return 主机ip或者域名
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 获取连接的端口
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 获取输出流
	 * @return 输出流
	 */
	public InputStream getIs() {
		return is;
	}

	/**
	 * 设置输出流
	 * @param is 输出流
	 */
	public void setIs(InputStream is) {
		this.is = is;
	}

	/**
	 * 获取输入流
	 * @return
	 */
	public OutputStream getOs() {
		return os;
	}

	/**
	 * 设置输入流
	 * @param os
	 */
	public void setOs(OutputStream os) {
		this.os = os;
	}

	/**
	 * 获取session会话
	 * @return session会话
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * 设置session会话
	 * @param session session会话
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * 获取session对应的channel
	 * @return channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * 设置session对应的channel
	 * @param channel
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * 获取shell的字符集
	 * @return 字符编码
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * 设置字符编码
	 * @param lang 字符编码
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	
}
