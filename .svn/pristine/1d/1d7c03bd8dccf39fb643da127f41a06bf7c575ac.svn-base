package com.briup.client;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.briup.util.BIDR;

public class GatherImpl implements com.briup.woss.client.Gather {
	//统计多少个BIDR
	private static int count = 0;

	@Override
	public void init(Properties arg0) {

	}

	/**
	 * @date 2016-10-09 15:10:45 采集数据：配对上下线.
	 */
	@Override
	public Collection<BIDR> gather() throws Exception {
		Collection<BIDR> bidrList = new ArrayList<BIDR>();

		/*
		 * 读取文件部分以及解析每行数据
		 */
		// 读取文件
		File file = new File("src/com/briup/file/radwtmp");
		// 包装成ra，模式：rw，方便读取，记录读取的位置
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		// 读取文件的每一行，判断是否结束
		String str1 = null;
		// 创建存放上下线的map
		Map<String,Integer> ooMap = new HashMap<String,Integer>();
		//创建存放BIDR类的map
		Map<String,String[]> bidrMap = new HashMap<String,String[]>();
		
		while ((str1 = raf.readLine()) != null) {
			// 解析每一行数据,去除开头的#,得到一个String,并分割得到String数组
			String str2 = str1.split("#")[1];
			String[] strInfo = str2.split("\\|");//这里需要注意
			
			/*
			 * 匹配上下线：使用2个map：1.存放BIDR 2.存放统计上下线
			 */
			String key = strInfo[4];//以IP作为key，用作之后的匹配
			if(ooMap.containsKey(key)){//如果当前有匹配上。
				int ooTmp = ooMap.get(key);//获取当前count
				if(strInfo[2].equals("7")){//如果是上线
					ooTmp++;//oo++
					ooMap.put(key, ooTmp);
				}
				else{//如果是下线
					ooTmp--;//oo--
					ooMap.put(key, ooTmp);
				}
				
				/*
				 * 如果count等于0，就创建BIDR对象
				 */
				if(ooMap.get(key)==0){
					String[] loginInfo = bidrMap.get(key);//获取登陆的所有信息
					String login_name = loginInfo[0];//获取用户名
					String login_str = loginInfo[3];//获取登陆的string
					String logout_str = strInfo[3];//获取登出的string
					String login_ip = loginInfo[4];//获取登陆IP
					Timestamp login_date = 
							new Timestamp(Long.parseLong(login_str));//获取登陆时间
					Timestamp logout_date = 
							new Timestamp(Long.parseLong(logout_str));//获取登出时间
					String nas_ip = 
							InetAddress.getLocalHost().getHostAddress();//获取NAS(本机地址)
					Integer time_deration = 
							Integer.parseInt(logout_str) - Integer.parseInt(login_str);//获取在线时间
					
					BIDR bidr = new BIDR(login_name, login_ip, login_date, logout_date, nas_ip, time_deration);//存放如所有信息
					
					ooMap.remove(key);//移除成功匹配的ooMap
					bidrMap.remove(key);//移除成功匹配的bidrMap
					
					printBidr(bidr);//打印bidr
					bidrList.add(bidr);//把统计好的bidr放入List中
				}
				
			}else{//如果没有匹配上：当前map中没有统计该用户的上下线信息
				ooMap.put(key, 1);//放入统计上下线次数的map
				bidrMap.put(key, strInfo);//存放第一次上线时间
			}
			
		
		}

		raf.close();
		return null;
	}

	/**
	 * @date 2016-10-09 18:31:43 
	 * @param 
	 * @throws Exception
	 */
	
	@Test
	public void test() throws Exception{
		Collection<BIDR> col = new ArrayList<BIDR>();

		/*
		 * 读取文件部分
		 */
		// 读取文件
		File file = new File("src/com/briup/file/radwtmp2");
		// 包装成ra，模式：rw，方便读取，记录读取的位置
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		// 读取文件的每一行，判断是否结束
		String str = null;
		while ((str = raf.readLine()) != null) {
			// 解析每一行数据,去除开头的#
//			String str2 = str.split("#")[1];
//			System.out.println(str2);
			String[] strInfo = str.split("#")[1].split("\\|");
			for(String strTmp:strInfo){
				System.out.println(strTmp);
			}
			System.out.println(strInfo);
		}
		
		raf.close();
	}
	
	/**
	 * @date 2016-10-09 17:48:24 测试打印bidr对象
	 * @param b
	 */
	public void printBidr(BIDR b){
		System.out.println("loginName:"+b.getAAA_login_name());
		System.out.println("loginIp:"+b.getLogin_ip());
		System.out.println("login_date:"+b.getLogin_date());
		System.out.println("logout_date:"+b.getLogout_date());
		System.out.println("NAS_ip:"+b.getNAS_ip());
		System.out.println("time_deration:"+b.getTime_deration());
		count++;
	}
	
	
	public static void main(String[] args) {
		try {
			new GatherImpl().gather();
			System.out.println(count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
