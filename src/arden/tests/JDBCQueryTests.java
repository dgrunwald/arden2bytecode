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

import uk.co.flamingpenguin.jewel.cli.CliFactory;

import arden.CommandLineOptions;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.jdbc.DriverHelper;
import arden.runtime.jdbc.JDBCExecutionContext;
import arden.runtime.jdbc.JDBCQuery;

public class JDBCQueryTests {
	private static boolean SQLiteLoaded = false;
	private static final String SQLITE_PATH = "./sqlite-jdbc-3.7.2.jar";
	
	public Driver loadSQLite() throws 
			MalformedURLException, 
			InstantiationException, 
			IllegalAccessException, 
			SQLException {
		if (SQLiteLoaded) {
			return DriverManager.getDriver("jdbc:sqlite:");
		}
		URL urlA = 
			new File(
					SQLITE_PATH
					).toURI().toURL();
		URL[] urls = { urlA };
		URLClassLoader ulc = new URLClassLoader(urls);
		Driver driver;
		try {
			driver = (Driver)Class.forName("org.sqlite.JDBC", true, ulc).newInstance();
		} catch (ClassNotFoundException e) {
			System.err.println("SQLite JDBC driver not found. Skipping associated test in " + this.getClass().getName());
			return null;
		}
		DriverManager.registerDriver(new DriverHelper(driver));
		SQLiteLoaded = true;
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
		    statement.executeUpdate("insert into person values(1, 'A')");
		    statement.executeUpdate("insert into person values(2, 'B')");		    
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
		boolean exceptionThrown = false;
		try {
			Assert.assertArrayEquals(expecteds, actuals);
		} catch (ArrayComparisonFailure f) {
			exceptionThrown = true;
		}
		if (!exceptionThrown) {
			throw new AssertionError("Array is same.");
		}
	}
	
	@Test
	public void ResultSetToArdenValues() throws Exception {
		if (loadSQLite() == null) {
			return;
		}
		Statement stmt = initDb();
		ResultSet results = stmt.executeQuery("select * from person");
		ArdenValue[] ardenValues = JDBCQuery.resultSetToArdenValues(results);
		
		ArdenValue[] expectedA = {new ArdenNumber(1), new ArdenNumber(2)};
		ArdenValue[] expectedB = {new ArdenString("A"), new ArdenString("B")};
		ArdenList[] expectedArrA = {new ArdenList(expectedA), new ArdenList(expectedB)};
		Assert.assertArrayEquals(expectedArrA, ardenValues);
		
		ArdenValue[] expectedC = {new ArdenString("A"), new ArdenString("X")};
		ArdenList[] expectedArrB = {new ArdenList(expectedA), new ArdenList(expectedC)};
		assertArrayNotEquals(expectedArrB, ardenValues);		
		
		ArdenValue[] expectedD = {new ArdenString("1"), new ArdenNumber(2)};
		ArdenList[] expectedArrC = {new ArdenList(expectedD), new ArdenList(expectedB)};
		assertArrayNotEquals(expectedArrC, ardenValues);	
	}
	
	@Test
	public void JDBCExecutionContextRead() throws Exception {
		if (loadSQLite() == null) {
			return;
		}
		String[] args = new String[]{"-e", "jdbc:sqlite:"};
		CommandLineOptions options = 
				CliFactory.parseArguments(CommandLineOptions.class, args);
		
		ExecutionContext testContext = new JDBCExecutionContext(options);
		MedicalLogicModule mlm = ActionTests.parseTemplate(
				"varA := read {drop table if exists person};\n" +
				"varB := read {create table person (id integer, name string)};\n" +
				"varC := read {insert into person values (1, 'A')};\n" +
				"varD := read {insert into person values (2, 'B')};\n" +
				"(varE, varF) := read {select * from person};\n", 
				"conclude true;", 
				"return (varE, varF);");
		ArdenValue[] result = mlm.run(testContext, null);
		Assert.assertEquals(1, result.length);

		ArdenValue[] expected = {new ArdenNumber(1), new ArdenNumber(2), 
				new ArdenString("A"), new ArdenString("B")};
		ArdenValue[] resultList = ((ArdenList)(result[0])).values;
		
		Assert.assertArrayEquals(expected, resultList);
	}
}
