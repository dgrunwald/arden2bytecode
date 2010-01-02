package arden.runtime;

/**
 * Static helper methods for ExpressionCompiler.
 * 
 * @author Daniel Grunwald
 */
public final class ExpressionHelpers {
	public static ArdenList unaryComma(ArdenValue value) {
		if (value instanceof ArdenList)
			return (ArdenList) value;
		else
			return new ArdenList(new ArdenValue[] { value });
	}

	public static ArdenList binaryComma(ArdenValue lhs, ArdenValue rhs) {
		ArdenValue[] left = unaryComma(lhs).values;
		ArdenValue[] right = unaryComma(rhs).values;
		ArdenValue[] result = new ArdenValue[left.length + right.length];
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
		return new ArdenList(result);
	}
}
