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

		// of_read_func_op =
		// {avge} average
		// | {avg} avg
		// | {cnt} count
		// | {ex} exist
		// | {exs} exists
		// | {sum} sum
		// | {med} median;
		if (node.getOfReadFuncOp() instanceof AAvgeOfReadFuncOp || node.getOfReadFuncOp() instanceof AAvgOfReadFuncOp) {
			invokeAggregationOperator("average");
		} else if (node.getOfReadFuncOp() instanceof ACntOfReadFuncOp) {
			invokeAggregationOperator("count");
		} else if (node.getOfReadFuncOp() instanceof AExOfReadFuncOp
				|| node.getOfReadFuncOp() instanceof AExsOfReadFuncOp) {
			invokeAggregationOperator("exist");
		} else if (node.getOfReadFuncOp() instanceof ASumOfReadFuncOp) {
			invokeAggregationOperator("sum");
		} else if (node.getOfReadFuncOp() instanceof AMedOfReadFuncOp) {
			invokeAggregationOperator("median");
		} else {
			throw new RuntimeException("unknown of_read_func_op");
		}
	}

	private void invokeAggregationOperator(String name) {
		try {
			context.writer.invokeInstance(DatabaseQuery.class.getMethod(name));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void caseAFReadPhrase(AFReadPhrase node) {
		// read_phrase = {f} from_of_func_op read_where
		node.getReadWhere().apply(this);
		handleAggregationOp(node.getFromOfFuncOp());
	}

	@Override
	public void caseAFofReadPhrase(AFofReadPhrase node) {
		// read_phrase = {fof} from_of_func_op of read_where
		node.getReadWhere().apply(this);
		handleAggregationOp(node.getFromOfFuncOp());
	}

	@Override
	public void caseAFoffReadPhrase(AFoffReadPhrase node) {
		// read_phrase = {foff} from_of_func_op expr_factor from read_where;
		node.getReadWhere().apply(this);
		handleTransformationOp(node.getFromOfFuncOp(), node.getExprFactor());
	}

	/** Adds the PFromOfFuncOp to the DatabaseQuery on the evaluation stack */
	private void handleAggregationOp(PFromOfFuncOp node) {
		invokeAggregationOperator(getOperatorName(node));
	}

	private static String getOperatorName(PFromOfFuncOp node) {
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
			return "minimum";
		} else if (node instanceof AMaxiFromOfFuncOp || node instanceof AMaxFromOfFuncOp) {
			return "maximum";
		} else if (node instanceof ALastFromOfFuncOp) {
			return "last";
		} else if (node instanceof AFirFromOfFuncOp) {
			return "first";
		} else if (node instanceof AEarFromOfFuncOp) {
			return "earliest";
		} else if (node instanceof ALatFromOfFuncOp) {
			return "latest";
		} else {
			throw new RuntimeException("unknown from_of_func_op");
		}
	}

	/** Adds the PFromOfFuncOp to the DatabaseQuery on the evaluation stack */
	private void handleTransformationOp(PFromOfFuncOp node, PExprFactor number) {
		// stack: query
		number.apply(new ExpressionCompiler(context));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("getPrimitiveIntegerValue", ArdenValue.class));
		// stack: query, number
		// now emit code like:
		// (number >= 0) ? query.op(number) : DatabaseQuery.NULL;
		context.writer.dup();
		// stack: query, number, number
		Label elseLabel = new Label();
		Label endLabel = new Label();
		context.writer.jumpIfNegative(elseLabel);
		// stack: query, number
		String name = getOperatorName(node);
		try {
			context.writer.invokeInstance(DatabaseQuery.class.getMethod(name, Integer.TYPE));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		// stack: newquery
		context.writer.jump(endLabel);
		context.writer.markForwardJumpsOnly(elseLabel);
		// stack: query, number
		context.writer.pop2();
		// stack: empty
		try {
			context.writer.loadStaticField(DatabaseQuery.class.getField("NULL"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		// stack: newquery
		context.writer.markForwardJumpsOnly(endLabel);
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
		final ExpressionCompiler expressionCompiler = new ExpressionCompiler(context);

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
				expressionCompiler.loadOperator(BinaryOperator.BEFORE);
				durationExpr.apply(expressionCompiler);
				timeExpr.apply(expressionCompiler);
				// stack: query, argument, BEFORE, dur, time
				context.writer.dup_x2();
				// stack: query, argument, time, BEFORE, dur, time
				expressionCompiler.invokeLoadedBinaryOperator();
				// stack: query, argument, time, time2
				context.writer.swap();
				// stack: query, argument, time2, time
				invokeWithinTo();
				// stack: newquery
			}

			@Override
			public void caseAFolTemporalCompOp(AFolTemporalCompOp node) {
				// within [left]:expr_string following [right]:expr_string
				expressionCompiler.loadOperator(BinaryOperator.AFTER);
				node.getLeft().apply(expressionCompiler);
				node.getRight().apply(expressionCompiler);
				// stack: query, argument, AFTER, dur, time
				context.writer.dup_x2();
				// stack: query, argument, time, AFTER, dur, time
				expressionCompiler.invokeLoadedBinaryOperator();
				// stack: query, argument, time, time2
				invokeWithinTo();
				// stack: newquery
			}

			@Override
			public void caseASurTemporalCompOp(ASurTemporalCompOp node) {
				// within [left]:expr_string surrounding [right]:expr_string
				node.getLeft().apply(expressionCompiler);
				node.getRight().apply(expressionCompiler);
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotWithinSurrounding",
							DatabaseQuery.class, ArdenValue.class, ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryWithinSurrounding",
							DatabaseQuery.class, ArdenValue.class, ArdenValue.class));
				}
			}

			@Override
			public void caseAWithinTemporalCompOp(AWithinTemporalCompOp node) {
				// within [lower]:expr_string to [upper]:expr_string
				node.getLower().apply(expressionCompiler);
				node.getUpper().apply(expressionCompiler);
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
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotWithinSameDay",
							DatabaseQuery.class, ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryWithinSameDay",
							DatabaseQuery.class, ArdenValue.class));
				}
			}

			@Override
			public void caseABefTemporalCompOp(ABefTemporalCompOp node) {
				// before expr_string
				node.getExprString().apply(expressionCompiler);
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotBefore",
							DatabaseQuery.class, ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryBefore", DatabaseQuery.class,
							ArdenValue.class));
				}
			}

			@Override
			public void caseAAfterTemporalCompOp(AAfterTemporalCompOp node) {
				// after expr_string
				node.getExprString().apply(expressionCompiler);
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotAfter",
							DatabaseQuery.class, ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryAfter", DatabaseQuery.class,
							ArdenValue.class));
				}
			}

			@Override
			public void caseAEqualTemporalCompOp(AEqualTemporalCompOp node) {
				// equal expr_string
				node.getExprString().apply(expressionCompiler);
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotAt", DatabaseQuery.class,
							ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryAt", DatabaseQuery.class,
							ArdenValue.class));
				}
			}

			@Override
			public void caseAAtTemporalCompOp(AAtTemporalCompOp node) {
				// at expr_string
				node.getExprString().apply(expressionCompiler);
				if (negate) {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryNotAt", DatabaseQuery.class,
							ArdenValue.class));
				} else {
					context.writer.invokeStatic(Compiler.getRuntimeHelper("constrainQueryAt", DatabaseQuery.class,
							ArdenValue.class));
				}
			}
		});
	}
}
