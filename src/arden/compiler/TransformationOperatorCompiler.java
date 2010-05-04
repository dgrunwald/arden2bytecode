// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.compiler;

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenValue;

/**
 * Compiles transformation operators (from_of_func_op and from_func_op
 * productions). example: "LAST number FROM sourceList"
 * 
 * Every operator.apply(this) call will generate code that pushes the operator's
 * result value onto the evaluation stack. The parent compiler is used to
 * generate code for the specified argument. Every possible code path will emit
 * code that evaluates the numberArgument and sourceListArgument exactly once.
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
		handleTransformationOperator("indexMinimum", true);
	}

	@Override
	public void caseAMinFromOfFuncOp(AMinFromOfFuncOp node) {
		handleTransformationOperator("indexMinimum", true);
	}

	@Override
	public void caseAMaxiFromOfFuncOp(AMaxiFromOfFuncOp node) {
		handleTransformationOperator("indexMaximum", true);
	}

	@Override
	public void caseAMaxFromOfFuncOp(AMaxFromOfFuncOp node) {
		handleTransformationOperator("indexMaximum", true);
	}

	@Override
	public void caseALastFromOfFuncOp(ALastFromOfFuncOp node) {
		handleTransformationOperator("last", false);
	}

	@Override
	public void caseAFirFromOfFuncOp(AFirFromOfFuncOp node) {
		handleTransformationOperator("first", false);
	}

	@Override
	public void caseAEarFromOfFuncOp(AEarFromOfFuncOp node) {
		handleTransformationOperator("indexEarliest", true);
	}

	@Override
	public void caseALatFromOfFuncOp(ALatFromOfFuncOp node) {
		handleTransformationOperator("indexLatest", true);
	}

	private void handleTransformationOperator(String name, boolean followedByElementAt) {
		numberArgument.apply(parent);
		sourceListArgument.apply(parent);
		if (followedByElementAt)
			context.writer.dup_x1();
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
		context.writer.invokeStatic(ExpressionCompiler.getMethod(name, ArdenValue.class, int.class));
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
		if (followedByElementAt)
			context.writer.invokeStatic(ExpressionCompiler.getMethod("elementAt", ArdenValue.class, ArdenValue.class));
	}

	// index_from_of_func_op =
	// {minimum} index minimum
	// | {indexmin} index min
	// | {maximum} index maximum
	// | {indexmax} index max
	// | {earliest} index earliest
	// | {latest} index latest;
	@Override
	public void caseAMinimumIndexFromOfFuncOp(AMinimumIndexFromOfFuncOp node) {
		handleTransformationOperator("indexMinimum", false);
	}

	@Override
	public void caseAIndexminIndexFromOfFuncOp(AIndexminIndexFromOfFuncOp node) {
		handleTransformationOperator("indexMinimum", false);
	}

	@Override
	public void caseAMaximumIndexFromOfFuncOp(AMaximumIndexFromOfFuncOp node) {
		handleTransformationOperator("indexMaximum", false);
	}

	@Override
	public void caseAIndexmaxIndexFromOfFuncOp(AIndexmaxIndexFromOfFuncOp node) {
		handleTransformationOperator("indexMaximum", false);
	}

	@Override
	public void caseAEarliestIndexFromOfFuncOp(AEarliestIndexFromOfFuncOp node) {
		handleTransformationOperator("indexEarliest", false);
	}

	@Override
	public void caseALatestIndexFromOfFuncOp(ALatestIndexFromOfFuncOp node) {
		handleTransformationOperator("indexLatest", false);
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
