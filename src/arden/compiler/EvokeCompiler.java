package arden.compiler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.AAnyEventOr;
import arden.compiler.node.AEblkEvokeBlock;
import arden.compiler.node.AEcycEvokeStatement;
import arden.compiler.node.AEdurEvokeTime;
import arden.compiler.node.AEfctEventAny;
import arden.compiler.node.AEmptyEvokeStatement;
import arden.compiler.node.AEorEvokeStatement;
import arden.compiler.node.AEstmtEvokeBlock;
import arden.compiler.node.AEvokeDuration;
import arden.compiler.node.AEvokeSlot;
import arden.compiler.node.AIdEventFactor;
import arden.compiler.node.ASimpleEvokeCycle;
import arden.compiler.node.ASuntQualifiedEvokeCycle;
import arden.compiler.node.ATofEvokeTime;
import arden.compiler.node.PEvokeBlock;
import arden.compiler.node.PEvokeStatement;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.events.NeverEvokeEvent;
import arden.runtime.events.EvokeEvent;

public class EvokeCompiler extends VisitorBase {
	private final CompilerContext context;
	
	public EvokeCompiler(CompilerContext context) {
		this.context = context;		
	}
		
	public List<PEvokeStatement> listEvokeBlocks(PEvokeBlock first) {
		/* 	
			evoke_block =
			      {estmt}  evoke_statement
			    | {eblk}   evoke_block semicolon evoke_statement;
		 * */
		final ArrayList<PEvokeStatement> result = new ArrayList<PEvokeStatement>();
		first.apply(new VisitorBase() {
			public void caseAEblkEvokeBlock(AEblkEvokeBlock evokeBlock) {
				evokeBlock.getEvokeBlock().apply(this);
				result.add(evokeBlock.getEvokeStatement());
			}
			
			public void caseAEstmtEvokeBlock(AEstmtEvokeBlock node) {
				result.add(node.getEvokeStatement());				
			}
		});
		return result;
	}
	
	@Override
	public void caseAEvokeSlot(AEvokeSlot evokeSlot) {
		List<PEvokeStatement> statements = listEvokeBlocks(evokeSlot.getEvokeBlock());
		if (statements.size() > 1) {
			throw new RuntimeException("not implemented yet");
		} else if (statements.size() == 1) {
			statements.get(0).apply(this);
		}
	}
	
	@Override
	public void caseAEmptyEvokeStatement(AEmptyEvokeStatement stmt) {
		context.writer.newObject(NeverEvokeEvent.class);
		context.writer.dup();
		try {
			context.writer.invokeConstructor(NeverEvokeEvent.class.getConstructor());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void caseAEorEvokeStatement(AEorEvokeStatement stmt) {
		stmt.getEventOr().apply(this);
	}
	
	@Override
	public void caseAAnyEventOr(AAnyEventOr anyevent) {
		anyevent.getEventAny().apply(this);
	}
	
	@Override
	public void caseAEfctEventAny(AEfctEventAny factor) {
		factor.getEventFactor().apply(this);
	}
	
	@Override
	public void caseAIdEventFactor(AIdEventFactor id) {
		String name = id.getIdentifier().getText();
		Variable var = context.codeGenerator.getVariable(name);
		if (var == null)
			throw new RuntimeCompilerException(id.getIdentifier(), "Unknown event variable: " + name);
		var.loadValue(context, id.getIdentifier());
		//context.writer.invokeStatic(ExpressionCompiler.getMethod("mlmVariableToEvokeEvent", Object.class));
	}
	
	@Override
	public void caseAEcycEvokeStatement(AEcycEvokeStatement stmt) {
		stmt.getQualifiedEvokeCycle().apply(this);
	}
	
	/** leaves EvokeEvent on stack */
	@Override
	public void caseASuntQualifiedEvokeCycle(ASuntQualifiedEvokeCycle qualifiedcycle) {
		qualifiedcycle.getSimpleEvokeCycle().apply(this);
		qualifiedcycle.getExpr().apply(new ExpressionCompiler(context));
		context.writer.invokeStatic(ExpressionCompiler.getMethod("until", EvokeEvent.class, ArdenValue.class));
	}
	
	/** leaves EvokeEvent on stack */
	@Override
	public void caseASimpleEvokeCycle(ASimpleEvokeCycle simplecycle) {
		simplecycle.getDurL().apply(this); // interval
		simplecycle.getDurR().apply(this); // for
		simplecycle.getEvokeTime().apply(this); // starting
		context.writer.invokeStatic(ExpressionCompiler.getMethod("createEvokeCycle", ArdenValue.class, ArdenValue.class, ArdenValue.class));
	}
	
	/** leaves ArdenDuration on stack */
	@Override
	public void caseAEvokeDuration(AEvokeDuration duration) {
		double durValue = ParseHelpers.getLiteralDoubleValue(duration.getNumberLiteral());
		context.writer.loadStaticField(context.codeGenerator.getNumberLiteral(durValue));
		new ExpressionCompiler(context).compileDurationOp(duration.getDurationOp());
		context.writer.invokeStatic(ExpressionCompiler.getMethod("createDuration", ArdenValue.class, double.class, boolean.class));
	}
	
	/** leaves ArdenValue on stack */
	@Override
	public void caseAEdurEvokeTime(AEdurEvokeTime duration) {
		duration.getEvokeDuration().apply(this);
		duration.getEvokeTime().apply(this);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("after", ArdenValue.class, ArdenValue.class));
	}
	
	/** converts EvokeEvent to ArdenTime or ArdenNull */
	@Override
	public void caseATofEvokeTime(ATofEvokeTime node) {
		node.getEventAny().apply(this);
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("timeOf", EvokeEvent.class, ExecutionContext.class));
	}
}
