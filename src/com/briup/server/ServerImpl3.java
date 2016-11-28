package com.briup.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.briup.util.BIDR;

/**
 * 添加多线程：使用内部类实现。同包的类无法修改List
 * @author alan
 * @date Oct 10, 2016 7:40:01 PM
 */
public class ServerImpl3 implements com.briup.woss.server.Server {

	public static Collection<BIDR> bidrList = new ArrayList<BIDR>();
	@Override
	public void init(Properties arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @date 2016-10-10 18:59:38  收集由Client端传入的BIDR集合
	 */
	@Override
	public Collection<BIDR> revicer() throws Exception {
		ServerSocket server = new ServerSocket(8888);
		while (true) {
			Socket client = server.accept();
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					ObjectInputStream ois;
					try {
						ois = new ObjectInputStream(client.getInputStream());
						bidrList=(Collection<BIDR>)ois.readObject();
						System.out.println("在线程中的HashCode"+ServerImpl3.bidrList.hashCode());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			System.out.println("i am a new Thread");
			th.start();
			
			System.out.println(bidrList.size());
			System.out.println("在方法中的HashCode"+ServerImpl3.getBidrList().hashCode());
			return null;
		}
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
			Collection<BIDR> bidrList = new ServerImpl3().revicer();
			new DBStoreImpl().saveToDB(bidrList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Collection<BIDR> getBidrList() {
		return bidrList;
	}

	public static void setBidrList(Collection<BIDR> bidrList) {
		ServerImpl3.bidrList = bidrList;
	}
}

class SThread2 implements Runnable{
	private Socket client;
	public SThread2(){}
	public SThread2(Socket client){
		this.client = client;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(client.getInputStream());
			ServerImpl3.bidrList=(Collection<BIDR>)ois.readObject();
			System.out.println("在线程中的HashCode"+ServerImpl3.bidrList.hashCode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
