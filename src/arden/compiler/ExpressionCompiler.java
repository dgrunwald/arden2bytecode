package arden.compiler;

import java.lang.reflect.Method;

import arden.compiler.node.*;
import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenValue;
import arden.runtime.ExpressionHelpers;

/**
 * Compiler for expressions
 * 
 * @author Daniel Grunwald
 */
final class ExpressionCompiler extends VisitorBase {
	private final CompilerContext context;

	public ExpressionCompiler(CompilerContext context) {
		this.context = context;
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
	// | {sort} sort expr_sort
	// | {sopt} sort l_brk sort_option r_brk expr_sort;
	@Override
	public void caseAWhereExprSort(AWhereExprSort node) {
		// expr_sort = {where} expr_where
		node.getExprWhere().apply(this);
	}

	@Override
	public void caseAMergeExprSort(AMergeExprSort node) {
		// TODO Auto-generated method stub
		super.caseAMergeExprSort(node);
	}

	@Override
	public void caseASortExprSort(ASortExprSort node) {
		// TODO Auto-generated method stub
		super.caseASortExprSort(node);
	}

	@Override
	public void caseASoptExprSort(ASoptExprSort node) {
		// TODO Auto-generated method stub
		super.caseASoptExprSort(node);
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
		// TODO Auto-generated method stub
		super.caseAWrangeExprWhere(node);
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
		// TODO Auto-generated method stub
		super.caseASeqExprRange(node);
	}

	// expr_or =
	// {or} expr_or or expr_and
	// | {and} expr_and;
	@Override
	public void caseAOrExprOr(AOrExprOr node) {
		// TODO Auto-generated method stub
		super.caseAOrExprOr(node);
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
		// TODO Auto-generated method stub
		super.caseAAndExprAnd(node);
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
		// TODO Auto-generated method stub
		super.caseANotExprNot(node);
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
	// | {range} expr_string P.occur range_comp_op
	// | {rngnot} expr_string P.occur not range_comp_op
	// | {match} [first_string]:expr_string matches pattern
	// [second_string]:expr_string;
	@Override
	public void caseAStrExprComparison(AStrExprComparison node) {
		// expr_comparison = {str} expr_string
		node.getExprString().apply(this);
	}

	@Override
	public void caseAFindExprComparison(AFindExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAFindExprComparison(node);
	}

	@Override
	public void caseASimExprComparison(ASimExprComparison node) {
		// TODO Auto-generated method stub
		super.caseASimExprComparison(node);
	}

	@Override
	public void caseAIsExprComparison(AIsExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAIsExprComparison(node);
	}

	@Override
	public void caseAInotExprComparison(AInotExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAInotExprComparison(node);
	}

	@Override
	public void caseAInExprComparison(AInExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAInExprComparison(node);
	}

	@Override
	public void caseANinExprComparison(ANinExprComparison node) {
		// TODO Auto-generated method stub
		super.caseANinExprComparison(node);
	}

	@Override
	public void caseAOccurExprComparison(AOccurExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAOccurExprComparison(node);
	}

	@Override
	public void caseAOcrnotExprComparison(AOcrnotExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAOcrnotExprComparison(node);
	}

	@Override
	public void caseARangeExprComparison(ARangeExprComparison node) {
		// TODO Auto-generated method stub
		super.caseARangeExprComparison(node);
	}

	@Override
	public void caseARngnotExprComparison(ARngnotExprComparison node) {
		// TODO Auto-generated method stub
		super.caseARngnotExprComparison(node);
	}

	@Override
	public void caseAMatchExprComparison(AMatchExprComparison node) {
		// TODO Auto-generated method stub
		super.caseAMatchExprComparison(node);
	}

	// expr_string =
	// {plus} expr_plus
	// | {or} expr_string logor expr_plus
	// | {form} expr_string formatted with format_string;
	@Override
	public void caseAPlusExprString(APlusExprString node) {
		// expr_string = {plus} expr_plus
		node.getExprPlus().apply(this);
	}

	@Override
	public void caseAOrExprString(AOrExprString node) {
		// TODO Auto-generated method stub
		super.caseAOrExprString(node);
	}

	@Override
	public void caseAFormExprString(AFormExprString node) {
		// TODO Auto-generated method stub
		super.caseAFormExprString(node);
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
		// TODO Auto-generated method stub
		super.caseAPlusExprPlus(node);
	}

	@Override
	public void caseAMinusExprPlus(AMinusExprPlus node) {
		// TODO Auto-generated method stub
		super.caseAMinusExprPlus(node);
	}

	@Override
	public void caseAPlustExprPlus(APlustExprPlus node) {
		// TODO Auto-generated method stub
		super.caseAPlustExprPlus(node);
	}

	@Override
	public void caseAMintExprPlus(AMintExprPlus node) {
		// TODO Auto-generated method stub
		super.caseAMintExprPlus(node);
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
		// TODO Auto-generated method stub
		super.caseATpowExprTimes(node);
	}

	@Override
	public void caseADpowExprTimes(ADpowExprTimes node) {
		// TODO Auto-generated method stub
		super.caseADpowExprTimes(node);
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
		// TODO Auto-generated method stub
		super.caseAExpExprPower(node);
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
		// TODO Auto-generated method stub
		super.caseABeforeExprBefore(node);
	}

	@Override
	public void caseAAfterExprBefore(AAfterExprBefore node) {
		// TODO Auto-generated method stub
		super.caseAAfterExprBefore(node);
	}

	@Override
	public void caseAFromExprBefore(AFromExprBefore node) {
		// TODO Auto-generated method stub
		super.caseAFromExprBefore(node);
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
		// TODO Auto-generated method stub
		super.caseADurExprAgo(node);
	}

	@Override
	public void caseAAgoExprAgo(AAgoExprAgo node) {
		// TODO Auto-generated method stub
		super.caseAAgoExprAgo(node);
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
	// | {factas} expr_factor as as_func_op;
	@Override
	public void caseAExprExprFunction(AExprExprFunction node) {
		// expr_function = {expr} expr_factor
		node.getExprFactor().apply(this);
	}

	@Override
	public void caseAOfexprExprFunction(AOfexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAOfexprExprFunction(node);
	}

	@Override
	public void caseAOfofexprExprFunction(AOfofexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAOfofexprExprFunction(node);
	}

	@Override
	public void caseAFromexprExprFunction(AFromexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAFromexprExprFunction(node);
	}

	@Override
	public void caseAFromofexprExprFunction(AFromofexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAFromofexprExprFunction(node);
	}

	@Override
	public void caseAFromofexprfromExprFunction(AFromofexprfromExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAFromofexprfromExprFunction(node);
	}

	@Override
	public void caseAFromexprfromExprFunction(AFromexprfromExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAFromexprfromExprFunction(node);
	}

	@Override
	public void caseAIfromexprExprFunction(AIfromexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAIfromexprExprFunction(node);
	}

	@Override
	public void caseAIfromofexprExprFunction(AIfromofexprExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAIfromofexprExprFunction(node);
	}

	@Override
	public void caseAIfromofexprfromExprFunction(AIfromofexprfromExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAIfromofexprfromExprFunction(node);
	}

	@Override
	public void caseAIfromexprfromExprFunction(AIfromexprfromExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAIfromexprfromExprFunction(node);
	}

	@Override
	public void caseAFactasExprFunction(AFactasExprFunction node) {
		// TODO Auto-generated method stub
		super.caseAFactasExprFunction(node);
	}

	// expr_factor =
	// {expf} expr_factor_atom
	// | {efe} expr_factor_atom l_brk expr r_brk ;
	@Override
	public void caseAExpfExprFactor(AExpfExprFactor node) {
		// expr_factor = {expf} expr_factor_atom
		node.getExprFactorAtom().apply(this);
	}

	@Override
	public void caseAEfeExprFactor(AEfeExprFactor node) {
		// expr_factor = {efe} expr_factor_atom l_brk expr r_brk
		// number [expr] is not a valid construct
		// TODO Auto-generated method stub
		super.caseAEfeExprFactor(node);
	}

	// expr_factor_atom =
	// {id} identifier
	// | {num} P.number
	// | {string} string_literal
	// | {time} time_value
	// | {bool} boolean_value
	// | {null} null
	// | {it} P.it
	// | {par} l_par r_par
	// | {exp} l_par expr r_par;
	@Override
	public void caseAIdExprFactorAtom(AIdExprFactorAtom node) {
		// TODO Auto-generated method stub
		super.caseAIdExprFactorAtom(node);
	}

	@Override
	public void caseANumExprFactorAtom(ANumExprFactorAtom node) {
		// expr_factor_atom = {num} P.number
		double value = ParseHelpers.getLiteralDoubleValue(node.getNumber());
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
		// TODO Auto-generated method stub
		super.caseATimeExprFactorAtom(node);
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

		// TODO Auto-generated method stub
		super.caseAItExprFactorAtom(node);
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
}
