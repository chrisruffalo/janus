package com.janus.server.configuration;

import java.io.File;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;

/**
 * 
 * @author cruffalo
 *
 */
@ApplicationScoped
public class ConfigurationFactory {

	/**
	 * Default Configuration File name.
	 */
	protected static final String DEFAULT_CONFIG_FILE = "default-janus.xml";
	
	protected static final String CONFIG_FILE_NAME = "janus.xml";

	@Inject
	private Logger logger;

	/**
	 * For overriding other config file locations
	 */
	@Inject
	@SystemProperty("janus.config.file.path")
	private String configFile;
	
	@Inject
	@SystemProperty("jboss.server.config.dir")
	private String jbossServerConfigDir;
	
	@Inject
	@SystemProperty("jboss.server.data.dir")
	private String jbossServerDataDir;
	
	/**
	 * Current XML Configuration Instance.
	 */
	private XMLConfiguration instance;

	/**
	 * Builds the configuration file
	 * 
	 * @throws ConfigurationException if the file cannot be loaded
	 */
	@PostConstruct
	private void createConfiguration()  throws ConfigurationException  {
		
		// try with default file path
		String filePath = this.configFile;		
		
		// if the file path is empty look in alternate locations
		if (filePath == null || filePath.isEmpty()) {
			filePath = this.getAlternateFilePath();
		}
		
		// if the file path is still empty, use the default configuration
		if(filePath == null || filePath.isEmpty()) {
			filePath = ConfigurationFactory.DEFAULT_CONFIG_FILE;
			this.logger.warn("No alternate configuration available. Using default configuration from: {}", filePath);
		} else {
			this.logger.info("Using configuration at: {}", filePath);
		}

		XMLConfiguration xmlConfig = new XMLConfiguration(filePath);
		xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
		
		Iterator<String> iterator = xmlConfig.getKeys();
		StringBuilder keyList = new StringBuilder();
		while(iterator.hasNext()){
			keyList.append(iterator.next()).append(", ");
		}
		this.logger.debug("Creating Configuration with Keys: " + keyList);
		
		this.instance = xmlConfig;
	}
	
	/**
	 * Helper method used to lookup alternate possibilities for the configuration file
	 * 
	 * @return returns path to file when found, returns null when nothing is found
	 */
	private String getAlternateFilePath() {
		// first try config directory
		String configFileName = this.jbossServerConfigDir + "/" + ConfigurationFactory.CONFIG_FILE_NAME;
		
		// lookup file handle
		File configDirConfigFile = new File(configFileName);
		
		// if the file exists, return
		if(configDirConfigFile.exists() && configDirConfigFile.isFile()) {
			return configFileName;
		}
		
		// log config file not found
		this.logger.warn("No configuration file found at '{}'", configFileName);
		
		String dataFileName = this.jbossServerDataDir + "/" + ConfigurationFactory.CONFIG_FILE_NAME;
		
		// lookup config file hande for data dir
		File dataDirConfigFile = new File(dataFileName);
		
		// the file exists, return the name
		if(dataDirConfigFile.exists() && dataDirConfigFile.isFile()) {
			return dataFileName;
		}
		
		// log config file not found
		this.logger.warn("No configuration file found at '{}'", dataFileName);
		
		return null;
	}
	
	/**
	 * Returns a configuration object
	 * 
	 * @return loaded xml configuration properties
	 * 
	 * @throws ConfigurationException
	 *             if the file cannot be loaded
	 */
	@Produces
	public XMLConfiguration getConfiguration() throws ConfigurationException{
		if(this.instance == null) {
			this.createConfiguration();
		}
		
		return this.instance;
	}

	/**
	 * Sets the configFile.
	 * 
	 * @param configFile the configFile to set
	 */
	protected void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
}
