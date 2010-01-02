package arden.runtime;

public final class ArdenTime extends ArdenValue {
	public final long value;

	public ArdenTime(long value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}

	@Override
	public String toString() {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean equals(Object obj) {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public int hashCode() {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public int compareTo(ArdenValue rhs) {
		// TODO: implement this
		throw new RuntimeException("NOT IMPLEMENTED");
	}
}
