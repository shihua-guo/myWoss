package com.briup.client;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;
import com.briup.util.Configuration;
import com.briup.util.ConfigurationImpl;
import com.briup.util.Logger;
import com.briup.util.LoggerImpl;
import com.briup.woss.ConfigurationAWare;

/**
 * 第一次写Client端，实现基本功能：和Server对接
 * @author alan
 * @date Oct 10, 2016 3:57:41 PM
 */
public class ClientImpl implements com.briup.woss.client.Client,ConfigurationAWare{
	//ip
	private String ip;
	//port
	private int port;
	//日志
	private Logger logger;
	//日志的名字
	private String logName;
	//config
	private Configuration config;
	
	@Override
	public void init(Properties arg0) {
		this.ip = arg0.getProperty("ip");
		this.port = Integer.parseInt(arg0.getProperty("port"));
		this.logName = arg0.getProperty("logName");
	}	

	@Override
	public void send(Collection<BIDR> arg0) throws Exception {
		setLogger();
		Socket socket = new Socket(ip, port);
//		Socket socket = new Socket("172.16.3.122", 10086);
		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(arg0);
		logger.debug("成功发送"+arg0.size()+"条数据到Server端");
		oos.flush();
		oos.close();
		os.flush();
		os.close();
		socket.close();
		logger.debug("关闭socket");
	}
	
	public static void main(String[] args){

		Long t1 = System.currentTimeMillis();
		// 测试第一轮解析
		try {
			ClientImpl client = (ClientImpl) new ConfigurationImpl().getClient();
			client.send(client.config.getGather().gather());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// g2.printBidrList();
		Long t2 = System.currentTimeMillis();
		System.out.println("时间" + (t2 - t1));
		
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
