package arden.tests;

import org.junit.Test;

public class NumericTests extends ExpressionTestBase {
	@Test
	public void arccos() throws Exception {
		assertEval("0", "ARCCOS 1");
		assertEval("\"3.14\"", "ARCCOS (-1) FORMATTED WITH \"%.2f\"");
	}

	@Test
	public void arcsin() throws Exception {
		assertEval("0", "ARCSIN 0");
		assertEval("\"0.52\"", "ARCSIN .5 FORMATTED WITH \"%.2f\"");
	}

	@Test
	public void arctan() throws Exception {
		assertEval("0", "ARCTAN 0");
		assertEval("\"0.46\"", "ARCTAN .5 FORMATTED WITH \"%.2f\"");
	}

	@Test
	public void cos() throws Exception {
		assertEval("1", "COSINE 0");
	}

	@Test
	public void sin() throws Exception {
		assertEval("0", "SINE 0");
	}

	@Test
	public void tan() throws Exception {
		assertEval("0", "TANGENT 0");
	}

	@Test
	public void exp() throws Exception {
		assertEval("1", "EXP 0");
	}

	@Test
	public void log() throws Exception {
		assertEval("0", "log 1");
		assertEval("\"2.30\"", "log 10 FORMATTED WITH \"%.2f\"");
		assertEval("null", "log 0");
	}

	@Test
	public void log10() throws Exception {
		assertEval("2", "log10 100");
	}

	@Test
	public void floor() throws Exception {
		assertEval("-2", "INT -1.5");
		assertEval("-2", "INT -2.0");
		assertEval("1", "INT (1.5)");
		assertEval("-3", "INT (-2.5)");
		assertEval("-4", "INT (-3.1)");
		assertEval("-4", "INT (-4)");

		assertEval("-2", "FLOOR -1.5");
		assertEval("-2", "FLOOR -2.0");
		assertEval("1", "FLOOR (1.5)");
		assertEval("-3", "FLOOR (-2.5)");
		assertEval("-4", "FLOOR (-3.1)");
		assertEval("-4", "FLOOR (-4)");
	}

	@Test
	public void ceiling() throws Exception {
		assertEval("-1", "CEILING -1.5");
		assertEval("-1", "CEILING -2.0");
		assertEval("1", "CEILING (1.5)");
		assertEval("-2", "CEILING (-2.5)");
		assertEval("-3", "CEILING (-3.9)");
		assertEval("-4", "CEILING (-4)");
	}

	@Test
	public void truncate() throws Exception {
		assertEval("-1", "TRUNCATE -1.5");
		assertEval("-1", "TRUNCATE -1");
		assertEval("1", "TRUNCATE (1.5)");
	}

	@Test
	public void round() throws Exception {
		assertEval("1", "ROUND 0.5");
		assertEval("3", "ROUND 3.4");
		assertEval("4", "ROUND 3.5");
		assertEval("-4", "ROUND -3.5");
		assertEval("-3", "ROUND -3.4");
		assertEval("-4", "ROUND -3.7");
	}

	@Test
	public void abs() throws Exception {
		assertEval("1.5", "ABS 1.5");
		assertEval("1.5", "ABS (-1.5)");
	}

	@Test
	public void sqrt() throws Exception {
		assertEval("2", "SQRT 4");
		assertEval("null", "SQRT -1.5");
	}

	@Test
	public void asNumber() throws Exception {
		assertEval("5", "\"5\" AS NUMBER");
		assertEval("null", "\"xyz\" AS NUMBER");
		assertEval("1", "True AS NUMBER");
		assertEval("0", "False AS NUMBER");
		assertEval("6", "6 AS NUMBER");
		assertEval("(7,8,230,4100,null,null,1,0,null,null,null)",
				"(\"7\", 8, \"2.3E+2\", 4.1E+3, \"ABC\", Null, True, False, 1997-10-31T00:00:00, now, 3 days) AS NUMBER");
		assertEval("()", "() AS NUMBER");
	}
}
