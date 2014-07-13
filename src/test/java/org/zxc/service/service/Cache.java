package org.zxc.trie;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;

public class Cache implements Runnable {	
	
	private List<String> strList = null;
	
	private List<String> oldList = null;
	
	private static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(10);
	
	public static void main(String[] args) {
		scheduler.scheduleAtFixedRate(new Cache(), 0, 30, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
//		while(true){
			System.out.println("加载开始 " + new Date());
			
			RadixTree<Integer> tree = new ConcurrentRadixTree<Integer>(
					new DefaultCharSequenceNodeFactory());
			try {
				strList = FileUtils.readLines(new File(
						"/home/asiainfo/tmp/tables"));
//				if(oldList == null){
//					oldList = strList;
//				}else{
//										
//				}
				for (int i = 0; i < strList.size(); i++) {
					tree.put(strList.get(i), i + 1);
				}
				strList = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("加载结束" + new Date());
//			try {
//				Thread.sleep(30000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
	}

}
