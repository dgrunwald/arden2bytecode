package arden.runtime;

public enum TemporalQueryOperator {
	/** OCCURS WITHIN arg1 TO arg2.
	 * 
	 * WITHIN PRECEDING, FOLLOWING, SURROUNDING, PAST and SAME DAY AS are converted to WITHIN TO.
	 */
	WithinTo,
	/** OCCURS BEFORE */
	Before,
	/** OCCURS AFTER */
	After,
	/** OCCURS AT */
	At
}
