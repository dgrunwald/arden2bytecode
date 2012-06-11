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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import arden.runtime.ArdenList;
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
			
			boolean resultSetAvailable = stmt.execute(mapping);
			
			ResultSet results = null;
			if (resultSetAvailable) {
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
