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

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenObject;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;

public class LogicTests {
	private ArdenValue eval(String data, String logic, String action, ExecutionContext context) throws Exception {
		MedicalLogicModule mlm = ActionTests.parseTemplate(data, logic, action);
		ArdenValue[] result = mlm.run(context, null);
		Assert.assertEquals(1, result.length);
		return result[0];
	}

	@Test
	public void WhileLoop() throws Exception {
		ArdenValue sum = eval("", "i := 0; isum := 0;\n" + "while i <= 100 do\n" + "   isum := isum + i; i := i + 1;\n"
				+ "enddo; conclude true;", "return isum;", new TestContext());
		Assert.assertEquals("5050", sum.toString());
	}

	@Test
	public void ForLoop() throws Exception {
		ArdenValue sum = eval("", "isum := 0;\n" + "for i in 1 seqto 100 do\n" + "   isum := isum + i;\n"
				+ "enddo; conclude true;", "return isum;", new TestContext());
		Assert.assertEquals("5050", sum.toString());
	}

	@Test
	public void UninitializedVar() throws Exception {
		ArdenValue sum = eval("", "if false then isum := 0; endif; conclude true;", "return isum;", new TestContext());
		Assert.assertEquals("null", sum.toString());
	}

	@Test
	public void SetTime() throws Exception {
		ArdenNumber num = (ArdenNumber) eval("data1 := 0", "TIME data1 := 2010-02-04; conclude true;", "return data1;",
				new TestContext());
		Assert.assertEquals("0", num.toString());
		Assert.assertEquals("2010-02-04T00:00:00", new ArdenTime(num.primaryTime, 0).toString());
	}

	@Test
	public void SetTimeOnList() throws Exception {
		ArdenList list = (ArdenList) eval("data1 := (1,\"abc\",null)", "TIME data1 := 2010-02-05; conclude true;",
				"return data1;", new TestContext());
		Assert.assertEquals("(1,\"abc\",null)", list.toString());
		Assert.assertEquals("2010-02-05T00:00:00", new ArdenTime(list.values[0].primaryTime, 0).toString());
		Assert.assertEquals("2010-02-05T00:00:00", new ArdenTime(list.values[1].primaryTime, 0).toString());
		Assert.assertEquals("2010-02-05T00:00:00", new ArdenTime(list.values[2].primaryTime, 0).toString());
	}

	@Test
	public void CallMlm() throws Exception {
		ArdenValue val = eval("x := MLM 'xtest'", "data1 := CALL x WITH 1,(2,3),4; conclude true;", "return data1;",
				new TestContext() {
					@Override
					public ArdenRunnable findModule(String name, String institution) {
						Assert.assertEquals("xtest", name);
						Assert.assertNull(institution);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								Assert.assertEquals(3, arguments.length);
								Assert.assertEquals("1", arguments[0].toString());
								Assert.assertEquals("(2,3)", arguments[1].toString());
								Assert.assertEquals("4", arguments[2].toString());
								return new ArdenValue[] { ArdenNumber.create(42, 0) };
							}
						};
					}
				});
		Assert.assertEquals("42", val.toString());
	}

	@Test
	public void CallMlmWithMultipleReturn() throws Exception {
		ArdenValue val = eval("x := MLM 'ytest' FROM INSTITUTION \"abc\"",
				"(data1, data2, data3) := CALL x WITH 1,(2,3),4; conclude true;",
				"return (COUNT data1, data2, data3);", new TestContext() {
					@Override
					public ArdenRunnable findModule(String name, String institution) {
						Assert.assertEquals("ytest", name);
						Assert.assertEquals("abc", institution);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								Assert.assertEquals(3, arguments.length);
								Assert.assertEquals("1", arguments[0].toString());
								Assert.assertEquals("(2,3)", arguments[1].toString());
								Assert.assertEquals("4", arguments[2].toString());
								return new ArdenValue[] { ArdenList.EMPTY, new ArdenString("hello") };
							}
						};
					}
				});
		Assert.assertEquals("(0,\"hello\",null)", val.toString());
	}

	@Test
	public void CallInterface() throws Exception {
		ArdenValue val = eval("x := INTERFACE {ytest}", "data1 := CALL x WITH 1,(2,3),4; conclude true;",
				"return data1;", new TestContext() {
					@Override
					public ArdenRunnable findInterface(String mapping) {
						Assert.assertEquals("ytest", mapping);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								Assert.assertEquals(3, arguments.length);
								Assert.assertEquals("1", arguments[0].toString());
								Assert.assertEquals("(2,3)", arguments[1].toString());
								Assert.assertEquals("4", arguments[2].toString());
								return new ArdenValue[] { ArdenNumber.create(42, 0) };
							}
						};
					}
				});
		Assert.assertEquals("42", val.toString());
	}

	@Test
	public void CallInterfaceWithMultipleReturn() throws Exception {
		ArdenValue val = eval("x := INTERFACE {ytest}",
				"(data1, data2, data3) := CALL x WITH 1,(2,3),4; conclude true;",
				"return (COUNT data1, data2, data3);", new TestContext() {
					@Override
					public ArdenRunnable findInterface(String mapping) {
						Assert.assertEquals("ytest", mapping);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								Assert.assertEquals(3, arguments.length);
								Assert.assertEquals("1", arguments[0].toString());
								Assert.assertEquals("(2,3)", arguments[1].toString());
								Assert.assertEquals("4", arguments[2].toString());
								return new ArdenValue[] { ArdenList.EMPTY, new ArdenString("hello") };
							}
						};
					}
				});
		Assert.assertEquals("(0,\"hello\",null)", val.toString());
	}

	@Test
	public void RecursiveMlm() throws Exception {
		CompiledMlm mlm = ActionTests.parseTemplate(
				"this := MLM mlm_self; arg := ARGUMENT;",
				"If arg > 1 THEN"
				+ "  result := CALL this WITH (arg-1);"
				+ "ELSE"
				+ "  result := 1;"
				+ "ENDIF; conclude true;",
				"return result * arg;");
		ArdenValue[] result = mlm.run(new TestContext(), new ArdenValue[] { new ArdenNumber(10) });
		Assert.assertEquals(1, result.length);
		Assert.assertEquals("3628800", result[0].toString());
	}

	@Test
	public void NewObject() throws Exception {
		ArdenObject obj = (ArdenObject) eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose with \"Ampicillin\", (500, 700), \"Active\"; conclude true;",
				"return dose;", new TestContext());
		Assert.assertEquals("MedicationDose := OBJECT [ Medication, Dose, Status ]", obj.type.toString());
		Assert.assertEquals("NEW MedicationDose WITH \"Ampicillin\", (500,700), \"Active\"", obj.toString());
	}

	@Test
	public void NewObjectWithoutInitialization() throws Exception {
		ArdenObject obj = (ArdenObject) eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose; conclude true;", "return dose;", new TestContext());
		Assert.assertEquals("MedicationDose := OBJECT [ Medication, Dose, Status ]", obj.type.toString());
		Assert.assertEquals("NEW MedicationDose WITH null, null, null", obj.toString());
	}

	@Test
	public void NewObjectWithTooMuchInitialization() throws Exception {
		ArdenObject obj = (ArdenObject) eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose WITH 1, 2, 3, 4; conclude true;", "return dose;", new TestContext());
		Assert.assertEquals("MedicationDose := OBJECT [ Medication, Dose, Status ]", obj.type.toString());
		Assert.assertEquals("NEW MedicationDose WITH 1, 2, 3", obj.toString());
	}

	@Test
	public void AttributeAssignment() throws Exception {
		ArdenObject obj = (ArdenObject) eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW Rectangle; rect.aLeft := 0; rect.aTop := 0; rect.aWidth := 10; rect.aHeight := 20; "
						+ " conclude true;", "return rect;", new TestContext());
		Assert.assertEquals("Rectangle := OBJECT [ aLeft, aTop, aWidth, aHeight ]", obj.type.toString());
		Assert.assertEquals("NEW Rectangle WITH 0, 0, 10, 20", obj.toString());
	}
	
	@Test
	public void AttributeAssignmentWithLet() throws Exception {
		ArdenObject obj = (ArdenObject) eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW Rectangle; LET rect.aLeft BE 0; LET rect.aTop BE 0; LET rect.aWidth BE 10; LET rect.aHeight BE 20; "
						+ " conclude true;", "return rect;", new TestContext());
		Assert.assertEquals("Rectangle := OBJECT [ aLeft, aTop, aWidth, aHeight ]", obj.type.toString());
		Assert.assertEquals("NEW Rectangle WITH 0, 0, 10, 20", obj.toString());
	}

	@Test
	public void ReferenceIdentity() throws Exception {
		ArdenObject obj = (ArdenObject) eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW RectanGle; rect2 := rect; rect.aTop := 10; " + " conclude true;", "return rect2;",
				new TestContext());
		Assert.assertEquals("Rectangle := OBJECT [ aLeft, aTop, aWidth, aHeight ]", obj.type.toString());
		Assert.assertEquals("NEW Rectangle WITH null, 10, null, null", obj.toString());
	}

	@Test
	public void InvalidAttributeAssignment() throws Exception {
		ArdenObject obj = (ArdenObject) eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW Rectangle; rect.aLeft := 0; rect.Invalid := 20; " + " conclude true;", "return rect;",
				new TestContext());
		Assert.assertEquals("Rectangle := OBJECT [ aLeft, aTop, aWidth, aHeight ]", obj.type.toString());
		Assert.assertEquals("NEW Rectangle WITH 0, null, null, null", obj.toString());
	}

	@Test
	public void GetInvalidAttribute() throws Exception {
		ArdenValue val = eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW Rectangle WITH 1,2,3,4; " + " conclude true;", "return rect.invalid;", new TestContext());
		Assert.assertEquals("null", val.toString());
	}

	@Test
	public void GetWidthAttribute() throws Exception {
		ArdenValue val = eval("Rectangle := Object [ aLeft, aTop, aWidth, aHeight ];",
				"rect := NEW Rectangle WITH 1,2,3,4; " + " conclude true;", "return rect.awidth;", new TestContext());
		Assert.assertEquals("3", val.toString());
	}

	@Test
	public void ExtractAttributeNames() throws Exception {
		ArdenValue val = eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose; conclude true;", "return EXTRACT ATTRIBUTE NAMES dose;", new TestContext());
		Assert.assertEquals("(\"Medication\",\"Dose\",\"Status\")", val.toString());
	}

	@Test
	public void AttributeFrom() throws Exception {
		ArdenValue val = eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose WITH 1,2,3;"
						+ "attrNames := (\"Medication\",\"Status\",\"Dose\"); conclude true;",
				"return ATTRIBUTE attrNames FROM dose;", new TestContext());
		Assert.assertEquals("(1,3,2)", val.toString());
	}

	@Test
	public void IsObject() throws Exception {
		ArdenValue val = eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose WITH 1,2,3; conclude true;", "return (dose IS OBJECT, 1 IS OBJECT);",
				new TestContext());
		Assert.assertEquals("(true,false)", val.toString());
	}

	@Test
	public void IsObjectType() throws Exception {
		ArdenValue val = eval("MedicationDose := OBJECT [ Medication, Dose, Status ];",
				"dose := NEW MedicationDose WITH 1,2,3; conclude true;",
				"return (dose IS MedicationDose, 1 IS MedicationDose);", new TestContext());
		Assert.assertEquals("(true,false)", val.toString());
	}
}
