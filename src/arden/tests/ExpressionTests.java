package arden.tests;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompilerException;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class ExpressionTests {
	public static ArdenValue evalExpression(String expressionCode) throws CompilerException, InvocationTargetException {
		MedicalLogicModule mlm = ActionTests.parseAction("return (" + expressionCode + ")");
		ArdenValue[] arr = mlm.run(new TestContext());
		Assert.assertEquals(1, arr.length);
		return arr[0];
	}

	@Test
	public void QuotationTest() throws Exception {
		ArdenString s = (ArdenString) evalExpression("\"this string has one quotation mark: \"\" \"");
		Assert.assertEquals("this string has one quotation mark: \" ", s.value);
	}
}
