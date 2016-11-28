package com.briup.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;
import com.briup.util.Configuration;
import com.briup.util.Logger;
import com.briup.util.LoggerImpl;
import com.briup.woss.ConfigurationAWare;
import com.briup.woss.WossModule;
import com.briup.woss.server.DBStore;

/**
 * 多线程，从线程池获取连接负责入库
 * 
 * @author alan
 * @date Oct 11, 2016 10:06:39 AM
 */
public class SThread4 implements Runnable {
	//传入的socket
	private Socket client = null;
	//日志文件
	private Logger logger;
	//传入的DBStore
	private DBStore dbStore;
	public SThread4(Socket client,DBStore dbStore) {
		this.client = client;
		this.dbStore = dbStore;
	}

	@Override
	public void run() {
		//设置日志
		setLogger();
		// 获取线程类
		logger.info("新建线程");
		ObjectInputStream ois = null;
		try {
			//创建bidrList
			Collection<BIDR> bidrList = new ArrayList<BIDR>();
			ois = new ObjectInputStream(client.getInputStream());
			// 读取客户端传来的列表
			bidrList = (Collection<BIDR>) ois.readObject();
			logger.info("Client发送：" + bidrList.size()+"条数据");
			// 入库
			dbStore.saveToDB(bidrList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				if (ois != null)
					ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setLogger(){
		try {
			logger = new com.briup.util.LoggerImpl();
			((LoggerImpl)logger).setLogger(org.apache.log4j.Logger.getLogger("Thread"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
