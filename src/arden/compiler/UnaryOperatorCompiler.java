package arden.compiler;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.UnaryOperator;

/**
 * Compiler for unary function operators (of_func_op, from_of_func_op and
 * related productions).
 * 
 * Every operator.apply(this) call will generate code that pushes the operator's
 * result value onto the evaluation stack. The parent compiler is used to
 * generate code for the specified argument. Every possible codepath will emit
 * code that evaluates the argument exactly once.
 * 
 * @author Daniel Grunwald
 */
final class UnaryOperatorCompiler extends VisitorBase {
	private final ExpressionCompiler parent;
	private final Node argument;
	private final CompilerContext context;

	public UnaryOperatorCompiler(ExpressionCompiler parent, Node argument) {
		this.parent = parent;
		this.argument = argument;
		this.context = parent.getContext();
	}

	// of_func_op =
	// {ofr} of_read_func_op
	// | {ofnr} of_noread_func_op;
	@Override
	public void caseAOfrOfFuncOp(AOfrOfFuncOp node) {
		node.getOfReadFuncOp().apply(this);
	}

	@Override
	public void caseAOfnrOfFuncOp(AOfnrOfFuncOp node) {
		node.getOfNoreadFuncOp().apply(this);
	}

	// of_read_func_op =
	// {avge} average
	// | {avg} avg
	// | {cnt} count
	// | {ex} exist
	// | {exs} exists
	// | {sum} sum
	// | {med} median;
	@Override
	public void caseAAvgeOfReadFuncOp(AAvgeOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("average", ArdenValue.class));
	}

	@Override
	public void caseAAvgOfReadFuncOp(AAvgOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("average", ArdenValue.class));
	}

	@Override
	public void caseACntOfReadFuncOp(ACntOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("count", ArdenValue.class));
	}

	@Override
	public void caseAExOfReadFuncOp(AExOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("exist", ArdenValue.class));
	}

	@Override
	public void caseAExsOfReadFuncOp(AExsOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("exist", ArdenValue.class));
	}

	@Override
	public void caseASumOfReadFuncOp(ASumOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("sum", ArdenValue.class));
	}

	@Override
	public void caseAMedOfReadFuncOp(AMedOfReadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("median", ArdenValue.class));
	}

	// of_noread_func_op =
	// {any} any
	// | {all} all
	// | {no} no
	// | {slp} slope
	// | {stdv} stddev
	// | {var} variance
	// | {inc} increase
	// | {peri} percent increase
	// | {modi} mod increase
	// | {dec} decrease
	// | {perd} percent decrease
	// | {modd} mod decrease
	// | {inter} interval
	// | {time} time
	// | {acos} arccos
	// | {asin} arcsin
	// | {atan} arctan
	// | {csin} cosine
	// | {cos} cos
	// | {sine} sine
	// | {sin} sin
	// | {tang} tangent
	// | {tan} tan
	// | {exp} exp
	// | {flr} floor
	// | {int} int
	// | {round} round
	// | {ceil} ceiling
	// | {trunc} truncate
	// | {log} log
	// | {logt} log10
	// | {abs} abs
	// | {sqrt} sqrt
	// | {exy} extract year
	// | {exmo} extract month
	// | {exd} extract day
	// | {exh} extract hour
	// | {exmi} extract minute
	// | {exs} extract second
	// | {str} T.string
	// | {exc} extract characters
	// | {rev} reverse
	// | {len} length
	// | {uc} uppercase
	// | {lc} lowercase;

	@Override
	public void caseAAnyOfNoreadFuncOp(AAnyOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("any", ArdenValue.class));
	}

	@Override
	public void caseAAllOfNoreadFuncOp(AAllOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("all", ArdenValue.class));
	}

	@Override
	public void caseANoOfNoreadFuncOp(ANoOfNoreadFuncOp node) {
		parent.loadOperator(UnaryOperator.NOT);
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("any", ArdenValue.class));
		parent.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseASlpOfNoreadFuncOp(ASlpOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseASlpOfNoreadFuncOp(node);
	}

	@Override
	public void caseAStdvOfNoreadFuncOp(AStdvOfNoreadFuncOp node) {
		parent.loadOperator(UnaryOperator.SQRT);
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("variance", ArdenValue.class));
		parent.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAVarOfNoreadFuncOp(AVarOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("variance", ArdenValue.class));
	}

	@Override
	public void caseAIncOfNoreadFuncOp(AIncOfNoreadFuncOp node) {
		// increase
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("increase", ArdenValue.class));
	}

	@Override
	public void caseAPeriOfNoreadFuncOp(APeriOfNoreadFuncOp node) {
		// percent increase
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("percentIncrease", ArdenValue.class));
	}

	@Override
	public void caseAModiOfNoreadFuncOp(AModiOfNoreadFuncOp node) {
		// % increase
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("percentIncrease", ArdenValue.class));
	}

	@Override
	public void caseADecOfNoreadFuncOp(ADecOfNoreadFuncOp node) {
		// decrease
		parent.loadOperator(UnaryOperator.MINUS);
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("increase", ArdenValue.class));
		parent.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAPerdOfNoreadFuncOp(APerdOfNoreadFuncOp node) {
		// percent decrease
		parent.loadOperator(UnaryOperator.MINUS);
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("percentIncrease", ArdenValue.class));
		parent.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAModdOfNoreadFuncOp(AModdOfNoreadFuncOp node) {
		// % decrease
		parent.loadOperator(UnaryOperator.MINUS);
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("percentIncrease", ArdenValue.class));
		parent.invokeLoadedUnaryOperator();
	}

	@Override
	public void caseAInterOfNoreadFuncOp(AInterOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAInterOfNoreadFuncOp(node);
	}

	@Override
	public void caseATimeOfNoreadFuncOp(ATimeOfNoreadFuncOp node) {
		parent.invokeOperator(UnaryOperator.TIME, argument);
	}

	@Override
	public void caseAAcosOfNoreadFuncOp(AAcosOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAAcosOfNoreadFuncOp(node);
	}

	@Override
	public void caseAAsinOfNoreadFuncOp(AAsinOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAAsinOfNoreadFuncOp(node);
	}

	@Override
	public void caseAAtanOfNoreadFuncOp(AAtanOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAAtanOfNoreadFuncOp(node);
	}

	@Override
	public void caseACsinOfNoreadFuncOp(ACsinOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseACsinOfNoreadFuncOp(node);
	}

	@Override
	public void caseACosOfNoreadFuncOp(ACosOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseACosOfNoreadFuncOp(node);
	}

	@Override
	public void caseASineOfNoreadFuncOp(ASineOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseASineOfNoreadFuncOp(node);
	}

	@Override
	public void caseASinOfNoreadFuncOp(ASinOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseASinOfNoreadFuncOp(node);
	}

	@Override
	public void caseATangOfNoreadFuncOp(ATangOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseATangOfNoreadFuncOp(node);
	}

	@Override
	public void caseATanOfNoreadFuncOp(ATanOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseATanOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExpOfNoreadFuncOp(AExpOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExpOfNoreadFuncOp(node);
	}

	@Override
	public void caseAFlrOfNoreadFuncOp(AFlrOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAFlrOfNoreadFuncOp(node);
	}

	@Override
	public void caseAIntOfNoreadFuncOp(AIntOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAIntOfNoreadFuncOp(node);
	}

	@Override
	public void caseARoundOfNoreadFuncOp(ARoundOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseARoundOfNoreadFuncOp(node);
	}

	@Override
	public void caseACeilOfNoreadFuncOp(ACeilOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseACeilOfNoreadFuncOp(node);
	}

	@Override
	public void caseATruncOfNoreadFuncOp(ATruncOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseATruncOfNoreadFuncOp(node);
	}

	@Override
	public void caseALogOfNoreadFuncOp(ALogOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseALogOfNoreadFuncOp(node);
	}

	@Override
	public void caseALogtOfNoreadFuncOp(ALogtOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseALogtOfNoreadFuncOp(node);
	}

	@Override
	public void caseAAbsOfNoreadFuncOp(AAbsOfNoreadFuncOp node) {
		parent.invokeOperator(UnaryOperator.ABS, argument);
	}

	@Override
	public void caseASqrtOfNoreadFuncOp(ASqrtOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseASqrtOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExyOfNoreadFuncOp(AExyOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExyOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExmoOfNoreadFuncOp(AExmoOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExmoOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExdOfNoreadFuncOp(AExdOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExdOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExhOfNoreadFuncOp(AExhOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExhOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExmiOfNoreadFuncOp(AExmiOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExmiOfNoreadFuncOp(node);
	}

	@Override
	public void caseAExsOfNoreadFuncOp(AExsOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExsOfNoreadFuncOp(node);
	}

	@Override
	public void caseAStrOfNoreadFuncOp(AStrOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("joinString", ArdenValue.class));
	}

	@Override
	public void caseAExcOfNoreadFuncOp(AExcOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAExcOfNoreadFuncOp(node);
	}

	@Override
	public void caseARevOfNoreadFuncOp(ARevOfNoreadFuncOp node) {
		// TODO Auto-generated method stub
		super.caseARevOfNoreadFuncOp(node);
	}

	@Override
	public void caseALenOfNoreadFuncOp(ALenOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("length", ArdenValue.class));
	}

	@Override
	public void caseAUcOfNoreadFuncOp(AUcOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("toUpperCase", ArdenValue.class));
	}

	@Override
	public void caseALcOfNoreadFuncOp(ALcOfNoreadFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("toLowerCase", ArdenValue.class));
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
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("minimum", ArdenValue.class));
	}

	@Override
	public void caseAMinFromOfFuncOp(AMinFromOfFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("minimum", ArdenValue.class));
	}

	@Override
	public void caseAMaxiFromOfFuncOp(AMaxiFromOfFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("maximum", ArdenValue.class));
	}

	@Override
	public void caseAMaxFromOfFuncOp(AMaxFromOfFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("maximum", ArdenValue.class));
	}

	@Override
	public void caseALastFromOfFuncOp(ALastFromOfFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("last", ArdenValue.class));
	}

	@Override
	public void caseAFirFromOfFuncOp(AFirFromOfFuncOp node) {
		argument.apply(parent);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("first", ArdenValue.class));
	}

	@Override
	public void caseAEarFromOfFuncOp(AEarFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseAEarFromOfFuncOp(node);
	}

	@Override
	public void caseALatFromOfFuncOp(ALatFromOfFuncOp node) {
		// TODO Auto-generated method stub
		super.caseALatFromOfFuncOp(node);
	}
}
