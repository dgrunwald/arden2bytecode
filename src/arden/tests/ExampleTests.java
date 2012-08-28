// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.tests;

import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;

public class ExampleTests {
	private MedicalLogicModule compile(String filename) throws Exception {
		Compiler c = new Compiler();
		c.enableDebugging(filename + ".mlm");
		CompiledMlm mlm = c
				.compileMlm(new InputStreamReader(ExampleTests.class.getResourceAsStream(filename + ".mlm")));
		/*FileOutputStream fos = new FileOutputStream(filename + ".class");
		mlm.saveClassFile(fos);
		fos.close();*/
		return mlm;
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
}
