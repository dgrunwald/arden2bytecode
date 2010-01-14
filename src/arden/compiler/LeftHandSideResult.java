package arden.compiler;

import java.lang.reflect.Modifier;

import arden.codegenerator.FieldReference;
import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.TNow;
import arden.compiler.node.Token;
import arden.runtime.ArdenValue;

/**
 * Represents the result of a LeftHandSideAnalyzer-run.
 * 
 * @author Daniel Grunwald
 * 
 */
abstract class LeftHandSideResult {
	public abstract Token getPosition();

	public abstract void assign(CompilerContext context, PExpr expr);
}

final class LeftHandSideIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}

	@Override
	public void assign(CompilerContext context, PExpr expr) {
		context.writer.sequencePoint(identifier.getLine());
		Variable v = context.codeGenerator.getVariable(identifier.getText());
		if (v == null) {
			FieldReference f = context.codeGenerator.createInitializedField(identifier.getText(), Modifier.PRIVATE);
			v = new DataVariable(identifier, f);
			context.codeGenerator.addVariable(v);
		}
		expr.apply(new ExpressionCompiler(context));
		v.saveValue(context, identifier);
	};
}

final class LeftHandSideTimeOfIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideTimeOfIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}

	@Override
	public void assign(CompilerContext context, PExpr expr) {
		context.writer.sequencePoint(identifier.getLine());
		Variable v = context.codeGenerator.getVariableOrShowError(identifier);
		v.loadValue(context, identifier);
		expr.apply(new ExpressionCompiler(context));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("changeTime", ArdenValue.class, ArdenValue.class));
		v.saveValue(context, identifier);
	}
}

final class LeftHandSideNow extends LeftHandSideResult {
	private final TNow now;

	public LeftHandSideNow(TNow now) {
		this.now = now;
	}

	@Override
	public Token getPosition() {
		return now;
	}

	@Override
	public void assign(CompilerContext context, PExpr expr) {
		context.writer.sequencePoint(now.getLine());
		context.writer.loadThis();
		expr.apply(new ExpressionCompiler(context));
		context.writer.storeInstanceField(context.codeGenerator.getNowField());
	}
}

// class LeftHandSideTimeOfIdentifier extends LeftHandSideResult {

// }
