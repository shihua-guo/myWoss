package com.briup.util;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.briup.woss.WossModule;

public class WossDom4j {
	private Map<String, WossModule> wossMap = new HashMap<String, WossModule>();

	public WossDom4j() {
	}

	public WossDom4j(String path) {
		// 新建sax解析器
		SAXReader sax = new SAXReader();
		// 读取文件
		File file = new File(path);
		try {
			if (!file.exists() || !file.canRead()) {// 如果文件不存在且不能读取
				throw new Exception("file not exists or file can't read");
			}
			// 把文件转化为树状结构
			Document doc = sax.read(file);
			// 获取根节点
			Element root = doc.getRootElement();
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
				// 定义Properties，属性存入
				Properties pro = new Properties();
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
					/*// 属性解析完毕，存入文件
					pro.store(new FileWriter("src/com/briup/file/" + className + ".properties"),
							className + "'s attribute");*/
				}
				//初始化：把properties传入
				wm.init(pro);
				//放入Map中
				wossMap.put(className, wm);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		WossDom4j wd = new WossDom4j("src/com/briup/file/conf.xml");
	}

	public Map<String, WossModule> getWossMap() {
		return wossMap;
	}

}
