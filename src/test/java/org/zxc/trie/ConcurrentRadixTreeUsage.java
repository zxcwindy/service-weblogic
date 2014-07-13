package org.zxc.trie;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;


public class ConcurrentRadixTreeUsage {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("加载开始");
		RadixTree<Integer> tree = new ConcurrentRadixTree1<Integer>(
				new DefaultCharArrayNodeFactory());
		List<String> strList = FileUtils.readLines(new File("/home/asiainfo/tmp/tablenames"));
		for(int i = 0; i< strList.size(); i++){
			tree.put(strList.get(i), i+1);
		}
		System.out.println("加载结束");
//		System.out.println("Keys starting with 'T': "
//				+ Iterables.toString(tree.getKeysStartingWith("T")));
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);
		String str = "";
		while((str = br.readLine()) != null){
			if(str.equals("quit")){
				break;
			}
			long starttime = System.currentTimeMillis();
			Iterable<CharSequence> ic = tree.getKeysStartingWith(str);
			Iterator<CharSequence> it = ic.iterator();
			int i = 0;
			List<String> newStrList = new ArrayList<String>();
			while(it.hasNext()){
				newStrList.add(it.next().toString());
				i++;
				if(i>10){
					break;
				}
			}
			long endtime = System.currentTimeMillis();
			System.out.println((endtime-starttime)+"ms");
			for(String str1 : newStrList){
				System.out.print(str1 + ", ");
			}	
			System.out.println();
		}
		
		br.close();
		is.close();
	}

}
