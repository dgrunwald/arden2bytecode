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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenTime;
import arden.runtime.MedicalLogicModule;
import arden.runtime.events.AfterEvokeEvent;
import arden.runtime.events.CyclicEvokeEvent;
import arden.runtime.events.EvokeEvent;
import arden.runtime.events.FixedDateEvokeEvent;

public class EvokeTests {
	private static Calendar calendar = null;
	
	public static ArdenTime createDate(int year, int month, int day) {
		if (calendar == null) {
			calendar = new GregorianCalendar();
		}
		calendar.clear();
		calendar.set(year, month, day);
		return new ArdenTime(calendar.getTimeInMillis());
	}
	
	public static ArdenTime createDateTime(int year, int month, int day, int hour, int minutes, int seconds) {
		if (calendar == null) {
			calendar = new GregorianCalendar();
		}
		calendar.clear();
		calendar.set(year, month, day, hour, minutes, seconds);
		return new ArdenTime(calendar.getTimeInMillis());
	}
	
	public static CompiledMlm parseTemplate(String dataCode, String evokeCode, String logicCode, String actionCode)
			throws CompilerException {
		try {
			InputStream s = EvokeTests.class.getResourceAsStream("EvokeTemplate.mlm");
			String fullCode = ActionTests.inputStreamToString(s)
					.replace("$ACTION", actionCode)
					.replace("$DATA", dataCode)
					.replace("$EVOKE", evokeCode)
					.replace("$LOGIC", logicCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static CompiledMlm parseEvoke(String evokeCode) throws CompilerException {
		return parseEvoke("", evokeCode);
	}
	
	public static CompiledMlm parseEvoke(String data, String evokeCode) throws CompilerException {
		return parseEvoke(data, evokeCode, "");
	}

	public static CompiledMlm parseEvoke(String data, String evokeCode, String actionCode) throws CompilerException {
		return parseTemplate(data, evokeCode, "conclude true;", actionCode);
	}
	
	public static TestContext createTestContext() {
		return new TestContext(createDate(1990, 0, 1)) {
			@Override
			public EvokeEvent getEvent(String mapping) {
				if (mapping.equals("penicillin storage")) {
					return new FixedDateEvokeEvent(createDate(1992, 0, 1));
				} else if (mapping.equals("cephalosporin storage")) {
					return new FixedDateEvokeEvent(createDate(1993, 0, 1));
				} else if (mapping.equals("aminoglycoside storage")) {
					return new FixedDateEvokeEvent(createDate(1994, 0, 1));
				}
				return super.getEvent(mapping);
			}
		};
	}
	
	@Test
	public void AfterFixedDateOperator() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("3 days after 1992-01-01T00:00:00");
		
		EvokeEvent e = mlm.getEvoke(context, null);
		Assert.assertEquals(createDate(1992, 0, 4), e.getNextRunTime(context));
	}

	@Test
	public void EventVariable() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("penicillin_storage := EVENT{penicillin storage}", "penicillin_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1992, 0, 1), e.getNextRunTime(context));
	}
	
	@Test
	public void AfterTimeOfEventOperator() throws Exception {
		TestContext context = createTestContext();
		
		CompiledMlm mlm = parseEvoke("event1 := EVENT{penicillin storage}", "3 days after time of event1");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1992, 0, 4), e.getNextRunTime(context));
	}

	@Test
	public void FixedDate() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("1992-03-04");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1992, 2, 4), e.getNextRunTime(context));
	}
	
	@Test
	public void FixedDateTime() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("1992-01-03T14:23:17.0");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDateTime(1992, 0, 3, 14, 23, 17), e.getNextRunTime(context));
	}	
	
	@Test
	public void OrOperator() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};" +
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "penicillin_storage OR cephalosporin_storage OR aminoglycoside_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1992, 0, 1), e.getNextRunTime(context));
	}
	
	@Test
	public void OrOperator2() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "cephalosporin_storage OR aminoglycoside_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1993, 0, 1), e.getNextRunTime(context));
	}
	
	@Test
	public void AnyOperator() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};" +
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "ANY OF (penicillin_storage, cephalosporin_storage, aminoglycoside_storage)");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1992, 0, 1), e.getNextRunTime(context));
	}
	
	@Test
	public void AnyOperator2() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "ANY OF (cephalosporin_storage, aminoglycoside_storage)");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(createDate(1993, 0, 1), e.getNextRunTime(context));
	}
	
	@Test
	public void AfterTimeOfEventOperator2() throws Exception {
		TestContext context = createTestContext();
		
		CompiledMlm mlm = parseEvoke("event1 := EVENT{test}", "3 days after time of event1");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof AfterEvokeEvent);
		Assert.assertEquals(null, e.getNextRunTime(context));
		Assert.assertEquals(createDate(1990, 0, 1), context.getCurrentTime());
		e.runOnEvent("test", context);
		Assert.assertEquals(createDate(1990, 0, 4), e.getNextRunTime(context));
	}
	
	@Test
	public void CyclicEvent() throws Exception {
		TestContext context = createTestContext();
		
		CompiledMlm mlm = parseEvoke("every 5 days for 10 years starting 5 days after 1992-03-04");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof CyclicEvokeEvent);
		Assert.assertEquals(createDate(1992, 2, 9), e.getNextRunTime(context));
		context.setCurrentTime(createDate(1992, 2, 10));
		Assert.assertEquals(createDate(1992, 2, 14), e.getNextRunTime(context));
	}
	
	@Test
	public void CyclicEventBeginningInThePast() throws Exception {
		TestContext context = createTestContext();
		
		CompiledMlm mlm = parseEvoke("every 5 days for 10 years starting 5 days after 1989-03-04");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertTrue(e instanceof CyclicEvokeEvent);
		Assert.assertEquals(createDate(1990, 0, 3), e.getNextRunTime(context));
		context.setCurrentTime(createDate(1990, 0, 4));
		Assert.assertEquals(createDate(1990, 0, 8), e.getNextRunTime(context));
	}
}
