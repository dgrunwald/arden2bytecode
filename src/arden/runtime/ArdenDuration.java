package arden.runtime;

public final class ArdenDuration extends ArdenValue {
	public static final double SECONDS_PER_MONTH = 2629746;
	
	public final double months, seconds;
	
	public ArdenDuration(double months, double seconds, long primaryTime) {
		super(primaryTime);
		this.months = months;
		this.seconds = seconds;
	}
}