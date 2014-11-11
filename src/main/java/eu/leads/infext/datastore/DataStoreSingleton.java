package eu.leads.infext.datastore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.leads.infext.datastore.impl.CassandraCQLDataStore;

//import root.datastore.impl.HBaseDataStore;
//import root.datastore.impl.HiveDataStore;

public class DataStoreSingleton {
	
	static String parametersFile = "eu/leads/infext/datastore/parameters/parameters.properties";
	static String mappingFile = "eu/leads/infext/datastore/mapping/casscql.properties";
	static String storagePropsFile = "eu/leads/infext/datastore/prop/HiveDataStore.properties";
	
	static AbstractDataStore dataStore;
	static Properties prop = new Properties();
	static Properties mapping = new Properties();
	static Properties parameters = new Properties();
	
	public static AbstractDataStore getDataStore() {
		if(dataStore == null) {
			initProperties();
			initMapping();
			initParameters();
			dataStore = new CassandraCQLDataStore(mapping);
		}
		return dataStore;
	}
	
	public static Properties getParameters() {
		if(dataStore == null) {
			return null;
		}
		return parameters;
	}
	
	public static Properties getMapping() {
		if(dataStore == null) {
			return null;
		}
		return mapping;
	}
	
	private static void initParameters() {
		InputStream input = null;
		 
		try {
			input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(parametersFile);
			// load a properties file
			parameters.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void initMapping() {
		InputStream input = null;
		 
		try {
			input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(mappingFile);
			// load a properties file
			mapping.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void initProperties() {
		InputStream input = null;
	 
		try {
			input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(storagePropsFile);
			// load a properties file
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
}
