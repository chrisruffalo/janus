package com.janus.server.configuration;

import javax.inject.Inject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * 
 * @author cruffalo
 *
 */
public class ConfigurationProperties {

	public static final String LIBRARY_LOCATION = "library.path";
	public static final String LIBRARY_DATABASE = "library.database";
	
	/**
	 * XML Configuration.
	 */
	@Inject
	private XMLConfiguration xmlConfig;

	/**
	 * Gets the XmlConfiguration.
	 * 
	 * @return the xmlConfig
	 */
	public XMLConfiguration getXmlConfiguration() {
		return this.xmlConfig;
	}

	/**
	 * Sets the specified Property.
	 * 
	 * @param property
	 *            Property to Set
	 * @param value
	 *            Property Value
	 */
	public void setProperty(String property, Object value) {
		this.xmlConfig.setProperty(property, value);
	}

	/**
	 * Gets the specified Property as a String.
	 * 
	 * @param property
	 *            Property Name.
	 * @return
	 */
	public String getStringProperty(String property) {
		return this.xmlConfig.getString(property);
	}

	/**
	 * Gets the specified Property as an Integer.
	 * 
	 * @param property
	 *            Property Name.
	 * @return
	 */
	public Integer getIntegerProperty(String property) {
		return this.xmlConfig.getInt(property);
	}

	/**
	 * Gets the specified Property as a Long.
	 * 
	 * @param property
	 *            Property Name.
	 * @return
	 */
	public Long getLongProperty(String property) {
		return this.xmlConfig.getLong(property);
	}

	/**
	 * Gets the specified Property as a Boolean.
	 * 
	 * @param property
	 *            Property Name.
	 * @return
	 */
	public Boolean getBooleanProperty(String property) {
		return this.xmlConfig.getBoolean(property);
	}

	/**
	 * Saves the current Configuration back to Disk.
	 * 
	 * @throws ConfigurationException
	 *             If the faile can not be saved.
	 */
	public void save() throws ConfigurationException {
		this.xmlConfig.save();
	}

}
