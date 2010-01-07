package arden.compiler;

final class DestinationVariable extends Variable {
	final String mapping;

	public DestinationVariable(String name, String mapping) {
		super(name);
		this.mapping = mapping;
	}
}
