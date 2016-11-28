package com.briup.util;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.briup.woss.ConfigurationAWare;
import com.briup.woss.WossModule;

public class LoggerImpl implements com.briup.util.Logger,ConfigurationAWare {
	private org.apache.log4j.Logger logger = 
			org.apache.log4j.Logger.getRootLogger();
	private String logPath;
	private Configuration config;
	@Override
	public void init(Properties arg0) {
		// TODO Auto-generated method stub
		this.logPath = arg0.getProperty("logPath");
		PropertyConfigurator.configure(logPath);
	}

	@Override
	public void debug(String arg0) {
		// TODO Auto-generated method stub
		getLogger().debug(arg0);
	}

	@Override
	public void error(String arg0) {
		// TODO Auto-generated method stub
		getLogger().error(arg0);
	}

	@Override
	public void fatal(String arg0) {
		// TODO Auto-generated method stub
		getLogger().fatal(arg0);
	}

	@Override
	public void info(String arg0) {
		// TODO Auto-generated method stub
		getLogger().info(arg0);
	}

	@Override
	public void warn(String arg0) {
		// TODO Auto-generated method stub
		getLogger().warn(arg0);
	}

	public org.apache.log4j.Logger getLogger() {
		return logger;
	}

	public void setLogger(org.apache.log4j.Logger logger) {
		this.logger = logger;
	}

	@Override
	public void setConfiguration(Configuration arg0) {
		// TODO Auto-generated method stub
		this.config = arg0;
	}

}
