package arden.compiler;

import arden.compiler.node.PExpr;
import arden.compiler.node.Token;

/** MLM Variables */
final class MlmVariable extends Variable {
	final String institution;

	public MlmVariable(String name, String institution) {
		super(name);
		this.institution = institution;
	}

	@Override
	public void call(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.loadStringConstant(name);
		context.writer.loadStringConstant(institution);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		if (delay != null) {
			delay.apply(new ExpressionCompiler(context));
		} else {
			context.writer.loadNull();
		}
		context.writer.invokeInstance(ExecutionContextMethods.call);
	}
}
