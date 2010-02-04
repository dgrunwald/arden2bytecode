package arden.compiler;

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenValue;

/**
 * Compiles transformation operators (from_of_func_op and from_func_op
 * productions). example: "LAST number FROM sourceList"
 * 
 * @author Daniel Grunwald
 * 
 */
final class TransformationOperatorCompiler extends VisitorBase {
	private final ExpressionCompiler parent;
	private final PExprFactor numberArgument;
	private final PExprFunction sourceListArgument;
	private final CompilerContext context;

	public TransformationOperatorCompiler(ExpressionCompiler parent, PExprFactor numberArgument,
			PExprFunction sourceListArgument) {
		this.parent = parent;
		this.numberArgument = numberArgument;
		this.sourceListArgument = sourceListArgument;
		this.context = parent.getContext();
	}

	// from_of_func_op =
	// {mini} minimum
	// | {min} min
	// | {maxi} maximum
	// | {max} max
	// | {last} last
	// | {fir} first
	// | {ear} earliest
	// | {lat} latest;
	@Override
	public void caseAMiniFromOfFuncOp(AMiniFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAMiniFromOfFuncOp(node);
	}

	@Override
	public void caseAMinFromOfFuncOp(AMinFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAMinFromOfFuncOp(node);
	}

	@Override
	public void caseAMaxiFromOfFuncOp(AMaxiFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAMaxiFromOfFuncOp(node);
	}

	@Override
	public void caseAMaxFromOfFuncOp(AMaxFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAMaxFromOfFuncOp(node);
	}

	@Override
	public void caseALastFromOfFuncOp(ALastFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseALastFromOfFuncOp(node);
	}

	@Override
	public void caseAFirFromOfFuncOp(AFirFromOfFuncOp node) {
		handleTransformationOperator("first");
	}

	@Override
	public void caseAEarFromOfFuncOp(AEarFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAEarFromOfFuncOp(node);
	}

	@Override
	public void caseALatFromOfFuncOp(ALatFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseALatFromOfFuncOp(node);
	}

	private void handleTransformationOperator(String name) {
		numberArgument.apply(parent);
		sourceListArgument.apply(parent);
		context.writer.swap();
		// stack: sourceList, number
		context.writer.invokeStatic(Compiler.getRuntimeHelper("getPrimitiveIntegerValue", ArdenValue.class));
		// stack: sourceList, number
		// now emit code like:
		// (number >= 0) ? ExprssionHelpers.op(sourceList, number) :
		// ArdenNull.INSTANCE;
		context.writer.dup();
		// stack: sourceList, number, number
		Label elseLabel = new Label();
		Label endLabel = new Label();
		context.writer.jumpIfNegative(elseLabel);
		// stack: sourceList, number
		context.writer.invokeStatic(ExpressionCompiler.getMethod(name, ArdenValue.class, Integer.TYPE));
		// stack: result
		context.writer.jump(endLabel);
		context.writer.markForwardJumpsOnly(elseLabel);
		// stack: sourceList, number
		context.writer.pop2();
		// stack: empty
		try {
			context.writer.loadStaticField(ArdenNull.class.getField("INSTANCE"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		// stack: null
		context.writer.markForwardJumpsOnly(endLabel);
		// stack: result or null
	}

	// from_func_op = nearest;
	@Override
	public void caseAFromFuncOp(AFromFuncOp node) {
		numberArgument.apply(parent);
		sourceListArgument.apply(parent);
		context.writer.dup_x1();
		context.writer.invokeStatic(ExpressionCompiler.getMethod("indexNearest", ArdenValue.class, ArdenValue.class));
		context.writer.invokeStatic(ExpressionCompiler.getMethod("elementAt", ArdenValue.class, ArdenValue.class));
	}

	// index_from_func_op = index nearest;
	@Override
	public void caseAIndexFromFuncOp(AIndexFromFuncOp node) {
		numberArgument.apply(parent);
		sourceListArgument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("indexNearest", ArdenValue.class, ArdenValue.class));
	}
}
