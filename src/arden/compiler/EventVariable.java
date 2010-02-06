package arden.compiler;

import arden.compiler.node.PMappingFactor;
import arden.compiler.node.TIdentifier;

final class EventVariable extends Variable {
	final PMappingFactor mapping;

	public EventVariable(TIdentifier name, PMappingFactor mapping) {
		super(name);
		this.mapping = mapping;
	}
}