package arden.compiler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.lexer.Lexer;
import arden.compiler.lexer.LexerException;
import arden.compiler.node.ADataSlot;
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
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.RuntimeHelpers;

/**
 * The main class of the compiler.
 * 
 * @author Daniel Grunwald
 */
public final class Compiler {
	private boolean isDebuggingEnabled = false;
	private String sourceFileName;

	/** Enables debugging for the code being produced. */
	public void enableDebugging(String sourceFileName) {
		this.isDebuggingEnabled = true;
		this.sourceFileName = sourceFileName;
	}

	/** Compiles a single MLM given in the input stream. */
	public CompiledMlm compileMlm(Reader input) throws CompilerException, IOException {
		List<CompiledMlm> output = compile(input);
		if (output.size() != 1)
			throw new CompilerException("Expected only a single MLM per file", 0, 0);
		return output.get(0);
	}

	/** Compiles a list of MLMs given in the input stream. */
	public List<CompiledMlm> compile(Reader input) throws CompilerException, IOException {
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

	/** Compiles a list of MLMs given in the syntax tree. */
	public List<CompiledMlm> compile(Start syntaxTree) throws CompilerException {
		try {
			final ArrayList<CompiledMlm> output = new ArrayList<CompiledMlm>();
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

	/** Compiles a single MLMs given in the syntax tree. */
	public MedicalLogicModule compileMlm(AMlm mlm) throws CompilerException {
		try {
			return doCompileMlm(mlm);
		} catch (RuntimeCompilerException ex) {
			throw new CompilerException(ex);
		}
	}

	private CompiledMlm doCompileMlm(AMlm mlm) {
		MetadataCompiler metadata = new MetadataCompiler();
		mlm.getMaintenanceCategory().apply(metadata);
		mlm.getLibraryCategory().apply(metadata);

		AKnowledgeCategory knowledgeCategory = (AKnowledgeCategory) mlm.getKnowledgeCategory();
		AKnowledgeBody knowledge = (AKnowledgeBody) knowledgeCategory.getKnowledgeBody();
		knowledge.getPrioritySlot().apply(metadata);

		// System.out.println(knowledge.toString());
		// knowledge.apply(new PrintTreeVisitor(System.out));

		CodeGenerator codeGen = new CodeGenerator(metadata.maintenance.getMlmName(), knowledgeCategory.getKnowledge()
				.getLine());
		if (isDebuggingEnabled)
			codeGen.enableDebugging(sourceFileName);

		compileData(codeGen, knowledge.getDataSlot());
		compileLogic(codeGen, knowledge.getLogicSlot());
		compileAction(codeGen, knowledge.getActionSlot());
		compileUrgency(codeGen, knowledge.getUrgencySlot());

		byte[] data;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream s = new DataOutputStream(bos);
			codeGen.save(s);
			s.close();
			data = bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new CompiledMlm(data, metadata.maintenance, metadata.library, metadata.priority);
	}

	private void compileData(CodeGenerator codeGen, PDataSlot dataSlot) {
		int lineNumber = ((ADataSlot) dataSlot).getDataColon().getLine();
		CompilerContext context = codeGen.createConstructor(lineNumber);
		dataSlot.apply(new DataCompiler(context));
		context.writer.returnFromProcedure();
	}

	private void compileLogic(CodeGenerator codeGen, PLogicSlot logicSlot) {
		CompilerContext context = codeGen.createLogic();
		logicSlot.apply(new LogicCompiler(context));
		// CONCLUDE FALSE; is default
		context.writer.loadIntegerConstant(0);
		context.writer.returnIntFromFunction();
	}

	private void compileAction(CodeGenerator codeGen, PActionSlot actionSlot) {
		CompilerContext context = codeGen.createAction();
		if (actionSlot != null) {
			actionSlot.apply(new ActionCompiler(context));
		}
		context.writer.loadNull();
		context.writer.returnObjectFromFunction();
	}

	private void compileUrgency(CodeGenerator codeGen, PUrgencySlot urgencySlot) {
		// urgency_slot =
		// {empty}
		// | {urg} urgency urgency_val semicolons;
		if (urgencySlot instanceof AUrgUrgencySlot) {
			PUrgencyVal val = ((AUrgUrgencySlot) urgencySlot).getUrgencyVal();
			CompilerContext context = codeGen.createUrgency();
			context.writer.sequencePoint(((AUrgUrgencySlot) urgencySlot).getUrgency().getLine());
			// urgency_val =
			// {num} P.number
			// | {id} identifier;
			if (val instanceof ANumUrgencyVal) {
				context.writer.loadDoubleConstant(ParseHelpers
						.getLiteralDoubleValue(((ANumUrgencyVal) val).getNumber()));
			} else if (val instanceof AIdUrgencyVal) {
				TIdentifier ident = ((AIdUrgencyVal) val).getIdentifier();
				Variable var = codeGen.getVariableOrShowError(ident);
				if (var instanceof DataVariable) {
					var.loadValue(context, ident);
					context.writer.invokeStatic(getRuntimeHelper("urgencyGetPrimitiveValue", ArdenValue.class));
				} else {
					throw new RuntimeCompilerException(ident, "Urgency cannot use this type of variable.");
				}
			} else {
				throw new RuntimeException("Unknown urgency value");
			}
			context.writer.returnDoubleFromFunction();
		}
	}

	static Method getRuntimeHelper(String name, Class<?>... parameterTypes) {
		try {
			return RuntimeHelpers.class.getMethod(name, parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
