package arden.compiler;

import java.util.ArrayList;
import java.util.List;

import arden.compiler.node.AAnyEventOr;
import arden.compiler.node.AAolstEventAny;
import arden.compiler.node.ACallEvokeStatement;
import arden.compiler.node.AEblkEvokeBlock;
import arden.compiler.node.AEcycEvokeStatement;
import arden.compiler.node.AEdurEvokeTime;
import arden.compiler.node.AEfctEventAny;
import arden.compiler.node.AElstEventList;
import arden.compiler.node.AEmptyEvokeStatement;
import arden.compiler.node.AEorEventList;
import arden.compiler.node.AEorEvokeStatement;
import arden.compiler.node.AEstmtEvokeBlock;
import arden.compiler.node.AEtimEvokeStatement;
import arden.compiler.node.AEvokeDuration;
import arden.compiler.node.AEvokeSlot;
import arden.compiler.node.AIdEventFactor;
import arden.compiler.node.AIdateEvokeTime;
import arden.compiler.node.AIdtEvokeTime;
import arden.compiler.node.AOrEventOr;
import arden.compiler.node.ASimpQualifiedEvokeCycle;
import arden.compiler.node.ASimpleEvokeCycle;
import arden.compiler.node.ASuntQualifiedEvokeCycle;
import arden.compiler.node.ATofEvokeTime;
import arden.compiler.node.PEventAny;
import arden.compiler.node.PEventList;
import arden.compiler.node.PEventOr;
import arden.compiler.node.PEvokeBlock;
import arden.compiler.node.PEvokeStatement;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.events.AnyEvokeEvent;
import arden.runtime.events.EmptyEvokeSlot;
import arden.runtime.events.EvokeEvent;
import arden.runtime.events.FixedDateEvokeEvent;

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
			@Override
			public void caseAEblkEvokeBlock(AEblkEvokeBlock evokeBlock) {
				evokeBlock.getEvokeBlock().apply(this);
				result.add(evokeBlock.getEvokeStatement());
			}
			
			@Override
			public void caseAEstmtEvokeBlock(AEstmtEvokeBlock node) {
				result.add(node.getEvokeStatement());				
			}
		});
		return result;
	}
	
	public List<PEventAny> listAnyBlocks(PEventOr first) {
		final ArrayList<PEventAny> result = new ArrayList<PEventAny>();
		first.apply(new VisitorBase() {
			@Override
			public void caseAOrEventOr(AOrEventOr node) {
				node.getEventOr().apply(this);
				result.add(node.getEventAny());
			}
			
			@Override
			public void caseAAnyEventOr(AAnyEventOr node) {
				result.add(node.getEventAny());
			}
		});
		return result;
	}
	
	public List<PEventOr> listOrBlocks(PEventList first) {
		final ArrayList<PEventOr> result = new ArrayList<PEventOr>();
		first.apply(new VisitorBase() {
			@Override
			public void caseAElstEventList(AElstEventList node) {
				node.getEventList().apply(this);
				result.add(node.getEventOr());
			}
			
			@Override
			public void caseAEorEventList(AEorEventList node) {
				result.add(node.getEventOr());
			}
		});
		return result;
	}
	
	@Override
	public void caseAEvokeSlot(AEvokeSlot evokeSlot) {
		List<PEvokeStatement> statements = listEvokeBlocks(evokeSlot.getEvokeBlock());
		if (statements.size() > 1) {
			throw new RuntimeCompilerException(evokeSlot.getEvoke(), "not implemented yet");
		} else if (statements.size() == 1) {
			statements.get(0).apply(this);
		} else {
			throw new RuntimeCompilerException(evokeSlot.getEvoke(), "no evoke event given");
		}
	}
	
	@Override
	public void caseAEmptyEvokeStatement(AEmptyEvokeStatement stmt) {
		context.writer.newObject(EmptyEvokeSlot.class);
		context.writer.dup();
		try {
			context.writer.invokeConstructor(EmptyEvokeSlot.class.getConstructor());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void caseAEtimEvokeStatement(AEtimEvokeStatement node) {
		node.getEvokeTime().apply(this);
	}
	
	public void createFixedDateEvokeEvent(long datetime) {
		context.writer.newObject(FixedDateEvokeEvent.class);
		context.writer.dup();
		
		context.writer.loadStaticField(context.codeGenerator.getTimeLiteral(datetime));
		try {
			context.writer.invokeConstructor(FixedDateEvokeEvent.class.getConstructor(ArdenTime.class));
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void caseAIdateEvokeTime(AIdateEvokeTime node) {
		createFixedDateEvokeEvent(ParseHelpers.parseIsoDate(node.getIsoDate()));
	}
	
	@Override
	public void caseAIdtEvokeTime(AIdtEvokeTime node) {
		createFixedDateEvokeEvent(ParseHelpers.parseIsoDateTime(node.getIsoDateTime()));
	}
	
	@Override
	public void caseAEorEvokeStatement(AEorEvokeStatement stmt) {
		stmt.getEventOr().apply(this);
	}
	
	@Override
	public void caseAElstEventList(AElstEventList node) {
		List<PEventOr> orBlocks = listOrBlocks(node);
		
		if (orBlocks.size() > 1) {
			context.writer.newObject(AnyEvokeEvent.class);
			context.writer.dup();
			context.writer.loadIntegerConstant(orBlocks.size());
			context.writer.newArray(EvokeEvent.class);
			for (int i = 0; i < orBlocks.size(); i++) {
				context.writer.dup();
				context.writer.loadIntegerConstant(i);
				orBlocks.get(i).apply(this);
				context.writer.storeObjectToArray();
			}
			try {
				context.writer.invokeConstructor(AnyEvokeEvent.class.getConstructor(EvokeEvent[].class));
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		} else if (orBlocks.size() == 1) {
			orBlocks.get(0).apply(this);
		} else {
			throw new RuntimeCompilerException("error parsing event list");
		}
	}
	
	@Override
	public void caseAOrEventOr(AOrEventOr node) {
		List<PEventAny> anyBlocks = listAnyBlocks(node);

		if (anyBlocks.size() > 1) {
			context.writer.newObject(AnyEvokeEvent.class);
			context.writer.dup();
			context.writer.loadIntegerConstant(anyBlocks.size());
			context.writer.newArray(EvokeEvent.class);
			for (int i = 0; i < anyBlocks.size(); i++) {
				context.writer.dup();
				context.writer.loadIntegerConstant(i);
				anyBlocks.get(i).apply(this);
				context.writer.storeObjectToArray();
			}
			try {
				context.writer.invokeConstructor(AnyEvokeEvent.class.getConstructor(EvokeEvent[].class));
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		} else if (anyBlocks.size() == 1) {
			anyBlocks.get(0).apply(this);
		} else {
			throw new RuntimeCompilerException(node.getOr(), "error parsing ");
		}
			
	}
	
	@Override
	public void caseAAnyEventOr(AAnyEventOr anyevent) {
		anyevent.getEventAny().apply(this);
	}
	
	@Override
	public void caseAAolstEventAny(AAolstEventAny node) {
		node.getEventList().apply(this);
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
			throw new RuntimeCompilerException(id.getIdentifier(), "Unknown event variable identifier: \"" + name + "\"");
		if (!(var instanceof EventVariable)) {
			throw new RuntimeCompilerException(id.getIdentifier(), "This is not an event variable: \"" + name + "\"");
		}
		var.loadValue(context, id.getIdentifier());
	}
	
	@Override
	public void caseAEcycEvokeStatement(AEcycEvokeStatement stmt) {
		stmt.getQualifiedEvokeCycle().apply(this);
	}
	
	@Override
	public void caseASimpQualifiedEvokeCycle(ASimpQualifiedEvokeCycle node) {
		node.getSimpleEvokeCycle().apply(this);
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
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("createEvokeCycle", ArdenValue.class, ArdenValue.class, EvokeEvent.class, ExecutionContext.class));
	}
	
	/** leaves ArdenDuration on stack */
	@Override
	public void caseAEvokeDuration(AEvokeDuration duration) {
		double durValue = ParseHelpers.getLiteralDoubleValue(duration.getNumberLiteral());
		context.writer.loadStaticField(context.codeGenerator.getNumberLiteral(durValue));
		new ExpressionCompiler(context).compileDurationOp(duration.getDurationOp());
		context.writer.invokeStatic(ExpressionCompiler.getMethod("createDuration", ArdenValue.class, double.class, boolean.class));
	}
	
	/** leaves EvokeEvent on stack */
	@Override
	public void caseAEdurEvokeTime(AEdurEvokeTime duration) {
		duration.getEvokeDuration().apply(this);
		duration.getEvokeTime().apply(this);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("after", ArdenValue.class, EvokeEvent.class));
	}
	
	/** takes and leaves EvokeEvent on the stack */
	@Override
	public void caseATofEvokeTime(ATofEvokeTime node) {
		node.getEventAny().apply(this);
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.invokeStatic(ExpressionCompiler.getMethod("timeOf", EvokeEvent.class, ExecutionContext.class));
	}
	
	@Override
	public void caseACallEvokeStatement(ACallEvokeStatement node) {
		context.writer.invokeStatic(ExpressionCompiler.getMethod("evokeSlotCall"));
	}
}
