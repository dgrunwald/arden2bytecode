package arden.tests;

import junit.framework.Assert;

import org.junit.Test;

import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class LogicTests {
	static int i;

	private ArdenValue eval(String data, String logic, String action) throws Exception {
		MedicalLogicModule mlm = ActionTests.parseTemplate(data, logic, action);
		ArdenValue[] result = mlm.run(new TestContext(), null);
		Assert.assertEquals(1, result.length);
		return result[0];
	}

	@Test
	public void WhileLoop() throws Exception {
		ArdenValue sum = eval("", "i := 0; isum := 0;\n" + "while i <= 100 do\n" + "   isum := isum + i; i := i + 1;\n"
				+ "enddo; conclude true;", "return isum;");
		Assert.assertEquals("5050", sum.toString());
	}

	@Test
	public void ForLoop() throws Exception {
		ArdenValue sum = eval("", "isum := 0;\n" + "for i in 1 seqto 100 do\n" + "   isum := isum + i;\n"
				+ "enddo; conclude true;", "return isum;");
		Assert.assertEquals("5050", sum.toString());
	}

	@Test
	public void UninitializedVar() throws Exception {
		ArdenValue sum = eval("", "if false then isum := 0; endif; conclude true;", "return isum;");
		Assert.assertEquals("null", sum.toString());
	}

}
