package arden.runtime;

/**
 * This class is used to implement operators of the form '<n:type> := <n:type>
 * op <n:type>'
 */
public abstract class BinaryOperator {
	private final String name;

	/**
	 * Runs the operator for a single element (both lhs and rhs must be
	 * non-lists)
	 */
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

	public static final BinaryOperator NE = new BinaryOperator("NE") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNull || rhs instanceof ArdenNull)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(!lhs.equals(rhs), newTime);
		};
	};

	public static final BinaryOperator LT = new BinaryOperator("LT") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			int cmp = lhs.compareTo(rhs);
			if (cmp == Integer.MIN_VALUE)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(cmp < 0, newTime);
		};
	};

	public static final BinaryOperator LE = new BinaryOperator("LE") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			int cmp = lhs.compareTo(rhs);
			if (cmp == Integer.MIN_VALUE)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(cmp <= 0, newTime);
		};
	};

	public static final BinaryOperator GT = new BinaryOperator("GT") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			int cmp = lhs.compareTo(rhs);
			if (cmp == Integer.MIN_VALUE)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(cmp > 0, newTime);
		};
	};

	public static final BinaryOperator GE = new BinaryOperator("GE") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			int cmp = lhs.compareTo(rhs);
			if (cmp == Integer.MIN_VALUE)
				return ArdenNull.create(newTime);
			return ArdenBoolean.create(cmp >= 0, newTime);
		};
	};

	public static final BinaryOperator ADD = new BinaryOperator("ADD") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double newValue = ((ArdenNumber) lhs).value + ((ArdenNumber) rhs).value;
				return ArdenNumber.create(newValue, newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenDuration) {
				ArdenDuration left = (ArdenDuration) lhs;
				ArdenDuration right = (ArdenDuration) rhs;
				if (left.isMonths == right.isMonths)
					return ArdenDuration.create(left.value + right.value, left.isMonths, newTime);
				else
					return ArdenDuration.seconds(left.toSeconds() + right.toSeconds(), newTime);
			} else if (lhs instanceof ArdenTime && rhs instanceof ArdenDuration) {
				return new ArdenTime(((ArdenTime) lhs).add((ArdenDuration) rhs), newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenTime) {
				return new ArdenTime(((ArdenTime) rhs).add((ArdenDuration) lhs), newTime);
			} else {
				return ArdenNull.create(newTime);
			}
		};
	};

	public static final BinaryOperator SUB = new BinaryOperator("SUB") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double newValue = ((ArdenNumber) lhs).value - ((ArdenNumber) rhs).value;
				return ArdenNumber.create(newValue, newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenDuration) {
				ArdenDuration left = (ArdenDuration) lhs;
				ArdenDuration right = (ArdenDuration) rhs;
				if (left.isMonths == right.isMonths)
					return ArdenDuration.create(left.value - right.value, left.isMonths, newTime);
				else
					return ArdenDuration.seconds(left.toSeconds() - right.toSeconds(), newTime);
			} else if (lhs instanceof ArdenTime && rhs instanceof ArdenDuration) {
				return new ArdenTime(((ArdenTime) lhs).subtract((ArdenDuration) rhs), newTime);
			} else if (lhs instanceof ArdenTime && rhs instanceof ArdenTime) {
				long milliseconds = ((ArdenTime) lhs).value - ((ArdenTime) rhs).value;
				return ArdenDuration.seconds(milliseconds / 1000.0, newTime);
			} else {
				return ArdenNull.create(newTime);
			}
		};
	};

	public static final BinaryOperator MUL = new BinaryOperator("MUL") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double newValue = ((ArdenNumber) lhs).value * ((ArdenNumber) rhs).value;
				return ArdenNumber.create(newValue, newTime);
			} else if (lhs instanceof ArdenNumber && rhs instanceof ArdenDuration) {
				ArdenNumber left = (ArdenNumber) lhs;
				ArdenDuration right = (ArdenDuration) rhs;
				return ArdenDuration.create(left.value * right.value, right.isMonths, newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenNumber) {
				ArdenDuration left = (ArdenDuration) lhs;
				ArdenNumber right = (ArdenNumber) rhs;
				return ArdenDuration.create(left.value * right.value, left.isMonths, newTime);
			} else {
				return ArdenNull.create(newTime);
			}
		};
	};

	public static final BinaryOperator DIV = new BinaryOperator("DIV") {
		@Override
		public ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double newValue = ((ArdenNumber) lhs).value / ((ArdenNumber) rhs).value;
				return ArdenNumber.create(newValue, newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenDuration) {
				ArdenDuration left = (ArdenDuration) lhs;
				ArdenDuration right = (ArdenDuration) rhs;
				if (left.isMonths == right.isMonths)
					return ArdenNumber.create(left.value / right.value, newTime);
				else
					return ArdenNumber.create(left.toSeconds() / right.toSeconds(), newTime);
			} else if (lhs instanceof ArdenDuration && rhs instanceof ArdenNumber) {
				ArdenDuration left = (ArdenDuration) lhs;
				ArdenNumber right = (ArdenNumber) rhs;
				return ArdenDuration.create(left.value / right.value, left.isMonths, newTime);
			} else {
				return ArdenNull.create(newTime);
			}
		};
	};

	public static final BinaryOperator POW = new BinaryOperator("POW") {
		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenNumber && rhs instanceof ArdenNumber) {
				double result = Math.pow(((ArdenNumber) lhs).value, ((ArdenNumber) rhs).value);
				return ArdenNumber.create(result, newTime);
			}
			return ArdenNull.create(newTime);
		}
	};

	public static final BinaryOperator AFTER = new BinaryOperator("AFTER") {
		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenDuration && rhs instanceof ArdenTime) {
				return new ArdenTime(((ArdenTime) rhs).add((ArdenDuration) lhs), newTime);
			}
			return ArdenNull.create(newTime);
		}
	};

	public static final BinaryOperator BEFORE = new BinaryOperator("BEFORE") {
		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenDuration && rhs instanceof ArdenTime) {
				return new ArdenTime(((ArdenTime) rhs).subtract((ArdenDuration) lhs), newTime);
			}
			return ArdenNull.create(newTime);
		}
	};

	public static final BinaryOperator ISAFTER = new BinaryOperator("ISAFTER") {
		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenTime && rhs instanceof ArdenTime) {
				return ArdenBoolean.create(((ArdenTime) lhs).value > ((ArdenTime) rhs).value, newTime);
			}
			return ArdenNull.create(newTime);
		}
	};

	public static final BinaryOperator ISBEFORE = new BinaryOperator("ISBEFORE") {
		@Override
		public final ArdenValue runElement(ArdenValue lhs, ArdenValue rhs) {
			long newTime = combinePrimaryTime(lhs.primaryTime, rhs.primaryTime);
			if (lhs instanceof ArdenTime && rhs instanceof ArdenTime) {
				return ArdenBoolean.create(((ArdenTime) lhs).value < ((ArdenTime) rhs).value, newTime);
			}
			return ArdenNull.create(newTime);
		}
	};

	public BinaryOperator(String name) {
		this.name = name;
	}

	/** Helper method for handling of primary times */
	public static final long combinePrimaryTime(long time1, long time2) {
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
}
