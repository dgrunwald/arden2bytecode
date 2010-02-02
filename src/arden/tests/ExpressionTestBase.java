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
