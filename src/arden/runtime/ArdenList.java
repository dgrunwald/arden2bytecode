package arden.runtime;

public final class ArdenList extends ArdenValue {
	public final static ArdenList EMPTY = new ArdenList(new ArdenValue[0]);

	public ArdenValue[] values;

	public ArdenList(ArdenValue[] values) {
		this.values = values;
	}
}
