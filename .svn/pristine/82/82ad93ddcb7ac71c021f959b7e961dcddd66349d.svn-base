package com.briup.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;

/**
 * 添加连接池
 * @author alan
 * @date Oct 11, 2016 9:58:58 AM
 */
public class DBStoreImpl2 implements com.briup.woss.server.DBStore {
	private Connection conn = null;
	public DBStoreImpl2(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void init(Properties arg0) {
		
	}

	@Override
	public void saveToDB(Collection<BIDR> arg0) throws Exception {
		//判断当前日期
		Date date = new Date(System.currentTimeMillis());
		System.out.println("现在是"+date.toString().split("-")[2]+"号");
		String dateStr = date.toString().split("-")[2];
		String sql = "insert into t_detail_"+dateStr+" values(?,?,?,?,?,?)";
		PreparedStatement pre = conn.prepareStatement(sql);
		for(BIDR bidr:arg0){
			pre.setString(1, bidr.getAAA_login_name());
			pre.setString(2, bidr.getLogin_ip());
			pre.setTimestamp(3, bidr.getLogin_date());
			pre.setTimestamp(4, bidr.getLogout_date());
			pre.setString(5, bidr.getNAS_ip());
			pre.setInt(6, bidr.getTime_deration());
			pre.addBatch();
		}
		int[] count = pre.executeBatch();
		conn.commit();
	}
	

}
