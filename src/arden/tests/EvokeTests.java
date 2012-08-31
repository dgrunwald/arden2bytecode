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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.events.EvokeEvent;
import arden.runtime.events.FixedDateEvokeEvent;

import org.junit.Assert;
import org.junit.Test;

public class EvokeTests {
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
		return new TestContext(new ArdenTime(new Date(1990 - 1900, 0, 1))) {
			@Override
			public EvokeEvent getEvent(String mapping) {
				if (mapping.equals("penicillin storage")) {
					return new FixedDateEvokeEvent(new ArdenTime(new Date(1992 - 1900, 0, 1)));
				} else if (mapping.equals("cephalosporin storage")) {
					return new FixedDateEvokeEvent(new ArdenTime(new Date(1993 - 1900, 0, 1)));
				} else if (mapping.equals("aminoglycoside storage")) {
					return new FixedDateEvokeEvent(new ArdenTime(new Date(1994 - 1900, 0, 1)));
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
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 4)), e.getNextRunTime(context));
	}

	@Test
	public void EventVariable() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("penicillin_storage := EVENT{penicillin storage}", "penicillin_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 1)), e.getNextRunTime(context));
	}
	
	@Test
	public void AfterTimeOfEventOperator() throws Exception {
		TestContext context = createTestContext();
		
		CompiledMlm mlm = parseEvoke("event1 := EVENT{penicillin storage}", "3 days after time of event1");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 4)), e.getNextRunTime(context));
	}

	@Test
	public void FixedDate() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("1992-03-04");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 2, 4)), e.getNextRunTime(context));
	}
	
	@Test
	public void FixedDateTime() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke("1992-01-03T14:23:17.0");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 3, 14, 23, 17)), e.getNextRunTime(context));
	}	
	
	@Test
	public void OrOperator() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};" +
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "penicillin_storage OR cephalosporin_storage OR aminoglycoside_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 1)), e.getNextRunTime(context));
	}
	
	@Test
	public void OrOperator2() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "cephalosporin_storage OR aminoglycoside_storage");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1993 - 1900, 0, 1)), e.getNextRunTime(context));
	}
	
	@Test
	public void AnyOperator() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};" +
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "ANY OF (penicillin_storage, cephalosporin_storage, aminoglycoside_storage)");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1992 - 1900, 0, 1)), e.getNextRunTime(context));
	}
	
	@Test
	public void AnyOperator2() throws Exception {
		TestContext context = createTestContext();
		
		MedicalLogicModule mlm = parseEvoke(
				"cephalosporin_storage := EVENT{cephalosporin storage};" +
				"aminoglycoside_storage := EVENT{aminoglycoside storage};", "ANY OF (cephalosporin_storage, aminoglycoside_storage)");
		EvokeEvent e = mlm.getEvoke(context, null);
		
		Assert.assertEquals(new ArdenTime(new Date(1993 - 1900, 0, 1)), e.getNextRunTime(context));
	}
}
