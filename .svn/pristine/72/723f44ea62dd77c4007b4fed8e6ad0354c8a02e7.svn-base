package com.briup.server;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;

/**
 * 初次写Server端，实现基本功能：接收Client端数据
 * @author alan
 * @date Oct 10, 2016 4:01:00 PM
 */
public class ServerImpl implements com.briup.woss.server.Server {

	@Override
	public void init(Properties arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<BIDR> revicer() throws Exception {
		ServerSocket server = new ServerSocket(8888);
		Socket client = server.accept();
		ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
		Collection<BIDR> bidrList = (Collection<BIDR>)ois.readObject();
//		printBidrList(bidrList);
		return bidrList;
	}

	/**
	 * @date 2016-10-09 19:08:33 打印当前List
	 */
	private void printBidrList(Collection<BIDR> bidrList) {
		for (BIDR bidr : bidrList) {
			printBidr(bidr);
		}
	}
	/**
	 * @date 2016-10-09 17:48:24 测试打印bidr对象
	 * @param b
	 */
	private void printBidr(BIDR bidr) {
		System.out.println("loginName:" + bidr.getAAA_login_name());
		System.out.println("loginIp:" + bidr.getLogin_ip());
		System.out.println("login_date:" + bidr.getLogin_date());
		System.out.println("logout_date:" + bidr.getLogout_date());
		System.out.println("NAS_ip:" + bidr.getNAS_ip());
		System.out.println("time_deration:" + bidr.getTime_deration());
	}


	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		try {
			Collection<BIDR> bidrList = new ServerImpl().revicer();
			new DBStoreImpl().saveToDB(bidrList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
