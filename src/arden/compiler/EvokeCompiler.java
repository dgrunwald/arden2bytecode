package arden.compiler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.AEblkEvokeBlock;
import arden.compiler.node.AEmptyEvokeStatement;
import arden.compiler.node.AEstmtEvokeBlock;
import arden.compiler.node.AEvokeSlot;
import arden.compiler.node.PEvokeBlock;
import arden.compiler.node.PEvokeStatement;
import arden.runtime.events.EmptyEvokeEvent;
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
		context.writer.newObject(EmptyEvokeEvent.class);
		context.writer.dup();
		try {
			context.writer.invokeConstructor(EmptyEvokeEvent.class.getConstructor());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
