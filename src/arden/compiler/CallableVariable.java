package arden.compiler;

import java.lang.reflect.Modifier;

import arden.codegenerator.FieldReference;
import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

/**
 * MLM or INTERFACE Variables.
 * 
 * An instance field of type ArdenRunnable is stored in the MLM implementation
 * class. It is set in the data block where the 'x := MLM y' statement occurs
 * and used for CALL statements.
 */
final class CallableVariable extends Variable {
	// instance field of type ArdenRunnable
	final FieldReference mlmField;

	public CallableVariable(TIdentifier varName, FieldReference mlmField) {
		super(varName);
		this.mlmField = mlmField;
	}

	/** Gets the CallableVariable for the LHSR, or creates it on demand. */
	public static CallableVariable getCallableVariable(CodeGenerator codeGen, LeftHandSideResult lhs) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(),
					"MLM or INTERFACE variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
		Variable variable = codeGen.getVariable(ident.getText());
		if (variable instanceof CallableVariable) {
			return (CallableVariable) variable;
		} else {
			FieldReference mlmField = codeGen.createField(ident.getText(), ArdenRunnable.class, Modifier.PRIVATE);
			CallableVariable cv = new CallableVariable(ident, mlmField);
			codeGen.addVariable(cv);
			return cv;
		}
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
