package arden.tests;

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;

public class ExpressionTests extends ExpressionTestBase {
	@Test
	public void QuotationTest() throws Exception {
		ArdenString s = (ArdenString) evalExpression("\"this string has one quotation mark: \"\" \"");
		Assert.assertEquals("this string has one quotation mark: \" ", s.value);
	}

	@Test
	public void StringWithTwoSpaces() throws Exception {
		ArdenString s = (ArdenString) evalExpression("\"test  string");
		Assert.assertEquals("test string", s.value);
	}

	@Test
	public void StringWithLineBreak() throws Exception {
		ArdenString s = (ArdenString) evalExpression("\"test\nstring");
		Assert.assertEquals("test  string", s.value);
	}

	@Test
	public void StringWithMultiLineBreak() throws Exception {
		ArdenString s = (ArdenString) evalExpression("\"test  \n  \t \r\n  string");
		Assert.assertEquals("test\nstring", s.value);
	}

	@Test
	public void NullConstant() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("null"));
	}

	@Test
	public void BooleanTrue() throws Exception {
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true"));
	}

	@Test
	public void BooleanFalse() throws Exception {
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("false"));
	}

	@Test
	public void NumberTests() throws Exception {
		ArdenNumber num = (ArdenNumber) evalExpression("345.5");
		Assert.assertEquals(345.5, num.value, 0);

		num = (ArdenNumber) evalExpression("0.1");
		Assert.assertEquals(0.1, num.value, 0);

		num = (ArdenNumber) evalExpression("34.5E34");
		Assert.assertEquals(34.5E34, num.value, 0);

		num = (ArdenNumber) evalExpression("0.1e+4");
		Assert.assertEquals(0.1e+4, num.value, 0);

		num = (ArdenNumber) evalExpression("0.1e-4");
		Assert.assertEquals(0.1e-4, num.value, 0);

		num = (ArdenNumber) evalExpression(".3");
		Assert.assertEquals(.3, num.value, 0);

		num = (ArdenNumber) evalExpression("3.");
		Assert.assertEquals(3., num.value, 0);

		num = (ArdenNumber) evalExpression("3e10");
		Assert.assertEquals(3e10, num.value, 0);
	}

	@Test
	public void TimeStampWithFractionalSeconds() throws Exception {
		ArdenTime t = (ArdenTime) evalExpression("1989-01-01T13:30:00.123");
		// TODO: test whether the time is represented correctly
		// check that the time was interpreted as local time.
	}

	@Test
	public void TimeStampWithFractionalSecondsUTC() throws Exception {
		ArdenTime t = (ArdenTime) evalExpression("1989-01-01T13:30:00.123z");
		// TODO: test whether the time is represented correctly
		// check that the time was interpreted as UTC.
	}

	@Test
	public void TimeCalculation() throws Exception {
		assertEval("1993-05-17T00:00:00", "1800-01-01 + (1993-1800) years + 4 months + (17-1) days");
	}

	@Test
	public void AdditionOperator() throws Exception {
		assertEval("6.0", "4 + 2");
		assertEval("()", "5 + ()");
		assertEval("null", "(1,2,3) + ()");
		assertEval("()", "null + ()");
		assertEval("null", "5 + null");
		assertEval("(null, null, null)", "(1,2,3) + null");
		assertEval("null", "null + null");
	}

	@Test
	public void DurationAdditionOperator() throws Exception {
		assertEval("3.0 seconds", "1 second + 2 seconds");
		assertEval("13.0 months", "1 month + 1 year");
		assertEval("2629746.5 seconds", "1 month + 0.5 seconds");
	}

	@Test
	public void TimeSecondAddition() throws Exception {
		assertEval("1990-03-01T00:00:01", "1990-02-01T00:00:00 + 2419201 seconds");
		assertEval("1990-03-15T00:00:00", "1990-03-13T00:00:00 + 2 days");
		assertEval("1990-03-15T00:00:00", "2 days + 1990-03-13T00:00:00");
	}

	@Test
	public void TimeMonthAddition() throws Exception {
		assertEval("1991-02-28T00:00:00", "1991-01-31T00:00:00 + 1 month");
		assertEval("1991-03-03T01:02:54.600", "1991-01-31T00:00:00 + 1.1 months");
		assertEval("1993-02-28T00:00:00", "1993-01-31 + 1 month");
		assertEval("2000-02-29T00:00:00", "2000-01-31 + 1 month");
		assertEval("1993-01-28T00:00:00", "1993-02-28 - 1 month");
		assertEval("1990-11-26T22:57:05.400", "1991-01-31T00:00:00 - 2.1 months");
		assertEval("1990-12-27T22:57:05.400", "1991-01-31T00:00:00 - 1.1 months");
		assertEval("1991-04-26T22:57:05.400", "1991-04-30T00:00:00 - 0.1 months");

		assertEval("1993-05-17T00:00:00", "0000-00-00 + 1993 years + 5 months + 17 days");
	}

	@Test
	public void AdditionOfBoolean() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("true + 3"));
	}

	@Test
	public void SubtractionOperator() throws Exception {
		assertEval("4.0", "6-2");
		assertEval("1.0 seconds", "3 seconds - 2 seconds");
		assertEval("1990-03-13T00:00:00", "1990-03-15T00:00:00 - 2 days");
		assertEval("172800.0 seconds", "1990-03-15T00:00:00 - 1990-03-13T00:00:00");

		assertEval("-6.0", "3-4-5");
		assertEval("2419200.0 seconds", "1990-03-01T00:00:00 - 1990-02-01T00:00:00");
	}

	@Test
	public void UnaryPlus() throws Exception {
		assertEval("(3.0, 4.0, null)", "+(3, 4, \"a\")");
	}

	@Test
	public void UnaryMinus() throws Exception {
		assertEval("(-3.0, -4.0, null, -12.0 months, -1.0 seconds)", "-(3, 4, \"a\", 1 year, 1 second)");
	}

	@Test
	public void MultiplicationOperator() throws Exception {
		assertEval("(8.0, 6.0 seconds, 6.0 months)", "(4*2, 3 * 2 seconds, 3 months * 2)");
	}

	@Test
	public void DivisionOperator() throws Exception {
		assertEval("0.5", "1/2");
		assertEval("2629746.0", "1 month / 1 second");
		assertEval("(4.0, 2.0 seconds, 120.0, 36.0)", "(8/2, 6 seconds / 3, 2 minutes / 1 second, 3 years / 1 month)");
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("3/0"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("2 seconds / 0"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("2 seconds / 0 seconds"));
	}

	@Test
	public void ExponentiationOperator() throws Exception {
		assertEval("9.0", "3 ** 2");
		assertEval("2.0", "4 ** 0.5");
	}

	@Test
	public void OrOperator() throws Exception {
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true or false"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("false or false"));
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true or null"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("false or null"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("false or 3.4"));
		assertEval("()", "() or ()");
		assertEval("(true, true)", "(true, false) or (false, true)");
	}

	@Test
	public void AndOperator() throws Exception {
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true and true"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("true and false"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("true and null"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("false and null"));
		assertEval("(false, null, true, null)", "true and (false, null, true, 3)");
	}

	@Test
	public void NotOperator() throws Exception {
		assertEval("(false, true, null, null)", "not (true, false, null, 3)");
	}

	@Test
	public void EqualsOperator() throws Exception {
		assertEval("false", "1 = 2");
		assertEval("false", "1 eq 2");
		assertEval("(null, true, false)", "(1,2,\"a\") = (null,2,3)");
		assertEval("null", "(3/0) = (3/0)");
		// TODO: check what should happen for "5 = ()"
		// assertEval("null", "5 = ()"); maybe a bug in the specification?
		// ignoring this case for now
		assertEval("null", "(1,2,3) = ()");
		assertEval("()", "null = ()");
		assertEval("()", "() = ()");
		assertEval("null", "5 = null");
		assertEval("(null, null, null)", "(1,2,3) = null");
		assertEval("null", "null = null");
		assertEval("(true, true, false)", "(1,2,3) = (1,2,4)");
	}

	@Test
	public void InEqualsOperator() throws Exception {
		assertEval("true", "1 <> 2");
		assertEval("true", "1 ne 2");
		assertEval("(null, false, true)", "(1,2,\"a\") <> (null,2,3)");
		assertEval("null", "(3/0) <> (3/0)");
	}

	@Test
	public void LessThanOperator() throws Exception {
		assertEval("true", "1 < 2");
		assertEval("true", "1 lt 2");
		assertEval("true", "1990-03-02T00:00:00 < 1990-03-10T00:00:00");
		assertEval("true", "2 days < 1 year");
		assertEval("true", "\"aaa\" < \"aab\"");
		assertEval("null", "\"aaa\" < 1");
	}

	@Test
	public void AfterOperator() throws Exception {
		assertEval("1990-03-15T00:00:00", "2 days AFTER 1990-03-13T00:00:00");
		assertEval("null", "2 days AFTER 1 day");

		assertEval("2000-09-13T00:08:00", "2 days FROM 2000-09-11T00:08:00");
	}

	@Test
	public void BeforeOperator() throws Exception {
		assertEval("1990-03-11T00:00:00", "2 days BEFORE 1990-03-13T00:00:00");
		assertEval("null", "2 days BEFORE 1 day");
	}
}
