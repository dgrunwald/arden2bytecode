package arden.compiler;

import java.util.List;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;
/**
 * Compiler for actions.
 * 
 * Every actionBlock.apply(this) call will generate code that executes the action.
 * The evaluation stack is empty between all apply calls.
 * 
 * @author Daniel Grunwald
 */
final class ActionCompiler extends VisitorBase {
	private final CompilerContext context;

	public ActionCompiler(CompilerContext context) {
		this.context = context;
	}

	// action_slot =
	// action action_block semicolons;
	@Override
	public void caseAActionSlot(AActionSlot node) {
		node.getActionBlock().apply(this);
	}

	// action_block =
	// {astmt} action_statement
	// | {ablk} action_block semicolon action_statement;
	@Override
	public void caseAAstmtActionBlock(AAstmtActionBlock node) {
		node.getActionStatement().apply(this);
	}

	@Override
	public void caseAAblkActionBlock(AAblkActionBlock node) {
		node.getActionBlock().apply(this);
		node.getActionStatement().apply(this);
	}

	// action_statement =
	// {empty}
	// | {if} if action_if_then_else2
	// | {for} for identifier in expr do action_block semicolon enddo
	// | {while} while expr do action_block semicolon enddo
	// | {call} call_phrase
	// | {cdel} call_phrase delay expr
	// | {write} write expr
	// | {wrtat} write expr at identifier
	// | {return} return expr;
	@Override
	public void caseAEmptyActionStatement(AEmptyActionStatement node) {
	}

	@Override
	public void caseAIfActionStatement(AIfActionStatement node) {
		// TODO Auto-generated method stub
		super.caseAIfActionStatement(node);
	}

	@Override
	public void caseAForActionStatement(AForActionStatement node) {
		// TODO Auto-generated method stub
		super.caseAForActionStatement(node);
	}

	@Override
	public void caseAWhileActionStatement(AWhileActionStatement node) {
		// TODO Auto-generated method stub
		super.caseAWhileActionStatement(node);
	}

	@Override
	public void caseACallActionStatement(ACallActionStatement node) {
		// TODO Auto-generated method stub
		super.caseACallActionStatement(node);
	}

	@Override
	public void caseACdelActionStatement(ACdelActionStatement node) {
		// TODO Auto-generated method stub
		super.caseACdelActionStatement(node);
	}

	@Override
	public void caseAWriteActionStatement(AWriteActionStatement node) {
		// action_statement = {write} write expr
		context.writer.loadVariable(context.executionContextVariable);
		node.getExpr().apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ExecutionContextMethods.write);
	}

	@Override
	public void caseAWrtatActionStatement(AWrtatActionStatement node) {
		// TODO Auto-generated method stub
		super.caseAWrtatActionStatement(node);
	}

	@Override
	public void caseAReturnActionStatement(AReturnActionStatement node) {
		// action_statement = {return} return expr;

		// Special case: RETURN a, b; does not return a list, but multiple
		// values.
		// "RETURN (a, b);" and "RETURN a, b;" are not equivalent!
		List<PExprSort> returnExpressions = ParseHelpers.toCommaSeparatedList(node.getExpr());
		ExpressionCompiler c = new ExpressionCompiler(context);
		context.writer.loadIntegerConstant(returnExpressions.size());
		context.writer.newArray(ArdenValue.class);
		for (int i = 0; i < returnExpressions.size(); i++) {
			context.writer.dup();
			context.writer.loadIntegerConstant(i);
			returnExpressions.get(i).apply(c);
			context.writer.storeObjectToArray();
		}
		context.writer.returnObjectFromFunction();
	}
}
