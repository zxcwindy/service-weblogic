package org.zxc.service.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;

public class TableCache {
	private static final int MAX_NUM = 10;
	
	private static final int PER_TREE_MAX_NUM = 50000;
	
	private List<RadixTree<Integer>> treeList = new ArrayList<RadixTree<Integer>>();
	
	public TableCache(List<String> tableNameList){
		RadixTree<Integer> tree = new ConcurrentRadixTree<Integer>(new DefaultCharSequenceNodeFactory());
		int index = 0;
		for(int i = 0; i < tableNameList.size(); i++){
			tree.put(tableNameList.get(i), i+1);
			if(index++ > PER_TREE_MAX_NUM){
				treeList.add(tree);
				index = 0;
				tree = new ConcurrentRadixTree<Integer>(new DefaultCharSequenceNodeFactory());
			}
		}
		treeList.add(tree);
	}
	
	public List<String> getTables(String prefix){
		List<String> resultList = new ArrayList<String>();
		int i = 0;
		outer_loop:for(RadixTree<Integer> tree : treeList){
			Iterator<CharSequence> iterator = tree.getKeysStartingWith(prefix).iterator();
			while(iterator.hasNext()){
				resultList.add(iterator.next().toString());
				i++;
				if(i == MAX_NUM){
					break outer_loop;
				}
			}
		}		
		return resultList;
	}
}
