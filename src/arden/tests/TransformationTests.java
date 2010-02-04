package arden.tests;

import org.junit.Test;

public class TransformationTests extends ExpressionTestBase {
	@Test
	public void minimumFrom() throws Exception {
		assertEval("(11,12)", "MINIMUM 2 FROM (11,14,13,12)");
		assertEval("(,3)", "MINIMUM 2 FROM 3");
		assertEval("null", "MINIMUM 2 FROM (3, \"asdf\")");
		assertEval("()", "MINIMUM 2 FROM ()");
		assertEval("()", "MINIMUM 0 FROM (2,3)");
		assertEval("(1,2,2)", "MINIMUM 3 FROM (3,5,1,2,4,2)");
	}

	@Test
	public void maximumFrom() throws Exception {
		assertEval("(14,13)", "MAXIMUM 2 FROM (11,14,13,12)");
		assertEval("(,3)", "MAXIMUM 2 FROM 3");
		assertEval("null", "MAXIMUM 2 FROM (3, \"asdf\")");
		assertEval("()", "MAXIMUM 2 FROM ()");
		assertEval("()", "MAXIMUM 0 FROM (1,2,3)");
		assertEval("(5,4,4)", "MAXIMUM 3 FROM (1,5,2,4,1,4)");
	}

	@Test
	public void firstFrom() throws Exception {
		assertEval("(11,14)", "FIRST 2 FROM (11,14,13,12)");
		assertEval("(,3)", "FIRST 2 FROM 3");
		assertEval("(null,1)", "FIRST 2 FROM (null,1,2,null)");
		assertEval("()", "FIRST 2 FROM ()");
	}

	@Test
	public void lastFrom() throws Exception {
		assertEval("(13,12)", "LAST 2 FROM (11,14,13,12)");
		assertEval("(,3)", "LAST 2 FROM 3");
		assertEval("(2,null)", "LAST 2 FROM (null,1,2,null)");
		assertEval("()", "LAST 2 FROM ()");
	}

	@Test
	public void increase() throws Exception {
		assertEval("(4,-2,-1)", "INCREASE (11,15,13,12)");
		assertEval("()", "INCREASE 3");
		assertEval("null", "INCREASE ()");
		assertEval("(,1 day)", "INCREASE (1990-03-01,1990-03-02)");
		assertEval("(,1 day)", "INCREASE (1 day, 2 days)");
	}

	@Test
	public void decrease() throws Exception {
		assertEval("(-4,2,1)", "DECREASE (11,15,13,12)");
		assertEval("()", "DECREASE 3");
		assertEval("null", "DECREASE ()");
		assertEval("(,-1 days)", "DECREASE (1990-03-01,1990-03-02)");
		assertEval("(,-1 days)", "DECREASE (1 day, 2 days)");
	}

	@Test
	public void percentIncrease() throws Exception {
		assertEval("\"+36.3636%,-13.3333%\"", "% INCREASE (11,15,13) FORMATTED WITH \"%+.4f%%,%+.4f%%\"");
		assertEval("()", "% INCREASE 3");
		assertEval("null", "% INCREASE ()");
		assertEval("(,100)", "PERCENT INCREASE (1 day, 2 days)");
	}

	@Test
	public void percentDecrease() throws Exception {
		assertEval("\"-36.3636%,+13.3333%\"", "% DECREASE (11,15,13) FORMATTED WITH \"%+.4f%%,%+.4f%%\"");
		assertEval("()", "% DECREASE 3");
		assertEval("null", "% DECREASE ()");
		assertEval("(,-100)", "PERCENT DECREASE (1 day, 2 days)");
	}

	@Test
	public void earliestFrom() throws Exception {
		assertEval("()", "EARLIEST 2 FROM ()");
		assertEval("null", "EARLIEST 2 FROM (1,2)");
	}

	@Test
	public void latestFrom() throws Exception {
		assertEval("()", "LATEST 2 FROM ()");
		assertEval("null", "LATEST 2 FROM (1,2)");
	}

	@Test
	public void indexMinimumFrom() throws Exception {
		assertEval("(1,4)", "INDEX MINIMUM 2 FROM (11,14,13,12)");
		assertEval("(3,4,6)", "INDEX MINIMUM 3 FROM (3,5,1,2,4,2)");
		assertEval("null", "INDEX MIN 2 FROM (3, \"asdf\")");
		assertEval("(,1)", "INDEX MINIMUM 2 FROM 3");
		assertEval("()", "INDEX MINIMUM 0 FROM (2,3)");
	}

	@Test
	public void indexMaximumFrom() throws Exception {
		assertEval("(2,3)", "INDEX MAXIMUM 2 FROM (11,14,13,12)");
		assertEval("(1,2,5)", "INDEX MAXIMUM 3 FROM (3,5,1,2,4,2)");
		assertEval("null", "INDEX MAX 2 FROM (3, \"asdf\")");
		assertEval("(,1)", "INDEX MAXIMUM 2 FROM 3");
		assertEval("()", "INDEX MAXIMUM 0 FROM (2,3)");
	}

	@Test
	public void indexEarliestFrom() throws Exception {
		assertEval("()", "EARLIEST 2 FROM ()");
		assertEval("null", "EARLIEST 2 FROM (1,2)");
	}

	@Test
	public void indexLatestFrom() throws Exception {
		assertEval("()", "LATEST 2 FROM ()");
		assertEval("null", "LATEST 2 FROM (1,2)");
	}
}
