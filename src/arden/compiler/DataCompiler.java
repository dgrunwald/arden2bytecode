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

import java.util.ArrayList;
import java.util.List;

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.ObjectType;

/**
 * Compiler for data block.
 * 
 * Every dataBlock.apply(this) call will generate code that executes the
 * statements. The evaluation stack is empty between all apply calls.
 * 
 * @author Daniel Grunwald
 */
final class DataCompiler extends VisitorBase {
	private final CompilerContext context;

	public DataCompiler(CompilerContext context) {
		this.context = context;
	}

	// data_slot = data data_block semicolons;
	@Override
	public void caseADataSlot(ADataSlot node) {
		node.getDataBlock().apply(this);
	}

	// data_block =
	// {block} data_block semicolon data_statement
	// | {func} data_statement;
	@Override
	public void caseABlockDataBlock(ABlockDataBlock node) {
		// data_block = {block} data_block semicolon data_statement
		node.getDataBlock().apply(this);
		node.getDataStatement().apply(this);
	}

	@Override
	public void caseAFuncDataBlock(AFuncDataBlock node) {
		// data_block = {func} data_statement
		node.getDataStatement().apply(this);
	}

	// data_statement =
	// {empty}
	// | {ass} data_assignment
	// | {if} if data_if_then_else2
	// | {for} for identifier in expr do data_block semicolon enddo
	// | {while} while expr do data_block semicolon enddo;
	@Override
	public void caseAEmptyDataStatement(AEmptyDataStatement node) {
		// data_statement = {empty}
	}

	@Override
	public void caseAAssDataStatement(AAssDataStatement node) {
		// data_statement = {ass} data_assignment
		node.getDataAssignment().apply(this);
	}

	@Override
	public void caseAIfDataStatement(AIfDataStatement node) {
		// data_statement = {if} if data_if_then_else2
		context.writer.sequencePoint(node.getIf().getLine());
		node.getDataIfThenElse2().apply(this);
	}

	@Override
	public void caseAForDataStatement(AForDataStatement node) {
		// data_statement =
		// {for} for identifier in expr do data_block semicolon enddo
		ActionCompiler.compileForStatement(context, node.getFor(), node.getIdentifier(), node.getExpr(), node
				.getDataBlock(), this);
	}

	@Override
	public void caseAWhileDataStatement(AWhileDataStatement node) {
		// data_statement = {while} while expr do data_block semicolon enddo;
		ActionCompiler.compileWhileStatement(context, node.getWhile(), node.getExpr(), node.getDataBlock(), this);
	}

	// data_if_then_else2 = expr then data_block semicolon data_elseif;
	@Override
	public void caseADataIfThenElse2(ADataIfThenElse2 node) {
		ActionCompiler.compileIfStatement(context, node.getExpr(), node.getDataBlock(), node.getDataElseif(), this);
	}

	// data_elseif =
	// {end} endif
	// | {else} else data_block semicolon endif
	// | {elseif} elseif data_if_then_else2;
	@Override
	public void caseAEndDataElseif(AEndDataElseif node) {
	}

	@Override
	public void caseAElseDataElseif(AElseDataElseif node) {
		node.getDataBlock().apply(this);
	}

	@Override
	public void caseAElseifDataElseif(AElseifDataElseif node) {
		context.writer.sequencePoint(node.getElseif().getLine());
		node.getDataIfThenElse2().apply(this);
	}

	// data_assignment =
	// {iphr} identifier_becomes data_assign_phrase
	// | {texpr} time_becomes expr
	// | {lphr} l_par data_var_list r_par assign read read_phrase
	// | {llphr} let l_par data_var_list r_par be read read_phrase
	// | {laarg} l_par data_var_list r_par assign argument
	// | {llbarg} let l_par data_var_list r_par be argument;
	@Override
	public void caseAIphrDataAssignment(AIphrDataAssignment node) {
		// data_assignment = {iphr} identifier_becomes data_assign_phrase
		final LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes());

		// data_assign_phrase =
		// {read} read read_phrase
		// | {readas} read as identifier read_phrase
		// | {mlm} T.mlm term
		// | {mlmi} T.mlm term from institution string_literal
		// | {mlms} T.mlm T.mlm_self
		// | {imap} interface mapping_factor
		// | {emap} event mapping_factor
		// | {mmap} message mapping_factor
		// | {masmap} message as identifier mapping_factor?
		// | {dmap} destination mapping_factor
		// | {dasmap} destination as identifier mapping_factor?
		// | {object} object l_brk object_attribute_list r_brk
		// | {arg} argument
		// | {cphr} call_phrase
		// | {newobj} new_object_phrase
		// | {expr} expr;
		node.getDataAssignPhrase().apply(new VisitorBase() {
			@Override
			public void caseAReadDataAssignPhrase(AReadDataAssignPhrase node) {
				// {read} read read_phrase
				assignPhrase(lhs, node.getReadPhrase());
			}

			@Override
			public void caseAReadasDataAssignPhrase(final AReadasDataAssignPhrase node) {
				// {readas} read as identifier read_phrase
				final Variable v = context.codeGenerator.getVariableOrShowError(node.getIdentifier());
				if (!(v instanceof ObjectTypeVariable))
					throw new RuntimeCompilerException(lhs.getPosition(), "EVENT variables must be simple identifiers");
				lhs.assign(context, new Switchable() {
					@Override
					public void apply(Switch sw) {
						node.getReadPhrase().apply(new ReadPhraseCompiler(context));
						try {
							context.writer.invokeInstance(DatabaseQuery.class.getMethod("execute"));
						} catch (NoSuchMethodException e) {
							throw new RuntimeException(e);
						}
						context.writer.loadStaticField(((ObjectTypeVariable) v).field);
						context.writer.invokeStatic(Compiler.getRuntimeHelper("readAs", ArdenValue[].class,
								ObjectType.class));
					}
				});
			}

			@Override
			public void caseAMlmDataAssignPhrase(AMlmDataAssignPhrase node) {
				// {mlm} T.mlm term
				createMlmVariable(lhs, node.getTerm(), null);
			}

			@Override
			public void caseAMlmiDataAssignPhrase(AMlmiDataAssignPhrase node) {
				// {mlmi} T.mlm term from institution string_literal
				createMlmVariable(lhs, node.getTerm(), node.getStringLiteral());
			}

			@Override
			public void caseAMlmsDataAssignPhrase(AMlmsDataAssignPhrase node) {
				// {mlms} T.mlm T.mlm_self
				createMlmVariable(lhs, null, null);
			}

			@Override
			public void caseAImapDataAssignPhrase(AImapDataAssignPhrase node) {
				// {imap} interface mapping_factor
				CallableVariable var = CallableVariable.getCallableVariable(context.codeGenerator, lhs);
				context.writer.sequencePoint(lhs.getPosition().getLine());
				context.writer.loadThis();
				context.writer.loadVariable(context.executionContextVariable);
				context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
				context.writer.invokeInstance(ExecutionContextMethods.findInterface);
				context.writer.storeInstanceField(var.mlmField);
			}

			@Override
			public void caseAEmapDataAssignPhrase(AEmapDataAssignPhrase node) {
				// {emap} event mapping_factor
				EventVariable e = EventVariable.getEventVariable(context.codeGenerator, lhs);
				context.writer.sequencePoint(lhs.getPosition().getLine());
				context.writer.loadThis();
				context.writer.loadVariable(context.executionContextVariable);
				context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
				context.writer.invokeInstance(ExecutionContextMethods.getEvent);
				context.writer.storeInstanceField(e.field);
			}

			@Override
			public void caseAMmapDataAssignPhrase(AMmapDataAssignPhrase node) {
				// {mmap} message mapping_factor
				final String mappingString = ParseHelpers.getStringForMapping(node.getMappingFactor());
				lhs.assign(context, new Switchable() {
					@Override
					public void apply(Switch sw) {
						context.writer.loadVariable(context.executionContextVariable);
						context.writer.loadStringConstant(mappingString);
						context.writer.invokeInstance(ExecutionContextMethods.getMessage);
					}
				});
			}

			@Override
			public void caseAMasmapDataAssignPhrase(AMasmapDataAssignPhrase node) {
				// {masmap} message as identifier mapping_factor?
				// TODO Auto-generated method stub
				super.caseAMasmapDataAssignPhrase(node);
			}

			@Override
			public void caseADmapDataAssignPhrase(ADmapDataAssignPhrase node) {
				// {dmap} destination mapping_factor
				DestinationVariable v = DestinationVariable.getDestinationVariable(context.codeGenerator, lhs);
				context.writer.loadThis();
				context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
				context.writer.storeInstanceField(v.field);
			}

			@Override
			public void caseADasmapDataAssignPhrase(ADasmapDataAssignPhrase node) {
				// {dasmap} destination as identifier mapping_factor?
				// TODO Auto-generated method stub
				super.caseADasmapDataAssignPhrase(node);
			}

			@Override
			public void caseAObjectDataAssignPhrase(AObjectDataAssignPhrase node) {
				// {object} object l_brk object_attribute_list r_brk
				ObjectTypeVariable.create(context.codeGenerator, lhs, node.getObjectAttributeList());
			}

			@Override
			public void caseAArgDataAssignPhrase(AArgDataAssignPhrase node) {
				// {arg} argument
				assignArgument(lhs);
			}

			@Override
			public void caseACphrDataAssignPhrase(ACphrDataAssignPhrase node) {
				// {cphr} call_phrase
				assignPhrase(lhs, node.getCallPhrase());
			}

			@Override
			public void caseANewobjDataAssignPhrase(ANewobjDataAssignPhrase node) {
				// {newobj} new_object_phrase
				lhs.assign(context, node.getNewObjectPhrase());
			}

			@Override
			public void caseAExprDataAssignPhrase(AExprDataAssignPhrase node) {
				// {expr} expr
				lhs.assign(context, node.getExpr());
			}
		});
	}

	@Override
	public void caseATexprDataAssignment(ATexprDataAssignment node) {
		// data_assignment = {texpr} time_becomes expr
		LeftHandSideAnalyzer.analyze(node.getTimeBecomes()).assign(context, node.getExpr());
	}

	@Override
	public void caseALphrDataAssignment(ALphrDataAssignment node) {
		// data_assignment = {lphr} l_par data_var_list r_par assign read
		// read_phrase
		assignPhrase(LeftHandSideAnalyzer.analyze(node.getDataVarList()), node.getReadPhrase());
	}

	@Override
	public void caseALlphrDataAssignment(ALlphrDataAssignment node) {
		// data_assignment = {llphr} let l_par data_var_list r_par be read
		// read_phrase
		assignPhrase(LeftHandSideAnalyzer.analyze(node.getDataVarList()), node.getReadPhrase());
	}

	@Override
	public void caseALaargDataAssignment(ALaargDataAssignment node) {
		// data_assignment = {laarg} l_par data_var_list r_par assign argument
		assignArgument(LeftHandSideAnalyzer.analyze(node.getDataVarList()));
	}

	@Override
	public void caseALlbargDataAssignment(ALlbargDataAssignment node) {
		// data_assignment = {llbarg} let l_par data_var_list r_par be argument;
		assignArgument(LeftHandSideAnalyzer.analyze(node.getDataVarList()));
	}

	/** Creates an MLM variable. */
	private void createMlmVariable(LeftHandSideResult lhs, TTerm name, TStringLiteral institution) {
		CallableVariable var = CallableVariable.getCallableVariable(context.codeGenerator, lhs);
		context.writer.sequencePoint(lhs.getPosition().getLine());
		context.writer.loadThis();
		if (name == null) {
			context.writer.loadVariable(context.selfMLMVariable);
		} else {
			context.writer.loadVariable(context.executionContextVariable);
			context.writer.loadStringConstant(ParseHelpers.getMlmName(name));
			if (institution != null) {
				context.writer.loadStringConstant(ParseHelpers.getLiteralStringValue(institution));
			} else {
				context.writer.loadNull();
			}
			context.writer.invokeInstance(ExecutionContextMethods.findModule);
		}
		context.writer.storeInstanceField(var.mlmField);
	}

	/** Assigns the argument to the variable. */
	private void assignArgument(LeftHandSideResult lhs) {
		context.writer.loadVariable(context.argumentsVariable);
		assignResultFromPhrase(lhs);
	}

	/** Assigns a read phrase to the variable. */
	private void assignPhrase(LeftHandSideResult lhs, PReadPhrase readPhrase) {
		context.writer.sequencePoint(lhs.getPosition().getLine());
		readPhrase.apply(new ReadPhraseCompiler(context));
		try {
			context.writer.invokeInstance(DatabaseQuery.class.getMethod("execute"));
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		assignResultFromPhrase(lhs);
	}

	/** Assigns a call phrase to the variable. */
	public void assignPhrase(LeftHandSideResult lhs, PCallPhrase callPhrase) {
		context.writer.sequencePoint(lhs.getPosition().getLine());

		// call_phrase =
		// {id} call identifier
		// | {idex} call identifier with expr;
		TIdentifier identifier;
		PExpr arguments;
		if (callPhrase instanceof AIdCallPhrase) {
			identifier = ((AIdCallPhrase) callPhrase).getIdentifier();
			arguments = null;
		} else if (callPhrase instanceof AIdexCallPhrase) {
			identifier = ((AIdexCallPhrase) callPhrase).getIdentifier();
			arguments = ((AIdexCallPhrase) callPhrase).getExpr();
		} else {
			throw new RuntimeException("unknown call phrase");
		}
		Variable var = context.codeGenerator.getVariableOrShowError(identifier);
		var.call(context, lhs.getPosition(), arguments);
		assignResultFromPhrase(lhs);
	}

	private void assignResultFromPhrase(LeftHandSideResult lhs) {
		final int phraseResultVar = context.allocateVariable();
		// store phrase result in variable
		context.writer.storeVariable(phraseResultVar);

		List<LeftHandSideIdentifier> idents;
		if (lhs instanceof LeftHandSideIdentifier) {
			idents = new ArrayList<LeftHandSideIdentifier>();
			idents.add((LeftHandSideIdentifier) lhs);
		} else if (lhs instanceof LeftHandSideIdentifierList) {
			idents = ((LeftHandSideIdentifierList) lhs).getList();
		} else {
			throw new RuntimeCompilerException(lhs.getPosition(), "Cannot use READ or CALL phrase in this context.");
		}
		for (int i = 0; i < idents.size(); i++) {
			final int identNumber = i;
			// for each identifier, emit:
			// var_i = (i < phraseResult.Length)
			// ? phraseResult.Length[i] : ArdenNull.Instance;
			idents.get(i).assign(context, new Switchable() {
				@Override
				public void apply(Switch sw) {
					Label trueLabel = new Label();
					Label endLabel = new Label();
					context.writer.loadIntegerConstant(identNumber);
					context.writer.loadVariable(phraseResultVar);
					context.writer.arrayLength();
					context.writer.jumpIfLessThan(trueLabel);
					// false part
					new ANullExprFactorAtom().apply(sw);
					context.writer.jump(endLabel);
					// true part
					context.writer.markForwardJumpsOnly(trueLabel);
					context.writer.loadVariable(phraseResultVar);
					context.writer.loadIntegerConstant(identNumber);
					context.writer.loadObjectFromArray();
					context.writer.markForwardJumpsOnly(endLabel);
				}
			});
		}
	}
}
