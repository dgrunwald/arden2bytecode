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
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;
import arden.runtime.events.CyclicEvokeEvent;
import arden.runtime.events.EmptyEvokeSlot;
import arden.runtime.events.EvokeEvent;
import arden.runtime.events.FixedDateEvokeEvent;
import arden.runtime.events.MappedEvokeEvent;

public class ExampleEvokeTests {
	private MedicalLogicModule compile(String filename) throws Exception {
		Compiler c = new Compiler();
		c.enableDebugging(filename + ".mlm");
		CompiledMlm mlm = c
				.compileMlm(new InputStreamReader(ExampleEvokeTests.class.getResourceAsStream(filename + ".mlm")));
		return mlm;
	}

	@Test
	public void X21() throws Exception {
		MedicalLogicModule mlm = compile("x2.1");

		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("storage of urine electrolytes", context));
	}

	@Test
	public void X22() throws Exception {
		MedicalLogicModule mlm = compile("x2.2");

		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("'06210519','06210669'", context));
	}

	@Test
	public void X23noAllergies() throws Exception {
		MedicalLogicModule mlm = compile("x2.3");
		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("medication_order where class = penicillin", context));
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
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("medication_order where class = penicillin", context));
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
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("medication_order where class = penicillin", context));
	}

	@Test
	public void X24() throws Exception {
		MedicalLogicModule mlm = compile("x2.4");

		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("medication_order where class = gentamicin", context));
	}

	@Test
	public void X25() throws Exception {
		MedicalLogicModule mlm = compile("x2.5");
		ArdenTime defaultTime = EvokeTests.createDateTime(1980, 0, 1, 0, 0, 0); // this is the default myExecutionContext.getCurrentTime()
		ArdenTime defaultEventDate = EvokeTests.createDateTime(2000, 0, 1, 0, 0, 0); // this is the default myExecutionContext.getEvent()
		EvokeEvent defaultEvokeEvent = new FixedDateEvokeEvent(defaultEventDate);

		TestContext context = new TestContext(defaultEvokeEvent, defaultTime);
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof CyclicEvokeEvent);
		
		ArdenDuration fiveDays = (ArdenDuration)ArdenDuration.create(
				60.0 * 60 * 24 * 5, 
				false, 
				context.getCurrentTime().value); 
		ArdenTime fiveDaysLater = new ArdenTime(
				defaultEventDate.add(fiveDays)); // add 5 days as in x2.5.mlm
		// default runtime should be 5 days after the event as declared in the MLM:
		Assert.assertEquals(
				fiveDaysLater, 
				e.getNextRunTime(context));

		ArdenTime tenDaysLater = new ArdenTime(fiveDaysLater.add(fiveDays));
		ArdenDuration oneSecond = (ArdenDuration)ArdenDuration.create(1, false, context.getCurrentTime().value);
		
		context.setCurrentTime(new ArdenTime(fiveDaysLater.add(oneSecond)));
		// after the first runtime has been passed, the mlm should be re-run another 5 days later:
		Assert.assertEquals(
				tenDaysLater,
				e.getNextRunTime(context)); 
	}

	@Test
	public void X26() throws Exception {
		MedicalLogicModule mlm = compile("x2.6");

		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof MappedEvokeEvent);
		Assert.assertTrue(e.runOnEvent("STORAGE OF ABSOLUTE_NEUTROPHILE_COUNT", context));
	}

	@Test
	public void X27() throws Exception {
		MedicalLogicModule mlm = compile("x2.7");

		TestContext context = new TestContext();
		EvokeEvent e = mlm.getEvoke(context, null);

		Assert.assertTrue(e instanceof EmptyEvokeSlot);		
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

		EvokeEvent e = mlm.getEvoke(context, new ArdenValue[] { medOrders, medAllergens, patientAllergies,
				patientReactions });

		Assert.assertTrue(e instanceof EmptyEvokeSlot);	
	}
}
