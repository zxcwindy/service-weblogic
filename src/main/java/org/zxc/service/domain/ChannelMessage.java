package org.zxc.service.domain;

/**
 *客户端发送的信息体
 * @author david
 * 2016年6月18日
 */
public class ChannelMessage {
	private String channelName;
	
	private String content;

	/**
	 * 会话编号，session-id
	 * @return
	 */
	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}
