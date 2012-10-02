package arden.tests;

import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class GetValueTests {

	private MedicalLogicModule compile(String filename) throws Exception {
		Compiler c = new Compiler();
		c.enableDebugging(filename + ".mlm");
		CompiledMlm mlm = c
				.compileMlm(new InputStreamReader(ExampleTests.class.getResourceAsStream(filename + ".mlm")));
		return mlm;
	}
	
	@Test
	public void X27() throws Exception {
		MedicalLogicModule mlm = compile("x2.7");

		// mlm has not been not run yet:
		Assert.assertNull(mlm.getValue("low_dose_beta_use"));
		
		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertTrue(mlm.getValue("low_dose_beta_use").isFalse());
		Assert.assertNull(mlm.getValue("does_not_exist"));
	}
	
	@Test
	public void X28() throws Exception {
		MedicalLogicModule mlm = compile("x2.8");

		// mlm has not been not run yet:
		Assert.assertNull(mlm.getValue("num"));
		
		TestContext context = new TestContext();
		mlm.run(context, null);
		
		Assert.assertEquals(ArdenNumber.create(2.0, ArdenValue.NOPRIMARYTIME), mlm.getValue("num"));
		Assert.assertNull(mlm.getValue("does_not_exist"));
	}
}
