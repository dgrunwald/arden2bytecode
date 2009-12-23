package arden.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.MedicalLogicModule;

import org.junit.Assert;
import org.junit.Test;

public class CompilerTests {
	private static String inputStreamToString(InputStream in) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append("\n");
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	public MedicalLogicModule parseAction(String actionCode) throws CompilerException {
		try {
			InputStream s = CompilerTests.class.getResourceAsStream("ActionTemplate.mlm");
			String fullCode = inputStreamToString(s).replace("$ACTION", actionCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void SimpleWrite() throws Exception {
		TestContext context = new TestContext();
		MedicalLogicModule mlm = parseAction("write \"Hello, World\"");
		mlm.run(context);
		Assert.assertEquals("Hello, World\n", context.getOutputText());
	}
}
