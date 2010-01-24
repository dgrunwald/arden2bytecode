package arden.tests;

import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.runtime.MedicalLogicModule;

public class ExampleTests {
	private MedicalLogicModule compile(String filename) throws Exception {
		Compiler c = new Compiler();
		CompiledMlm mlm = c
				.compileMlm(new InputStreamReader(MetadataTests.class.getResourceAsStream(filename + ".mlm")));
		FileOutputStream fos = new FileOutputStream(filename + ".class");
		mlm.saveClassFile(fos);
		fos.close();
		return mlm;
	}

	@Test
	public void X21() throws Exception {
		MedicalLogicModule mlm = compile("x2.1");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X22() throws Exception {
		MedicalLogicModule mlm = compile("x2.2");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X23() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X24() throws Exception {
		MedicalLogicModule mlm = compile("x2.4");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X25() throws Exception {
		MedicalLogicModule mlm = compile("x2.5");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X26() throws Exception {
		MedicalLogicModule mlm = compile("x2.6");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X27() throws Exception {
		MedicalLogicModule mlm = compile("x2.7");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
	
	@Test
	public void X28() throws Exception {
		MedicalLogicModule mlm = compile("x2.8");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("x", context.getOutputText());
	}
}
