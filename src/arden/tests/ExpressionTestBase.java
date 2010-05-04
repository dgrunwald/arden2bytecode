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

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.junit.Assert;

import arden.compiler.CompilerException;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;

public class ExpressionTestBase {
	public static ArdenValue evalExpression(String expressionCode) throws CompilerException, InvocationTargetException {
		return evalExpression(expressionCode, new TestContext());
	}

	public static ArdenValue evalExpression(String expressionCode, ExecutionContext context) throws CompilerException,
			InvocationTargetException {
		MedicalLogicModule mlm = ActionTests.parseAction("return (" + expressionCode + ")");
		ArdenValue[] arr = mlm.run(context, null);
		Assert.assertEquals(1, arr.length);
		return arr[0];
	}

	public static void assertEval(String expectedResult, String expressionCode) throws CompilerException,
			InvocationTargetException {
		ArdenValue val = evalExpression(expressionCode);
		Assert.assertEquals(expressionCode, expectedResult, val.toString());
	}

	public static void assertEval(String expectedResult, String expressionCode, ExecutionContext context)
			throws CompilerException, InvocationTargetException {
		ArdenValue val = evalExpression(expressionCode, context);
		Assert.assertEquals(expressionCode, expectedResult, val.toString());
	}

	public static void assertEvalWithArgument(String expectedResult, String expressionCode, ArdenValue argument,
			ExecutionContext context) throws CompilerException, InvocationTargetException {
		MedicalLogicModule mlm = ActionTests.parseTemplate("arg := argument;", "conclude true;", "return ("
				+ expressionCode + ")");
		ArdenValue[] arr = mlm.run(context, new ArdenValue[] { argument });
		Assert.assertEquals(1, arr.length);
		Assert.assertEquals(expressionCode, expectedResult, arr[0].toString());
	}

	static TestContext getContextWithNow(final int year, final int month, final int day) {
		return new TestContext() {
			@Override
			public ArdenTime getCurrentTime() {
				return new ArdenTime(new GregorianCalendar(year, month - 1, day).getTime());
			}
		};
	}
}
