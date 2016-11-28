package com.briup.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.briup.server.ConnPool;
import com.briup.server.SThread3;
import com.briup.woss.ConfigurationAWare;
import com.briup.woss.WossModule;
import com.briup.woss.client.Client;
import com.briup.woss.client.Gather;
import com.briup.woss.server.DBStore;
import com.briup.woss.server.Server;

public class ConfigurationImpl implements com.briup.util.Configuration {
	private Map<String, WossModule> wossMap ;

	public ConfigurationImpl() {
		// 获取wossMap
		wossMap = dom4j();

	}

	private Map<String, WossModule> dom4j() {
		// 新建sax解析器
		SAXReader sax = new SAXReader();
		// 读取文件
		File file = new File("src/com/briup/file/conf.xml");
		//存放对象的map
		Map<String, WossModule> wossMap = new HashMap<String, WossModule>();
		try {
			if (!file.exists() || !file.canRead()) {// 如果文件不存在且不能读取
				throw new Exception("file not exists or file can't read");
			}
			// 把文件转化为树状结构
			Document doc = sax.read(file);
			// 获取根节点
			Element root = doc.getRootElement();
			// 定义Properties，属性存入
			Properties pro = new Properties();
			// 遍历二级节点
			for (Object o1 : root.elements()) {
				// 获取二级节点
				Element e1 = (Element) o1;
				// 获取节点的名字
				String className = e1.getName();
				// 获取节点的class
				String classPath = e1.attributeValue("class");
				// 创建WossModule
				WossModule wm = (WossModule) Class.forName(classPath).newInstance();
				// 解析属性
				for (Object o2 : e1.elements()) {
					// 获取第一个三级节点
					Element e2 = (Element) o2;
					// 获取属性的名字
					String attName = e2.getName();
					// 获取属性的值
					String attValue = e2.getText();
					// 存入properties
					pro.put(attName, attValue);
				}
				// 初始化：把properties传入
				//判断是否实现了Aware接口
				if(wm instanceof ConfigurationAWare){
					((ConfigurationAWare) wm).setConfiguration(this);
				}
				wm.init(pro);
				// 放入Map中
				wossMap.put(className, wm);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wossMap;
	}

	@Override
	public BackUP getBackup() throws Exception {
		// TODO Auto-generated method stub
		return (BackUP) wossMap.get("backup");
	}

	@Override
	public Client getClient() throws Exception {
		// TODO Auto-generated method stub
		return (Client) wossMap.get("client");
	}

	@Override
	public DBStore getDBStore() throws Exception {
		// TODO Auto-generated method stub
		return (DBStore) wossMap.get("dbstore");
	}

	@Override
	public Gather getGather() throws Exception {
		// TODO Auto-generated method stub
		return (Gather) wossMap.get("gather");
	}

	@Override
	public Logger getLogger() throws Exception {
		// TODO Auto-generated method stub
		return (Logger) wossMap.get("logger");
	}

	@Override
	public Server getServer() throws Exception {
		// TODO Auto-generated method stub
		return (Server) wossMap.get("server");
	}
	
	public ConnPool getConnPool() throws Exception{
		return (ConnPool) wossMap.get("connpool");
	}
	
}
