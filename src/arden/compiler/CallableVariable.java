package arden.compiler;

import arden.codegenerator.FieldReference;
import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

/** MLM or INTERFACE Variables */
final class CallableVariable extends Variable {
	// instance field of type ArdenRunnable 
	final FieldReference mlmField;

	public CallableVariable(TIdentifier varName, FieldReference mlmField) {
		super(varName);
		this.mlmField = mlmField;
	}

	@Override
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadThis();
		context.writer.loadInstanceField(mlmField);
		context.writer.loadVariable(context.executionContextVariable);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		context.writer.invokeStatic(Compiler.getRuntimeHelper("call", ArdenRunnable.class, ExecutionContext.class,
				ArdenValue[].class));
	}

	@Override
	public void callWithDelay(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.loadThis();
		context.writer.loadInstanceField(mlmField);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		delay.apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ExecutionContextMethods.callWithDelay);
	}
}
