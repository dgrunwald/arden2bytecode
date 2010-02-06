package arden.compiler;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;

/**
 * Compiler for logic block.
 * 
 * Every logicBlock.apply(this) call will generate code that executes the
 * statements. The evaluation stack is empty between all apply calls.
 * 
 * @author Daniel Grunwald
 */
final class LogicCompiler extends VisitorBase {
	private final CompilerContext context;

	public LogicCompiler(CompilerContext context) {
		this.context = context;
	}

	// logic_slot = logic logic_block semicolons;
	@Override
	public void caseALogicSlot(ALogicSlot node) {
		node.getLogicBlock().apply(this);
	}

	// logic_block =
	// {lblk} logic_block semicolon logic_statement
	// | {lstmt} logic_statement;
	@Override
	public void caseALblkLogicBlock(ALblkLogicBlock node) {
		node.getLogicBlock().apply(this);
		node.getLogicStatement().apply(this);
	}

	@Override
	public void caseALstmtLogicBlock(ALstmtLogicBlock node) {
		node.getLogicStatement().apply(this);
	}

	// logic_statement =
	// {empty}
	// | {ass} logic_assignment
	// | {if} if logic_if_then_else2
	// | {for} for identifier in expr do logic_block semicolon enddo
	// | {while} while expr do logic_block semicolon enddo
	// | {conc} conclude expr;
	@Override
	public void caseAEmptyLogicStatement(AEmptyLogicStatement node) {
	}

	@Override
	public void caseAAssLogicStatement(AAssLogicStatement node) {
		node.getLogicAssignment().apply(this);
	}

	@Override
	public void caseAIfLogicStatement(AIfLogicStatement node) {
		// logic_statement = {if} if logic_if_then_else2
		context.writer.sequencePoint(node.getIf().getLine());
		node.getLogicIfThenElse2().apply(this);
	}

	@Override
	public void caseAForLogicStatement(AForLogicStatement node) {
		// logic_statement =
		// {for} for identifier in expr do logic_block semicolon enddo
		ActionCompiler.compileForStatement(context, node.getFor(), node.getIdentifier(), node.getExpr(), node
				.getLogicBlock(), this);
	}

	@Override
	public void caseAWhileLogicStatement(AWhileLogicStatement node) {
		// logic_statement = {while} while expr do logic_block semicolon enddo
		ActionCompiler.compileWhileStatement(context, node.getWhile(), node.getExpr(), node.getLogicBlock(), this);
	}

	@Override
	public void caseAConcLogicStatement(AConcLogicStatement node) {
		// logic_statement = {conc} conclude expr
		context.writer.sequencePoint(node.getConclude().getLine());
		node.getExpr().apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("isTrue"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		context.writer.returnIntFromFunction();
	}

	// logic_if_then_else2 =
	// [condition]:expr then [then_block]:logic_block [s1]:semicolon
	// [else_block]:logic_elseif;
	@Override
	public void caseALogicIfThenElse2(ALogicIfThenElse2 node) {
		ActionCompiler.compileIfStatement(context, node.getCondition(), node.getThenBlock(), node.getElseBlock(), this);
	}

	// logic_elseif =
	// {end} endif
	// | {else} else logic_block semicolon endif
	// | {elseif} elseif logic_if_then_else2;
	@Override
	public void caseAEndLogicElseif(AEndLogicElseif node) {
	}

	@Override
	public void caseAElseLogicElseif(AElseLogicElseif node) {
		node.getLogicBlock().apply(this);
	}

	@Override
	public void caseAElseifLogicElseif(AElseifLogicElseif node) {
		context.writer.sequencePoint(node.getElseif().getLine());
		node.getLogicIfThenElse2().apply(this);
	}

	// logic_assignment =
	// {idex} identifier_becomes expr
	// | {tex} time_becomes expr
	// | {icall} identifier_becomes call_phrase
	// | {lphr} l_par data_var_list r_par assign call_phrase
	// | {llphr} let l_par data_var_list r_par be call_phrase
	// | {new} identifier_becomes new_object_phrase;
	@Override
	public void caseAIdexLogicAssignment(AIdexLogicAssignment node) {
		LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes()).assign(context, node.getExpr());
	}

	@Override
	public void caseATexLogicAssignment(ATexLogicAssignment node) {
		LeftHandSideAnalyzer.analyze(node.getTimeBecomes()).assign(context, node.getExpr());
	}

	@Override
	public void caseAIcallLogicAssignment(AIcallLogicAssignment node) {
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes());
		new DataCompiler(context).assignPhrase(lhs, node.getCallPhrase());
	}

	@Override
	public void caseALphrLogicAssignment(ALphrLogicAssignment node) {
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getDataVarList());
		new DataCompiler(context).assignPhrase(lhs, node.getCallPhrase());
	}

	@Override
	public void caseALlphrLogicAssignment(ALlphrLogicAssignment node) {
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getDataVarList());
		new DataCompiler(context).assignPhrase(lhs, node.getCallPhrase());
	}

	@Override
	public void caseANewLogicAssignment(ANewLogicAssignment node) {
		LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes()).assign(context, node.getNewObjectPhrase());
	}
}
