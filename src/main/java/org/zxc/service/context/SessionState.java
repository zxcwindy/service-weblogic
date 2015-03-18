package org.zxc.service.context;

public enum SessionState {
	EXISTS("会话已创建"),SUCCESS("会话创建成功");
	
	private String state = null;
	
	private SessionState(String state){
		this.state  = state;
	}
	
	public String getState(){
		return this.state;
	}
}
