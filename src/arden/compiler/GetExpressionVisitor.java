package arden.compiler;

import arden.compiler.analysis.AnalysisAdapter;
import arden.compiler.node.*;

/**
 * Retrieves the first concrete expression node.
 * 
 * @author Daniel Grunwald
 * 
 */
final class GetExpressionVisitor extends AnalysisAdapter {
	Node result;

	@Override
	public void defaultCase(Node node) {
		result = node;
	}

	@Override
	public void caseASortExpr(ASortExpr node) {
		node.getExprSort().apply(this);
	}

	@Override
	public void caseAWhereExprSort(AWhereExprSort node) {
		node.getExprWhere().apply(this);
	}

	@Override
	public void caseARangeExprWhere(ARangeExprWhere node) {
		node.getExprRange().apply(this);
	}

	@Override
	public void caseAOrExprRange(AOrExprRange node) {
		node.getExprOr().apply(this);
	}

	@Override
	public void caseAAndExprOr(AAndExprOr node) {
		node.getExprAnd().apply(this);
	}

	@Override
	public void caseANotExprAnd(ANotExprAnd node) {
		node.getExprNot().apply(this);
	}

	@Override
	public void caseACompExprNot(ACompExprNot node) {
		node.getExprComparison().apply(this);
	}

	@Override
	public void caseAStrExprComparison(AStrExprComparison node) {
		node.getExprString().apply(this);
	}

	@Override
	public void caseAPlusExprString(APlusExprString node) {
		node.getExprPlus().apply(this);
	}

	@Override
	public void caseATimesExprPlus(ATimesExprPlus node) {
		node.getExprTimes().apply(this);
	}

	@Override
	public void caseAPowerExprTimes(APowerExprTimes node) {
		node.getExprPower().apply(this);
	}

	@Override
	public void caseABeforeExprPower(ABeforeExprPower node) {
		node.getExprBefore().apply(this);
	}

	@Override
	public void caseAAgoExprBefore(AAgoExprBefore node) {
		node.getExprAgo().apply(this);
	}

	@Override
	public void caseAFuncExprAgo(AFuncExprAgo node) {
		node.getExprFunction().apply(this);
	}

	@Override
	public void caseAExprExprFunction(AExprExprFunction node) {
		node.getExprFactor().apply(this);
	}

	@Override
	public void caseAExpfExprFactor(AExpfExprFactor node) {
		node.getExprFactorAtom().apply(this);
	}
}
