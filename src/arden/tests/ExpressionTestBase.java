package arden.tests;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;

import arden.compiler.CompilerException;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class ExpressionTestBase {
	public static ArdenValue evalExpression(String expressionCode) throws CompilerException, InvocationTargetException {
		MedicalLogicModule mlm = ActionTests.parseAction("return (" + expressionCode + ")");
		ArdenValue[] arr = mlm.run(new TestContext(), null);
		Assert.assertEquals(1, arr.length);
		return arr[0];
	}

	public static void assertEval(String expectedResult, String expressionCode) throws CompilerException,
			InvocationTargetException {
		ArdenValue val = evalExpression(expressionCode);
		Assert.assertEquals(expressionCode, expectedResult, val.toString());
	}
}
