package arden.compiler;

import arden.compiler.node.PMappingFactor;
import arden.compiler.node.TIdentifier;

final class MessageVariable extends Variable {
	final PMappingFactor mapping;

	public MessageVariable(TIdentifier name, PMappingFactor mapping) {
		super(name);
		this.mapping = mapping;
	}
}
