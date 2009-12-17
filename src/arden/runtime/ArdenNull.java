package arden.runtime;

public final class ArdenNull extends ArdenValue {
	/** Shared instance: null without primary time. */
	public static final ArdenNull INSTANCE = new ArdenNull(NOPRIMARYTIME);

	public ArdenNull(long primaryTime) {
		super(primaryTime);
	}
}
