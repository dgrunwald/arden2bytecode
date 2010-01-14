package arden.compiler;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.BinaryOperator;
import arden.runtime.TernaryOperator;

/**
 * Compiler for 'Is' operators (main_comp_op and related productions).
 * 
 * Every operator.apply(this) call will generate code that pushes the operator's
 * result value onto the evaluation stack. The parent compiler is used to
 * generate code for the specified argument. Every possible code path will emit
 * code that evaluates the argument exactly once.
 * 
 * @author Daniel Grunwald
 */
final class ComparisonCompiler extends VisitorBase {
	private final ExpressionCompiler parent;
	private final Switchable argument;
	private final CompilerContext context;

	public ComparisonCompiler(ExpressionCompiler parent, Switchable argument) {
		this.parent = parent;
		this.argument = argument;
		this.context = parent.getContext();
	}

	// main_comp_op =
	// {tcomp} temporal_comp_op
	// | {ucomp} unary_comp_op
	// | {bcomp} binary_comp_op expr_string
	// | {incomp} in_comp_op;
	@Override
	public void caseATcompMainCompOp(ATcompMainCompOp node) {
		// main_comp_op = {tcomp} temporal_comp_op
		node.getTemporalCompOp().apply(this);
	}

	@Override
	public void caseAUcompMainCompOp(AUcompMainCompOp node) {
		// TODO Auto-generated method stub
		super.caseAUcompMainCompOp(node);
	}

	@Override
	public void caseABcompMainCompOp(ABcompMainCompOp node) {
		// main_comp_op = {bcomp} binary_comp_op expr_string

		// binary_comp_op =
		// {lt} less than
		// | {gt} greater than
		// | {ge} greater than or equal
		// | {le} less than or equal;
		BinaryOperator op;
		if (node.getBinaryCompOp() instanceof ALtBinaryCompOp)
			op = BinaryOperator.LT;
		else if (node.getBinaryCompOp() instanceof AGtBinaryCompOp)
			op = BinaryOperator.GT;
		else if (node.getBinaryCompOp() instanceof AGeBinaryCompOp)
			op = BinaryOperator.GE;
		else if (node.getBinaryCompOp() instanceof ALeBinaryCompOp)
			op = BinaryOperator.LE;
		else
			throw new RuntimeException("Unknown binary_comp_op");
		parent.invokeOperator(op, argument, node.getExprString());
	}

	@Override
	public void caseAIncompMainCompOp(AIncompMainCompOp node) {
		// main_comp_op = {incomp} in_comp_op
		node.getInCompOp().apply(this);
	}

	// temporal_comp_op =
	// {prec} within [left]:expr_string preceding [right]:expr_string
	// | {fol} within [left]:expr_string following [right]:expr_string
	// | {sur} within [left]:expr_string surrounding [right]:expr_string
	// | {within} within [lower]:expr_string to [upper]:expr_string
	// | {past} within past expr_string
	// | {same} within same day as expr_string
	// | {bef} before expr_string
	// | {after} after expr_string
	// | {equal} equal expr_string
	// | {at} at expr_string;
	@Override
	public void caseAPrecTemporalCompOp(APrecTemporalCompOp node) {
		// temporal_comp_op =
		// within [left]:expr_string preceding [right]:expr_string

		// <n:time> IS WITHIN <n:duration> PRECEDING <n:time>
		// => argument IS WITHIN (dur BEFORE time) TO time

		parent.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(parent);
		parent.loadOperator(BinaryOperator.BEFORE);
		node.getLeft().apply(parent);
		node.getRight().apply(parent);
		// stack: WITHINTO, argument, BEFORE, dur, time
		context.writer.dup_x2();
		// stack: WITHINTO, argument, time, BEFORE, dur, time
		parent.invokeLoadedBinaryOperator();
		// stack: WITHINTO, argument, time, time2
		context.writer.swap();
		// stack: WITHINTO, argument, time2, time
		parent.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseAFolTemporalCompOp(AFolTemporalCompOp node) {
		// temporal_comp_op =
		// {fol} within [left]:expr_string following [right]:expr_string

		// <n:time> IS WITHIN <n:duration> FOLLOWING <n:time>
		// => argument IS WITHIN time TO (dur AFTER time)
		parent.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(parent);
		parent.loadOperator(BinaryOperator.AFTER);
		node.getLeft().apply(parent);
		node.getRight().apply(parent);
		// stack: WITHINTO, argument, AFTER, dur, time
		context.writer.dup_x2();
		// stack: WITHINTO, argument, time, AFTER, dur, time
		parent.invokeLoadedBinaryOperator();
		// stack: WITHINTO, argument, time, time2
		parent.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseASurTemporalCompOp(ASurTemporalCompOp node) {
		// temporal_comp_op =
		// {sur} within [left]:expr_string surrounding [right]:expr_string
		parent.invokeOperator(TernaryOperator.WITHINSURROUNDING, argument, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAWithinTemporalCompOp(AWithinTemporalCompOp node) {
		// temporal_comp_op = {within} within [lower]:expr_string to
		// [upper]:expr_string
		parent.invokeOperator(TernaryOperator.WITHINTO, argument, node.getLower(), node.getUpper());
	}

	@Override
	public void caseAPastTemporalCompOp(APastTemporalCompOp node) {
		// temporal_comp_op = {past} within past expr_string
		// (time IS WITHIN PAST dur)
		// => (time IS WITHIN (dur BEFORE NOW) TO NOW)
		parent.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(parent);
		parent.loadOperator(BinaryOperator.BEFORE);
		node.getExprString().apply(parent);
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		// Stack: WITHINTO, time, BEFORE, dur, now
		parent.invokeLoadedBinaryOperator();
		// Stack: WITHINTO, time, starttime
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		// Stack: WITHINTO, time, starttime, now
		parent.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseASameTemporalCompOp(ASameTemporalCompOp node) {
		// temporal_comp_op = {same} within same day as expr_string
		parent.invokeOperator(BinaryOperator.WITHINSAMEDAY, argument, node.getExprString());
	}

	@Override
	public void caseABefTemporalCompOp(ABefTemporalCompOp node) {
		// temporal_comp_op = {bef} before expr_string
		parent.invokeOperator(BinaryOperator.ISBEFORE, argument, node.getExprString());
	}

	@Override
	public void caseAAfterTemporalCompOp(AAfterTemporalCompOp node) {
		// temporal_comp_op = {after} after expr_string
		parent.invokeOperator(BinaryOperator.ISAFTER, argument, node.getExprString());
	}

	@Override
	public void caseAEqualTemporalCompOp(AEqualTemporalCompOp node) {
		// temporal_comp_op = {equal} equal expr_string
		parent.invokeOperator(BinaryOperator.EQ, argument, node.getExprString());
	}

	@Override
	public void caseAAtTemporalCompOp(AAtTemporalCompOp node) {
		// temporal_comp_op = {at} at expr_string
		// OCCURRED AT is synonym for OCCURRED EQUAL
		parent.invokeOperator(BinaryOperator.EQ, argument, node.getExprString());
	}

	// in_comp_op = in expr_string;
	@Override
	public void caseAInCompOp(AInCompOp node) {
		argument.apply(parent);
		node.getExprString().apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("isIn", ArdenValue.class, ArdenValue.class));
	}
}
