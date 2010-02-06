package arden.compiler;

import java.lang.reflect.Modifier;

import arden.codegenerator.FieldReference;
import arden.compiler.node.TIdentifier;

/**
 * DESTINATION Variable.
 * 
 * An instance field of type String is stored in the MLM implementation class.
 * It is set in the data block where the 'x := DESTINATION y' statement occurs
 * and used for WRITE AT statements.
 */
final class DestinationVariable extends Variable {
	final FieldReference field;

	public DestinationVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}

	/**
	 * Gets the DestinationVariable for the LHSR, or creates it on demand.
	 */
	public static DestinationVariable getDestinationVariable(CodeGenerator codeGen, LeftHandSideResult lhs) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "DESTINATION variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
		Variable variable = codeGen.getVariable(ident.getText());
		if (variable instanceof DestinationVariable) {
			return (DestinationVariable) variable;
		} else {
			FieldReference mlmField = codeGen.createField(ident.getText(), String.class, Modifier.PRIVATE);
			DestinationVariable dv = new DestinationVariable(ident, mlmField);
			codeGen.addVariable(dv);
			return dv;
		}
	}
}
