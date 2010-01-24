package arden.compiler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenNull;
import arden.runtime.DatabaseQuery;
import arden.runtime.MedicalLogicModule;

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
		// | {mlm} T.mlm term
		// | {mlmi} T.mlm term from institution string_literal
		// | {mlms} T.mlm T.mlm_self
		// | {imap} interface mapping_factor
		// | {emap} event mapping_factor
		// | {mmap} message mapping_factor
		// | {dmap} destination mapping_factor
		// | {arg} argument
		// | {cphr} call_phrase
		// | {expr} expr;
		node.getDataAssignPhrase().apply(new VisitorBase() {
			@Override
			public void caseAReadDataAssignPhrase(AReadDataAssignPhrase node) {
				// {read} read read_phrase
				assignPhrase(lhs, node.getReadPhrase());
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
				if (!(lhs instanceof LeftHandSideIdentifier))
					throw new RuntimeCompilerException(lhs.getPosition(),
							"INTERFACE variables must be simple identifiers");
				TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
				context.codeGenerator.addVariable(new InterfaceVariable(ident, node.getMappingFactor()));
			}

			@Override
			public void caseAEmapDataAssignPhrase(AEmapDataAssignPhrase node) {
				// {emap} event mapping_factor
				if (!(lhs instanceof LeftHandSideIdentifier))
					throw new RuntimeCompilerException(lhs.getPosition(), "EVENT variables must be simple identifiers");
				TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
				context.codeGenerator.addVariable(new EventVariable(ident, node.getMappingFactor()));
			}

			@Override
			public void caseAMmapDataAssignPhrase(AMmapDataAssignPhrase node) {
				// {mmap} message mapping_factor
				if (!(lhs instanceof LeftHandSideIdentifier))
					throw new RuntimeCompilerException(lhs.getPosition(),
							"MESSAGE variables must be simple identifiers");
				TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
				context.codeGenerator.addVariable(new MessageVariable(ident, node.getMappingFactor()));
			}

			@Override
			public void caseADmapDataAssignPhrase(ADmapDataAssignPhrase node) {
				// {dmap} destination mapping_factor
				if (!(lhs instanceof LeftHandSideIdentifier))
					throw new RuntimeCompilerException(lhs.getPosition(),
							"DESTINATION variables must be simple identifiers");
				TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
				context.codeGenerator.addVariable(new DestinationVariable(ident, node.getMappingFactor()));
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
			public void caseAExprDataAssignPhrase(AExprDataAssignPhrase node) {
				// {expr} expr
				assignExpression(lhs, node.getExpr());
			}
		});
	}

	@Override
	public void caseATexprDataAssignment(ATexprDataAssignment node) {
		// data_assignment = {texpr} time_becomes expr
		assignExpression(LeftHandSideAnalyzer.analyze(node.getTimeBecomes()), node.getExpr());
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
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "MLM variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
		FieldReference mlmField = context.codeGenerator.createField(ident.getText(), MedicalLogicModule.class,
				Modifier.PRIVATE);
		context.writer.sequencePoint(ident.getLine());
		context.writer.loadThis();
		if (name == null) {
			context.writer.loadVariable(context.selfMLMVariable);
		} else {
			context.writer.loadVariable(context.executionContextVariable);
			context.writer.loadStringConstant(name.getText());
			if (institution != null) {
				context.writer.loadStringConstant(ParseHelpers.getLiteralStringValue(institution));
			} else {
				context.writer.loadNull();
			}
			context.writer.invokeInstance(ExecutionContextMethods.findModule);
		}
		context.writer.storeInstanceField(mlmField);
		context.codeGenerator.addVariable(new MlmVariable(ident, mlmField));
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
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		assignResultFromPhrase(lhs);
	}

	/** Assigns a call phrase to the variable. */
	private void assignPhrase(LeftHandSideResult lhs, PCallPhrase callPhrase) {
		// TODO
		throw new RuntimeCompilerException("TODO");
		// assignResultFromPhrase(lhs);
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

	private void assignExpression(LeftHandSideResult lhs, PExpr expr) {
		lhs.assign(context, expr);
	}
}
