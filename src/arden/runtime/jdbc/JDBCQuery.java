package arden.runtime.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;

public class JDBCQuery extends DatabaseQuery {
	private Connection connection;
	private String mapping;
	
	public JDBCQuery(String mapping, Connection connection) {
		this.mapping = mapping;
		this.connection = connection;
	}
	
	public static ArdenValue objectToArdenValue(Object o) {
		if (o instanceof String) {
			return new ArdenString((String)o);
		} else if (o instanceof Double) {
			return new ArdenNumber(((Double)o).doubleValue());
		} else if (o instanceof Integer) {
			return new ArdenNumber(((Integer)o).doubleValue());
		} else {
			return new ArdenString(o.toString());
		}
	}
	
	public static ArdenValue[] resultSetToArdenValues(ResultSet results) throws SQLException {
		if (results == null) {
			return ArdenList.EMPTY.values;
		}
		
		int columnCount = results.getMetaData().getColumnCount();
		
		List<List<ArdenValue>> resultTable = 
				new ArrayList<List<ArdenValue>>(columnCount);
		for (int column = 0; column < columnCount; column++) {
			resultTable.add(new LinkedList<ArdenValue>());
		}
		
		int rowCount = 0;
		while (results.next()) {
			rowCount++;
			for (int column = 0; column < columnCount; column++) {
				Object o = results.getObject(column + 1);
				resultTable.get(column).add(objectToArdenValue(o));
			}
		}
		
		if (rowCount == 0) {
			throw new RuntimeException("no results");
		} else if (rowCount == 1) {
			// one row
			List<ArdenValue> ardenResult = new LinkedList<ArdenValue>();
			for (int column = 0; column < columnCount; column++) {
				ardenResult.add(resultTable.get(column).get(0));
			}
			return ardenResult.toArray(new ArdenValue[0]);
		} else if (rowCount > 1) {
			List<ArdenList> ardenResult = new LinkedList<ArdenList>();
			for (int column = 0; column < columnCount; column++) {
				// convert every column to ArdenList
				ardenResult.add(
						new ArdenList(
								resultTable.get(column).toArray(
										new ArdenValue[0])));
			}
			return ardenResult.toArray(new ArdenList[0]);
		} else {
			throw new RuntimeException("not implemented");				
		}
	}
	
	@Override
	public ArdenValue[] execute() { 
		try {
			Statement stmt = connection.createStatement();
			
			stmt.execute(mapping);
			
			ResultSet results = null;
			if (stmt.getMoreResults()) {
				results = stmt.getResultSet();
			}
			
			return resultSetToArdenValues(results);
		} catch (SQLException e) {
			System.out.println("SQL Exception");
			while (e != null) {
				System.out.println("    State:   " + e.getSQLState());
				System.out.println("    Message: " + e.getMessage());
				System.out.println("    Error:   " + e.getErrorCode());
				e = e.getNextException();
			}
			return ArdenList.EMPTY.values;
		}
	}

	
}
