package com.briup.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.briup.util.BIDR;
import com.briup.util.BackUP;
import com.briup.util.BackUpImpl;

/**
 * 添加备份模块
 * 
 * @author alan
 * @date Oct 11, 2016 3:33:15 PM
 */
public class DBStoreImpl3 implements com.briup.woss.server.DBStore {
	private Connection conn = null;

	public DBStoreImpl3(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void init(Properties arg0) {

	}

	@Override
	public void saveToDB(Collection<BIDR> arg0) {
		BackUpImpl backUp = new BackUpImpl();
		String path = "src/com/briup/server/FailDB";
		
		try {
			Collection<BIDR> fail = (Collection<BIDR>) backUp.load(path, BackUpImpl.LOAD_REMOVE);
			if(fail!=null){//如果fail不为空（之前有存入入库失败的）
				arg0.addAll(fail);
			}
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// 判断当前日期
		Date date = new Date(System.currentTimeMillis());
		String dateStr = date.toString().split("-")[2];
		String sql = "insert into t_detail_" + dateStr + " values(?,?,?,?,?,?)";
		
		int count = 0;// 存入条数的计数
		try {
			PreparedStatement pre = conn.prepareStatement(sql);
			Collection<BIDR> success = new ArrayList<BIDR>();
			for (BIDR bidr : arg0) {
				pre.setString(1, bidr.getAAA_login_name());
				pre.setString(2, bidr.getLogin_ip());
				pre.setTimestamp(3, bidr.getLogin_date());
				pre.setTimestamp(4, bidr.getLogout_date());
				pre.setString(5, bidr.getNAS_ip());
				pre.setInt(6, bidr.getTime_deration());
				pre.addBatch();
				//把成功的添加
				success.add(bidr);
				//记录读取的次数
				count++;
				
				if (count == 1056) {// 异常
					count = count / 0;
				}
				if (count % 1000 == 0) { // 每1000条就执行
					pre.executeBatch();
					arg0.removeAll(success);//把成功的移除
					success.clear();//把记录成功的集合清除
				}
			}
			// 把剩余的执行完毕
			pre.executeBatch();
			// conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			// 处理异常
			try {
				// 备份
				System.out.println("存入成功:"+(count/1000)*1000+"条数据");
				System.out.println("失败："+arg0.size());
				backUp.store(path, arg0, BackUP.STORE_OVERRIDE);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	
}
