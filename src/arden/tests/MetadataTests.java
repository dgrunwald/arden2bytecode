package arden.tests;

import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.runtime.MaintenanceMetadata;

public class MetadataTests {
	@Test
	public void X21() throws Exception {
		Compiler c = new Compiler();
		CompiledMlm mlm = c.compileMlm(new InputStreamReader(MetadataTests.class.getResourceAsStream("x2.1.mlm")));

		MaintenanceMetadata m = mlm.getMaintenance();
		Assert.assertEquals("Fractional excretion of sodium", m.getTitle());
		Assert.assertEquals("fractional_na", mlm.getName());
		Assert.assertEquals("2", m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.(hripcsak@cucis.cis.columbia.edu)", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals("testing", m.getValidation());

		Assert.assertEquals(3, mlm.getLibrary().getKeywords().size());
		Assert.assertEquals("fractional excretion", mlm.getLibrary().getKeywords().get(0));
		Assert.assertEquals("serum sodium", mlm.getLibrary().getKeywords().get(1));
		Assert.assertEquals("azotemia", mlm.getLibrary().getKeywords().get(2));

		Assert.assertEquals(50, mlm.getPriority(), 0);
	}
	
	@Test
	public void X23() throws Exception {
		Compiler c = new Compiler();
		CompiledMlm mlm = c.compileMlm(new InputStreamReader(MetadataTests.class.getResourceAsStream("x2.3.mlm")));

		MaintenanceMetadata m = mlm.getMaintenance();
		Assert.assertEquals("Check for penicillin allergy", m.getTitle());
		Assert.assertEquals("pen_allergy", mlm.getName());
		Assert.assertEquals("1", m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals("testing", m.getValidation());

		Assert.assertEquals(2, mlm.getLibrary().getKeywords().size());
		Assert.assertEquals("penicillin", mlm.getLibrary().getKeywords().get(0));
		Assert.assertEquals("allergy", mlm.getLibrary().getKeywords().get(1));

		Assert.assertEquals(42, mlm.getPriority(), 0);
	}
}
