package com.briup.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;
import com.briup.util.Configuration;
import com.briup.util.ConfigurationImpl;
import com.briup.util.Logger;
import com.briup.util.LoggerImpl;
import com.briup.woss.ConfigurationAWare;
import com.briup.woss.server.DBStore;

/**
 * 优化代码：线程无须由配置产生
 * 
 * @author alan
 * @date Oct 16, 2016 6:34:08 PM
 */
public class ServerImpl5 implements com.briup.woss.server.Server, ConfigurationAWare {
	// 端口号
	private int port;
	// configuration
	private Configuration config;
	// 控制停止
	private boolean flag = true;
	// 日志文件
	private Logger logger;
	// 日志的名字
	private String logName;

	@Override
	public void init(Properties arg0) {
		// TODO Auto-generated method stub
		this.port = Integer.parseInt(arg0.getProperty("port"));
		this.logName = arg0.getProperty("logName");

	}

	/**
	 * @return
	 * @date 2016-10-10 18:59:38 收集由Client端传入的BIDR集合
	 */
	@Override
	public Collection<BIDR> revicer() throws Exception {
		// 设置日志
		setLogger();
		// socket：接收客户端的
		logger.info("创建socket");
		ServerSocket server = new ServerSocket(port);
		// 连接池，初始化长度为5
		logger.info("创建连接池");
		ConnPool pool = ((ConfigurationImpl) config).getConnPool();

		while (flag) {
			// 获取连接
			Connection conn = pool.getConnection();
			// 把连接传入给DB store!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			logger.info("把连接传给DBStore");
			((DBStoreImpl4) config.getDBStore()).setConn(conn);
			Socket client = server.accept();
			Thread th = new Thread(new SThread4(client,config.getDBStore()));
			// 启动线程
			logger.info("启动线程");
			th.start();
			// 释放连接
			pool.releaseConnection(conn);
			// 设置日志
			setLogger();
		}
		return null;
	}


	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConfiguration(Configuration arg0) {
		// TODO Auto-generated method stub
		this.config = arg0;
	}

	private void setLogger() {
		try {
			logger = config.getLogger();
			((LoggerImpl) logger).setLogger(org.apache.log4j.Logger.getLogger(logName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		try {
			new ConfigurationImpl().getServer().revicer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
