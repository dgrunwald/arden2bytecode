package arden.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfiguration {
	private static ApplicationConfiguration INSTANCE = null;
	
	public static ApplicationConfiguration getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ApplicationConfiguration();
		}
		return INSTANCE;
	}
	
	private Properties config = null;
	private File configFile = null;
	private final static String comment = "Arden2ByteCode config file";
	
	private ApplicationConfiguration() {
		config = new Properties();
		File configDir = new File(System.getProperty("user.home") 
				+ File.separator + ".arden2bytecode");
		configFile = new File(System.getProperty("user.home") 
				+ File.separator + ".arden2bytecode"
				+ File.separator + "config");				
		
		InputStream configResource = this.getClass().getClassLoader().getResourceAsStream(
						"arden2bytecode.config");		
		
		// copy config resource to config file
		if (configFile.exists() == false && configResource != null) {
			Properties configResProperties = new Properties();			
			try {
				configDir.mkdirs();
				configResProperties.load(configResource);
				configResProperties.store(new FileWriter(configFile), 
								comment);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// reset input stream
		configResource = this.getClass().getClassLoader().getResourceAsStream(
				"arden2bytecode.config");
		// try to load config priorizing a config file before a resource
		boolean configLoaded = false;
		if (configFile.exists()) {
			try {
				config.load(new FileReader(configFile));
				configLoaded = true;
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		if (!configLoaded && configResource != null) {
			try {
				config.load(configResource);
			} catch (IOException e) {
				System.err.println("error: Could not load configuration from resource.");
			}
		}
	}
	
	public void finalize() {
		try {
			config.store(new FileWriter(configFile), comment);
		} catch (IOException e) {
		}
	}	
	
	public static String get(String key) {
		return getInstance().getValue(key);
	}
	
	public static void set(String key, String value) {
		getInstance().setValue(key, value);
	}
	
	public String getValue(String key) {
		return config.getProperty(key);
	}
	
	public void setValue(String key, String value) {
		config.setProperty(key, value);
	}
}
