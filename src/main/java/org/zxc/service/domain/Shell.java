package org.zxc.service.domain;

import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Shell implements Runnable {
	private String shellName;
	
	private InputStream is;
	
	private OutputStream os;
	
	public Shell(String shellName){
		this.shellName = shellName;
		JSch jsch=new JSch();
		String host="darkstar";
	      String user="david";
	      String passwd="zxc628";
	      Session shellSession = null;
		try {
			shellSession = jsch.getSession(user, host, 22);
			shellSession.setPassword(passwd);
		      shellSession.setConfig("StrictHostKeyChecking", "no");
	 
		      shellSession.connect(30000);   // making a connection with timeout.

		      Channel channel=shellSession.openChannel("shell");
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
