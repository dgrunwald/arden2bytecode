package arden.compiler;

import java.lang.reflect.Method;

import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.BinaryOperator;
import arden.runtime.DatabaseQuery;

/**
 * Compiler for READ phrases.
 * 
 * Every phrase.apply(this) call will generate code that pushes a DatabaseQuery
 * onto the evaluation stack.
 * 
 * @author Daniel Grunwald
 */
final class ReadPhraseCompiler extends VisitorBase {
	private final CompilerContext context;

	public ReadPhraseCompiler(CompilerContext context) {
		this.context = context;
	}

	// read_phrase =
	// {read} read_where
	// | {of} of_read_func_op read_where
	// | {off} of_read_func_op of read_where
	// | {f} from_of_func_op read_where
	// | {fof} from_of_func_op of read_where
	// | {foff} from_of_func_op expr_factor from read_where;
	@Override
	public void caseAReadReadPhrase(AReadReadPhrase node) {
		// read_phrase = {read} read_where
		node.getReadWhere().apply(this);
	}

	@Override
	public void caseAOfReadPhrase(AOfReadPhrase node) {
		// read_phrase = {of} of_read_func_op read_where
		node.getReadWhere().apply(this);
		// TODO Auto-generated method stub
		throw new RuntimeCompilerException("TODO");
	}

	@Override
	public void caseAFReadPhrase(AFReadPhrase node) {
		// read_phrase = {f} from_of_func_op read_where
		node.getReadWhere().apply(this);
		handleFromOfFuncOp(node.getFromOfFuncOp());
	}

	@Override
	public void caseAFofReadPhrase(AFofReadPhrase node) {
		// read_phrase = {fof} from_of_func_op of read_where
		node.getReadWhere().apply(this);
		handleFromOfFuncOp(node.getFromOfFuncOp());
	}

	@Override
	public void caseAFoffReadPhrase(AFoffReadPhrase node) {
		// read_phrase = {foff} from_of_func_op expr_factor from read_where;
		node.getReadWhere().apply(this);
		// TODO Auto-generated method stub
		throw new RuntimeCompilerException("TODO");
	}

	/** Adds the PFromOfFuncOp to the DatabaseQuery on the evaluation stack */
	private void handleFromOfFuncOp(PFromOfFuncOp node) {
		// from_of_func_op =
		// {mini} minimum
		// | {min} min
		// | {maxi} maximum
		// | {max} max
		// | {last} last
		// | {fir} first
		// | {ear} earliest
		// | {lat} latest;
		if (node instanceof AMiniFromOfFuncOp || node instanceof AMinFromOfFuncOp) {
			invokeSimpleAggregationOperator("minimum");
		} else if (node instanceof AMaxiFromOfFuncOp || node instanceof AMaxFromOfFuncOp) {
			invokeSimpleAggregationOperator("maximum");
		} else if (node instanceof ALastFromOfFuncOp) {
			invokeSimpleAggregationOperator("last");
		} else if (node instanceof AFirFromOfFuncOp) {
			invokeSimpleAggregationOperator("first");
		} else if (node instanceof AEarFromOfFuncOp) {
			invokeSimpleAggregationOperator("earliest");
		} else if (node instanceof ALatFromOfFuncOp) {
			invokeSimpleAggregationOperator("latest");
		} else {
			throw new RuntimeException("unknown from_of_func_op");
		}
	}

	private void invokeSimpleAggregationOperator(String name) {
		try {
			context.writer.invokeInstance(DatabaseQuery.class.getMethod(name));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	// read_where =
	// {map} mapping_factor
	// | {tmp} mapping_factor where P.it P.occur temporal_comp_op
	// | {ntmp} mapping_factor where P.it P.occur not temporal_comp_op
	// | {rdwhr} l_par read_where r_par;
	@Override
	public void caseAMapReadWhere(AMapReadWhere node) {
		// read_where = {map} mapping_factor
		node.getMappingFactor().apply(this);
	}

	@Override
	public void caseATmpReadWhere(ATmpReadWhere node) {
		// read_where = {tmp} mapping_factor where P.it P.occur temporal_comp_op
		node.getMappingFactor().apply(this);
		handleTemporalCompOp(node.getTemporalCompOp(), false);
	}

	@Override
	public void caseANtmpReadWhere(ANtmpReadWhere node) {
		// read_where = {ntmp} mapping_factor where P.it P.occur not
		// temporal_comp_op
		node.getMappingFactor().apply(this);
		handleTemporalCompOp(node.getTemporalCompOp(), true);
	}

	@Override
	public void caseARdwhrReadWhere(ARdwhrReadWhere node) {
		// read_where = {rdwhr} l_par read_where r_par
		node.getReadWhere().apply(this);
	}

	// mapping_factor = l_brc data_mapping r_brc;
	@Override
	public void caseAMappingFactor(AMappingFactor node) {
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.loadStringConstant(node.getDataMapping().getText());
		context.writer.invokeInstance(ExecutionContextMethods.createQuery);
	}

	/** Adds the temporalCompOp to the DatabaseQuery on the evaluation stack */
	private void handleTemporalCompOp(PTemporalCompOp pTemporalCompOp, final boolean negate) {
		final ExpressionCompiler exprCompiler = new ExpressionCompiler(context);

		pTemporalCompOp.apply(new VisitorBase() {
			// temporal_comp_op =
			// {prec} within [left]:expr_string preceding [right]:expr_string
			// | {fol} within [left]:expr_string following [right]:expr_string
			// | {sur} within [left]:expr_string surrounding [right]:expr_string
			// | {within} within [lower]:expr_string to [upper]:expr_string
			// | {past} within past expr_string
			// | {same} within same day as expr_string
			// | {bef} before expr_string
			// | {after} after expr_string
			// | {equal} equal expr_string
			// | {at} at expr_string;
			@Override
			public void caseAPrecTemporalCompOp(APrecTemporalCompOp node) {
				// within [left]:expr_string preceding [right]:expr_string
				caseAPrecTemporalCompOp(node.getLeft(), node.getRight());
			}

			private void caseAPrecTemporalCompOp(Switchable durationExpr, Switchable timeExpr) {
				// within duration preceding time
				// = within (duration before time) to time
				exprCompiler.loadOperator(BinaryOperator.BEFORE);
				durationExpr.apply(exprCompiler);
				timeExpr.apply(exprCompiler);
				// stack: query, argument, BEFORE, dur, time
				context.writer.dup_x2();
				// stack: query, argument, time, BEFORE, dur, time
				exprCompiler.invokeLoadedBinaryOperator();
				// stack: query, argument, time, time2
				context.writer.swap();
				// stack: query, argument, time2, time
				invokeWithinTo();
				// stack: newquery
			}

			@Override
			public void caseAFolTemporalCompOp(AFolTemporalCompOp node) {
				// within [left]:expr_string following [right]:expr_string
				exprCompiler.loadOperator(BinaryOperator.AFTER);
				node.getLeft().apply(exprCompiler);
				node.getRight().apply(exprCompiler);
				// stack: query, argument, AFTER, dur, time
				context.writer.dup_x2();
				// stack: query, argument, time, AFTER, dur, time
				exprCompiler.invokeLoadedBinaryOperator();
				// stack: query, argument, time, time2
				invokeWithinTo();
				// stack: newquery
			}

			@Override
			public void caseASurTemporalCompOp(ASurTemporalCompOp node) {
				// within [left]:expr_string surrounding [right]:expr_string
				// TODO Auto-generated method stub
				throw new RuntimeCompilerException("TODO");
			}

			@Override
			public void caseAWithinTemporalCompOp(AWithinTemporalCompOp node) {
				// within [lower]:expr_string to [upper]:expr_string
				node.getLower().apply(new ExpressionCompiler(context));
				node.getUpper().apply(new ExpressionCompiler(context));
				invokeWithinTo();
			}

			private void invokeWithinTo() {
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotWithinTo",
							DatabaseQuery.class, ArdenValue.class, ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryWithinTo",
							DatabaseQuery.class, ArdenValue.class, ArdenValue.class));
				}
			}

			@Override
			public void caseAPastTemporalCompOp(APastTemporalCompOp node) {
				// within past expr_string
				// = within expr_string preceding now
				caseAPrecTemporalCompOp(node.getExprString(), new ANowTimeValue());
			}

			@Override
			public void caseASameTemporalCompOp(ASameTemporalCompOp node) {
				// within same day as expr_string
				// TODO Auto-generated method stub
				throw new RuntimeCompilerException("TODO");
			}

			@Override
			public void caseABefTemporalCompOp(ABefTemporalCompOp node) {
				// before expr_string
				// TODO Auto-generated method stub
				throw new RuntimeCompilerException("TODO");
			}

			@Override
			public void caseAAfterTemporalCompOp(AAfterTemporalCompOp node) {
				// after expr_string
				// TODO Auto-generated method stub
				super.caseAAfterTemporalCompOp(node);
			}

			@Override
			public void caseAEqualTemporalCompOp(AEqualTemporalCompOp node) {
				// equal expr_string
				// TODO Auto-generated method stub
				super.caseAEqualTemporalCompOp(node);
			}

			@Override
			public void caseAAtTemporalCompOp(AAtTemporalCompOp node) {
				// at expr_string
				// TODO Auto-generated method stub
				super.caseAAtTemporalCompOp(node);
			}
		});
	}
}
