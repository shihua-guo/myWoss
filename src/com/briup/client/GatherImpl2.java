package com.briup.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.briup.util.BIDR;

/**
 * 封装解析方法，获取BIDR类方法
 * @author alan
 * @date Oct 9, 2016 6:56:34 PM
 */
public class GatherImpl2 implements com.briup.woss.client.Gather {
	// 统计多少个BIDR
	private static int count = 0;
	// 存放
	private Collection<BIDR> bidrList = new ArrayList<BIDR>();

	@Override
	public void init(Properties arg0) {

	}

	/**
	 * @date 2016-10-09 15:10:45 采集数据：配对上下线.
	 */
	@Override
	public Collection<BIDR> gather() throws Exception {
		// 读取文件
		File file = new File("src/com/briup/file/radwtmp");
		// 包装成ra，模式：rw，方便读取，记录读取的位置
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		parser(raf);
		raf.close();//关闭raf
		return bidrList;
	}

	/**
	 * @date 2016-10-09 19:00:37 封装parser方法。
	 * @param raf 文件随机流
	 * @throws Exception
	 */
	public void parser(RandomAccessFile raf)
			throws Exception {
		// 读取文件的每一行，判断是否结束
		String str1 = null;
		// 创建存放上下线的map
		Map<String, Integer> ooMap = new HashMap<String, Integer>();
		// 创建存放BIDR类的map
		Map<String, String[]> bidrMap = new HashMap<String, String[]>();
		while ((str1 = raf.readLine()) != null) {
			// 解析每一行数据,去除开头的#,得到一个String,并分割得到String数组
			String str2 = str1.split("#")[1];
			String[] strInfo = str2.split("\\|");// 这里需要注意
			/*
			 * 匹配上下线：使用2个map：1.存放BIDR 2.存放统计上下线
			 */
			String key = strInfo[4];// 以IP作为key，用作之后的匹配
			if (ooMap.containsKey(key)) {// 如果当前有匹配上。
				int ooTmp = ooMap.get(key);// 获取当前count
				if (strInfo[2].equals("7")) {// 如果是上线
					ooTmp++;// oo++
					ooMap.put(key, ooTmp);
				} else {// 如果是下线
					ooTmp--;// oo--
					ooMap.put(key, ooTmp);
				}

				/*
				 * 如果count等于0，就创建BIDR对象
				 */
				if (ooMap.get(key) == 0) {
					BIDR bidr = getBIDR(key,bidrMap,strInfo);// 存放如所有信息
					ooMap.remove(key);// 移除成功匹配的ooMap
					bidrMap.remove(key);// 移除成功匹配的bidrMap
					count++;//记录有多少个
					bidrList.add(bidr);// 把统计好的bidr放入List中
				}

			} else {// 如果没有匹配上：当前map中没有统计该用户的上下线信息
				ooMap.put(key, 1);// 放入统计上下线次数的map
				bidrMap.put(key, strInfo);// 存放第一次上线时间
			}
		}

	}
	
	/**
	 * @date 2016-10-09 18:53:23 获取一个BIDR对象
	 * @param key	匹配上下线的ip
	 * @param bidrMap	存放登陆信息的Map
	 * @param strInfo	存放当前读取到的信息
	 * @return BIDR	返回一个BIDR对象
	 * @throws UnknownHostException
	 */
	public BIDR getBIDR(String key,Map<String, String[]> bidrMap,String[] strInfo) throws UnknownHostException{
		String[] loginInfo = bidrMap.get(key);// 获取登陆的所有信息
		String login_name = loginInfo[0];// 获取用户名
		String login_str = loginInfo[3];// 获取登陆的string
		String logout_str = strInfo[3];// 获取登出的string
		String login_ip = loginInfo[4];// 获取登陆IP
		Timestamp login_date = new Timestamp(Long.parseLong(login_str));// 获取登陆时间
		Timestamp logout_date = new Timestamp(Long.parseLong(logout_str));// 获取登出时间
		String nas_ip = InetAddress.getLocalHost().getHostAddress();// 获取NAS(本机地址)
		Integer time_deration = Integer.parseInt(logout_str) - Integer.parseInt(login_str);// 获取在线时间
		BIDR bidr = new BIDR(login_name, login_ip, login_date, logout_date, nas_ip, time_deration);// 存放如所有信息
		return bidr;
	}

	
	/**
	 * @date 2016-10-09 17:48:24 测试打印bidr对象
	 * @param b
	 */
	public void printBidr(BIDR bidr) {
		System.out.println("loginName:" + bidr.getAAA_login_name());
		System.out.println("loginIp:" + bidr.getLogin_ip());
		System.out.println("login_date:" + bidr.getLogin_date());
		System.out.println("logout_date:" + bidr.getLogout_date());
		System.out.println("NAS_ip:" + bidr.getNAS_ip());
		System.out.println("time_deration:" + bidr.getTime_deration());
	}
	
	/**
	 * @date 2016-10-09 19:08:33 打印当前List
	 */
	public void printBidrList(){
		for(BIDR bidr:bidrList){
			printBidr(bidr);
		}
	}
	
	public static void main(String[] args) {
		try {
			GatherImpl2 g2 = new GatherImpl2();
			g2.gather();
			g2.printBidrList();
			System.out.println(count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
