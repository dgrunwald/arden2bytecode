// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.runtime.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import arden.CommandLineOptions;
import arden.runtime.ArdenString;
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
	
	public void write(ArdenValue message, String destination) {
		if ("database".equalsIgnoreCase(destination) || "query".equalsIgnoreCase(destination)) {
			String msgString = ArdenString.getStringFromValue(message);
			
			// execute query:
			new JDBCQuery(msgString, connection).execute();
		} else if ("email".equalsIgnoreCase(destination)) {
			// TODO: implement email sending
		} else {
			super.write(message, destination);
		}
	}
	
	public DatabaseQuery createQuery(String mapping) {		
		return new JDBCQuery(mapping, connection);
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
