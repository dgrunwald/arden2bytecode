package arden.compiler;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.BinaryOperator;
import arden.runtime.TernaryOperator;
import arden.runtime.UnaryOperator;

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
	private final ExpressionCompiler expressionCompiler;
	private final Switchable argument;
	private final CompilerContext context;

	public ComparisonCompiler(ExpressionCompiler expressionCompiler, Switchable argument) {
		this.expressionCompiler = expressionCompiler;
		this.argument = argument;
		this.context = expressionCompiler.getContext();
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
		// main_comp_op = {ucomp} unary_comp_op
		node.getUnaryCompOp().apply(this);
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
		expressionCompiler.invokeOperator(op, argument, node.getExprString());
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

		expressionCompiler.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(expressionCompiler);
		expressionCompiler.loadOperator(BinaryOperator.BEFORE);
		node.getLeft().apply(expressionCompiler);
		node.getRight().apply(expressionCompiler);
		// stack: WITHINTO, argument, BEFORE, dur, time
		context.writer.dup_x2();
		// stack: WITHINTO, argument, time, BEFORE, dur, time
		expressionCompiler.invokeLoadedBinaryOperator();
		// stack: WITHINTO, argument, time, time2
		context.writer.swap();
		// stack: WITHINTO, argument, time2, time
		expressionCompiler.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseAFolTemporalCompOp(AFolTemporalCompOp node) {
		// temporal_comp_op =
		// {fol} within [left]:expr_string following [right]:expr_string

		// <n:time> IS WITHIN <n:duration> FOLLOWING <n:time>
		// => argument IS WITHIN time TO (dur AFTER time)
		expressionCompiler.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(expressionCompiler);
		expressionCompiler.loadOperator(BinaryOperator.AFTER);
		node.getLeft().apply(expressionCompiler);
		node.getRight().apply(expressionCompiler);
		// stack: WITHINTO, argument, AFTER, dur, time
		context.writer.dup_x2();
		// stack: WITHINTO, argument, time, AFTER, dur, time
		expressionCompiler.invokeLoadedBinaryOperator();
		// stack: WITHINTO, argument, time, time2
		expressionCompiler.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseASurTemporalCompOp(ASurTemporalCompOp node) {
		// temporal_comp_op =
		// {sur} within [left]:expr_string surrounding [right]:expr_string
		expressionCompiler.invokeOperator(TernaryOperator.WITHINSURROUNDING, argument, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAWithinTemporalCompOp(AWithinTemporalCompOp node) {
		// temporal_comp_op = {within} within [lower]:expr_string to
		// [upper]:expr_string
		expressionCompiler.invokeOperator(TernaryOperator.WITHINTO, argument, node.getLower(), node.getUpper());
	}

	@Override
	public void caseAPastTemporalCompOp(APastTemporalCompOp node) {
		// temporal_comp_op = {past} within past expr_string
		// (time IS WITHIN PAST dur)
		// => (time IS WITHIN (dur BEFORE NOW) TO NOW)
		expressionCompiler.loadOperator(TernaryOperator.WITHINTO);
		argument.apply(expressionCompiler);
		expressionCompiler.loadOperator(BinaryOperator.BEFORE);
		node.getExprString().apply(expressionCompiler);
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		// Stack: WITHINTO, time, BEFORE, dur, now
		expressionCompiler.invokeLoadedBinaryOperator();
		// Stack: WITHINTO, time, starttime
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		// Stack: WITHINTO, time, starttime, now
		expressionCompiler.invokeLoadedTernaryOperator();
	}

	@Override
	public void caseASameTemporalCompOp(ASameTemporalCompOp node) {
		// temporal_comp_op = {same} within same day as expr_string
		expressionCompiler.invokeOperator(BinaryOperator.WITHINSAMEDAY, argument, node.getExprString());
	}

	@Override
	public void caseABefTemporalCompOp(ABefTemporalCompOp node) {
		// temporal_comp_op = {bef} before expr_string
		expressionCompiler.invokeOperator(BinaryOperator.ISBEFORE, argument, node.getExprString());
	}

	@Override
	public void caseAAfterTemporalCompOp(AAfterTemporalCompOp node) {
		// temporal_comp_op = {after} after expr_string
		expressionCompiler.invokeOperator(BinaryOperator.ISAFTER, argument, node.getExprString());
	}

	@Override
	public void caseAEqualTemporalCompOp(AEqualTemporalCompOp node) {
		// temporal_comp_op = {equal} equal expr_string
		expressionCompiler.invokeOperator(BinaryOperator.EQ, argument, node.getExprString());
	}

	@Override
	public void caseAAtTemporalCompOp(AAtTemporalCompOp node) {
		// temporal_comp_op = {at} at expr_string
		// OCCURRED AT is synonym for OCCURRED EQUAL
		expressionCompiler.invokeOperator(BinaryOperator.EQ, argument, node.getExprString());
	}

	// in_comp_op = in expr_string;
	@Override
	public void caseAInCompOp(AInCompOp node) {
		argument.apply(expressionCompiler);
		node.getExprString().apply(expressionCompiler);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("isIn", ArdenValue.class, ArdenValue.class));
	}

	// unary_comp_op =
	// {pres} present
	// | {null} null
	// | {bool} boolean
	// | {num} T.number
	// | {time} time
	// | {dur} duration
	// | {str} T.string
	// | {list} list
	// | {obj} object  TODO
	// | {typeof} identifier; TODO
	@Override
	public void caseAPresUnaryCompOp(APresUnaryCompOp node) {
		// is present
		expressionCompiler.loadOperator(UnaryOperator.NOT);
		expressionCompiler.invokeOperator(UnaryOperator.ISNULL, argument);
		expressionCompiler.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseANullUnaryCompOp(ANullUnaryCompOp node) {
		// is null
		expressionCompiler.invokeOperator(UnaryOperator.ISNULL, argument);
	}

	@Override
	public void caseABoolUnaryCompOp(ABoolUnaryCompOp node) {
		// is boolean
		expressionCompiler.invokeOperator(UnaryOperator.ISBOOLEAN, argument);
	}

	@Override
	public void caseANumUnaryCompOp(ANumUnaryCompOp node) {
		// is number
		expressionCompiler.invokeOperator(UnaryOperator.ISNUMBER, argument);
	}

	@Override
	public void caseATimeUnaryCompOp(ATimeUnaryCompOp node) {
		// is number
		expressionCompiler.invokeOperator(UnaryOperator.ISTIME, argument);
	}

	@Override
	public void caseADurUnaryCompOp(ADurUnaryCompOp node) {
		// is duration
		expressionCompiler.invokeOperator(UnaryOperator.ISDURATION, argument);
	}

	@Override
	public void caseAStrUnaryCompOp(AStrUnaryCompOp node) {
		// is string
		expressionCompiler.invokeOperator(UnaryOperator.ISSTRING, argument);
	}
	
	@Override
	public void caseAListUnaryCompOp(AListUnaryCompOp node) {
		// is list
		argument.apply(expressionCompiler);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("isList", ArdenValue.class));
	}
}
