package arden.tests;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompilerException;
import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenNumber;
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
	}

	@Test
	public void DivisionByZero() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("3/0"));
	}

	@Test
	public void AdditionOfBoolean() throws Exception {
		Assert.assertSame(ArdenNull.INSTANCE, evalExpression("true + 3"));
	}

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
}
