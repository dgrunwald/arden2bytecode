package arden.compiler;

import arden.codegenerator.FieldReference;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;

/**
 * A normal variable that holds an ArdenValue.
 * 
 * Variables are stored as fields inside the
 * MedicalLogicModuleImplementation-derived class.
 * 
 * @author Daniel Grunwald
 * 
 */
final class DataVariable extends Variable {
	final FieldReference field;

	public DataVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}

	@Override
	public void loadValue(CompilerContext context, Token errorPosition) {
		context.writer.loadThis();
		context.writer.loadInstanceField(field);
	}

	@Override
	public void saveValue(CompilerContext context, Token errorPosition) {
		context.writer.loadThis();
		context.writer.swap();
		context.writer.storeInstanceField(field);
	}
}
