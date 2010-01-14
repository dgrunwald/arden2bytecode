package arden.compiler;

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenValue;

/**
 * Compiler for actions.
 * 
 * Every actionBlock.apply(this) call will generate code that executes the
 * action. The evaluation stack is empty between all apply calls.
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
		// action_statement = {if} if action_if_then_else2
		context.writer.sequencePoint(node.getIf().getLine());
		node.getActionIfThenElse2().apply(this);
	}

	@Override
	public void caseAForActionStatement(AForActionStatement node) {
		// action_statement =
		// {for} for identifier in expr do action_block semicolon enddo
		compileForStatement(context, node.getFor(), node.getIdentifier(), node.getExpr(), node.getActionBlock(), this);
	}

	public static void compileForStatement(CompilerContext context, TFor tFor, TIdentifier identifier,
			PExpr collectionExpr, Switchable block, Switch blockCompiler) {
		context.writer.sequencePoint(tFor.getLine());
		collectionExpr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("getElements"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		int arrayVar = context.allocateVariable();
		context.writer.storeVariable(arrayVar);
		int loopIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(loopIndexVar);

		String varName = identifier.getText();
		if (context.codeGenerator.getVariable(varName) != null)
			throw new RuntimeCompilerException(identifier, "A variable with the name '" + varName
					+ "' is already defined at this location.");

		ForLoopVariable newLoopVariable = new ForLoopVariable(identifier, context.allocateVariable());
		context.codeGenerator.addVariable(newLoopVariable);
		context.writer.defineLocalVariable(newLoopVariable.variableIndex, varName, ArdenValue.class);

		Label loopCondition = new Label();
		Label loopBody = new Label();
		context.writer.jump(loopCondition);
		context.writer.mark(loopBody);

		// loopVar = arrayVar[loopIndexVar];
		context.writer.loadVariable(arrayVar);
		context.writer.loadIntVariable(loopIndexVar);
		context.writer.loadObjectFromArray();
		context.writer.storeVariable(newLoopVariable.variableIndex);

		block.apply(blockCompiler);

		// if (loopIndexVar < arrayVar.length) goto loopBody;
		context.writer.markForwardJumpsOnly(loopCondition);
		context.writer.loadIntVariable(loopIndexVar);
		context.writer.loadVariable(arrayVar);
		context.writer.arrayLength();
		context.writer.jumpIfLessThan(loopBody);

		if (newLoopVariable != null)
			context.codeGenerator.deleteVariable(newLoopVariable);
	}

	@Override
	public void caseAWhileActionStatement(AWhileActionStatement node) {
		// action_statement = {while} while expr do action_block semicolon enddo
		compileWhileStatement(context, node.getWhile(), node.getExpr(), node.getActionBlock(), this);
	}

	public static void compileWhileStatement(CompilerContext context, TWhile tWhile, PExpr expr, Switchable block,
			Switch blockCompiler) {
		Label start = new Label();
		Label end = new Label();
		context.writer.mark(start);
		context.writer.sequencePoint(tWhile.getLine());
		expr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("isTrue"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		context.writer.jumpIfZero(end);
		block.apply(blockCompiler);
		context.writer.jump(start);
		context.writer.markForwardJumpsOnly(end);
	}

	PExpr currentCallDelay;

	@Override
	public void caseACallActionStatement(ACallActionStatement node) {
		// action_statement = {call} call_phrase
		currentCallDelay = null;
		node.getCallPhrase().apply(this);
	}

	@Override
	public void caseACdelActionStatement(ACdelActionStatement node) {
		// action_statement = {cdel} call_phrase delay expr
		currentCallDelay = node.getExpr();
		node.getCallPhrase().apply(this);
	}

	@Override
	public void caseAWriteActionStatement(AWriteActionStatement node) {
		// action_statement = {write} write expr
		context.writer.sequencePoint(node.getWrite().getLine());
		context.writer.loadVariable(context.executionContextVariable);
		node.getExpr().apply(new ExpressionCompiler(context));
		context.writer.loadNull();
		context.writer.invokeInstance(ExecutionContextMethods.write);
	}

	@Override
	public void caseAWrtatActionStatement(AWrtatActionStatement node) {
		// action_statement = {wrtat} write expr at identifier
		context.writer.sequencePoint(node.getWrite().getLine());
		context.writer.loadVariable(context.executionContextVariable);
		node.getExpr().apply(new ExpressionCompiler(context));

		Variable destination = context.codeGenerator.getVariableOrShowError(node.getIdentifier());
		if (!(destination instanceof DestinationVariable))
			throw new RuntimeCompilerException(node.getIdentifier(), "'" + node.getIdentifier().getText()
					+ "' is not a valid destination variable.");
		PMappingFactor destinationMapping = ((DestinationVariable) destination).mapping;
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(destinationMapping));
		context.writer.invokeInstance(ExecutionContextMethods.write);
	}

	@Override
	public void caseAReturnActionStatement(AReturnActionStatement node) {
		// action_statement = {return} return expr;

		context.writer.sequencePoint(node.getReturn().getLine());

		// Special case: RETURN a, b; does not return a list, but multiple
		// values.
		// "RETURN (a, b);" and "RETURN a, b;" are not equivalent!
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.returnObjectFromFunction();
	}

	// action_if_then_else2 =
	// expr then action_block semicolon action_elseif;
	@Override
	public void caseAActionIfThenElse2(AActionIfThenElse2 node) {
		compileIfStatement(context, node.getExpr(), node.getActionBlock(), node.getActionElseif(), this);
	}

	public static void compileIfStatement(CompilerContext context, PExpr expr, Switchable trueBlock,
			Switchable falseBlock, Switch blockCompiler) {
		expr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("isTrue"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		Label falseLabel = new Label();
		Label endLabel = new Label();
		context.writer.jumpIfZero(falseLabel);
		trueBlock.apply(blockCompiler);
		context.writer.markForwardJumpsOnly(falseLabel);
		falseBlock.apply(blockCompiler);
		context.writer.markForwardJumpsOnly(endLabel);
	}

	// action_elseif =
	// {end} endif
	// | {else} else action_block semicolon endif
	// | {elseif} elseif action_if_then_else2;
	@Override
	public void caseAEndActionElseif(AEndActionElseif node) {
	}

	@Override
	public void caseAElseActionElseif(AElseActionElseif node) {
		node.getActionBlock().apply(this);
	}

	@Override
	public void caseAElseifActionElseif(AElseifActionElseif node) {
		context.writer.sequencePoint(node.getElseif().getLine());
		node.getActionIfThenElse2().apply(this);
	}

	// call_phrase =
	// {id} call identifier
	// | {idex} call identifier with expr;
	@Override
	public void caseAIdCallPhrase(AIdCallPhrase node) {
		performCall(node.getCall(), node.getIdentifier(), null);
	}

	@Override
	public void caseAIdexCallPhrase(AIdexCallPhrase node) {
		performCall(node.getCall(), node.getIdentifier(), node.getExpr());
	}

	private void performCall(TCall call, TIdentifier ident, PExpr arguments) {
		Variable var = context.codeGenerator.getVariableOrShowError(ident);
		if (currentCallDelay != null) {
			var.callWithDelay(context, call, arguments, currentCallDelay);
		} else {
			var.call(context, call, arguments);
			context.writer.pop(); // remove unused return value from stack
		}
	}
}
