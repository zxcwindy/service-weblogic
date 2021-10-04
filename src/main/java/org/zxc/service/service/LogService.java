package org.zxc.service.service;

public class LogService {
	
	/**
	 * 日志信息
	 */
	protected String scheduleLog = "";

	protected void log(String logInfo) {
		this.scheduleLog += "\r\n" + logInfo;
	}
	

	public String getScheduleLog() {
		return scheduleLog;
	}
}
