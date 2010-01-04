package arden.compiler;

import arden.compiler.node.AAfterTemporalCompOp;
import arden.compiler.node.AAtTemporalCompOp;
import arden.compiler.node.ABcompMainCompOp;
import arden.compiler.node.ABefTemporalCompOp;
import arden.compiler.node.AEqualTemporalCompOp;
import arden.compiler.node.AFolTemporalCompOp;
import arden.compiler.node.AInCompOp;
import arden.compiler.node.AIncompMainCompOp;
import arden.compiler.node.APastTemporalCompOp;
import arden.compiler.node.APrecTemporalCompOp;
import arden.compiler.node.ARcompMainCompOp;
import arden.compiler.node.ASameTemporalCompOp;
import arden.compiler.node.ASurTemporalCompOp;
import arden.compiler.node.ATcompMainCompOp;
import arden.compiler.node.AUcompMainCompOp;
import arden.compiler.node.Node;
import arden.runtime.BinaryOperator;

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
	private final Node argument;
	private final CompilerContext context;

	public ComparisonCompiler(ExpressionCompiler parent, Node argument) {
		this.parent = parent;
		this.argument = argument;
		this.context = parent.getContext();
	}

	// main_comp_op =
	// {tcomp} temporal_comp_op
	// | {rcomp} range_comp_op
	// | {ucomp} unary_comp_op
	// | {bcomp} binary_comp_op expr_string
	// | {incomp} in_comp_op;
	@Override
	public void caseATcompMainCompOp(ATcompMainCompOp node) {
		node.getTemporalCompOp().apply(this);
	}

	@Override
	public void caseARcompMainCompOp(ARcompMainCompOp node) {
		// TODO Auto-generated method stub
		super.caseARcompMainCompOp(node);
	}

	@Override
	public void caseAUcompMainCompOp(AUcompMainCompOp node) {
		// TODO Auto-generated method stub
		super.caseAUcompMainCompOp(node);
	}

	@Override
	public void caseABcompMainCompOp(ABcompMainCompOp node) {
		// TODO Auto-generated method stub
		super.caseABcompMainCompOp(node);
	}

	@Override
	public void caseAIncompMainCompOp(AIncompMainCompOp node) {
		node.getInCompOp().apply(this);
	}

	// temporal_comp_op =
	// {prec} within [left]:expr_string preceding [right]:expr_string
	// | {fol} within [left]:expr_string following [right]:expr_string
	// | {sur} within [left]:expr_string surrounding [right]:expr_string
	// | {past} within past expr_string
	// | {same} within same day as expr_string
	// | {bef} before expr_string
	// | {after} after expr_string
	// | {equal} equal expr_string
	// | {at} at expr_string;
	@Override
	public void caseAPrecTemporalCompOp(APrecTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseAPrecTemporalCompOp(node);
	}

	@Override
	public void caseAFolTemporalCompOp(AFolTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseAFolTemporalCompOp(node);
	}

	@Override
	public void caseASurTemporalCompOp(ASurTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseASurTemporalCompOp(node);
	}

	@Override
	public void caseAPastTemporalCompOp(APastTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseAPastTemporalCompOp(node);
	}

	@Override
	public void caseASameTemporalCompOp(ASameTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseASameTemporalCompOp(node);
	}

	@Override
	public void caseABefTemporalCompOp(ABefTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseABefTemporalCompOp(node);
	}

	@Override
	public void caseAAfterTemporalCompOp(AAfterTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseAAfterTemporalCompOp(node);
	}

	@Override
	public void caseAEqualTemporalCompOp(AEqualTemporalCompOp node) {
		// temporal_comp_op = {equal} equal expr_string
		parent.invokeOperator(BinaryOperator.EQ, argument, node.getExprString());
	}

	@Override
	public void caseAAtTemporalCompOp(AAtTemporalCompOp node) {
		// TODO Auto-generated method stub
		super.caseAAtTemporalCompOp(node);
	}

	// in_comp_op = in expr_string;
	@Override
	public void caseAInCompOp(AInCompOp node) {
		// TODO Auto-generated method stub
		super.caseAInCompOp(node);
	}
}
