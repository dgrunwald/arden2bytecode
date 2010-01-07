package arden.compiler;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import arden.codegenerator.MethodWriter;
import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.lexer.Lexer;
import arden.compiler.lexer.LexerException;
import arden.compiler.node.AIdUrgencyVal;
import arden.compiler.node.AKnowledgeBody;
import arden.compiler.node.AKnowledgeCategory;
import arden.compiler.node.AMlm;
import arden.compiler.node.ANumUrgencyVal;
import arden.compiler.node.AUrgUrgencySlot;
import arden.compiler.node.PActionSlot;
import arden.compiler.node.PDataSlot;
import arden.compiler.node.PLogicSlot;
import arden.compiler.node.PUrgencySlot;
import arden.compiler.node.PUrgencyVal;
import arden.compiler.node.Start;
import arden.compiler.node.TIdentifier;
import arden.compiler.parser.Parser;
import arden.compiler.parser.ParserException;
import arden.runtime.MedicalLogicModule;

/**
 * The main class of the compiler.
 * 
 * @author Daniel Grunwald
 */
public final class Compiler {
	private boolean enableDebugging = true;

	public boolean getEnableDebugging() {
		return enableDebugging;
	}

	public void setEnableDebugging(boolean value) {
		enableDebugging = value;
	}

	public MedicalLogicModule compileMlm(Reader input) throws CompilerException, IOException {
		List<MedicalLogicModule> output = compile(input);
		if (output.size() != 1)
			throw new CompilerException("Expected only a single MLM per file", 0, 0);
		return output.get(0);
	}

	public List<MedicalLogicModule> compile(Reader input) throws CompilerException, IOException {
		Lexer lexer = new Lexer(new PushbackReader(input, 1024));
		Parser parser = new Parser(lexer);
		Start syntaxTree;
		try {
			syntaxTree = parser.parse();
		} catch (ParserException e) {
			throw new CompilerException(e);
		} catch (LexerException e) {
			throw new CompilerException(e);
		}
		return compile(syntaxTree);
	}

	public List<MedicalLogicModule> compile(Start syntaxTree) throws CompilerException {
		try {
			final ArrayList<MedicalLogicModule> output = new ArrayList<MedicalLogicModule>();
			// find all AMlm nodes and compile each individually
			syntaxTree.apply(new DepthFirstAdapter() {
				@Override
				public void caseAMlm(AMlm node) {
					output.add(doCompileMlm(node));
				}
			});
			return output;
		} catch (RuntimeCompilerException ex) {
			throw new CompilerException(ex);
		}
	}

	public MedicalLogicModule compileMlm(AMlm mlm) throws CompilerException {
		try {
			return doCompileMlm(mlm);
		} catch (RuntimeCompilerException ex) {
			throw new CompilerException(ex);
		}
	}

	private MedicalLogicModule doCompileMlm(AMlm mlm) {
		AKnowledgeCategory knowledgeCategory = (AKnowledgeCategory) mlm.getKnowledgeCategory();
		AKnowledgeBody knowledge = (AKnowledgeBody) knowledgeCategory.getKnowledgeBody();

//		System.out.println(knowledge.toString());
//		knowledge.apply(new PrintTreeVisitor(System.out));

		CodeGenerator codeGen = new CodeGenerator("xyz");

		compileData(codeGen, knowledge.getDataSlot());
		compileLogic(codeGen, knowledge.getLogicSlot());
		compileAction(codeGen, knowledge.getActionSlot());
		compileUrgency(codeGen, knowledge.getUrgencySlot());

		return new CompiledMlm(codeGen.loadClassFromMemory());
	}

	void compileData(CodeGenerator codeGen, PDataSlot dataSlot) {
		MethodWriter ctor = codeGen.createConstructor();
		if (enableDebugging)
			ctor.enableLineNumberTable();
		ctor.returnFromProcedure();
	}

	void compileLogic(CodeGenerator codeGen, PLogicSlot logicSlot) {
		MethodWriter logic = codeGen.createLogic();
		if (enableDebugging)
			logic.enableLineNumberTable();
		logic.loadIntegerConstant(1);
		logic.returnIntFromFunction();
	}

	void compileAction(CodeGenerator codeGen, PActionSlot actionSlot) {
		MethodWriter action = codeGen.createAction();
		if (enableDebugging)
			action.enableLineNumberTable();
		if (actionSlot != null) {
			CompilerContext context = new CompilerContext(codeGen, action);
			actionSlot.apply(new ActionCompiler(context));
		}
		action.loadNull();
		action.returnObjectFromFunction();
	}

	void compileUrgency(CodeGenerator codeGen, PUrgencySlot urgencySlot) {
		if (urgencySlot instanceof AUrgUrgencySlot) {
			PUrgencyVal val = ((AUrgUrgencySlot) urgencySlot).getUrgencyVal();
			MethodWriter urgency = codeGen.createUrgency();
			if (enableDebugging)
				urgency.enableLineNumberTable();
			if (val instanceof ANumUrgencyVal) {
				urgency.loadDoubleConstant(ParseHelpers.getLiteralDoubleValue(((ANumUrgencyVal) val).getNumber()));
			} else if (val instanceof AIdUrgencyVal) {
				TIdentifier ident = ((AIdUrgencyVal) val).getIdentifier();
				Variable var = codeGen.getVariableOrShowError(ident);
				throw new RuntimeCompilerException(ident, "Urgency cannot use this type of variable.");
			} else {
				throw new RuntimeException("Unknown urgency value");
			}
			urgency.returnDoubleFromFunction();
		}
	}
}
