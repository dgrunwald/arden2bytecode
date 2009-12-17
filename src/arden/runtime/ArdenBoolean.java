package arden.runtime;

public final class ArdenBoolean extends ArdenValue {
	public static final ArdenBoolean TRUE = new ArdenBoolean(true, NOPRIMARYTIME);
	public static final ArdenBoolean FALSE = new ArdenBoolean(false, NOPRIMARYTIME);

	public final boolean value;

	public ArdenBoolean(boolean value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}
}
