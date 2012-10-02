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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;

import org.junit.Assert;
import org.junit.Test;

public class ActionTests {
	public static String inputStreamToString(InputStream in) throws IOException {
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

	public static CompiledMlm parseTemplate(String dataCode, String logicCode, String actionCode)
			throws CompilerException {
		try {
			InputStream s = ActionTests.class.getResourceAsStream("ActionTemplate.mlm");
			String fullCode = inputStreamToString(s).replace("$ACTION", actionCode).replace("$DATA", dataCode).replace(
					"$LOGIC", logicCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static MedicalLogicModule parseAction(String actionCode) throws CompilerException {
		return parseTemplate("", "conclude true;", actionCode);
	}

	public static MedicalLogicModule parseAction(String data, String actionCode) throws CompilerException {
		return parseTemplate(data, "conclude true;", actionCode);
	}

	@Test
	public void SimpleWrite() throws Exception {
		TestContext context = new TestContext();
		MedicalLogicModule mlm = parseAction("write \"Hello, World\"");
		mlm.run(context, null);
		Assert.assertEquals("Hello, World\n", context.getOutputText());
	}

	@Test
	public void SimpleWriteToCustomDestination() throws Exception {
		TestContext context = new TestContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				Assert.assertEquals("email: a.b@c.de", destination);
				super.write(message, destination);
			}
		};
		MedicalLogicModule mlm = parseAction("dest := DESTINATION {email: a.b@c.de}", "write \"Hello, World\" AT dest");
		mlm.run(context, null);
		Assert.assertEquals("Hello, World\n", context.getOutputText());
	}

	@Test
	public void WriteMessageVariable() throws Exception {
		TestContext context = new TestContext();
		MedicalLogicModule mlm = parseAction("msg := MESSAGE {xyz}", "write msg");
		mlm.run(context, null);
		Assert.assertEquals("xyz\n", context.getOutputText());
	}

	@Test
	public void EmptyProgram() throws Exception {
		MedicalLogicModule mlm = parseAction("");
		ArdenValue[] result = mlm.run(new TestContext(), null);
		Assert.assertNull(result);
	}

	@Test
	public void SimpleReturn() throws Exception {
		MedicalLogicModule mlm = parseAction("return \"A\"");
		ArdenValue[] result = mlm.run(new TestContext(), null);
		Assert.assertEquals(1, result.length);
		Assert.assertEquals("A", ((ArdenString) result[0]).value);
	}

	@Test
	public void MultipleReturn() throws Exception {
		MedicalLogicModule mlm = parseAction("return \"A\", \"B\"");
		ArdenValue[] result = mlm.run(new TestContext(), null);
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("A", ((ArdenString) result[0]).value);
		Assert.assertEquals("B", ((ArdenString) result[1]).value);
	}

	@Test
	public void CallStatement() throws Exception {
		TestContext context = new TestContext() {
			@Override
			public ArdenRunnable findModule(String name, String institution) {
				Assert.assertEquals("abc", name);
				Assert.assertNull(institution);
				return new ArdenRunnable() {
					@Override
					public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
							throws InvocationTargetException {
						context.write(new ArdenString("got called!"), null);
						return new ArdenValue[0];
					}
				};
			}
		};
		MedicalLogicModule mlm = parseAction("x := MLM 'abc'", "CALL x");
		mlm.run(context, null);
		Assert.assertEquals("got called!\n", context.getOutputText());
	}

	@Test
	public void DelayCallStatement() throws Exception {
		TestContext context = new TestContext() {
			@Override
			public ArdenRunnable findModule(String name, String institution) {
				Assert.assertEquals("abc", name);
				Assert.assertNull(institution);
				return new ArdenRunnable() {
					@Override
					public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
							throws InvocationTargetException {
						throw new RuntimeException("Unexpected call");
					}
				};
			}

			@Override
			public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
				Assert.assertNotNull(mlm);
				Assert.assertNull(arguments);
				write(new ArdenString("delaycall with " + delay.toString()), null);
			}
		};
		MedicalLogicModule mlm = parseAction("x := MLM 'abc'", "CALL x DELAY 1 day");
		mlm.run(context, null);
		Assert.assertEquals("delaycall with 1 day\n", context.getOutputText());
	}

	@Test
	public void AssignmentInActionSlot() throws Exception {
		MedicalLogicModule mlm = parseAction("a := \"Hello, World!\"; return a;");
		ArdenValue[] result = mlm.run(new TestContext(), null);
		Assert.assertEquals(1, result.length);
		Assert.assertEquals("Hello, World!", ((ArdenString) result[0]).value);
	}
}
