package arden.compiler;

import arden.compiler.node.AIdDataVarList;
import arden.compiler.node.AIdIdentifierBecomes;
import arden.compiler.node.AIdlDataVarList;
import arden.compiler.node.ALetIdentifierBecomes;
import arden.compiler.node.ALettoTimeBecomes;
import arden.compiler.node.ALtimeTimeBecomes;
import arden.compiler.node.ANowIdentifierBecomes;
import arden.compiler.node.ATimeTimeBecomes;
import arden.compiler.node.ATimeofTimeBecomes;
import arden.compiler.node.Switchable;
import arden.compiler.node.TIdentifier;

/**
 * 
 * Analyzes the left-hand side of an assignment.
 * Produces a LeftHandSideResult, a high-level representation of the syntax tree on the left hand side of expressions.
 * 
 * Supported productions: identifier_becomes, time_becomes, data_var_list
 * 
 * @author Daniel Grunwald
 * 
 */
final class LeftHandSideAnalyzer extends VisitorBase {
	private LeftHandSideResult result;

	public static LeftHandSideResult analyze(Switchable node) {
		LeftHandSideAnalyzer a = new LeftHandSideAnalyzer();
		node.apply(a);
		return a.result;
	}

	// identifier_becomes =
	// {id} identifier assign
	// | {let} let identifier be
	// | {now} now assign;
	@Override
	public void caseAIdIdentifierBecomes(AIdIdentifierBecomes node) {
		result = new LeftHandSideIdentifier(node.getIdentifier());
	}

	@Override
	public void caseALetIdentifierBecomes(ALetIdentifierBecomes node) {
		result = new LeftHandSideIdentifier(node.getIdentifier());
	}

	@Override
	public void caseANowIdentifierBecomes(ANowIdentifierBecomes node) {
		result = new LeftHandSideNow(node.getNow());
	}

	// time_becomes =
	// {timeof} time of identifier assign
	// | {time} time identifier assign
	// | {letto} let time of identifier be
	// | {ltime} let time identifier be
	@Override
	public void caseATimeofTimeBecomes(ATimeofTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseATimeTimeBecomes(ATimeTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseALettoTimeBecomes(ALettoTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseALtimeTimeBecomes(ALtimeTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	// data_var_list =
	// {id} identifier
	// | {idl} identifier comma data_var_list;
	@Override
	public void caseAIdDataVarList(AIdDataVarList node) {
		addIdentifierToList(node.getIdentifier());
	}

	@Override
	public void caseAIdlDataVarList(AIdlDataVarList node) {
		addIdentifierToList(node.getIdentifier());
		node.getDataVarList().apply(this);
	}

	private void addIdentifierToList(TIdentifier identifier) {
		if (result == null) {
			result = new LeftHandSideIdentifier(identifier);
		} else if (result instanceof LeftHandSideIdentifier) {
			LeftHandSideIdentifierList list = new LeftHandSideIdentifierList();
			list.add((LeftHandSideIdentifier) result);
			list.add(new LeftHandSideIdentifier(identifier));
			result = list;
		} else {
			((LeftHandSideIdentifierList) result).add(new LeftHandSideIdentifier(identifier));
		}
	}
}
