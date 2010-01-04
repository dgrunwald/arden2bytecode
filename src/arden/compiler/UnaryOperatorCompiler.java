package arden.compiler;

import arden.compiler.node.AAvgOfReadFuncOp;
import arden.compiler.node.AAvgeOfReadFuncOp;
import arden.compiler.node.ACntOfReadFuncOp;
import arden.compiler.node.AExOfReadFuncOp;
import arden.compiler.node.AExsOfReadFuncOp;
import arden.compiler.node.AMedOfReadFuncOp;
import arden.compiler.node.AOfnrOfFuncOp;
import arden.compiler.node.AOfrOfFuncOp;
import arden.compiler.node.AStdvOfNoreadFuncOp;
import arden.compiler.node.ASumOfReadFuncOp;
import arden.compiler.node.AVarOfNoreadFuncOp;
import arden.compiler.node.Node;
import arden.runtime.ArdenValue;
import arden.runtime.UnaryOperator;

/**
 * Compiler for unary function operators (of_func_op and related productions).
 * 
 * Every operator.apply(this) call will generate code that pushes the
 * operator's result value onto the evaluation stack.
 * The parent compiler is used to generate code for the specified argument. Every possible codepath
 * will emit code that evaluates the argument exactly once.
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
	// TODO: add missing definitions from this production
	
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
}
