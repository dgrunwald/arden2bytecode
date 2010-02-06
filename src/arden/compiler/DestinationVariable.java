package arden.compiler;

import arden.codegenerator.FieldReference;
import arden.compiler.node.TIdentifier;

final class DestinationVariable extends Variable {
	final FieldReference field;

	public DestinationVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}
}
