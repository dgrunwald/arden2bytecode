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

	public static final UnaryOperator SQRT = new UnaryOperator("SQRT") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenNumber.create(Math.sqrt(((ArdenNumber) val).value), val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator YEARS = new UnaryOperator("YEARS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.months(12 * ((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator MONTHS = new UnaryOperator("MONTHS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.months(((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator WEEKS = new UnaryOperator("WEEKS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.seconds(604800 * ((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator DAYS = new UnaryOperator("DAYS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.seconds(86400 * ((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator HOURS = new UnaryOperator("HOURS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.seconds(3600 * ((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator MINUTES = new UnaryOperator("MINUTES") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.seconds(60 * ((ArdenNumber) val).value, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator SECONDS = new UnaryOperator("SECONDS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenDuration.seconds(((ArdenNumber) val).value, val.primaryTime);
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
}
