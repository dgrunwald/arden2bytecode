package arden.runtime;

public final class ArdenBoolean extends ArdenValue {
	public static final ArdenBoolean TRUE = new ArdenBoolean(true, NOPRIMARYTIME);
	public static final ArdenBoolean FALSE = new ArdenBoolean(false, NOPRIMARYTIME);

	public final boolean value;

	private ArdenBoolean(boolean value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	public static ArdenBoolean create(boolean value, long primaryTime) {
		if (primaryTime == NOPRIMARYTIME)
			return value ? TRUE : FALSE;
		else
			return new ArdenBoolean(value, primaryTime);
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return create(value, newPrimaryTime);
	}
	
	@Override
	public boolean isTrue() {
		return value;
	}

	@Override
	public boolean isFalse() {
		return !value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArdenBoolean)
			return value == ((ArdenBoolean) obj).value;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return value ? 42 : 23;
	}
}
