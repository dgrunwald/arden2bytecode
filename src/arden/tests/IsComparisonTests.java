package arden.tests;

import java.util.GregorianCalendar;

import org.junit.Test;

import arden.runtime.ArdenNumber;

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
		assertEval("true", "1 is less than or equal 2");
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
	public void IsWithinPast() throws Exception {
		assertEval("false", "1990-03-08T00:00:00 IS WITHIN PAST 3 days");
		assertEval("true", "1990-03-08T00:00:00 IS WITHIN PAST 3 days", getContextWithNow(1990, 03, 10));
	}

	@Test
	public void IsWithinSameDayAs() throws Exception {
		assertEval("true", "1990-03-08T11:11:11 IS WITHIN SAME DAY AS 1990-03-08T01:01:01");
		assertEval("false", "1990-03-08T00:00:00 IS WITHIN SAME DAY AS 1990-03-07T23:59:59");
	}

	@Test
	public void IsBefore() throws Exception {
		assertEval("true", "1990-03-08T00:00:00 IS BEFORE 1990-03-08T00:00:01");
		assertEval("false", "1990-03-08T00:00:02 IS BEFORE 1990-03-08T00:00:01");
		assertEval("false", "1990-03-08T00:00:01 IS BEFORE 1990-03-08T00:00:01");
	}

	@Test
	public void IsAfter() throws Exception {
		assertEval("false", "1990-03-08T00:00:00 IS AFTER 1990-03-08T00:00:01");
		assertEval("true", "1990-03-08T00:00:02 IS AFTER 1990-03-08T00:00:01");
		assertEval("false", "1990-03-08T00:00:01 IS AFTER 1990-03-08T00:00:01");
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
	public void WhereNotInTest() throws Exception {
		assertEval("(1,3,5)", "(1 seqto 5) WHERE it IS NOT IN (2, 4)");
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

	@Test
	public void IsPresentTest() throws Exception {
		assertEval("true", "3 IS PRESENT");
		assertEval("false", "null IS PRESENT");
		assertEval("(true,false)", "(3,null) IS PRESENT");
	}

	@Test
	public void IsNullTest() throws Exception {
		assertEval("false", "3 IS NULL");
		assertEval("true", "null IS NULL");
		assertEval("(false,true)", "(3,null) IS NULL");
	}

	@Test
	public void IsBooleanTest() throws Exception {
		assertEval("(false,true,false)", "(null,false,3) IS BOOLEAN");
	}

	@Test
	public void IsNumberTest() throws Exception {
		assertEval("(false,false,true)", "(null,false,3) IS NUMBER");
	}

	@Test
	public void IsStringTest() throws Exception {
		assertEval("(false,true,false,false)", "(null,\"asdf\",3,1991-03-12) IS STRING");
	}

	@Test
	public void IsTimeTest() throws Exception {
		assertEval("(false,true,false)", "(null,1991-03-12,3) IS TIME");
	}

	@Test
	public void IsDurationTest() throws Exception {
		assertEval("(false,true,false,false)", "(null,3 days,3,1991-03-12) IS DURATION");
	}

	@Test
	public void IsListTest() throws Exception {
		assertEval("true", "(3, 2, 1) IS LIST");
		assertEval("false", "5 IS LIST");
		assertEval("false", "null IS LIST");
		assertEval("true", "(,5) IS LIST");
	}

	private void occurTest(String expected, String code) throws Exception {
		assertEvalWithArgument(expected, code, ArdenNumber.create(1,
				new GregorianCalendar(1990, 03 - 1, 05, 11, 11, 11).getTimeInMillis()), getContextWithNow(1990, 03, 06));
	}

	@Test
	public void OccurEqual() throws Exception {
		occurTest("null", "1 OCCURRED EQUAL 1990-03-06");
		occurTest("false", "arg OCCUR EQUAL 1990-03-01T00:00:00");
		occurTest("true", "arg OCCURRED EQUAL 1990-03-05T11:11:11");
	}

	@Test
	public void OccurAt() throws Exception {
		occurTest("null", "1 OCCURRED AT 1990-03-06");
		occurTest("false", "arg OCCUR AT 1990-03-01T00:00:00");
		occurTest("true", "arg OCCURRED AT 1990-03-05T11:11:11");
	}

	@Test
	public void OccurWithinTo() throws Exception {
		occurTest("true", "arg OCCURRED WITHIN 1990-03-01T00:00:00 TO 1990-03-11T00:00:00");
		occurTest("null", "null OCCURRED WITHIN 1990-03-01T00:00:00 TO 1990-03-11T00:00:00");
		occurTest("(false,null)", "arg OCCURRED WITHIN (now,null) TO 1990-03-11T00:00:00");
	}

	@Test
	public void OccurPreceding() throws Exception {
		occurTest("(false,null,true)", "arg OCCURRED WITHIN 3 days PRECEDING (1990-03-10T00:00:00,null,now)");
	}

	@Test
	public void OccurFollowing() throws Exception {
		occurTest("(true,null,false)", "arg OCCURRED WITHIN 3 days FOLLOWING (1990-03-04T00:00:00,null,now)");
	}

	@Test
	public void OccurSurrounding() throws Exception {
		occurTest("(true,null,true,false)",
				"arg OCCURRED WITHIN 3 days SURROUNDING (1990-03-04T00:00:00,null,now,1990-03-10)");
	}

	@Test
	public void OccurWithinPast() throws Exception {
		occurTest("(null,true)", "(null,arg) OCCURRED WITHIN PAST 3 days");
	}

	@Test
	public void OccurWithinSameDayAs() throws Exception {
		occurTest("(null,true,false,false)",
				"arg OCCURRED WITHIN SAME DAY AS (false,now-1 second,now,1990-03-08T01:01:01)");
	}

	@Test
	public void OccurBefore() throws Exception {
		occurTest("(true,true,false)", "arg OCCURRED BEFORE (1990-03-08T01:01:01,now,1990-03-01)");
	}

	@Test
	public void OccurAfter() throws Exception {
		occurTest("(false,false,true)", "arg OCCURRED AFTER (1990-03-08T01:01:01,now,1990-03-01)");
	}
}
