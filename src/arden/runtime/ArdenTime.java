package arden.runtime;

public final class ArdenTime extends ArdenValue {
	public final long value;
	
	public ArdenTime(long value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}
}
