package arden.runtime;

/**
 * This class is used to implement operators of the form '<n:type> := <n:type>
 * op <n:type>'
 */
public abstract class BinaryOperator {
	private final String name;

	public abstract ArdenValue runElement(ArdenValue lhs, ArdenValue rhs);

	public static final BinaryOperator OR = new BinaryOperator("OR") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs.isTrue() || rhs.isTrue())
				return ArdenBoolean.create(true, newTime);
			if (lhs.isFalse() && rhs.isFalse())
				return ArdenBoolean.create(false, newTime);
			return ArdenNull.create(newTime);
		};
	};

	public static final BinaryOperator AND = new BinaryOperator("AND") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs.isTrue() && rhs.isTrue())
				return ArdenBoolean.create(true, newTime);
			if (lhs.isFalse() || rhs.isFalse())
				return ArdenBoolean.create(false, newTime);
			return ArdenNull.create(newTime);
		};
	};

	public static final BinaryOperator EQ = new BinaryOperator("EQ") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNull || rhs instanceof ArdenNull)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(lhs.equals(rhs), newTime);
		};
	};

	public static final BinaryOperator DIV = new NumericBinaryOperator("DIV") {
		@Override
		public double runNumeric(double lhs, double rhs) {
			return lhs / rhs;
		};
	};

	public BinaryOperator(String name) {
		this.name = name;
	}

	/** Helper method for handling of primary times */
	protected static final long combinePrimaryTime(long time1, long time2) {
		if (time1 == time2)
			return time1;
		else
			return ArdenValue.NOPRIMARYTIME;
	}

	/** Implements the list logic for running the operator. */
	public final ArdenValue run(ArdenValue lhs, ArdenValue rhs) {
		if (lhs instanceof ArdenList) {
			ArdenList leftList = (ArdenList) lhs;
			if (rhs instanceof ArdenList) {
				ArdenList rightList = (ArdenList) rhs;
				if (leftList.values.length != rightList.values.length)
					return ArdenNull.INSTANCE;
				ArdenValue[] results = new ArdenValue[leftList.values.length];
				for (int i = 0; i < results.length; i++) {
					results[i] = runElement(leftList.values[i], rightList.values[i]);
				}
				return new ArdenList(results);
			} else {
				ArdenValue[] results = new ArdenValue[leftList.values.length];
				for (int i = 0; i < results.length; i++) {
					results[i] = runElement(leftList.values[i], rhs);
				}
				return new ArdenList(results);
			}
		} else {
			if (rhs instanceof ArdenList) {
				ArdenList rightList = (ArdenList) rhs;
				ArdenValue[] results = new ArdenValue[rightList.values.length];
				for (int i = 0; i < results.length; i++) {
					results[i] = runElement(lhs, rightList.values[i]);
				}
				return new ArdenList(results);
			} else {
				return runElement(lhs, rhs);
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	static abstract class NumericBinaryOperator extends BinaryOperator {
		public NumericBinaryOperator(String name) {
			super(name);
		}

		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double result = runNumeric(((ArdenNumber) lhs).value, ((ArdenNumber) rhs).value);
				if (!Double.isNaN(result) && !Double.isInfinite(result))
					return new ArdenNumber(result, newTime);
			}
			return ArdenNull.create(newTime);
		}

		public abstract double runNumeric(double lhs, double rhs);
	}
}
