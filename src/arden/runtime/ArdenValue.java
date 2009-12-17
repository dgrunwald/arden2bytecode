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
}
