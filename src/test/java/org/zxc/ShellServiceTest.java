package org.zxc;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ShellServiceTest {
	public static void main(String[] arg){
	    
	    try{
	      JSch jsch=new JSch();
	      String host="darkstar";
	      String user="david";
	      String passwd="zxc628";
	      Session session=jsch.getSession(user, host, 22);	      
	      session.setPassword(passwd);
	       session.setConfig("StrictHostKeyChecking", "no");
 
	      session.connect(30000);   // making a connection with timeout.

	      Channel channel=session.openChannel("shell");

	      channel.setInputStream(System.in);
	   
	      channel.setOutputStream(System.out);

	      channel.connect(3000);
	    }
	    catch(Exception e){
	      System.out.println(e);
	    }
	  }
}
