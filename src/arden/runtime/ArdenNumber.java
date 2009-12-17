package arden.runtime;

public final class ArdenNumber extends ArdenValue {
	public final double value;
	
	public ArdenNumber(double value) {
		this.value = value;
	}
	
	public ArdenNumber(double value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}
}
