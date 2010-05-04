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

package arden.tests;

import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;

public class ExpressionTests extends ExpressionTestBase {
	@Test
	public void NullConstant() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("null"));
	}

	@Test
	public void TheNullConstant() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("the null"));
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
		assertEval("1989-01-01T13:30:00.123", "1989-01-01T13:30:00.123");
	}

	// @Test
	// public void TimeStampWithFractionalSecondsUTC() throws Exception {
	// ArdenTime t = (ArdenTime) evalExpression("1989-01-01T13:30:00.123z");
	// TODO: test whether the time is represented correctly
	// check that the time was interpreted as UTC.
	// }

	@Test
	public void TimeCalculation() throws Exception {
		assertEval("1993-05-17T00:00:00", "1800-01-01 + (1993-1800) years + 4 months + (17-1) days");
	}

	@Test
	public void AdditionOperator() throws Exception {
		assertEval("6", "4 + 2");
		assertEval("()", "5 + ()");
		assertEval("null", "(1,2,3) + ()");
		assertEval("()", "null + ()");
		assertEval("null", "5 + null");
		assertEval("(null,null,null)", "(1,2,3) + null");
		assertEval("null", "null + null");
	}

	@Test
	public void DurationAdditionOperator() throws Exception {
		assertEval("3 seconds", "1 second + 2 seconds");
		assertEval("13 months", "1 month + 1 year");
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
		assertEval("4", "6-2");
		assertEval("1 second", "3 seconds - 2 seconds");
		assertEval("1990-03-13T00:00:00", "1990-03-15T00:00:00 - 2 days");
		assertEval("2 days", "1990-03-15T00:00:00 - 1990-03-13T00:00:00");

		assertEval("-6", "3-4-5");
		assertEval("28 days", "1990-03-01T00:00:00 - 1990-02-01T00:00:00");
	}

	@Test
	public void UnaryPlus() throws Exception {
		assertEval("(3,4,null)", "+(3, 4, \"a\")");
	}

	@Test
	public void UnaryMinus() throws Exception {
		assertEval("(-3,-4,null,-1 years,-1 seconds)", "-(3, 4, \"a\", 1 year, 1 second)");
	}

	@Test
	public void MultiplicationOperator() throws Exception {
		assertEval("(8,6 hours,6 months)", "(4*2, 3 * 2 hours, 3 months * 2)");
	}

	@Test
	public void DivisionOperator() throws Exception {
		assertEval("0.5", "1/2");
		assertEval("2629746", "1 month / 1 second");
		assertEval("(4,3 minutes,120,36)", "(8/2, 6 hours / 120, 2 minutes / 1 second, 3 years / 1 month)");
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("3/0"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("2 seconds / 0"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("2 seconds / 0 seconds"));
	}

	@Test
	public void ExponentiationOperator() throws Exception {
		assertEval("9", "3 ** 2");
		assertEval("2", "4 ** 0.5");
	}

	@Test
	public void OrOperator() throws Exception {
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true or false"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("false or false"));
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true or null"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("false or null"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("false or 3.4"));
		assertEval("()", "() or ()");
		assertEval("(true,true)", "(true,false) or (false,true)");
	}

	@Test
	public void AndOperator() throws Exception {
		Assert.assertSame(ArdenBoolean.TRUE, evalExpression("true and true"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("true and false"));
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("true and null"));
		Assert.assertSame(ArdenBoolean.FALSE, evalExpression("false and null"));
		assertEval("(false,null,true,null)", "true and (false, null, true, 3)");
	}

	@Test
	public void NotOperator() throws Exception {
		assertEval("(false,true,null,null)", "not (true, false, null, 3)");
	}

	@Test
	public void EqualsOperator() throws Exception {
		assertEval("false", "1 = 2");
		assertEval("false", "1 eq 2");
		assertEval("(null,true,false)", "(1,2,\"a\") = (null,2,3)");
		assertEval("null", "(3/0) = (3/0)");
		// TODO: check what should happen for "5 = ()"
		// assertEval("null", "5 = ()"); maybe a bug in the specification?
		// ignoring this case for now
		assertEval("null", "(1,2,3) = ()");
		assertEval("()", "null = ()");
		assertEval("()", "() = ()");
		assertEval("null", "5 = null");
		assertEval("(null,null,null)", "(1,2,3) = null");
		assertEval("null", "null = null");
		assertEval("(true,true,false)", "(1,2,3) = (1,2,4)");
	}

	@Test
	public void InEqualsOperator() throws Exception {
		assertEval("true", "1 <> 2");
		assertEval("true", "1 ne 2");
		assertEval("(null,false,true)", "(1,2,\"a\") <> (null,2,3)");
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

	@Test
	public void AgoOperator() throws Exception {
		assertEval("2010-02-02T00:00:00", "2 days AGO", getContextWithNow(2010, 2, 4));
	}

	@Test
	public void StringConcat() throws Exception {
		assertEval("\"ab\"", "\"a\" || \"b\"");
		assertEval("\"null3\"", "null || 3");
		assertEval("\"45\"", "4 || 5");
		assertEval("\"4.7four\"", "4.7 || \"four\"");
		assertEval("\"true\"", "true || \"\"");
		assertEval("\"3 days left\"", "3 days || \" left\"");
		assertEval("\"on 1990-03-15T13:45:01\"", "\"on \" || 1990-03-15T13:45:01");
		assertEval("\"list=(1,2,3)\"", "\"list=\" || (1,2,3)");
	}

	@Test
	public void ExtractTimeComponents() throws Exception {
		assertEval("1990", "EXTRACT YEAR 1990-01-03T14:23:17.3");
		assertEval("(1990,2009,null)", "EXTRACT YEAR (1990-01-03, 2009-01-03, 1 year)");

		assertEval("1", "EXTRACT MONTH 1990-01-03T14:23:17.3");
		assertEval("3", "EXTRACT DAY 1990-01-03T14:23:17.3");
		assertEval("14", "EXTRACT HOUR 1990-01-03T14:23:17.3");
		assertEval("23", "EXTRACT MINUTE 1990-01-03T14:23:17.3");
		assertEval("17.3", "EXTRACT SECOND 1990-01-03T14:23:17.3");
	}

	@Test
	public void TimeOfOperator() throws Exception {
		assertEval("null", "TIME OF null");
		assertEval("null", "TIME OF 1990-01-03T14:23:17.3");

		long time = new GregorianCalendar(2010, 1, 5).getTimeInMillis();
		assertEvalWithArgument("2010-02-05T00:00:00", "TIME OF arg", ArdenBoolean.create(true, time), new TestContext());
		assertEvalWithArgument("2010-02-05T00:00:00", "TIME OF TIME arg", ArdenNumber.create(0, time),
				new TestContext());
	}
}
