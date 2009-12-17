package arden.runtime;

public final class ArdenString extends ArdenValue {
	public final String value;

	public ArdenString(String value) {
		this.value = value;
	}

	public ArdenString(String value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}
}
