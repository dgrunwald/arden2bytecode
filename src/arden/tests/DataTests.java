package arden.tests;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;

public class DataTests {
	private ArdenValue eval(String data, String logic, String action, ExecutionContext context) throws Exception {
		MedicalLogicModule mlm = ActionTests.parseTemplate(data, logic, action);
		ArdenValue[] result = mlm.run(context, null);
		Assert.assertEquals(1, result.length);
		return result[0];
	}

	@Test
	public void SimpleRead() throws Exception {
		final ArdenValue[] potassiumQueryResults = new ArdenValue[] { new ArdenList(new ArdenValue[] {
				new ArdenString("a"), new ArdenString("b") }) };
		ArdenValue result = eval("var1 := READ {select potassium from results where specimen = 'serum'};",
				"CONCLUDE true;", "return var1;", new TestContext() {
					@Override
					public DatabaseQuery createQuery(String mapping) {
						Assert.assertEquals("select potassium from results where specimen = 'serum'", mapping);
						return new DatabaseQuery() {
							@Override
							public ArdenValue[] execute() {
								return potassiumQueryResults;
							}
						};
					}
				});
		Assert.assertEquals("(\"a\",\"b\")", result.toString());
	}

	@Test
	public void ReadLast() throws Exception {
		final ArdenValue[] potassiumQueryResults = new ArdenValue[] { new ArdenList(new ArdenValue[] {
				new ArdenString("a"), new ArdenString("b") }) };
		ArdenValue result = eval("var1 := READ last {select potassium from results};", "CONCLUDE true;",
				"return var1;", new TestContext() {
					@Override
					public DatabaseQuery createQuery(String mapping) {
						Assert.assertEquals("select potassium from results", mapping);
						return new DatabaseQuery() {
							int state = 0;

							@Override
							public ArdenValue[] execute() {
								Assert.assertEquals(1, state);
								return new MemoryQuery(potassiumQueryResults).last().execute();
							}

							@Override
							public DatabaseQuery last() {
								Assert.assertEquals(0, state);
								state = 1;
								return this;
							}
						};
					}
				});
		Assert.assertEquals("\"b\"", result.toString());
	}

	@Test
	public void ReadFirst3WithConstraint() throws Exception {
		final ArdenValue[] potassiumQueryResults = new ArdenValue[] { new ArdenList(new ArdenValue[] {
				new ArdenString("a", -1), new ArdenString("b", 86400000), new ArdenString("c", -2),
				new ArdenString("d", 86400001), new ArdenString("e", -3), new ArdenString("f", -4) }) };

		ArdenValue result = eval(
				"LET var1 BE READ FIRST 3 FROM {select potassium from results} WHERE it occurred BEFORE 1970-01-02;",
				"CONCLUDE true;", "return var1;", new TestContext() {
					@Override
					public DatabaseQuery createQuery(String mapping) {
						Assert.assertEquals("select potassium from results", mapping);
						return new DatabaseQuery() {
							int state = 0;

							@Override
							public ArdenValue[] execute() {
								Assert.assertEquals(2, state);
								return new MemoryQuery(potassiumQueryResults).occursBefore(new ArdenTime(0)).first(3)
										.execute();
							}

							@Override
							public DatabaseQuery occursBefore(ArdenTime time) {
								Assert.assertEquals("1970-01-02T00:00:00", time.toString());
								Assert.assertEquals(0, state);
								state = 1;
								return this;
							}

							@Override
							public DatabaseQuery first(int numberOfElements) {
								Assert.assertEquals(1, state);
								state = 2;
								return this;
							}
						};
					}
				});
		Assert.assertEquals("(\"a\",\"c\",\"e\")", result.toString());
	}

	@Test
	public void ChooseMlmOrInterfaceToCall() throws Exception {
		ArdenValue val = eval("if false then x := MLM 'xtest'; else x := INTERFACE {ytest}; endif; data1 := CALL x;",
				"conclude true;", "return data1;", new TestContext() {
					@Override
					public ArdenRunnable findModule(String name, String institution) {
						Assert.assertEquals("xtest", name);
						Assert.assertNull(institution);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								throw new RuntimeException("unexpected call");
							}
						};
					}

					@Override
					public ArdenRunnable findInterface(String mapping) {
						Assert.assertEquals("ytest", mapping);
						return new ArdenRunnable() {
							@Override
							public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments)
									throws InvocationTargetException {
								return new ArdenValue[] { ArdenNumber.create(42, 0) };
							}
						};
					}
				});
		Assert.assertEquals("42", val.toString());
	}

	@Test
	public void WriteToChosenDestination() throws Exception {
		TestContext context = new TestContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				Assert.assertEquals("b", destination);
				super.write(message, destination);
			}
		};
		MedicalLogicModule mlm = ActionTests.parseAction(
				"if false then dest := DESTINATION {a}; else dest := DESTINATION {b}; endif;",
				"write \"Hello, World\" AT dest");
		mlm.run(context, null);
		Assert.assertEquals("Hello, World\n", context.getOutputText());
	}
}
