package arden.compiler;

import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;

/**
 * Represents the loop variable introduced by for loops.
 * 
 * @author Daniel Grunwald
 */
final class ForLoopVariable extends Variable {
	final int variableIndex;

	public ForLoopVariable(TIdentifier name, int variableIndex) {
		super(name);
		this.variableIndex = variableIndex;
	}

	@Override
	public void loadValue(CompilerContext context, Token errorPosition) {
		context.writer.loadVariable(variableIndex);
	}
}
