package arden.compiler;

import arden.compiler.node.PExpr;
import arden.compiler.node.PMappingFactor;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;

final class InterfaceVariable extends Variable {
	public final PMappingFactor mapping;

	public InterfaceVariable(TIdentifier name, PMappingFactor mapping) {
		super(name);
		this.mapping = mapping;
	}

	@Override
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		// TODO Auto-generated method stub
		super.call(context, errorPosition, arguments);
	}
}
