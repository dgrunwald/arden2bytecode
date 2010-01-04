package arden.tests;

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;

public class ListOperatorTests extends ExpressionTestBase {
	@Test
	public void EmptyList() throws Exception {
		Assert.assertSame(ArdenList.EMPTY, evalExpression("()"));
	}

	@Test
	public void SingleElementList() throws Exception {
		ArdenList list = (ArdenList) evalExpression(",null");
		Assert.assertEquals(1, list.values.length);
		Assert.assertSame(ArdenNull.INSTANCE, list.values[0]);
	}

	@Test
	public void UnaryCommaKeepsExistingList() throws Exception {
		Assert.assertSame(ArdenList.EMPTY, evalExpression(",()"));
	}

	@Test
	public void BinaryComma() throws Exception {
		ArdenList list = (ArdenList) evalExpression("4,2");
		Assert.assertEquals(2, list.values.length);
		Assert.assertEquals(4, ((ArdenNumber) list.values[0]).value, 0);
		Assert.assertEquals(2, ((ArdenNumber) list.values[1]).value, 0);
	}

	@Test
	public void BinaryComma2() throws Exception {
		ArdenList list = (ArdenList) evalExpression("(4,\"a\") , null");
		Assert.assertEquals(3, list.values.length);
		Assert.assertEquals(4, ((ArdenNumber) list.values[0]).value, 0);
		Assert.assertEquals("a", ((ArdenString) list.values[1]).value);
		Assert.assertSame(ArdenNull.INSTANCE, list.values[2]);
	}

	@Test
	public void WhereOperator() throws Exception {
		assertEval("(10.0, 30.0)", "(10,20,30,40) where (true,false,true,3)");
		assertEval("1.0", "1.0 where true");
		assertEval("(1.0, 2.0, 3.0)", "(1,2,3) where true");
		assertEval("()", "(1,2,3) where false");
		assertEval("(2.0, 3.0)", "(1,2,3) where it > 1.5");
		assertEval("(1.0, 3.0)", "(1,2,3) where they are not equal 2");
		assertEval("(1.0, 1.0)", "1 where (true,false,true)");
		assertEval("null", "(1,2,3,4) where (true,false,true)");
	}

	@Test
	public void CountOperator() throws Exception {
		assertEval("4.0", "COUNT (12,13,14,null)");
		assertEval("1.0", "COUNT \"asdf\"");
		assertEval("0.0", "COUNT ()");
		assertEval("1.0", "COUNT null");
	}

	@Test
	public void ExistOperator() throws Exception {
		assertEval("true", "EXIST (12,13,14)");
		assertEval("false", "EXIST null");
		assertEval("false", "EXIST ()");
		assertEval("true", "EXIST (\"plugh\", null)");
	}

	@Test
	public void AverageOperator() throws Exception {
		assertEval("14.0", "AVERAGE (12,13,17)");
		assertEval("3.0", "AVERAGE 3.0");
		assertEval("null", "AVERAGE ()");
		assertEval("1990-03-11T03:10:00", "AVERAGE (1990-03-10T03:10:00, 1990-03-12T03:10:00)");
		assertEval("4.0 seconds", "AVERAGE (2 seconds, 3 seconds, 7 seconds)");
		assertEval("null", "AVERAGE (2, 3 seconds)");
		assertEval("1.57784765E7 seconds", "AVERAGE (1 second, 1 year)");
	}

	@Test
	public void MedianOperator() throws Exception {
		assertEval("13.0", "MEDIAN (12,17,13)");
		assertEval("3.0", "MEDIAN 3.0");
		assertEval("null", "MEDIAN ()");
		assertEval("1990-03-11T03:10:00", "MEDIAN (1990-03-10T03:10:00, 1990-03-11T03:10:00, 1990-03-28T03:10:00)");
		assertEval("1990-03-11T03:10:00", "MEDIAN (1990-03-10T03:10:00, 1990-03-12T03:10:00)");
		assertEval("3.0 seconds", "MEDIAN (2 seconds, 3 seconds, 7 years)");
		assertEval("5.0 months", "MEDIAN (2 seconds, 3 months, 7 months, 2 years)");
		assertEval("12.0 months", "MEDIAN (2 seconds, 1 year, 7 years)");
	}

	@Test
	public void SumOperator() throws Exception {
		assertEval("39.0", "SUM (12,13,14)");
		assertEval("3.0", "SUM 3");
		assertEval("0.0", "SUM ()");
		assertEval("7.0 months", "SUM (1 month, 6 months)");
	}

	@Test
	public void VarianceOperator() throws Exception {
		// assertEval("2.5", "VARIANCE (12,13,14,15,16)"); // TODO: spec bug?
		// should be 2.0 according to manual calculation
		assertEval("0.0", "VARIANCE (,3)");
		assertEval("null", "VARIANCE 3");
		assertEval("null", "VARIANCE ()");
	}

	@Test
	public void StdDevOperator() throws Exception {
		// assertEval("1.58113883", "STDDEV (12,13,14,15,16)"); // TODO: spec
		// bug? see variance
		assertEval("null", "STDDEV 3");
		assertEval("null", "STDDEV ()");
	}
}