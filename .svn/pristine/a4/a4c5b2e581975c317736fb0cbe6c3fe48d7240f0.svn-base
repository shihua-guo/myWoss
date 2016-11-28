package com.briup.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;

/**
 * 实现基本的连接，存储
 * @author alan
 * @date Oct 10, 2016 6:32:00 PM
 */
public class DBStoreImpl implements com.briup.woss.server.DBStore {

	@Override
	public void init(Properties arg0) {
		
	}

	@Override
	public void saveToDB(Collection<BIDR> arg0) throws Exception {
		//注册驱动
		Class.forName("oracle.jdbc.driver.OracleDriver");
		//获取连接
		Connection conn = 
				DriverManager.getConnection(
						"jdbc:oracle:thin:@localhost:1521:xe", "woss", "root");
		String sql = "insert into t_detail_1 values(?,?,?,?,?,?)";
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
