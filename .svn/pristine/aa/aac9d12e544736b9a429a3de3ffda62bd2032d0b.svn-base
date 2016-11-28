package com.briup;

import java.util.HashMap;
import java.util.Map;

public class TESTmAP {
	public static void test1(Map<String,String> map1){
		Map<String,String> tmp = new HashMap<String,String>();
		tmp.put("name", "alan");
		map1 = tmp;
	}
	public static Map<String,String> test2(){
		Map<String,String> tmp = new HashMap<String,String>();
		tmp.put("name", "jade");
		return tmp;
	}
	public static void main(String[] args) {
		Map<String,String> tmp = new HashMap<String,String>();
		test1(tmp);
		System.out.println("未传出"+tmp.get("name"));
		tmp = test2();
		System.out.println("返回"+tmp.get("name"));
	}
}
