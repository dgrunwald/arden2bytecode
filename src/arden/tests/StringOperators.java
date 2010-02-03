package arden.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenString;

public class StringOperators extends ExpressionTestBase {
	private void assertEvalString(String expectedString, String code) throws CompilerException,
			InvocationTargetException {
		ArdenString s = (ArdenString) evalExpression(code);
		Assert.assertEquals(expectedString, s.value);
	}

	@Test
	public void QuotationTest() throws Exception {
		assertEvalString("this string has one quotation mark: \" ", "\"this string has one quotation mark: \"\" \"");
	}

	@Test
	public void StringWithTwoSpaces() throws Exception {
		assertEvalString("test  string", "\"test  string\"");
	}

	@Test
	public void StringWithLineBreak() throws Exception {
		assertEvalString("test string", "\"test\nstring\"");
	}

	@Test
	public void StringWithLineBreakAndWhiteSpace() throws Exception {
		assertEvalString("test string", "\"test \n\tstring\"");
	}

	@Test
	public void EmptyString() throws Exception {
		assertEvalString("", "\"\"");
	}

	@Test
	public void StringWithMultiLineBreak() throws Exception {
		assertEvalString("test\nstring", "\"test  \n  \t \r\n  string\"");
	}

	@Test
	public void StringConcat() throws Exception {
		assertEvalString("ab", "\"a\" || \"b\"");
		assertEvalString("null3", "null || 3");
		assertEvalString("45", "4 || 5");
		assertEvalString("4.7four", "4.7 || \"four\"");
		assertEvalString("true", "true || \"\"");
		assertEvalString("3 days left", "3 days || \" left\"");
		assertEvalString("on 1990-03-15T13:45:01", "\"on \" || 1990-03-15T13:45:01");
		assertEvalString("list=(1,2,3)", "\"list=\" || (1,2,3)");
	}

	@Test
	public void FormattedWith() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		assertEvalString("01::02::03", "(1,2,3.3) formatted with \"%2.2d::%2.2d::%2.2d\"");
		assertEvalString("The result was 10.61 mg", "10.60528 formatted with \"The result was %.2f mg\"");
		assertEvalString("The date was Jan 10, 1998", "1998-01-10T17:25:00 formatted with \"The date was %.2t\"");
		assertEvalString("The year was 1998", "1998-01-10T17:25:00 formatted with \"The year was %.0t\"");
		assertEvalString("ten, twenty, thirty or more",
				"(\"ten\", \"twenty\", \"thirty\") formatted with \"%s, %s, %s or more\"");

		assertEvalString("12345678", "12345678 formatted with \"%i\"");
		assertEvalString("aBc", "66 formatted with \"a%cc\"");
		assertEvalString("  1%", "1 formatted with \"%3d%%\"");
		assertEvalString("001%", "1 formatted with \"%03d%%\"");
		assertEvalString("0042", "(4,42) formatted with \"%0*d\"");
		assertEvalString("1  ", "1 formatted with \"%-3i\"");
		assertEvalString("+1 ", "1 formatted with \"%-+3i\"");
		assertEvalString(" ab", "\"abc\" formatted with \"%3.2s\"");
	}

	@Test
	public void StringOperator() throws Exception {
		assertEvalString("abc", "STRING (\"a\", \"b\", \"c\")");
		assertEvalString("abc", "STRING (\"a\", \"bc\")");
		assertEvalString("", "STRING ()");
		assertEvalString("edcba", "STRING REVERSE EXTRACT CHARACTERS \"abcde\"");
	}

	@Test
	public void MatchesPattern() throws Exception {
		assertEval("true", "\"fatal heart attack\" MATCHES PATTERN \"%heart%\"");
		assertEval("false", "\"fatal heart attack\" MATCHES PATTERN \"heart\"");
		assertEval("true", "\"abnormal values\" MATCHES PATTERN \"%value_\"");
		assertEval("false", "\"fatal pneumonia\" MATCHES PATTERN \"%pulmonary%\"");
		assertEval("(true,false)",
				"(\"stunned myocardium\", \"myocardial infarction\") MATCHES PATTERN \"%myocardium\"");
		assertEval("true", "\"5%\" MATCHES PATTERN \"_\\%\"");
	}

	@Test
	public void Length() throws Exception {
		assertEval("7", "LENGTH OF \"Example\"");
		assertEval("14", "LENGTH \"Example String\"");
		assertEval("0", "LENGTH \"\"");
		assertEval("null", "LENGTH ()");
		assertEval("null", "LENGTH OF null");
		assertEval("(8,3,null)", "LENGTH OF (\"Negative\", \"Pos\", 2)");
	}

	@Test
	public void Uppercase() throws Exception {
		assertEvalString("EXAMPLE STRING", "UPPERCASE \"Example String\"");
		assertEvalString("", "UPPERCASE \"\"");
		assertEval("null", "UPPERCASE null");
		assertEval("null", "UPPERCASE ()");
		assertEval("(\"5-HIAA\",\"POS\",null)", "UPPERCASE (\"5-Hiaa\",\"Pos\",2)");
	}

	@Test
	public void Lowercase() throws Exception {
		assertEvalString("example string", "LOWERCASE \"Example String\"");
		assertEvalString("", "LOWERCASE \"\"");
		assertEval("null", "LOWERCASE 12.8");
		assertEval("null", "LOWERCASE ()");
		assertEval("(\"5-hiaa\",\"pos\",null)", "LOWERCASE (\"5-Hiaa\",\"Pos\",2)");
	}

	@Test
	public void Trim() throws Exception {
		assertEvalString("example", "TRIM \"  example  \"");
		assertEvalString("", "TRIM \"\"");
		assertEval("null", "TRIM ()");
		assertEval("null", "TRIM LEFT ()");
		assertEval("null", "TRIM RIGHT ()");
		assertEvalString("result:  ", "TRIM LEFT \"  result:  \"");
		assertEvalString("  result:", "TRIM RIGHT \"  result:  \"");
		assertEval("(\"5 N\",\"2 E\",null)", "TRIM (\" 5 N\", \"2 E \", 2)");
		assertEval("(\"5 N\",\"2 E \",null)", "TRIM LEFT (\" 5 N\", \"2 E \", 2)");
		assertEval("(\" 5 N\",\"2 E\",null)", "TRIM RIGHT (\" 5 N\", \"2 E \", 2)");
	}

	@Test
	public void FindInString() throws Exception {
		assertEval("3", "FIND \"a\" IN STRING \"Example Here\"");
		assertEval("5", "FIND \"ple\" IN STRING \"Example Here\"");
		assertEval("0", "FIND \"s\" IN STRING \"Example Here\"");
		assertEval("null", "FIND 2 IN STRING \"Example Here\"");
		assertEval("null", "FIND \"a\" IN STRING 510");
		assertEval("(2,0,4)", "FIND \"t\" IN STRING (\"start\", \"meds\", \"halt\")");
		assertEval("7", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 1");
		assertEval("1", "FIND \"e\" IN STRING LOWERCASE \"Example Here\" STARTING AT 1");
		assertEval("10", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 8");
		assertEval("10", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 10");
		assertEval("12", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 11");
		assertEval("0", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 13");
		assertEval("null", "FIND \"e\" IN STRING \"Example Here\" STARTING AT 1.5");
		assertEval("null", "FIND \"e\" IN STRING \"Example Here\" STARTING AT \"x\"");
	}

	@Test
	public void Substring() throws Exception {
		assertEvalString("ab", "SUBSTRING 2 CHARACTERS FROM \"abcdefg\"");
		assertEvalString("abcdefg", "SUBSTRING 100 CHARACTERS FROM \"abcdefg\"");
		assertEvalString("def", "SUBSTRING 3 CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEvalString("defg", "SUBSTRING 20 CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEval("null", "SUBSTRING 2.3 CHARACTERS FROM \"abcdefg\"");
		assertEval("null", "SUBSTRING 2 CHARACTERS STARTING AT 4.7 FROM \"abcdefg\"");
		assertEval("null", "SUBSTRING 3 CHARACTERS STARTING AT \"c\" FROM \"abcdefg\"");
		assertEval("null", "SUBSTRING \"b\" CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEval("null", "SUBSTRING 3 CHARACTERS STARTING AT 4 FROM 281471");
		assertEvalString("d", "SUBSTRING 1 CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEvalString("d", "SUBSTRING -1 CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEvalString("bcd", "SUBSTRING -3 CHARACTERS STARTING AT 4 FROM \"abcdefg\"");
		assertEvalString("a", "SUBSTRING 1 CHARACTERS FROM \"abcdefg\"");
		assertEvalString("g", "SUBSTRING -1 CHARACTERS STARTING AT (LENGTH OF \"abcdefg\") FROM \"abcdefg\"");
		assertEval("(\"Pos\",\"Neg\",null)", "SUBSTRING 3 CHARACTERS FROM (\"Positive\",\"Negative\",2)");

		assertEvalString("fg", "SUBSTRING -2 CHARACTERS FROM \"abcdefg\"");
	}

	@Test
	public void ExtractCharacters() throws Exception {
		assertEval("(\"a\",\"b\",\"c\")", "EXTRACT CHARACTERS \"abc\"");
		assertEval("(\"a\",\"b\",\"c\")", "EXTRACT CHARACTERS (\"ab\",\"c\")");
		assertEval("()", "EXTRACT CHARACTERS ()");
		assertEval("()", "EXTRACT CHARACTERS \"\"");
		assertEval("(\"4\",\"2\")", "EXTRACT CHARACTERS 42");
		assertEvalString("edcba", "STRING REVERSE EXTRACT CHARACTERS \"abcde\"");
	}
}
