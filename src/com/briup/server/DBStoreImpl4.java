package com.briup.server;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.briup.util.BIDR;
import com.briup.util.BackUP;
import com.briup.util.BackUpImpl;
import com.briup.util.Configuration;
import com.briup.util.Logger;
import com.briup.util.LoggerImpl;
import com.briup.woss.ConfigurationAWare;

/**
 * 用迭代器遍历失败（不支持remove），修改成普通for循环遍历
 * 
 * @author alan
 * @date Oct 11, 2016 6:42:38 PM
 */
public class DBStoreImpl4 implements com.briup.woss.server.DBStore, ConfigurationAWare {
	// 连接
	private Connection conn = null;
	// 备份的文件
	private String backUpFile;
	// 日志文件
	private Logger logger;
	// 日志的名字
	private String logName;
	//传入异常
	private String errorIndex;
	// config
	private Configuration config;

	public DBStoreImpl4() {
	}

	public DBStoreImpl4(Connection conn) {
		this.setConn(conn);
	}

	@Override
	public void init(Properties arg0) {
		this.backUpFile = arg0.getProperty("backUpFile");
		this.logName = arg0.getProperty("logName");
		this.errorIndex = arg0.getProperty("errorIndex");
		System.out.println(errorIndex);
	}

	@Override
	public void saveToDB(Collection<BIDR> arg0) {
		// 设置日志
		setLogger();
		logger.info("入库开始");
		BackUP backUp = null;
		try {
			backUp = config.getBackup();
			Collection<BIDR> fail = (Collection<BIDR>) backUp.load(backUpFile, true);
			if (fail != null) {// 如果fail不为空（之前有存入入库失败的）
				arg0.addAll(fail);
			}
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// 判断当前日期
		logger.info("判断当前日期");
		Date date = new Date(System.currentTimeMillis());
		String dateStr = date.toString().split("-")[2];
		//设置SQL语句
		logger.info("设置SQL语句");
		String sql = "insert into t_detail_" + dateStr + " values(?,?,?,?,?,?)";
		// count：统计成功的个数
		int count = 0;
		try {
			// 预编译SQL语句
			PreparedStatement pre = getConn().prepareStatement(sql);
			// 临时存放成功的数据，以便后面进行删除
			Collection<BIDR> success = new ArrayList<BIDR>();
			// 转化为List：方便后面使用下标
			List<BIDR> bidrList = (ArrayList<BIDR>) arg0;
			// new 一个BIDR对象，遍历
			BIDR bidr = new BIDR();
			// 导致异常的数
			int error = 0;
			System.out.println(errorIndex);
			// 获取异常的数
			logger.info("获取异常的数(如果需要发生异常)");
			Properties errorPro = new Properties();
			errorPro.load(new FileInputStream(errorIndex));
			int errorLocal = Integer.parseInt(errorPro.getProperty("error"));
			
			logger.info("线程传入的数据："+arg0.size()+"条");
			for (int i = 0; i < bidrList.size(); i++) {
				bidr = bidrList.get(i);
				pre.setString(1, bidr.getAAA_login_name());
				pre.setString(2, bidr.getLogin_ip());
				pre.setTimestamp(3, bidr.getLogin_date());
				pre.setTimestamp(4, bidr.getLogout_date());
				pre.setString(5, bidr.getNAS_ip());
				pre.setInt(6, bidr.getTime_deration());
				pre.addBatch();
				// 把成功的添加
				success.add(bidr);
				// 记录读取的次数
				error++;
				if (error == errorLocal) {// 异常
					logger.info("强制发生异常");
					i = i / 0;
				}
				if (i!=0 && i % 999 == 0 ) { // 每1000条就执行
					pre.executeBatch();// 执行SQL语句，默认自动提交
					logger.info("成功入库1000条");
					bidrList.removeAll(success);// 把成功的移除
					success.clear();// 把记录成功的集合清除
					count += i;// 把成功的个数传给count
					i = -1;// 移除成功的之后把下标置0
				}
			}
			// 把剩余的执行完毕
			pre.executeBatch();
			logger.info("存入成功:" + (count+bidrList.size()) + "条数据");
			// conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println();
			// 处理异常
			try {
				// 备份
				logger.error("发生异常，进行备份");
				logger.error("存入成功:" + (count+1) + "条数据");
				logger.error("失败：" + (arg0.size() - count+1) + "条数据");
				backUp.store(backUpFile, arg0, BackUP.STORE_OVERRIDE);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void setConfiguration(Configuration arg0) {
		// TODO Auto-generated method stub
		this.config = arg0;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
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

}
