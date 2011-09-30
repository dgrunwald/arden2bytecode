package arden.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.junit.Assert;
import org.junit.internal.ArrayComparisonFailure;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.jdbc.DriverHelper;
import arden.runtime.jdbc.JDBCQuery;

public class JDBCQueryTests {
	public Driver loadSQLite() throws ClassNotFoundException, MalformedURLException, InstantiationException, IllegalAccessException, SQLException {
		URL urlA = 
			new File(
					"C:/Dokumente und Einstellungen/Flickar/Eigene Dateien/sqlite-jdbc-3.7.2.jar"
					).toURI().toURL();
		URL[] urls = { urlA };
		URLClassLoader ulc = new URLClassLoader(urls);
		Driver driver = (Driver)Class.forName("org.sqlite.JDBC", true, ulc).newInstance();
		DriverManager.registerDriver(new DriverHelper(driver));
		return driver;
	}
	
	public Statement initDb() {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:");
		    statement = connection.createStatement();
		
		    statement.executeUpdate("drop table if exists person");
		    statement.executeUpdate("create table person (id integer, name string)");
		    statement.executeUpdate("insert into person values(1, 'leo')");
		    statement.executeUpdate("insert into person values(2, 'yui')");		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statement;
	}
	
	@Test
	public void ObjectToArdenValue() throws Exception {
		Assert.assertEquals(new ArdenNumber(1.0), 
				JDBCQuery.objectToArdenValue(new Integer(1)));
		Assert.assertEquals(new ArdenNumber(1.0), 
				JDBCQuery.objectToArdenValue(new Double(1.0)));
		Assert.assertEquals(new ArdenString("hey"),
				JDBCQuery.objectToArdenValue(new String("hey")));
	}
	
	private static void assertArrayNotEquals(Object[] expecteds, Object[] actuals) throws AssertionError {
		boolean exceptionThrown = true;
		try {
			Assert.assertArrayEquals(expecteds, actuals);
			exceptionThrown = false;
		} catch (ArrayComparisonFailure f) {
			
		}
		if (!exceptionThrown) {
			throw new AssertionError("Array is same.");
		}
	}
	
	@Test
	public void Execute() throws Exception {
		loadSQLite();
		Statement stmt = initDb();
		ResultSet results = stmt.executeQuery("select * from person");
		ArdenValue[] ardenVals = JDBCQuery.resultSetToArdenValues(results);
		
		ArdenValue[] valsA = {new ArdenNumber(1), new ArdenNumber(2)};
		ArdenValue[] valsB = {new ArdenString("leo"), new ArdenString("yui")};
		ArdenList[] arrA = {new ArdenList(valsA), new ArdenList(valsB)};
		Assert.assertArrayEquals(arrA, ardenVals);
		
		ArdenValue[] valsC = {new ArdenString("leo"), new ArdenString("X")};
		ArdenList[] arrB = {new ArdenList(valsA), new ArdenList(valsC)};
		assertArrayNotEquals(arrB, ardenVals);		
		
		ArdenValue[] valsD = {new ArdenString("1"), new ArdenNumber(2)};
		ArdenList[] arrC = {new ArdenList(valsD), new ArdenList(valsB)};
		assertArrayNotEquals(arrC, ardenVals);	
	}
}
