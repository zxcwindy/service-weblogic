package org.zxc.service.service;

import org.apache.commons.dbutils.QueryRunner;

public class DdlTest {
	
	int a = 0;
	
   void set(int b){
		this.a = b;
	}
	
	public DdlTest(int a){
		this.a = a;
	}
	
	static void change(DdlTest test){
		test.set(3);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DdlTest test = new DdlTest(5);
		change(test);
		System.out.println(test.get());
	}

	int get() {
		return this.a;
	}

}
