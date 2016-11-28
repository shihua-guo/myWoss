package com.briup.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import com.briup.util.Configuration;
import com.briup.util.Logger;
import com.briup.util.LoggerImpl;
import com.briup.woss.ConfigurationAWare;
import com.briup.woss.WossModule;

/**
 * 改进：1.存入未匹配时引用一个大Map，存入读取文件使用Object~流 2.读取index文件时使用Data~~流 3.把判断为0放入到--之后
 * 
 * @author alan
 * @date Oct 10, 2016 2:12:28 PM
 */
public class GatherImpl4 implements com.briup.woss.client.Gather, ConfigurationAWare {
	// 统计多少个BIDR
	private static int count = 0;
	// 存放
	private Collection<BIDR> bidrList = new ArrayList<BIDR>();
	// private Collection<BIDR> bidrList = new LinkedList<BIDR>();
	// nas-ip
	private String nasIp;
	// 解析的文件
	private String srcFile;
	// 存放失败的文件
	private String failFile;
	// 存放位置的文件
	private String indexFile;
	// configuration
	private Configuration config;
	// 日志
	private Logger logger;
	// 日志的名字
	private String logName;

	@Override
	public void init(Properties arg0) {
		this.nasIp = arg0.getProperty("nas-ip");
		this.srcFile = arg0.getProperty("src-file");
		this.failFile = arg0.getProperty("failFile");
		this.indexFile = arg0.getProperty("indexFile");
		this.logName = arg0.getProperty("logName");
	}

	/**
	 * @date 2016-10-09 15:10:45 采集数据：配对上下线.
	 */
	@Override
	public Collection<BIDR> gather() throws Exception {
		//设置日志
		setLogger();
		// 读取文件
		File file = new File(srcFile);
		logger.info("读取文件解析文件");
		// 包装成ra，模式：rw，方便读取，记录读取的位置
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		// 读取index文件
		raf.seek(getIndex());
		// 解析文件
		parser(raf);
		// 存入当前位置
		putIndex(raf.getFilePointer());
		// 关闭raf
		raf.close();
		return bidrList;
	}

	/**
	 * @date 2016-10-09 19:00:37 封装parser方法。
	 * @param raf
	 *            文件随机流
	 * @throws Exception
	 */
	private void parser(RandomAccessFile raf) throws Exception {
		// 读取文件的每一行，判断是否结束
		String str1 = null;
		// 创建存放上下线的map
		Map<String, Integer> ooMap = new HashMap<String, Integer>();
		// 创建存放BIDR类的map
		Map<String, String[]> bidrMap = new HashMap<String, String[]>();
		// 放入oo，bidrMap的大Map
		Map<String, Map> allMap = null;
		// 获取上次未匹配成功的
		// getFail(ooMap, bidrMap);
		allMap = getNoMatch();
		// 把从文件获取存入
		if (!allMap.isEmpty()) {
			ooMap = allMap.get("ooMap");
			bidrMap = allMap.get("bidrMap");
			logger.info("上次未匹配有" + ooMap.size() + "组数据");
		}

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
					/*
					 * 如果count等于0，就创建BIDR对象
					 */
					if (ooMap.get(key) == 0) {
						BIDR bidr = getBIDR(key, bidrMap, strInfo);// 存放如所有信息
						ooMap.remove(key);// 移除成功匹配的ooMap
						bidrMap.remove(key);// 移除成功匹配的bidrMap
						count++;// 记录有多少个
						bidrList.add(bidr);// 把统计好的bidr放入List中
					}
				}

			} else {// 如果没有匹配上：当前map中没有统计该用户的上下线信息
				// 默认一个用户第一次一定是上线
				ooMap.put(key, 1);// 放入统计上下线次数的map
				bidrMap.put(key, strInfo);// 存放第一次上线时间
			}
		}
		if (!ooMap.isEmpty()) {// 如果存在未匹配成功的
			allMap.put("ooMap", ooMap);
			allMap.put("bidrMap", bidrMap);
			logger.info("本次次未匹配有" + ooMap.size() + "组数据");
			storeNoMatch(allMap);// 最后把未匹配的存入文件
		}

	}

	/**
	 * @date 2016-10-09 18:53:23 获取一个BIDR对象
	 * @param key
	 *            匹配上下线的ip
	 * @param bidrMap
	 *            存放登陆信息的Map
	 * @param strInfo
	 *            存放当前读取到的信息
	 * @return BIDR 返回一个BIDR对象
	 * @throws UnknownHostException
	 */
	private BIDR getBIDR(String key, Map<String, String[]> bidrMap, String[] strInfo) throws UnknownHostException {
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
	 * @date 2016-10-10 14:46:44 使用一个map存储进文件
	 * @param allMap
	 *            存了ooMap和bidrMap的Map
	 */
	private void storeNoMatch(Map<String, Map> allMap) {
		logger.info("把未匹配的存入文件");
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(failFile));
			oos.writeObject(allMap);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @date 2016年10月10日14:49:37 读取未匹配的数据的文件
	 * @param allMap
	 *            存了ooMap和bidrMap的Map
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Map> getNoMatch() {
		logger.info("读取未匹配的数据的文件");
		File file = new File(failFile);
		// 判断是否有该文件
		if (!file.exists()) {
			logger.info("没有未匹配的数据的文件");
			return new HashMap<String, Map>();
		}
		ObjectInputStream ois = null;
		Map<String, Map> allMap = new HashMap<String, Map>();
		try {
			ois = new ObjectInputStream(new FileInputStream("src/com/briup/client/FailMap"));
			allMap = (Map<String, Map>) ois.readObject();
			ois.close();
			file.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allMap;
	}

	/**
	 * @date 2016-10-10 15:29:40 获取文件记录的位置
	 * @return
	 */
	private Long getIndex() {
		logger.info("获取文件记录的位置");
		File file = new File(indexFile);
		if (!file.exists()) {
			return 0L;
		} else {
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(new FileInputStream(file));
				long index = dis.readLong();
				return index;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		logger.info("读取index文件时，未知错误！");
		return null;
	}

	/**
	 * @date 2016-10-10 15:35:37 把index放入文件中
	 * @param index
	 *            当前读取的index
	 */
	private void putIndex(Long index) {
		logger.info("把index放入文件中");
		File file = new File(indexFile);
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(file));
			dos.writeLong(index);
			dos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setConfiguration(Configuration arg0) {
		// TODO Auto-generated method stub
		this.config = arg0;
	}

	private void setLogger(){
		try {
			logger = config.getLogger();
			((LoggerImpl)logger).setLogger(org.apache.log4j.Logger.getLogger(logName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
