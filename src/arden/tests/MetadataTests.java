package arden.tests;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.runtime.MedicalLogicModule;

public class MetadataTests {
	@Test
	public void X21Metadata() throws Exception {
		Compiler c = new Compiler();
		CompiledMlm mlm = c.compileMlm(new InputStreamReader(MetadataTests.class.getResourceAsStream("x2.1.mlm")));
		FileOutputStream os = new FileOutputStream("xyz.class");
		mlm.saveClassFile(os);
		os.close();
		// TODO: check metadata
	}
}
