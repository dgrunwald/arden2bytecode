package arden.tests;

import org.junit.Test;

public class IsComparisonTests extends ExpressionTestBase {
	@Test
	public void IsEqual() throws Exception {
		assertEval("false", "1 is equal 2");
		assertEval("true", "1 is not equal 2");
	}

	@Test
	public void IsLessThan() throws Exception {
		assertEval("false", "2 is less than 2");
		assertEval("true", "1 is less than 2");
	}

	@Test
	public void IsGreaterThan() throws Exception {
		assertEval("false", "2 is greater than 2");
		assertEval("true", "2 is greater than 1");
	}

	@Test
	public void IsLessThanOrEqual() throws Exception {
		assertEval("false", "3 is less than or equal 2");
		assertEval("true", "2 is less than or equal 2");
		assertEval("true", "1 is less than 2");
	}

	@Test
	public void IsWithinTo() throws Exception {
		assertEval("true", "3 is within 2 to 5");
		assertEval("true", "1990-03-10T00:00:00 IS WITHIN 1990-03-05T00:00:00 TO 1990-03-15T00:00:00");
		assertEval("true", "3 days IS WITHIN 2 days TO 5 months");
		assertEval("true", "\"ccc\" IS WITHIN \"a\" TO \"d\"");
		assertEval("(true,true)", "(1,2) is within (0,2) to (3,4)");
		assertEval("(false,true)", "(1,2) is within 2 to (3,4)");
		assertEval("null", "(1,2) is within 2 to (3,4,5)");
	}

	@Test
	public void IsWithinPreceding() throws Exception {
		assertEval("(false,true,true,true)",
				"1990-03-08T00:00:00 IS WITHIN (1,2,3,4) days PRECEDING 1990-03-10T00:00:00");
	}

	@Test
	public void IsWithinFollowing() throws Exception {
		assertEval("(false,true,true,true)",
				"1990-03-08T00:00:00 IS WITHIN (1,2,3,4) days FOLLOWING 1990-03-06T00:00:00");
	}

	@Test
	public void IsWithinSurrounding() throws Exception {
		assertEval("(false,true,true)", "1990-03-08T00:00:00 IS WITHIN (1,2,3) days SURROUNDING 1990-03-10T00:00:00");
		assertEval("(false,true,true)", "1990-03-08T00:00:00 IS WITHIN (1,2,3) days SURROUNDING 1990-03-06T00:00:00");
	}

	@Test
	public void IsWithinSameDayAs() throws Exception {
		assertEval("true", "1990-03-08T11:11:11 IS WITHIN SAME DAY AS 1990-03-08T01:01:01");
		assertEval("false", "1990-03-08T00:00:00 IS WITHIN SAME DAY AS 1990-03-07T23:59:59");
	}

	@Test
	public void IsInTest() throws Exception {
		assertEval("false", "2 IS IN (4,5,6)");
		assertEval("(false,true)", "(3,4) IS IN (4,5,6)");
		assertEval("true", "null IS IN (1/0, 2)");
	}

	@Test
	public void IsNotInTest() throws Exception {
		assertEval("true", "2 IS NOT IN (4,5,6)");
		assertEval("(true,false)", "(3,4) IS NOT IN (4,5,6)");
		assertEval("false", "null IS NOT IN (1/0, 2)");
	}

	@Test
	public void InTest() throws Exception {
		assertEval("false", "2 IN (4,5,6)");
		assertEval("(false,true)", "(3,4) IN (4,5,6)");
		assertEval("true", "null IN (1/0, 2)");
	}

	@Test
	public void NotInTest() throws Exception {
		assertEval("true", "2 NOT IN (4,5,6)");
		assertEval("(true,false)", "(3,4) NOT IN (4,5,6)");
		assertEval("false", "null NOT IN (1/0, 2)");
	}
}
