package arden.runtime;

public abstract class ArdenValue {
	public static final long NOPRIMARYTIME = Long.MIN_VALUE;
	public final long primaryTime;

	protected ArdenValue() {
		this.primaryTime = NOPRIMARYTIME;
	}

	protected ArdenValue(long primaryTime) {
		this.primaryTime = primaryTime;
	}

	/**
	 * Returns whether this value is 'true' in a boolean context. Returns 'true'
	 * for boolean true; false for everything else (even for the list ",true")
	 */
	public boolean isTrue() {
		return false;
	}

	/**
	 * Returns whether this value is 'false' in a boolean context. Returns
	 * 'true' for boolean false; returns 'false' for everything else (boolean
	 * true or null)
	 */
	public boolean isFalse() {
		return false;
	}

	protected final String primaryTimeToString() {
		if (primaryTime == NOPRIMARYTIME)
			return "";
		else
			return " (time=" + new ArdenTime(primaryTime, NOPRIMARYTIME).toString() + ")";
	}
}
