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

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

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
	public void MergeWithoutPrimaryTimes() throws Exception {
		assertEval("null", "1 merge 2");
		assertEval("null", "(1,3) merge (2,4)"); // no primary times
	}

	@Test
	public void MergeWithPrimaryTimes() throws Exception {
		ArdenValue[] a = { ArdenNumber.create(10, 1), ArdenNumber.create(3, 3) };
		ArdenValue[] b = { new ArdenString("last", 4), ArdenNumber.create(2, 2) };
		ArdenValue[] args = { new ArdenList(a), new ArdenList(b) };

		MedicalLogicModule mlm = ActionTests.parseTemplate("(a,b) := ARGUMENT;", "conclude true;",
				"return (a merge b);");
		ArdenValue[] arr = mlm.run(new TestContext(), args);
		Assert.assertEquals(1, arr.length);
		Assert.assertEquals("(10,2,3,\"last\")", arr[0].toString());
	}

	@Test
	public void SortByPrimaryTimes() throws Exception {
		assertEval("()", "SORT TIME ()");

		ArdenValue[] a = { ArdenNumber.create(10, 1), ArdenNumber.create(3, 3), new ArdenString("last", 4),
				ArdenNumber.create(2, 2) };
		ArdenValue[] args = { new ArdenList(a) };

		MedicalLogicModule mlm = ActionTests.parseTemplate("a := ARGUMENT;", "conclude true;", "return (sort time a);");
		ArdenValue[] arr = mlm.run(new TestContext(), args);
		Assert.assertEquals(1, arr.length);
		Assert.assertEquals("(10,2,3,\"last\")", arr[0].toString());
	}

	@Test
	public void SortPrimaryTimes() throws Exception {
		ArdenValue[] a = { ArdenNumber.create(10, 1), ArdenNumber.create(3, 3), new ArdenString("last", 4),
				ArdenNumber.create(2, 2) };
		ArdenValue[] args = { new ArdenList(a) };

		MedicalLogicModule mlm = ActionTests.parseTemplate("a := ARGUMENT;", "conclude true;",
				"return (sort time of a);");
		ArdenValue[] arr = mlm.run(new TestContext(), args);
		Assert.assertEquals(1, arr.length);
		ArdenList list = (ArdenList) arr[0];
		Assert.assertEquals(4, list.values.length);
		Assert.assertEquals(1, ((ArdenTime) list.values[0]).value);
		Assert.assertEquals(2, ((ArdenTime) list.values[1]).value);
		Assert.assertEquals(3, ((ArdenTime) list.values[2]).value);
		Assert.assertEquals(4, ((ArdenTime) list.values[3]).value);
	}

	@Test
	public void SortByData() throws Exception {
		assertEval("(1,2,3,4)", "sort (4,2,3,1)");
		assertEval("(1,2,3,4)", "sort data (4,2,3,1)");
		assertEval("null", "SORT DATA (3,1,2,null)");
		assertEval("null", "SORT DATA (3,\"abc\")");
	}

	@Test
	public void WhereOperator() throws Exception {
		assertEval("(10,30)", "(10,20,30,40) where (true,false,true,3)");
		assertEval("1", "1.0 where true");
		assertEval("(1,2,3)", "(1,2,3) where true");
		assertEval("()", "(1,2,3) where false");
		assertEval("(2,3)", "(1,2,3) where it > 1.5");
		assertEval("(1,3)", "(1,2,3) where they are not equal 2");
		assertEval("(1,1)", "1 where (true,false,true)");
		assertEval("null", "(1,2,3,4) where (true,false,true)");
	}

	@Test
	public void CountOperator() throws Exception {
		assertEval("4", "COUNT (12,13,14,null)");
		assertEval("1", "COUNT \"asdf\"");
		assertEval("0", "COUNT ()");
		assertEval("1", "COUNT null");
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
		assertEval("14", "AVERAGE (12,13,17)");
		assertEval("3", "AVERAGE 3.0");
		assertEval("null", "AVERAGE ()");
		assertEval("1990-03-11T03:10:00", "AVERAGE (1990-03-10T03:10:00, 1990-03-12T03:10:00)");
		assertEval("4 seconds", "AVERAGE (2 seconds, 3 seconds, 7 seconds)");
		assertEval("null", "AVERAGE (2, 3 seconds)");
		assertEval("1.57784765E7 seconds", "AVERAGE (1 second, 1 year)");
	}

	@Test
	public void MedianOperator() throws Exception {
		assertEval("13", "MEDIAN (12,17,13)");
		assertEval("3", "MEDIAN 3.0");
		assertEval("null", "MEDIAN ()");
		assertEval("1990-03-11T03:10:00", "MEDIAN (1990-03-10T03:10:00, 1990-03-11T03:10:00, 1990-03-28T03:10:00)");
		assertEval("1990-03-11T03:10:00", "MEDIAN (1990-03-10T03:10:00, 1990-03-12T03:10:00)");
		assertEval("3 seconds", "MEDIAN (2 seconds, 3 seconds, 7 years)");
		assertEval("5 months", "MEDIAN (2 seconds, 3 months, 7 months, 2 years)");
		assertEval("1 year", "MEDIAN (2 seconds, 1 year, 7 years)");
	}

	@Test
	public void SumOperator() throws Exception {
		assertEval("39", "SUM (12,13,14)");
		assertEval("3", "SUM 3");
		assertEval("0", "SUM ()");
		assertEval("7 months", "SUM (1 month, 6 months)");
	}

	@Test
	public void VarianceOperator() throws Exception {
		assertEval("2.5", "VARIANCE (12,13,14,15,16)");
		assertEval("0.5", "VARIANCE (1,2)");
		assertEval("null", "VARIANCE (,3)");
		assertEval("null", "VARIANCE 3");
		assertEval("null", "VARIANCE ()");
	}

	@Test
	public void StdDevOperator() throws Exception {
		assertEval("\"1.58113883\"", "STDDEV (12,13,14,15,16) FORMATTED WITH \"%.8f\"");
		assertEval("null", "STDDEV 3");
		assertEval("null", "STDDEV ()");
	}

	@Test
	public void MinimumOperator() throws Exception {
		assertEval("12", "MINIMUM (13,12,14)");
		assertEval("3", "MIN 3");
		assertEval("null", "MINIMUM ()");
		assertEval("null", "MINIMUM (1,\"abc\")");
		assertEval("1 day", "MIN (1 day, 1 week, 1 month)");
	}

	@Test
	public void MaximumOperator() throws Exception {
		assertEval("14", "MAXIMUM (13,12,14)");
		assertEval("3", "MAX 3");
		assertEval("null", "MAXIMUM ()");
		assertEval("null", "MAXIMUM (1,\"abc\")");
		assertEval("1 month", "MAX (1 day, 1 week, 1 month)");
	}

	@Test
	public void Last() throws Exception {
		assertEval("14", "LAST (13,12,14)");
		assertEval("3", "LAST 3");
		assertEval("null", "LAST ()");
	}

	@Test
	public void First() throws Exception {
		assertEval("13", "FIRST (13,12,14)");
		assertEval("3", "FIRST 3");
		assertEval("null", "FIRST ()");
	}

	@Test
	public void Any() throws Exception {
		assertEval("true", "ANY (true,false,false)");
		assertEval("false", "ANY false");
		assertEval("false", "ANY ()");
		assertEval("null", "ANY (3, 5, \"red\")");
		assertEval("false", "ANY (false, false)");
		assertEval("null", "ANY (false, null)");
		assertEval("true", "ANY (true, null)");
	}

	@Test
	public void All() throws Exception {
		assertEval("false", "ALL (true,false,false)");
		assertEval("true", "ALL true");
		assertEval("true", "ALL ()");
		assertEval("null", "ALL (3, 5, \"red\")");
		assertEval("true", "ALL (true, true)");
		assertEval("false", "ALL (false, null)");
		assertEval("null", "ALL (true, null)");
	}

	@Test
	public void No() throws Exception {
		assertEval("false", "NO (true,false,false)");
		assertEval("true", "NO false");
		assertEval("true", "NO ()");
		assertEval("null", "NO (3, 5, \"red\")");
		assertEval("true", "NO (false, false)");
		assertEval("null", "NO (false, null)");
		assertEval("false", "NO (true, null)");
	}

	@Test
	public void Latest() throws Exception {
		assertEval("null", "LATEST ()");
		assertEval("null", "LATEST 1");

		ArdenValue[] args = { ArdenNumber.create(2, 10), ArdenNumber.create(3, 20), ArdenNumber.create(4, 15) };
		assertEvalWithArgument("3", "LATEST arg", new ArdenList(args), new TestContext());

		ArdenValue[] args2 = { ArdenNumber.create(2, 10), ArdenNumber.create(3, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(4, 15) };
		assertEvalWithArgument("null", "LATEST arg", new ArdenList(args2), new TestContext());
	}

	@Test
	public void Earliest() throws Exception {
		assertEval("null", "EARLIEST ()");
		assertEval("null", "EARLIEST 1");

		ArdenValue[] args = { ArdenNumber.create(2, 10), ArdenNumber.create(3, 20), ArdenNumber.create(4, 15) };
		assertEvalWithArgument("2", "EARLIEST arg", new ArdenList(args), new TestContext());

		ArdenValue[] args2 = { ArdenNumber.create(2, 10), ArdenNumber.create(3, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(4, 15) };
		assertEvalWithArgument("null", "EARLIEST arg", new ArdenList(args2), new TestContext());
	}

	@Test
	public void ElementAt() throws Exception {
		assertEval("20", "(10,20,30,40)[2]");
		assertEval("()", "(10,20)[()]");
		assertEval("(null,20)", "(10,20)[1.5,2]");
		assertEval("(10,30,50)", "(10,20,30,40,50)[1,3,5]");
		assertEval("(10,30,50)", "(10,20,30,40,50)[1,(3,5)]");
		assertEval("(10,20,30)", "(10,20,30,40,50)[1 seqto 3]");
	}

	@Test
	public void SeqtoOperator() throws Exception {
		assertEval("(2,3,4)", "2 SEQTO 4");
		assertEval("()", "4 SEQTO 2");
		assertEval("null", "4.5 SEQTO 2");
		assertEval("(,2)", "2 SEQTO 2");
		assertEval("(-3,-2,-1)", "-3 SEQTO -1");
	}

	@Test
	public void ReverseOperator() throws Exception {
		assertEval("(3,2,1)", "REVERSE (1,2,3)");
		assertEval("(6,5,4,3,2,1)", "reverse (1 seqto 6)");
		assertEval("()", "reverse ()");
		assertEval("(,null)", "reverse null");
	}

	@Test
	public void IndexLatest() throws Exception {
		assertEval("null", "INDEX LATEST ()");
		assertEval("null", "INDEX LATEST 1");

		ArdenValue[] args = { ArdenNumber.create(2, 10), ArdenNumber.create(3, 20), ArdenNumber.create(4, 15) };
		assertEvalWithArgument("2", "INDEX LATEST arg", new ArdenList(args), new TestContext());

		ArdenValue[] args2 = { ArdenNumber.create(2, 10), ArdenNumber.create(3, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(4, 15) };
		assertEvalWithArgument("null", "INDEX LATEST arg", new ArdenList(args2), new TestContext());
	}

	@Test
	public void IndexEarliest() throws Exception {
		assertEval("null", "INDEX EARLIEST ()");
		assertEval("null", "INDEX EARLIEST 1");

		ArdenValue[] args = { ArdenNumber.create(2, 10), ArdenNumber.create(3, 20), ArdenNumber.create(4, 15) };
		assertEvalWithArgument("1", "INDEX EARLIEST arg", new ArdenList(args), new TestContext());

		ArdenValue[] args2 = { ArdenNumber.create(2, 10), ArdenNumber.create(3, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(4, 15) };
		assertEvalWithArgument("null", "INDEX EARLIEST arg", new ArdenList(args2), new TestContext());
	}

	@Test
	public void IndexMinimumOperator() throws Exception {
		assertEval("2", "INDEX MINIMUM (13,12,14)");
		assertEval("1", "INDEX MIN 3");
		assertEval("null", "INDEX MINIMUM ()");
		assertEval("null", "INDEX MINIMUM (1,\"abc\")");
		assertEval("1", "INDEX MIN (1 day, 1 week, 1 month)");
	}

	@Test
	public void IndexMaximumOperator() throws Exception {
		assertEval("3", "INDEX MAXIMUM (13,12,14)");
		assertEval("1", "INDEX MAX 3");
		assertEval("null", "INDEX MAXIMUM ()");
		assertEval("null", "INDEX MAXIMUM (1,\"abc\")");
		assertEval("3", "INDEX MAX (1 day, 1 week, 1 month)");
	}

	@Test
	public void nearestOperator() throws Exception {
		assertEval("null", "NEAREST now FROM ()");
		assertEval("null", "NEAREST now FROM (2,3)");

		ArdenValue[] arg = { ArdenNumber.create(12, 1000), ArdenNumber.create(13, 2000), ArdenNumber.create(14, 3000) };
		final ArdenTime now = new ArdenTime(2200);
		assertEvalWithArgument("13", "NEAREST now FROM arg", new ArdenList(arg), new TestContext() {
			@Override
			public ArdenTime getCurrentTime() {
				return now;
			}
		});
	}

	@Test
	public void indexNearestOperator() throws Exception {
		assertEval("null", "INDEX NEAREST now FROM ()");
		assertEval("null", "INDEX NEAREST now FROM (2,3)");

		ArdenValue[] arg = { ArdenNumber.create(12, 1000), ArdenNumber.create(13, 2000), ArdenNumber.create(14, 3000) };
		final ArdenTime now = new ArdenTime(2200);
		assertEvalWithArgument("2", "INDEX NEAREST now FROM arg", new ArdenList(arg), new TestContext() {
			@Override
			public ArdenTime getCurrentTime() {
				return now;
			}
		});
	}

	@Test
	public void slopeOperator() throws Exception {
		assertEval("null", "SLOPE null");
		assertEval("null", "SLOPE ()");
		assertEval("null", "SLOPE (1,2,3)");

		final int day = 86000 * 1000; // 1 day in ms

		ArdenValue[] arg = { ArdenNumber.create(1, day), ArdenNumber.create(5, 2 * day) };
		assertEvalWithArgument("4", "SLOPE arg", new ArdenList(arg), new TestContext());

		ArdenValue[] arg2 = { ArdenNumber.create(1, day), ArdenNumber.create(5, ArdenValue.NOPRIMARYTIME) };
		assertEvalWithArgument("null", "SLOPE arg", new ArdenList(arg2), new TestContext());

		ArdenValue[] arg3 = { ArdenNumber.create(1, day), ArdenNumber.create(10, 2 * day),
				ArdenNumber.create(6, day + day / 2) };
		assertEvalWithArgument("9", "SLOPE arg", new ArdenList(arg3), new TestContext());

		ArdenValue[] arg4 = { ArdenNumber.create(1, day), ArdenNumber.create(10, 2 * day),
				ArdenNumber.create(6, day + day / 2), ArdenNumber.create(4, day * 3) };
		assertEvalWithArgument("1.2", "SLOPE arg", new ArdenList(arg4), new TestContext());
	}
}
