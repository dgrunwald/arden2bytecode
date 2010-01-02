package arden.compiler;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arden.compiler.node.*;

/**
 * Static methods that do help using the parse tree.
 * 
 * @author Daniel Grunwald
 */
final class ParseHelpers {
	/** Returns the value represented by a string literal. */
	public static String getLiteralStringValue(TStringLiteral literal) {
		String input = literal.getText();
		if (input.length() < 2 || input.charAt(0) != '"' || input.charAt(input.length() - 1) != '"')
			throw new RuntimeCompilerException(literal, "Invalid string literal");
		StringBuilder output = new StringBuilder();
		for (int i = 1; i < input.length() - 1; i++) {
			char c = input.charAt(i);
			if (c == '\r' || c == '\n') {
				// TODO: implement this feature
				// (see spec for special rules in this case)
				throw new RuntimeCompilerException(literal, "Linebreak in strings not yet supported");
			}
			if (c == '"') {
				i += 1;
				if (input.charAt(i) != '"')
					throw new RuntimeCompilerException(literal, "Invalid string literal");
			}
			output.append(c);
		}
		return output.toString();
	}
	
	public static double getLiteralDoubleValue(PNumber number) {
		String input = number.toString().replace(" ", "");
		double d;
		try {
			d = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(input).doubleValue();
		} catch (ParseException e) {
			throw new RuntimeCompilerException(e.getMessage());
		}
		if (Double.isInfinite(d) || Double.isNaN(d))
			throw new RuntimeCompilerException("Invalid number literal: " + input);
		return d;
	}

	/** Returns the list of comma-separated terms in the expression */
	public static List<PExprSort> toCommaSeparatedList(PExpr expr) {
		// expr =
		// {sort} expr_sort
		// | {exsort} expr comma expr_sort
		// | {comma} comma expr_sort;
		final ArrayList<PExprSort> output = new ArrayList<PExprSort>();
		expr.apply(new VisitorBase() {
			@Override
			public void caseASortExpr(ASortExpr node) {
				output.add(node.getExprSort());
			};

			@Override
			public void caseAExsortExpr(AExsortExpr node) {
				node.getExpr().apply(this);
				output.add(node.getExprSort());
			}

			@Override
			public void caseACommaExpr(ACommaExpr node) {
				output.add(node.getExprSort());
			}
		});
		return output;
	}
}
