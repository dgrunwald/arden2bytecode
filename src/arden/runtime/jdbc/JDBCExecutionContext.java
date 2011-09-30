package arden.runtime.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import arden.CommandLineOptions;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.StdIOExecutionContext;

public class JDBCExecutionContext extends StdIOExecutionContext {
	private Connection connection = null;	
	
	public void loadDatabaseDriver(String className) {
		try {
			Driver driver = (Driver)Class.forName(className, 
					true, 
					Thread.currentThread().getContextClassLoader())
						.newInstance();
			DriverManager.registerDriver(new DriverHelper(driver));
		} catch (InstantiationException e) {			
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public JDBCExecutionContext(CommandLineOptions options) {
		super(options);
		
		if (options.isDbdriver()) {
			loadDatabaseDriver(options.getDbdriver());
		}
		
		// handle environment option
		if (options.isEnvironment() && options.getEnvironment() != null) {
			String environment = options.getEnvironment();			
			try {
				connection = DriverManager.getConnection(environment);
			} catch (SQLException e) {
				e.printStackTrace();				
			}
		} else {
			throw new RuntimeException("No JDBC URL given. Can't connect.");
		}
	}
	
	public DatabaseQuery createQuery(String mapping) {		
		return new JDBCQuery(mapping, connection);
	}
	
	public ArdenRunnable findModule(String name, String institution) {
		throw new RuntimeException("findModule not implemented");
	}
	
	public ArdenRunnable findInterface(String mapping) {
		throw new RuntimeException("findInterface not implemented");
	}
	
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
		throw new RuntimeException("callWithDelay not implemented");
	}
	
	private ArdenTime eventtime = new ArdenTime(new Date());
	
	public ArdenTime getEventTime() {
		return eventtime;
	}

	public ArdenTime getTriggerTime() {
		return eventtime;
	}

	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}
