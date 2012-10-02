package arden.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.Compiler;
import arden.compiler.CompiledMlm;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;
import arden.runtime.RuntimeHelpers;

public class LoadMlmFromBytecodeTests {
	private MedicalLogicModule compile(String filename) throws Exception {
		Compiler c = new Compiler();
		c.enableDebugging(filename + ".mlm");
		CompiledMlm mlm = c
				.compileMlm(new InputStreamReader(ExampleTests.class.getResourceAsStream(filename + ".mlm")));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mlm.saveClassFile(bos);
		bos.close();		
		return new CompiledMlm(new ByteArrayInputStream(bos.toByteArray()), mlm.getName());
	}
	

	@Test
	public void X21() throws Exception {
		MedicalLogicModule mlm = compile("x2.1");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X22() throws Exception {
		MedicalLogicModule mlm = compile("x2.2");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X23noAllergies() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");
		TestContext context = new TestContext();
		mlm.run(context, null);
		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X23allergies() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");
		TestContext context = new TestContext() {
			@Override
			public DatabaseQuery createQuery(String mapping) {
				Assert.assertEquals("allergy where agent_class = penicillin", mapping);
				ArdenList list = new ArdenList(new ArdenValue[] { new ArdenString("all1"), new ArdenString("all2") });
				return new MemoryQuery(new ArdenValue[] { list });
			}
		};
		mlm.run(context, null);
		Assert.assertEquals("Caution, the patient has the following allergy to penicillin documented: all2\n", context
				.getOutputText());
	}

	@Test
	public void X23allergiesButLastIsNull() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");
		TestContext context = new TestContext() {
			@Override
			public DatabaseQuery createQuery(String mapping) {
				Assert.assertEquals("allergy where agent_class = penicillin", mapping);
				ArdenList list = new ArdenList(new ArdenValue[] { new ArdenString("all1"), ArdenNull.INSTANCE });
				return new MemoryQuery(new ArdenValue[] { list });
			}
		};
		mlm.run(context, null);
		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X23urgency() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");
		Assert.assertEquals(51.0, mlm.createInstance(new TestContext(), null).getUrgency(), 0);
	}

	@Test
	public void X24() throws Exception {
		MedicalLogicModule mlm = compile("x2.4");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X25() throws Exception {
		MedicalLogicModule mlm = compile("x2.5");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals(
				"Suggest obtaining a serum creatinine to follow up on renal function in the setting of gentamicin.\n",
				context.getOutputText());
	}

	@Test
	public void X26() throws Exception {
		MedicalLogicModule mlm = compile("x2.6");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X27() throws Exception {
		MedicalLogicModule mlm = compile("x2.7");

		TestContext context = new TestContext();
		mlm.run(context, null);

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void X28() throws Exception {
		MedicalLogicModule mlm = compile("x2.8");

		TestContext context = new TestContext();
		ArdenList medOrders = new ArdenList(new ArdenValue[] { new ArdenString("order1"), new ArdenString("order2"),
				new ArdenString("order3") });
		ArdenList medAllergens = new ArdenList(new ArdenValue[] { new ArdenString("a1"), new ArdenString("a2"),
				new ArdenString("a3") });
		ArdenList patientAllergies = new ArdenList(new ArdenValue[] { new ArdenString("a2"), new ArdenString("a2"),
				new ArdenString("a1") });
		ArdenList patientReactions = new ArdenList(new ArdenValue[] { new ArdenString("r1"), new ArdenString("r2"),
				new ArdenString("r3") });
		ArdenValue[] result = mlm.run(context, new ArdenValue[] { medOrders, medAllergens, patientAllergies,
				patientReactions });
		Assert.assertEquals(3, result.length);

		Assert.assertEquals("(\"order1\",\"order2\")", result[0].toString());
		Assert.assertEquals("(\"a1\",\"a2\")", result[1].toString());
		Assert.assertEquals("(\"r3\",\"r1\",\"r2\")", result[2].toString());

		Assert.assertEquals("", context.getOutputText());
	}
	
	@Test
	public void X21Metadata() throws Exception {
		MedicalLogicModule compiledMlm = compile("x2.1");

		MaintenanceMetadata m = compiledMlm.getMaintenance();
		Assert.assertEquals("Fractional excretion of sodium", m.getTitle());
		Assert.assertEquals("fractional_na", m.getMlmName());
		Assert.assertEquals("2", m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.(hripcsak@cucis.cis.columbia.edu)", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals("testing", m.getValidation());

		LibraryMetadata l = compiledMlm.getLibrary();
		Assert.assertEquals(3, l.getKeywords().size());
		Assert.assertEquals("fractional excretion", l.getKeywords().get(0));
		Assert.assertEquals("serum sodium", l.getKeywords().get(1));
		Assert.assertEquals("azotemia", l.getKeywords().get(2));
		
		Assert.assertEquals(RuntimeHelpers.DEFAULT_PRIORITY, compiledMlm.getPriority(), 0);
		
		Assert.assertEquals(RuntimeHelpers.DEFAULT_URGENCY, compiledMlm.getUrgency(), 0);
	}
	
	@Test
	public void X23Metadata() throws Exception {
		MedicalLogicModule compiledMlm = compile("x2.3");

		MaintenanceMetadata m = compiledMlm.getMaintenance();
		Assert.assertEquals("Check for penicillin allergy", m.getTitle());
		Assert.assertEquals("pen_allergy", m.getMlmName());
		Assert.assertEquals("1", m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals("testing", m.getValidation());

		LibraryMetadata l = compiledMlm.getLibrary();
		Assert.assertEquals(2, l.getKeywords().size());
		Assert.assertEquals("penicillin", l.getKeywords().get(0));
		Assert.assertEquals("allergy", l.getKeywords().get(1));

		Assert.assertEquals(42, compiledMlm.getPriority(), 0);
		
		Assert.assertEquals(51, compiledMlm.getUrgency(), 0);
	}
}
