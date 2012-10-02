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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenObject;
import arden.runtime.ArdenValue;
import arden.runtime.BinaryOperator;
import arden.runtime.ExpressionHelpers;
import arden.runtime.ObjectType;
import arden.runtime.TernaryOperator;
import arden.runtime.UnaryOperator;

/**
 * Compiler for expressions.
 * 
 * Every expression.apply(this) call will generate code that pushes the
 * expression's result value onto the evaluation stack.
 * 
 * @author Daniel Grunwald
 */
final class ExpressionCompiler extends VisitorBase {
	private final CompilerContext context;

	public CompilerContext getContext() {
		return context;
	}

	public ExpressionCompiler(CompilerContext context) {
		this.context = context;
	}

	public void buildArrayForCommaSeparatedExpression(PExpr expr) {
		List<PExprSort> returnExpressions = ParseHelpers.toCommaSeparatedList(expr);
		context.writer.loadIntegerConstant(returnExpressions.size());
		context.writer.newArray(ArdenValue.class);
		for (int i = 0; i < returnExpressions.size(); i++) {
			context.writer.dup();
			context.writer.loadIntegerConstant(i);
			returnExpressions.get(i).apply(this);
			context.writer.storeObjectToArray();
		}
	}

	public static Method getMethod(String name, Class<?>... parameterTypes) {
		try {
			return ExpressionHelpers.class.getMethod(name, parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void loadOperator(TernaryOperator operator) {
		try {
			Field field = TernaryOperator.class.getField(operator.toString());
			context.writer.loadStaticField(field);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeLoadedTernaryOperator() {
		try {
			Method run = TernaryOperator.class.getMethod("run", ArdenValue.class, ArdenValue.class, ArdenValue.class);
			context.writer.invokeInstance(run);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeOperator(TernaryOperator operator, Switchable arg1, Switchable arg2, Switchable arg3) {
		loadOperator(operator);
		arg1.apply(this);
		arg2.apply(this);
		arg3.apply(this);
		invokeLoadedTernaryOperator();
	}

	public void loadOperator(BinaryOperator operator) {
		try {
			Field field = BinaryOperator.class.getField(operator.toString());
			context.writer.loadStaticField(field);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeLoadedBinaryOperator() {
		try {
			Method run = BinaryOperator.class.getMethod("run", ArdenValue.class, ArdenValue.class);
			context.writer.invokeInstance(run);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeOperator(BinaryOperator operator, Switchable lhs, Switchable rhs) {
		loadOperator(operator);
		lhs.apply(this);
		rhs.apply(this);
		invokeLoadedBinaryOperator();
	}

	public void loadOperator(UnaryOperator operator) {
		try {
			Field field = UnaryOperator.class.getField(operator.toString());
			context.writer.loadStaticField(field);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeLoadedUnaryOperator() {
		try {
			Method run = UnaryOperator.class.getMethod("run", ArdenValue.class);
			context.writer.invokeInstance(run);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeOperator(UnaryOperator operator, Switchable arg) {
		loadOperator(operator);
		arg.apply(this);
		invokeLoadedUnaryOperator();
	}

	// expr =
	// {sort} expr_sort
	// | {exsort} expr comma expr_sort
	// | {comma} comma expr_sort;
	@Override
	public void caseASortExpr(ASortExpr node) {
		// expr = {sort} expr_sort
		node.getExprSort().apply(this);
	}

	@Override
	public void caseAExsortExpr(AExsortExpr node) {
		// expr = {exsort} expr comma expr_sort
		node.getExpr().apply(this);
		node.getExprSort().apply(this);
		context.writer.invokeStatic(getMethod("binaryComma", ArdenValue.class, ArdenValue.class));
	}

	@Override
	public void caseACommaExpr(ACommaExpr node) {
		// expr = {comma} comma expr_sort;
		node.getExprSort().apply(this);
		context.writer.invokeStatic(getMethod("unaryComma", ArdenValue.class));
	}

	// expr_sort =
	// {where} expr_where
	// | {merge} expr_where merge expr_sort
	// | {sort} sort data? expr_sort;
	@Override
	public void caseAWhereExprSort(AWhereExprSort node) {
		// expr_sort = {where} expr_where
		node.getExprWhere().apply(this);
	}

	@Override
	public void caseAMergeExprSort(AMergeExprSort node) {
		// expr_sort = {merge} expr_where merge expr_sort
		node.getExprWhere().apply(this);
		node.getExprSort().apply(this);
		context.writer.invokeStatic(getMethod("binaryComma", ArdenValue.class, ArdenValue.class));
		context.writer.invokeStatic(getMethod("sortByTime", ArdenValue.class));
	}

	@Override
	public void caseASortExprSort(ASortExprSort node) {
		// expr_sort = {sort} sort data? expr_sort
		GetExpressionVisitor exprVisitor = new GetExpressionVisitor();
		node.getExprSort().apply(exprVisitor);
		// if data wasn't specified, we need to check whether this is was a
		// 'sort time x' expression
		// we have to do this in code as handling it in the grammar would cause
		// a shift/reduce conflict
		if (node.getData() == null && exprVisitor.result instanceof AOfexprExprFunction) {
			AOfexprExprFunction ofExpr = (AOfexprExprFunction) exprVisitor.result;
			if (ofExpr.getOfFuncOp() instanceof AOfnrOfFuncOp) {
				AOfnrOfFuncOp ofOp = (AOfnrOfFuncOp) ofExpr.getOfFuncOp();
				if (ofOp.getOfNoreadFuncOp() instanceof ATimeOfNoreadFuncOp) {
					// found pattern 'sort time x'
					ofExpr.getExprFunction().apply(this); // evaluate 'x'
					context.writer.invokeStatic(getMethod("sortByTime", ArdenValue.class));
					return;
				}
			}
		}
		// did not find pattern, so sort by data
		exprVisitor.result.apply(this);
		context.writer.invokeStatic(getMethod("sortByData", ArdenValue.class));
	}

	// expr_where =
	// {range} expr_range
	// | {wrange} [this_range]:expr_range where [next_range]:expr_range;
	@Override
	public void caseARangeExprWhere(ARangeExprWhere node) {
		// expr_where = {range} expr_range
		node.getExprRange().apply(this);
	}

	@Override
	public void caseAWrangeExprWhere(AWrangeExprWhere node) {
		// expr_where = {wrange} [this_range]:expr_range where
		// [next_range]:expr_range
		node.getThisRange().apply(this);
		context.writer.dup();
		int it = context.allocateItVariable();
		context.writer.storeVariable(it);
		node.getNextRange().apply(this);
		context.writer.invokeStatic(getMethod("where", ArdenValue.class, ArdenValue.class));
		context.popItVariable();
	}

	// expr_range =
	// {or} expr_or
	// | {seq} [this_or]:expr_or seqto [next_or]:expr_or;
	@Override
	public void caseAOrExprRange(AOrExprRange node) {
		// expr_range = {or} expr_or
		node.getExprOr().apply(this);
	}

	@Override
	public void caseASeqExprRange(ASeqExprRange node) {
		// expr_range = {seq} [this_or]:expr_or seqto [next_or]:expr_or
		node.getThisOr().apply(this);
		node.getNextOr().apply(this);
		context.writer.invokeStatic(getMethod("seqto", ArdenValue.class, ArdenValue.class));
	}

	// expr_or =
	// {or} expr_or or expr_and
	// | {and} expr_and;
	@Override
	public void caseAOrExprOr(AOrExprOr node) {
		// expr_or = {or} expr_or or expr_and
		invokeOperator(BinaryOperator.OR, node.getExprOr(), node.getExprAnd());
	}

	@Override
	public void caseAAndExprOr(AAndExprOr node) {
		// expr_or = {and} expr_and
		node.getExprAnd().apply(this);
	}

	// expr_and =
	// {and} expr_and and expr_not
	// | {not} expr_not;
	@Override
	public void caseAAndExprAnd(AAndExprAnd node) {
		// expr_and = {and} expr_and and expr_not
		invokeOperator(BinaryOperator.AND, node.getExprAnd(), node.getExprNot());
	}

	@Override
	public void caseANotExprAnd(ANotExprAnd node) {
		// expr_and = {not} expr_not
		node.getExprNot().apply(this);
	}

	// expr_not =
	// {not} not expr_comparison
	// | {comp} expr_comparison;
	@Override
	public void caseANotExprNot(ANotExprNot node) {
		// expr_not = {not} not expr_comparison
		invokeOperator(UnaryOperator.NOT, node.getExprComparison());
	}

	@Override
	public void caseACompExprNot(ACompExprNot node) {
		// expr_not = {comp} expr_comparison
		node.getExprComparison().apply(this);
	}

	// expr_comparison =
	// {str} expr_string
	// | {find} expr_find_string
	// | {sim} [first_string]:expr_string simple_comp_op
	// [second_string]:expr_string
	// | {is} expr_string P.is main_comp_op
	// | {inot} expr_string P.is not main_comp_op
	// | {in} expr_string in_comp_op
	// | {nin} expr_string not in_comp_op
	// | {occur} expr_string P.occur temporal_comp_op
	// | {ocrnot} expr_string P.occur not temporal_comp_op
	// | {match} [first_string]:expr_string matches pattern
	// [second_string]:expr_string;
	@Override
	public void caseAStrExprComparison(AStrExprComparison node) {
		// expr_comparison = {str} expr_string
		node.getExprString().apply(this);
	}

	@Override
	public void caseAFindExprComparison(AFindExprComparison node) {
		// expr_comparison = {find} expr_find_string
		node.getExprFindString().apply(this);
	}

	@Override
	public void caseASimExprComparison(ASimExprComparison node) {
		// expr_comparison = [first_string]:expr_string simple_comp_op
		// [second_string]:expr_string
		BinaryOperator op;
		PSimpleCompOp compOp = node.getSimpleCompOp();
		if (compOp instanceof AEqSimpleCompOp || compOp instanceof AEqsSimpleCompOp)
			op = BinaryOperator.EQ;
		else if (compOp instanceof ANeSimpleCompOp || compOp instanceof ANesSimpleCompOp)
			op = BinaryOperator.NE;
		else if (compOp instanceof AGeSimpleCompOp || compOp instanceof AGesSimpleCompOp)
			op = BinaryOperator.GE;
		else if (compOp instanceof AGtSimpleCompOp || compOp instanceof AGtsSimpleCompOp)
			op = BinaryOperator.GT;
		else if (compOp instanceof ALeSimpleCompOp || compOp instanceof ALesSimpleCompOp)
			op = BinaryOperator.LE;
		else if (compOp instanceof ALtSimpleCompOp || compOp instanceof ALtsSimpleCompOp)
			op = BinaryOperator.LT;
		else
			throw new RuntimeCompilerException("Unsupported comparison operator: " + compOp.toString());
		invokeOperator(op, node.getFirstString(), node.getSecondString());
	}

	@Override
	public void caseAIsExprComparison(AIsExprComparison node) {
		// expr_comparison = {is} expr_string P.is main_comp_op
		node.getMainCompOp().apply(new ComparisonCompiler(this, node.getExprString()));
	}

	@Override
	public void caseAInotExprComparison(AInotExprComparison node) {
		// expr_comparison = {inot} expr_string P.is not main_comp_op
		loadOperator(UnaryOperator.NOT);
		node.getMainCompOp().apply(new ComparisonCompiler(this, node.getExprString()));
		invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAInExprComparison(AInExprComparison node) {
		// expr_comparison = {in} expr_string in_comp_op
		node.getInCompOp().apply(new ComparisonCompiler(this, node.getExprString()));
	}

	@Override
	public void caseANinExprComparison(ANinExprComparison node) {
		// expr_comparison = {nin} expr_string not in_comp_op
		loadOperator(UnaryOperator.NOT);
		node.getInCompOp().apply(new ComparisonCompiler(this, node.getExprString()));
		invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAOccurExprComparison(AOccurExprComparison node) {
		// expr_comparison = {occur} expr_string P.occur temporal_comp_op
		node.getTemporalCompOp().apply(new ComparisonCompiler(this, new TimeOf(node.getExprString())));
	}

	@Override
	public void caseAOcrnotExprComparison(AOcrnotExprComparison node) {
		// expr_comparison = {ocrnot} expr_string P.occur not temporal_comp_op
		loadOperator(UnaryOperator.NOT);
		node.getTemporalCompOp().apply(new ComparisonCompiler(this, new TimeOf(node.getExprString())));
		invokeLoadedUnaryOperator();
	}

	class TimeOf implements Switchable {
		final PExprString exprString;

		public TimeOf(PExprString exprString) {
			this.exprString = exprString;
		}

		@Override
		public void apply(Switch sw) {
			if (sw != ExpressionCompiler.this)
				throw new RuntimeException("unexpected switch");
			invokeOperator(UnaryOperator.TIME, exprString);
		}
	}

	@Override
	public void caseAMatchExprComparison(AMatchExprComparison node) {
		// expr_comparison = {match} [first_string]:expr_string matches pattern
		// [second_string]:expr_string;
		node.getFirstString().apply(this);
		node.getSecondString().apply(this);
		context.writer.invokeStatic(getMethod("matchesPattern", ArdenValue.class, ArdenValue.class));
	}

	// expr_string =
	// {plus} expr_plus
	// | {or} expr_string logor expr_plus
	// | {form} expr_string formatted with string_literal
	// | {trim} trim trim_option? expr_plus
	// | {sub} substring [charcount]:expr_plus characters substring_start? from
	// [inputstr]:expr_plus;
	@Override
	public void caseAPlusExprString(APlusExprString node) {
		// expr_string = {plus} expr_plus
		node.getExprPlus().apply(this);
	}

	@Override
	public void caseAOrExprString(AOrExprString node) {
		// expr_string = expr_string logor expr_plus
		node.getExprString().apply(this);
		node.getExprPlus().apply(this);
		context.writer.invokeStatic(getMethod("concat", ArdenValue.class, ArdenValue.class));
	}

	@Override
	public void caseAFormExprString(AFormExprString node) {
		// expr_string formatted with string_literal
		node.getExprString().apply(this);
		String formatting = ParseHelpers.getLiteralStringValue(node.getStringLiteral());
		new FormattingCompiler(formatting, node.getStringLiteral()).run(context);
	}

	@Override
	public void caseATrimExprString(ATrimExprString node) {
		// expr_string = trim trim_option? expr_plus
		node.getExprPlus().apply(this);
		if (node.getTrimOption() instanceof ALeftTrimOption)
			context.writer.invokeStatic(getMethod("trimLeft", ArdenValue.class));
		else if (node.getTrimOption() instanceof ARightTrimOption)
			context.writer.invokeStatic(getMethod("trimRight", ArdenValue.class));
		else
			context.writer.invokeStatic(getMethod("trim", ArdenValue.class));
	}

	@Override
	public void caseASubExprString(ASubExprString node) {
		// expr_string = = substring [charcount]:expr_plus characters
		// substring_start? from [inputstr]:expr_plus;

		// substring_start = T.starting T.at expr_factor;
		loadOperator(TernaryOperator.SUBSTRING);
		node.getCharcount().apply(this);
		if (node.getSubstringStart() instanceof ASubstringStart) {
			((ASubstringStart) node.getSubstringStart()).getExprFactor().apply(this);
		} else {
			context.writer.loadNull();
		}
		node.getInputstr().apply(this);
		invokeLoadedTernaryOperator();
	}

	// expr_find_string =
	// {istr} find [substring]:expr_string T.in T.string
	// [fullstring]:expr_string string_search_start?
	// | {str} find [substring]:expr_string T.string [fullstring]:expr_string
	// string_search_start?;
	@Override
	public void caseAIstrExprFindString(AIstrExprFindString node) {
		findString(node.getSubstring(), node.getFullstring(), node.getStringSearchStart());
	}

	@Override
	public void caseAStrExprFindString(AStrExprFindString node) {
		findString(node.getSubstring(), node.getFullstring(), node.getStringSearchStart());
	}

	private void findString(PExprString substring, PExprString fullstring, PStringSearchStart stringSearchStart) {
		loadOperator(TernaryOperator.FINDSTRING);
		substring.apply(this);
		fullstring.apply(this);
		if (stringSearchStart instanceof AStringSearchStart) {
			((AStringSearchStart) stringSearchStart).getExprPlus().apply(this);
		} else {
			context.writer.loadStaticField(context.codeGenerator.getNumberLiteral(1));
		}
		invokeLoadedTernaryOperator();
	}

	// expr_plus =
	// {times} expr_times
	// | {plus} expr_plus plus expr_times
	// | {minus} expr_plus minus expr_times
	// | {plust} plus expr_times
	// | {mint} minus expr_times;
	@Override
	public void caseATimesExprPlus(ATimesExprPlus node) {
		// expr_plus = {times} expr_times
		node.getExprTimes().apply(this);
	}

	@Override
	public void caseAPlusExprPlus(APlusExprPlus node) {
		// expr_plus = {plus} expr_plus plus expr_times
		invokeOperator(BinaryOperator.ADD, node.getExprPlus(), node.getExprTimes());
	}

	@Override
	public void caseAMinusExprPlus(AMinusExprPlus node) {
		// expr_plus = {minus} expr_plus minus expr_times
		invokeOperator(BinaryOperator.SUB, node.getExprPlus(), node.getExprTimes());
	}

	@Override
	public void caseAPlustExprPlus(APlustExprPlus node) {
		// expr_plus = {plust} plus expr_times
		invokeOperator(UnaryOperator.PLUS, node.getExprTimes());
	}

	@Override
	public void caseAMintExprPlus(AMintExprPlus node) {
		// expr_plus = {mint} minus expr_times
		invokeOperator(UnaryOperator.MINUS, node.getExprTimes());
	}

	// expr_times =
	// {power} expr_power
	// | {tpow} expr_times times expr_power
	// | {dpow} expr_times div expr_power;
	@Override
	public void caseAPowerExprTimes(APowerExprTimes node) {
		// expr_times = {power} expr_power
		node.getExprPower().apply(this);
	}

	@Override
	public void caseATpowExprTimes(ATpowExprTimes node) {
		// expr_times = {tpow} expr_times times expr_power
		invokeOperator(BinaryOperator.MUL, node.getExprTimes(), node.getExprPower());
	}

	@Override
	public void caseADpowExprTimes(ADpowExprTimes node) {
		// expr_times = {dpow} expr_times div expr_power
		invokeOperator(BinaryOperator.DIV, node.getExprTimes(), node.getExprPower());
	}

	// expr_power =
	// {before} expr_before
	// | {exp} [base]:expr_function dexp [exp]:expr_function;
	@Override
	public void caseABeforeExprPower(ABeforeExprPower node) {
		node.getExprBefore().apply(this);
	}

	@Override
	public void caseAExpExprPower(AExpExprPower node) {
		// expr_power = {exp} [base]:expr_function dexp [exp]:expr_function
		// Exponent (second arguement) must be an expression that evaluates to a
		// scalar number
		invokeOperator(BinaryOperator.POW, node.getBase(), node.getExp());
	}

	// expr_before =
	// {ago} expr_ago
	// | {before} expr_duration before expr_ago
	// | {after} expr_duration after expr_ago
	// | {from} expr_duration from expr_ago;
	@Override
	public void caseAAgoExprBefore(AAgoExprBefore node) {
		// expr_before = {ago} expr_ago
		node.getExprAgo().apply(this);
	}

	@Override
	public void caseABeforeExprBefore(ABeforeExprBefore node) {
		// expr_before = {before} expr_duration before expr_ago
		invokeOperator(BinaryOperator.BEFORE, node.getExprDuration(), node.getExprAgo());
	}

	@Override
	public void caseAAfterExprBefore(AAfterExprBefore node) {
		// expr_before = {after} expr_duration after expr_ago
		invokeOperator(BinaryOperator.AFTER, node.getExprDuration(), node.getExprAgo());
	}

	@Override
	public void caseAFromExprBefore(AFromExprBefore node) {
		// expr_before = {from} expr_duration from expr_ago
		// FROM and AFTER both do the same (duration + time)
		invokeOperator(BinaryOperator.AFTER, node.getExprDuration(), node.getExprAgo());
	}

	// expr_ago =
	// {func} expr_function
	// | {dur} expr_duration
	// | {ago} expr_duration ago;
	@Override
	public void caseAFuncExprAgo(AFuncExprAgo node) {
		// expr_ago = {func} expr_function
		node.getExprFunction().apply(this);
	}

	@Override
	public void caseADurExprAgo(ADurExprAgo node) {
		// expr_ago = {dur} expr_duration
		node.getExprDuration().apply(this);
	}

	@Override
	public void caseAAgoExprAgo(AAgoExprAgo node) {
		// expr_ago = {ago} expr_duration ago
		loadOperator(BinaryOperator.BEFORE);
		node.getExprDuration().apply(this);
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		invokeLoadedBinaryOperator();
	}

	// expr_duration = expr_function duration_op;
	@Override
	public void caseAExprDuration(AExprDuration node) {
		node.getExprFunction().apply(this);
		PDurationOp durOp = node.getDurationOp();
		compileDurationOp(durOp);
		context.writer.invokeStatic(getMethod("createDuration", ArdenValue.class, double.class, boolean.class));
	}
	
	public void compileDurationOp(Node durOp) {
		boolean isMonths;
		double multiplier;
		if (durOp instanceof ADayDurationOp || durOp instanceof ADaysDurationOp) {
			isMonths = false;
			multiplier = 86400;
		} else if (durOp instanceof AHourDurationOp || durOp instanceof AHoursDurationOp) {
			isMonths = false;
			multiplier = 3600;
		} else if (durOp instanceof AMinDurationOp || durOp instanceof AMinsDurationOp) {
			isMonths = false;
			multiplier = 60;
		} else if (durOp instanceof AMonthDurationOp || durOp instanceof AMonthsDurationOp) {
			isMonths = true;
			multiplier = 1;
		} else if (durOp instanceof ASecDurationOp || durOp instanceof ASecsDurationOp) {
			isMonths = false;
			multiplier = 1;
		} else if (durOp instanceof AWeekDurationOp || durOp instanceof AWeeksDurationOp) {
			isMonths = false;
			multiplier = 604800;
		} else if (durOp instanceof AYearDurationOp || durOp instanceof AYearsDurationOp) {
			isMonths = true;
			multiplier = 12;
		} else {
			throw new RuntimeCompilerException("Unsupported duration operator: " + durOp.toString());
		}
		context.writer.loadDoubleConstant(multiplier);
		context.writer.loadIntegerConstant(isMonths ? 1 : 0);
	}
	
	// expr_function =
	// {expr} expr_factor
	// | {ofexpr} of_func_op expr_function
	// | {ofofexpr} of_func_op of expr_function
	// | {fromexpr} from_of_func_op expr_function
	// | {fromofexpr} from_of_func_op of expr_function
	// | {fromofexprfrom} from_of_func_op expr_factor from expr_function
	// | {fromexprfrom} from_func_op expr_factor from expr_function
	// | {ifromexpr} index_from_of_func_op expr_function
	// | {ifromofexpr} index_from_of_func_op of expr_function
	// | {ifromofexprfrom} index_from_of_func_op expr_factor from expr_function
	// | {ifromexprfrom} index_from_func_op expr_factor from expr_function
	// | {factas} expr_factor as as_func_op
	// | {attr} attribute expr_factor from expr_function;
	@Override
	public void caseAExprExprFunction(AExprExprFunction node) {
		// expr_function = {expr} expr_factor
		node.getExprFactor().apply(this);
	}

	@Override
	public void caseAOfexprExprFunction(AOfexprExprFunction node) {
		// expr_function = {ofexpr} of_func_op expr_function
		node.getOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAOfofexprExprFunction(AOfofexprExprFunction node) {
		// expr_function = {ofofexpr} of_func_op of expr_function
		node.getOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAFromexprExprFunction(AFromexprExprFunction node) {
		// {fromexpr} from_of_func_op expr_function
		node.getFromOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAFromofexprExprFunction(AFromofexprExprFunction node) {
		// {fromofexpr} from_of_func_op of expr_function
		node.getFromOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAFromofexprfromExprFunction(AFromofexprfromExprFunction node) {
		// {fromofexprfrom} from_of_func_op expr_factor from expr_function
		node.getFromOfFuncOp().apply(
				new TransformationOperatorCompiler(this, node.getExprFactor(), node.getExprFunction()));
	}

	@Override
	public void caseAFromexprfromExprFunction(AFromexprfromExprFunction node) {
		// {fromexprfrom} from_func_op expr_factor from expr_function
		node.getFromFuncOp().apply(
				new TransformationOperatorCompiler(this, node.getExprFactor(), node.getExprFunction()));
	}

	@Override
	public void caseAIfromexprExprFunction(AIfromexprExprFunction node) {
		// {ifromexpr} index_from_of_func_op expr_function
		node.getIndexFromOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAIfromofexprExprFunction(AIfromofexprExprFunction node) {
		// {ifromofexpr} index_from_of_func_op of expr_function
		node.getIndexFromOfFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFunction()));
	}

	@Override
	public void caseAIfromofexprfromExprFunction(AIfromofexprfromExprFunction node) {
		// {ifromofexprfrom} index_from_of_func_op expr_factor from
		node.getIndexFromOfFuncOp().apply(
				new TransformationOperatorCompiler(this, node.getExprFactor(), node.getExprFunction()));

	}

	@Override
	public void caseAIfromexprfromExprFunction(AIfromexprfromExprFunction node) {
		// {ifromexprfrom} index_from_func_op expr_factor from expr_function
		node.getIndexFromFuncOp().apply(
				new TransformationOperatorCompiler(this, node.getExprFactor(), node.getExprFunction()));
	}

	@Override
	public void caseAFactasExprFunction(AFactasExprFunction node) {
		// {factas} expr_factor as as_func_op
		node.getAsFuncOp().apply(new UnaryOperatorCompiler(this, node.getExprFactor()));
	}

	@Override
	public void caseAAttrExprFunction(AAttrExprFunction node) {
		// {attr} attribute expr_factor from expr_function
		invokeOperator(BinaryOperator.ATTRIBUTEFROM, node.getExprFactor(), node.getExprFunction());
	}

	// expr_factor =
	// {expf} expr_factor_atom
	// | {efe} expr_factor_atom l_brk expr r_brk
	// | {dot} expr_factor dot identifier;
	@Override
	public void caseAExpfExprFactor(AExpfExprFactor node) {
		// expr_factor = {expf} expr_factor_atom
		node.getExprFactorAtom().apply(this);
	}

	@Override
	public void caseAEfeExprFactor(AEfeExprFactor node) {
		// expr_factor = {efe} expr_factor_atom l_brk expr r_brk
		node.getExprFactorAtom().apply(this);
		node.getExpr().apply(this);
		context.writer.invokeStatic(getMethod("elementAt", ArdenValue.class, ArdenValue.class));
	}

	@Override
	public void caseADotExprFactor(ADotExprFactor node) {
		// expr_factor = {dot} expr_factor dot identifier
		node.getExprFactor().apply(this);
		context.writer.loadStringConstant(node.getIdentifier().getText().toUpperCase(Locale.ENGLISH));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("getObjectMember", ArdenValue.class, String.class));
	}

	// expr_factor_atom =
	// {id} identifier
	// | {num} number_literal
	// | {string} string_literal
	// | {time} time_value
	// | {bool} boolean_value
	// | {null} null
	// | {it} P.it
	// | {par} l_par r_par
	// | {exp} l_par expr r_par;
	@Override
	public void caseAIdExprFactorAtom(AIdExprFactorAtom node) {
		// expr_factor_atom = {id} identifier
		String name = node.getIdentifier().getText();
		Variable var = context.codeGenerator.getVariable(name);
		if (var == null)
			throw new RuntimeCompilerException(node.getIdentifier(), "Unknown variable: " + name);
		var.loadValue(context, node.getIdentifier());
	}

	@Override
	public void caseANumExprFactorAtom(ANumExprFactorAtom node) {
		// expr_factor_atom = {num} number_literal
		double value = ParseHelpers.getLiteralDoubleValue(node.getNumberLiteral());
		context.writer.loadStaticField(context.codeGenerator.getNumberLiteral(value));
	}

	@Override
	public void caseAStringExprFactorAtom(AStringExprFactorAtom node) {
		// expr_factor_atom = {string} string_literal
		String text = ParseHelpers.getLiteralStringValue(node.getStringLiteral());
		context.writer.loadStaticField(context.codeGenerator.getStringLiteral(text));
	}

	@Override
	public void caseATimeExprFactorAtom(ATimeExprFactorAtom node) {
		// expr_factor_atom = {time} time_value
		node.getTimeValue().apply(this);
	}

	@Override
	public void caseABoolExprFactorAtom(ABoolExprFactorAtom node) {
		// expr_factor_atom = {bool} boolean_value
		node.getBooleanValue().apply(this);
	}

	@Override
	public void caseANullExprFactorAtom(ANullExprFactorAtom node) {
		// expr_factor_atom = {null} null
		try {
			context.writer.loadStaticField(ArdenNull.class.getDeclaredField("INSTANCE"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void caseAItExprFactorAtom(AItExprFactorAtom node) {
		// expr_factor_atom = {it} P.it
		/* Value is NULL outside of a where */
		/* clause and may be flagged as an */
		/* error in some implementations. */
		int it = context.getCurrentItVariable();
		if (it < 0)
			throw new RuntimeCompilerException("'" + node.getIt().toString()
					+ "' is only valid within WHERE conditions");
		context.writer.loadVariable(it);
	}

	@Override
	public void caseAParExprFactorAtom(AParExprFactorAtom node) {
		// expr_factor_atom = {par} l_par r_par
		try {
			context.writer.loadStaticField(ArdenList.class.getDeclaredField("EMPTY"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void caseAExpExprFactorAtom(AExpExprFactorAtom node) {
		// expr_factor_atom = {exp} l_par expr r_par
		node.getExpr().apply(this);
	}

	// boolean_value =
	// {true} true
	// | {false} false;
	@Override
	public void caseATrueBooleanValue(ATrueBooleanValue node) {
		// boolean_value = {true} true
		try {
			context.writer.loadStaticField(ArdenBoolean.class.getDeclaredField("TRUE"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void caseAFalseBooleanValue(AFalseBooleanValue node) {
		// boolean_value = {false} false
		try {
			context.writer.loadStaticField(ArdenBoolean.class.getDeclaredField("FALSE"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	// time_value =
	// {now} now
	// | {idt} iso_date_time
	// | {idat} iso_date
	// | {etim} eventtime
	// | {ttim} triggertime
	// | {ctim} currenttime;
	@Override
	public void caseANowTimeValue(ANowTimeValue node) {
		// time_value = {now} now
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
	}

	@Override
	public void caseAIdtTimeValue(AIdtTimeValue node) {
		// time_value = {idt} iso_date_time
		long time = ParseHelpers.parseIsoDateTime(node.getIsoDateTime());
		context.writer.loadStaticField(context.codeGenerator.getTimeLiteral(time));
	}

	@Override
	public void caseAIdatTimeValue(AIdatTimeValue node) {
		// time_value = {idat} iso_date
		long time = ParseHelpers.parseIsoDate(node.getIsoDate());
		context.writer.loadStaticField(context.codeGenerator.getTimeLiteral(time));
	}

	@Override
	public void caseAEtimTimeValue(AEtimTimeValue node) {
		// time_value = {etim} eventtime
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeInstance(ExecutionContextMethods.getEventTime);
	}

	@Override
	public void caseATtimTimeValue(ATtimTimeValue node) {
		// time_value = {ttim} triggertime
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeInstance(ExecutionContextMethods.getTriggerTime);
	}

	@Override
	public void caseACtimTimeValue(ACtimTimeValue node) {
		// time_value = {ctim} currenttime
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeInstance(ExecutionContextMethods.getCurrentTime);
	}

	// new_object_phrase =
	// {simple} new identifier
	// | {init} new identifier with expr;
	@Override
	public void caseASimpleNewObjectPhrase(ASimpleNewObjectPhrase node) {
		createNewObject(node.getIdentifier());
	}

	private void createNewObject(TIdentifier typeName) {
		Variable v = context.codeGenerator.getVariable(typeName.getText());
		if (!(v instanceof ObjectTypeVariable))
			throw new RuntimeCompilerException(typeName, typeName.getText() + " is not an OBJECT type.");

		context.writer.newObject(ArdenObject.class);
		context.writer.dup();
		context.writer.loadStaticField(((ObjectTypeVariable) v).field);
		try {
			context.writer.invokeConstructor(ArdenObject.class.getConstructor(ObjectType.class));
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
	}

	@Override
	public void caseAInitNewObjectPhrase(AInitNewObjectPhrase node) {
		// new identifier with expr
		Field objectFieldsField;
		try {
			objectFieldsField = ArdenObject.class.getField("fields");
		} catch (NoSuchFieldException e) {
			throw new Error(e);
		}
		// stack: (empty)
		createNewObject(node.getIdentifier());
		// stack: newobj
		context.writer.dup();
		// stack: newobj, newobj
		context.writer.loadInstanceField(objectFieldsField);
		// stack: newobj, fields
		int fieldsVariable = context.allocateVariable();
		context.writer.storeVariable(fieldsVariable);
		// stack: newobj
		List<PExprSort> arguments = ParseHelpers.toCommaSeparatedList(node.getExpr());
		for (int i = 0; i < arguments.size(); i++) {
			// emit: if (i < fields.length) fields[i] = arg[i];
			// stack: newobj
			context.writer.loadVariable(fieldsVariable);
			context.writer.loadIntegerConstant(i);
			arguments.get(i).apply(this);
			context.writer.loadIntegerConstant(i);
			context.writer.loadVariable(fieldsVariable);

			// stack: newobj, fields, i, arg[i], i, fields
			context.writer.arrayLength();
			// stack: newobj, fields, i, arg[i], i, fields.length
			Label endLabel = new Label();
			Label trueLabel = new Label();
			context.writer.jumpIfLessThan(trueLabel);

			// false:
			// stack: newobj, fields, i, arg[i]
			context.writer.pop();
			context.writer.pop();
			context.writer.pop();
			// stack: newobj
			context.writer.jump(endLabel);

			context.writer.markForwardJumpsOnly(trueLabel);
			// stack: newobj, fields, i, arg[i]
			// emit: fields[i] = arg[i];
			context.writer.storeObjectToArray();
			// stack: newobj
			context.writer.markForwardJumpsOnly(endLabel);
		}
		// stack: newobj
	}
}
