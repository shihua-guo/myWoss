package com.briup.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.briup.util.BIDR;

/**
 * 解决分时读取radwtmp，每次读取完毕记录文件位置,修改bidrMap的values变为String
 * @author alan
 * @date Oct 9, 2016 6:56:34 PM
 */
public class GatherImpl3 implements com.briup.woss.client.Gather {
	// 统计多少个BIDR
	private static int count = 0;
	//统计本次有多少未匹配成功
	private static int countFail=0;
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
		File file = new File("src/com/briup/file/radwtmp2");
		// 包装成ra，模式：rw，方便读取，记录读取的位置
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		//读取index文件
		File file2 = new File("src/com/briup/client/index");
		if(file2.length()!=0){
			BufferedReader br = new BufferedReader(new FileReader(file2));
			Long index = Long.parseLong(br.readLine());
			raf.seek(index);
		}
		//解析文件
		parser(raf);
		//存入当前位置
		FileWriter fw = new FileWriter(file2);
		fw.write(raf.getFilePointer()+"");
		//关闭raf
		fw.close();
		raf.close();
		return bidrList;
	}

	/**
	 * @date 2016-10-09 19:00:37 封装parser方法。
	 * @param raf 文件随机流
	 * @throws Exception
	 */
	private void parser(RandomAccessFile raf)
			throws Exception {
		// 读取文件的每一行，判断是否结束
		String str1 = null;
		// 创建存放上下线的map
		Map<String, Integer> ooMap = new HashMap<String, Integer>();
		// 创建存放BIDR类的map
		Map<String, String[]> bidrMap = new HashMap<String, String[]>();
		//获取上次未匹配成功的
		getFail(ooMap, bidrMap);
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
				// 默认一个用户第一次一定是上线
				ooMap.put(key, 1);// 放入统计上下线次数的map
				bidrMap.put(key, strInfo);// 存放第一次上线时间
			}
		}
		if(!ooMap.isEmpty()){//如果存在未匹配成功的
			countFail=ooMap.size();//得到未匹配成功的个数
			storeFail(ooMap,bidrMap);//最后把未匹配的存入文件
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
	private BIDR getBIDR(String key,Map<String, String[]> bidrMap,String[] strInfo) throws UnknownHostException{
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
	 * @date 2016-10-09 21:09:22 把未匹配成功的存入2个文件：
	 * @param ooMap 统计上下线的Map
	 * @param bidrMap 统计上线未匹配的Map
	 */
	private void storeFail(Map<String, Integer> ooMap,Map<String, String[]> bidrMap){
		Properties ooP =new Properties();//存放统计上下线的Map的Properties
		Properties bidrP =new Properties();//存放统计上线未匹配的Map的Properties
		FileWriter oofw = null;
		FileWriter bidrfw = null;
		try {
			oofw = new FileWriter("src/com/briup/client/ooMap.properties");
			bidrfw = new FileWriter("src/com/briup/client/bidrMap.properties");
			for(String key:ooMap.keySet()){//遍历ooMap存放进Properties
				ooP.setProperty(key, ooMap.get(key).toString());
			}
			for(String key:bidrMap.keySet()){//遍历bidrMap存放Properties
				String info ="";
				int len = bidrMap.get(key).length;
				String[] infoArr = bidrMap.get(key);
				for(int i=0;i<len;i++){
//					System.out.println(infoArr[i]);
					if(i==len-1){
						info+=infoArr[i];
					}
					else{
						info=info+infoArr[i]+",";
					}
				}
				bidrP.setProperty(key, info);
			}
			ooP.store(oofw, "存放统计上下线的Map");
			bidrP.store(bidrfw, "存放统计上线未匹配的BIDR信息的Map");
			bidrP.load(new FileReader("src/com/briup/client/bidrMap.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(bidrfw!=null){
					bidrfw.close();
				}
				if(oofw!=null){
					oofw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @date 2016-10-09 22:10:44 从文件中获取上次未匹配的
	 * @param ooMap 统计上下线的Map
	 * @param bidrMap 统计上线未匹配的Map
	 */
	private void getFail(Map<String, Integer> ooMap,Map<String, String[]> bidrMap){
		
		//判断是否文件为空
		if(new File("src/com/briup/client/ooMap.properties").length()==0){
			System.out.println("文件是空的，退出读取上次未匹配成功的");
			return;
		}
		Properties ooP =new Properties();//获取统计上下线的Map的Properties
		Properties bidrP =new Properties();//获取统计上线未匹配的Map的Properties
		try {
			ooP.load(new FileReader("src/com/briup/client/ooMap.properties"));//获取统计上下线的Map的Properties
			bidrP.load(new FileReader("src/com/briup/client/bidrMap.properties"));//获取统计上线未匹配的Map的Properties
			for(Object key:ooP.keySet()){//遍历properties文件，放入Map中
				ooMap.put((String) key, Integer.parseInt(ooP.getProperty((String) key)));
			}
			for(Object key:bidrP.keySet()){
				String[] strInfo = bidrP.getProperty((String) key).split(",");//获取值
				bidrMap.put((String) key, strInfo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @date 2016-10-10 15:21:41 测试Object流
	 */
	@Test
	public void testObject() {
		try {
			Map<String, Map> allMap = new HashMap<String, Map>();
			Map<String, String> map1 = new HashMap<String, String>();
			Map<String, Integer> map2 = new HashMap<String, Integer>();
			map1.put("name", "alan");
			map2.put("num", 1000);
			allMap.put("map1", map1);
			allMap.put("map2", map2);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/com/briup/client/FailMap"));
			oos.writeObject(allMap);

			allMap.remove("map1");
			allMap.remove("map1");
			map1.remove("name");
			map2.remove("num");

			// 读取
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/com/briup/client/FailMap"));
			allMap = (Map<String, Map>) ois.readObject();
			map1 = allMap.get("map1");
			map2 = allMap.get("map2");
			System.out.println(map1.get("name"));
			System.out.println(map2.get("num"));
			oos.flush();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @date 2016-10-10 15:22:01 测试Properties
	 */
	@Test
	public void testProperties() {
		Properties p = new Properties();
		try {
			FileWriter fw = new FileWriter("src/com/briup/client/ooMap.properties");
			/*
			 * for(Object key:p.keySet()){ // p.getProperty((String) key);
			 * System.out.println(p.getProperty((String) key)); }
			 */
			Integer i = 1000;
			p.setProperty("num", i.toString());
			p.store(fw, "nihao");
			/*
			 * bidrP.load(new
			 * FileReader("src/com/briup/client/bidrMap.properties"));
			 * for(Object key:bidrP.keySet()){
			 * System.out.println(bidrP.getProperty((String) key)); }
			 */
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	public void printBidrList() {
		for (BIDR bidr : bidrList) {
			printBidr(bidr);
		}
	}

	/*
	 * public static void main(String[] args) { try { GatherImpl4 g2 = new
	 * GatherImpl4(); Long t1 = System.currentTimeMillis(); // 测试第一轮解析
	 * g2.gather(); // g2.printBidrList(); Long t2 = System.currentTimeMillis();
	 * System.out.println("本次匹配成功有：" + count + "对数据");
	 * System.out.println("本次未匹配成功有：" + countFail + "对数据");
	 * System.out.println("时间" + (t2 - t1)); count = 0; countFail = 0;
	 * Thread.sleep(10000); //测试第二轮解析 g2.gather(); // g2.printBidrList();
	 * System.out.println("本次匹配成功有："+count+"对数据");
	 * System.out.println("本次未匹配成功有："+countFail+"对数据"); count=0; countFail=0;
	 * Thread.sleep(10000); //测试第三轮解析 g2.gather(); // g2.printBidrList();
	 * System.out.println("本次匹配成功有："+count+"对数据");
	 * System.out.println("本次未匹配成功有："+countFail+"对数据"); count=0; countFail=0; }
	 * catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */
	@Test
	public void testDay() {
		Date date = new Date(System.currentTimeMillis());
		java.util.Date date2 = new java.util.Date();
		System.out.println(date.toString().split("-")[2]);
		System.out.println(date2);
	}

	public static void main(String[] args) {
		try {
			GatherImpl3 g2 = new GatherImpl3();
			Long t1 = System.currentTimeMillis();
			//测试第一轮解析
			g2.gather();
//			g2.printBidrList();
			Long t2 = System.currentTimeMillis();
			System.out.println("本次匹配成功有："+count+"对数据");
			System.out.println("本次未匹配成功有："+countFail+"对数据");
			System.out.println("时间"+(t2-t1));
			count=0;
			countFail=0;
			/*Thread.sleep(10000);
			
			//测试第二轮解析
			g2.gather();
//			g2.printBidrList();
			System.out.println("本次匹配成功有："+count+"对数据");
			System.out.println("本次未匹配成功有："+countFail+"对数据");
			count=0;
			countFail=0;
			Thread.sleep(10000);
			
			//测试第三轮解析
			g2.gather();
//			g2.printBidrList();
			System.out.println("本次匹配成功有："+count+"对数据");
			System.out.println("本次未匹配成功有："+countFail+"对数据");
			count=0;
			countFail=0;*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
