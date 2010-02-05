package arden.runtime;

/** Unary operators of the form '<n:type> := op <n:type>' */
public abstract class UnaryOperator {
	private final String name;

	public abstract ArdenValue runElement(ArdenValue val);

	public static final UnaryOperator NOT = new UnaryOperator("NOT") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenBoolean)
				return ArdenBoolean.create(!((ArdenBoolean) val).value, val.primaryTime);
			else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public static final UnaryOperator PLUS = new UnaryOperator("PLUS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber || val instanceof ArdenDuration)
				return val;
			else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public static final UnaryOperator MINUS = new UnaryOperator("MINUS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenNumber.create(-((ArdenNumber) val).value, val.primaryTime);
			} else if (val instanceof ArdenDuration) {
				ArdenDuration dur = (ArdenDuration) val;
				return ArdenDuration.create(-dur.value, dur.isMonths, dur.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator EXTRACTSECOND = new UnaryOperator("EXTRACTSECOND") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenTime) {
				long ms = ((ArdenTime) val).value % 60000;
				return ArdenNumber.create(ms / 1000.0, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator TIME = new UnaryOperator("TIME") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val.primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			else
				return new ArdenTime(val.primaryTime, val.primaryTime);
		};
	};

	public static final UnaryOperator ISNULL = new UnaryOperator("ISNULL") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenNull, val.primaryTime);
		};
	};

	public static final UnaryOperator ISBOOLEAN = new UnaryOperator("ISBOOLEAN") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenBoolean, val.primaryTime);
		};
	};

	public static final UnaryOperator ISNUMBER = new UnaryOperator("ISNUMBER") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenNumber, val.primaryTime);
		};
	};

	public static final UnaryOperator ISTIME = new UnaryOperator("ISTIME") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenTime, val.primaryTime);
		};
	};

	public static final UnaryOperator ISDURATION = new UnaryOperator("ISDURATION") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenDuration, val.primaryTime);
		};
	};

	public static final UnaryOperator ISSTRING = new UnaryOperator("ISSTRING") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenString, val.primaryTime);
		};
	};

	public static final UnaryOperator ABS = new NumericUnaryOperator("ABS") {
		@Override
		public double runNumber(double input) {
			return Math.abs(input);
		}
	};

	public static final UnaryOperator SQRT = new NumericUnaryOperator("SQRT") {
		@Override
		public double runNumber(double input) {
			return Math.sqrt(input);
		}
	};

	public static final UnaryOperator LOG = new NumericUnaryOperator("LOG") {
		@Override
		public double runNumber(double input) {
			return Math.log(input);
		}
	};

	public static final UnaryOperator LOG10 = new NumericUnaryOperator("LOG10") {
		@Override
		public double runNumber(double input) {
			return Math.log10(input);
		}
	};

	public static final UnaryOperator ARCCOS = new NumericUnaryOperator("ARCCOS") {
		@Override
		public double runNumber(double input) {
			return Math.acos(input);
		}
	};

	public static final UnaryOperator ARCSIN = new NumericUnaryOperator("ARCSIN") {
		@Override
		public double runNumber(double input) {
			return Math.asin(input);
		}
	};

	public static final UnaryOperator ARCTAN = new NumericUnaryOperator("ARCTAN") {
		@Override
		public double runNumber(double input) {
			return Math.atan(input);
		}
	};

	public static final UnaryOperator COS = new NumericUnaryOperator("COS") {
		@Override
		public double runNumber(double input) {
			return Math.cos(input);
		}
	};

	public static final UnaryOperator SIN = new NumericUnaryOperator("SIN") {
		@Override
		public double runNumber(double input) {
			return Math.sin(input);
		}
	};

	public static final UnaryOperator TAN = new NumericUnaryOperator("TAN") {
		@Override
		public double runNumber(double input) {
			return Math.tan(input);
		}
	};

	public static final UnaryOperator EXP = new NumericUnaryOperator("EXP") {
		@Override
		public double runNumber(double input) {
			return Math.exp(input);
		}
	};

	public static final UnaryOperator FLOOR = new NumericUnaryOperator("FLOOR") {
		@Override
		public double runNumber(double input) {
			return Math.floor(input);
		}
	};

	public static final UnaryOperator CEILING = new NumericUnaryOperator("CEILING") {
		@Override
		public double runNumber(double input) {
			return Math.ceil(input);
		}
	};

	public static final UnaryOperator ROUND = new NumericUnaryOperator("ROUND") {
		@Override
		public double runNumber(double input) {
			return input < 0 ? Math.ceil(input - 0.5) : Math.floor(input + 0.5);
		}
	};

	public static final UnaryOperator TRUNCATE = new NumericUnaryOperator("TRUNCATE") {
		@Override
		public double runNumber(double input) {
			return input < 0 ? Math.ceil(input) : Math.floor(input);
		}
	};

	public static final UnaryOperator ASNUMBER = new UnaryOperator("ASNUMBER") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenBoolean)
				return ArdenNumber.create(((ArdenBoolean) val).value ? 1 : 0, val.primaryTime);
			else if (val instanceof ArdenNumber)
				return val;
			else if (val instanceof ArdenString) {
				double d;
				try {
					d = Double.parseDouble(((ArdenString) val).value);
				} catch (NumberFormatException e) {
					d = Double.NaN;
				}
				return ArdenNumber.create(d, val.primaryTime);
			} else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public UnaryOperator(String name) {
		this.name = name;
	}

	/** Implements the list logic for running the operator. */
	public final ArdenValue run(ArdenValue val) {
		if (val instanceof ArdenList) {
			ArdenList inputList = (ArdenList) val;
			ArdenValue[] results = new ArdenValue[inputList.values.length];
			for (int i = 0; i < results.length; i++) {
				results[i] = runElement(inputList.values[i]);
			}
			return new ArdenList(results);
		} else {
			return runElement(val);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	private static abstract class NumericUnaryOperator extends UnaryOperator {
		public NumericUnaryOperator(String name) {
			super(name);
		}

		public abstract double runNumber(double input);

		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenNumber.create(runNumber(((ArdenNumber) val).value), val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		}
	}
}
