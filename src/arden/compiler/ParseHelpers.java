package arden.compiler;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import arden.compiler.node.*;
import arden.runtime.ArdenTime;

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
				// (see spec for special rules in this case)
				while (output.length() > 0 && isWhitespace(output.charAt(output.length() - 1)))
					output.deleteCharAt(output.length() - 1);
				int numLineFeed = 0;
				while (isWhitespace(c)) {
					if (c == '\n')
						numLineFeed++;
					c = input.charAt(++i);
				}
				if (numLineFeed > 1)
					output.append('\n');
				else
					output.append(' ');
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

	private static boolean isWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	public static String getStringForMapping(PMappingFactor mapping) {
		return ((AMappingFactor) mapping).getDataMapping().getText();
	}

	public static double getLiteralDoubleValue(PNumber number) {
		String input = number.toString().replace(" ", "");
		double d;
		try {
			d = Double.parseDouble(input);
		} catch (NumberFormatException e) {
			throw new RuntimeCompilerException(e.getMessage());
		}
		if (Double.isInfinite(d) || Double.isNaN(d))
			throw new RuntimeCompilerException("Invalid number literal: " + input);
		return d;
	}

	public static long parseIsoDateTime(TIsoDateTime dateTime) {
		String text = dateTime.getText();
		ParsePosition parsePos = new ParsePosition(0);
		Date date = ArdenTime.isoDateTimeFormat.parse(text, parsePos);
		if (date == null)
			throw new RuntimeCompilerException(dateTime, "Invalid DateTime literal");
		long time = date.getTime();
		int pos = parsePos.getIndex();
		if (pos < text.length() && text.charAt(pos) == '.') {
			// fractional seconds
			pos++;
			int multiplier = 100;
			while (pos < text.length()) {
				char c = text.charAt(pos);
				if (c >= '0' && c <= '9') {
					time += (c - '0') * multiplier;
					multiplier /= 10;
					pos++;
				} else {
					break;
				}
			}
		}
		if (pos != text.length())
			throw new RuntimeCompilerException(dateTime, "Invalid DateTime literal");
		return time;
	}

	public static long parseIsoDate(TIsoDate date) {
		try {
			return ArdenTime.isoDateFormat.parse(date.getText()).getTime();
		} catch (ParseException e) {
			throw new RuntimeCompilerException(e.getMessage());
		}
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
