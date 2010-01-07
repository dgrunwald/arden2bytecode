package arden.runtime;

public final class ArdenNull extends ArdenValue {
	/** Shared instance: null without primary time. */
	public static final ArdenNull INSTANCE = new ArdenNull(NOPRIMARYTIME);

	private ArdenNull(long primaryTime) {
		super(primaryTime);
	}

	public static ArdenNull create(long primaryTime) {
		if (primaryTime == NOPRIMARYTIME)
			return INSTANCE;
		else
			return new ArdenNull(primaryTime);
	}

	@Override
	public ArdenValue[] getElements() {
		return ArdenList.EMPTY.values;
	}

	@Override
	public String toString() {
		return "null" + primaryTimeToString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ArdenNull;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
