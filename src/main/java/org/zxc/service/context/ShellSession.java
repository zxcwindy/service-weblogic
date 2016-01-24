package org.zxc.service.context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.log4j.Logger;
import org.zxc.service.dao.DBUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ShellSession {
	
	private static final Logger LOG = Logger.getLogger(ShellSession.class);
	
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
	
	public byte[] exeCmd(String cmd){
		StringBuilder outputBuffer = new StringBuilder();
		BufferedReader br = null;
        InputStreamReader isr = null;
	     try
	     {
	        Channel channel = session.openChannel("exec");
	        LOG.info("cmd:" + cmd);
	        ((ChannelExec)channel).setCommand(cmd);
//	        InputStream commandOutput = channel.getInputStream();
	        isr = new InputStreamReader(channel.getInputStream(),"utf-8");
	        br = new BufferedReader(isr); 
	        
	        channel.connect();
	        String str = null;
	       
	        while((str = br.readLine()) != null){
	        	outputBuffer.append(str+"\n");
	        }
//	        int readByte = commandOutput.read();
//	        while(readByte != 0xffffffff)
//	        {
//	           outputBuffer.append((char)readByte);
//	           readByte = commandOutput.read();
//	        }

	        channel.disconnect();
	     }
	     catch(IOException ioX)
	     {
	       ioX.printStackTrace();
	     }
	     catch(JSchException jschX)
	     {
	    	 jschX.printStackTrace();
	     }finally{
	    	 if(br != null){
	    		 try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	 }
	    	 
	    	 if(isr != null){
	    		 try {
					isr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	 }
	     }
	     return outputBuffer.toString().getBytes();
	}	
}
