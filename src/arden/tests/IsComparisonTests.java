package arden.tests;

import org.junit.Test;

public class IsComparisonTests extends ExpressionTestBase {
	@Test
	public void IsEqual() throws Exception {
		assertEval("false", "1 is equal 2");
		assertEval("true", "1 is not equal 2");
	}

	@Test
	public void IsInTest() throws Exception {
		assertEval("false", "2 IS IN (4,5,6)");
		assertEval("(false, true)", "(3,4) IS IN (4,5,6)");
		assertEval("true", "null IS IN (1/0, 2)");
	}

	@Test
	public void IsNotInTest() throws Exception {
		assertEval("true", "2 IS NOT IN (4,5,6)");
		assertEval("(true, false)", "(3,4) IS NOT IN (4,5,6)");
		assertEval("false", "null IS NOT IN (1/0, 2)");
	}

	@Test
	public void InTest() throws Exception {
		assertEval("false", "2 IN (4,5,6)");
		assertEval("(false, true)", "(3,4) IN (4,5,6)");
		assertEval("true", "null IN (1/0, 2)");
	}

	@Test
	public void NotInTest() throws Exception {
		assertEval("true", "2 NOT IN (4,5,6)");
		assertEval("(true, false)", "(3,4) NOT IN (4,5,6)");
		assertEval("false", "null NOT IN (1/0, 2)");
	}
}
