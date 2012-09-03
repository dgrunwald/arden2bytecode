package arden.runtime;

public class ComparableArdenTime extends ArdenValue implements Comparable<ComparableArdenTime> {

	public final long value;
	
	public ComparableArdenTime(long value) {
		this.value = value;
	}
	
	public ComparableArdenTime(long value, long primaryTime) {
		super(primaryTime);
		this.value = value;
	}
	
	public ComparableArdenTime(ArdenTime ardenTime) {
		this(ardenTime.value);
	}
	
	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		return new ComparableArdenTime(0, newPrimaryTime);
	}

	@Override
	public int compareTo(ComparableArdenTime o) {
		if (value < o.value) {
			return -1;
		} else if (value > o.value) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof ComparableArdenTime) && ((ComparableArdenTime)o).value == value;
	}

	@Override
	public int hashCode() {
		return new Long(value).hashCode();
	}
	
	public ArdenTime toArdenTime() {
		return new ArdenTime(value);
	}
}
