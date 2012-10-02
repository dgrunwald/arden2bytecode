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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.codegenerator.MethodWriter;
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
import arden.compiler.node.PEvokeSlot;
import arden.compiler.node.PLogicSlot;
import arden.compiler.node.PUrgencySlot;
import arden.compiler.node.PUrgencyVal;
import arden.compiler.node.Start;
import arden.compiler.node.TIdentifier;
import arden.compiler.parser.Parser;
import arden.compiler.parser.ParserException;
import arden.runtime.ArdenValue;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.RuntimeHelpers;
import arden.runtime.events.EvokeEvent;

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
		compileEvoke(codeGen, knowledge.getEvokeSlot());
		compileUrgency(codeGen, knowledge.getUrgencySlot());
		try {
			compileMaintenance(codeGen, metadata.maintenance);
			compileLibrary(codeGen, metadata.library);
			compilePriority(codeGen, metadata.priority);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}

		codeGen.createGetValue();
			
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
		return new CompiledMlm(data, metadata.maintenance.getMlmName());
	}

	private void compileMaintenance(CodeGenerator codeGen, MaintenanceMetadata maintenance) throws NoSuchMethodException, SecurityException {		
		FieldReference maintenanceField = codeGen.createStaticFinalField(MaintenanceMetadata.class);
		MethodWriter init = codeGen.getStaticInitializer();
		// format = new MaintenanceMetadata(String title, String mlmName, String ardenVersion, String version, String institution, String author, String specialist, Date date, String validation)
		init.newObject(MaintenanceMetadata.class);
		init.dup();
		init.loadStringConstant(maintenance.getTitle());
		init.loadStringConstant(maintenance.getMlmName());
		init.loadStringConstant(maintenance.getArdenVersion());
		init.loadStringConstant(maintenance.getVersion());
		init.loadStringConstant(maintenance.getInstitution());
		init.loadStringConstant(maintenance.getAuthor());
		init.loadStringConstant(maintenance.getSpecialist());
		
		init.newObject(Date.class);
		init.dup();
		init.loadLongConstant(maintenance.getDate().getTime());
		init.invokeConstructor(Date.class.getConstructor(new Class<?>[]{Long.TYPE}));
		
		init.loadStringConstant(maintenance.getValidation());
		
		init.invokeConstructor(MaintenanceMetadata.class.getConstructor(new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, Date.class, String.class}));
		init.storeStaticField(maintenanceField);
		
		CompilerContext context = codeGen.createMaintenance();
		context.writer.loadStaticField(maintenanceField);
		context.writer.returnObjectFromFunction();
	}
	
	private void compileLibrary(CodeGenerator codeGen, LibraryMetadata library) throws NoSuchMethodException, SecurityException {		
		FieldReference libraryField = codeGen.createStaticFinalField(LibraryMetadata.class);
		MethodWriter init = codeGen.getStaticInitializer();
		// format = new LibraryMetadata(String purpose, String explanation, String[] keywords, String citations, String links)
		init.newObject(LibraryMetadata.class);
		init.dup();
		init.loadStringConstant(library.getPurpose());
		init.loadStringConstant(library.getExplanation());
		init.loadIntegerConstant(library.getKeywords().size());
		init.newArray(String.class);
		for (int i = 0; i < library.getKeywords().size(); i++) {
			init.dup();
			init.loadIntegerConstant(i);
			init.loadStringConstant(library.getKeywords().get(i));
			init.storeObjectToArray();
		}
		init.loadStringConstant(library.getCitations());
		init.loadStringConstant(library.getLinks());
		
		init.invokeConstructor(LibraryMetadata.class.getConstructor(new Class<?>[]{String.class, String.class, String[].class, String.class, String.class}));
		init.storeStaticField(libraryField);
		
		CompilerContext context = codeGen.createLibrary();
		context.writer.loadStaticField(libraryField);
		context.writer.returnObjectFromFunction();
	}
	
	private void compilePriority(CodeGenerator codeGen, double priority) {
		CompilerContext context = codeGen.createPriority();
		context.writer.loadDoubleConstant(priority);
		context.writer.returnDoubleFromFunction();
	}
	
	private void compileEvoke(CodeGenerator codeGen, PEvokeSlot evokeSlot) {
		CompilerContext context = codeGen.createEvokeEvent();

		// event is a keyword, thus there cannot be another field named 'event'
		FieldReference eventField = context.codeGenerator.createField("event", EvokeEvent.class, Modifier.PRIVATE);
		
		Label isNull = new Label();
		
		context.writer.loadThis();
		context.writer.loadInstanceField(eventField);
		context.writer.jumpIfNull(isNull);
		context.writer.loadThis();
		context.writer.loadInstanceField(eventField);
		context.writer.returnObjectFromFunction();
		
		context.writer.mark(isNull);		
		evokeSlot.apply(new EvokeCompiler(context)); 
		
		// the evoke compiler is supposed to leave an EvokeEvent subclass instance on the stack		
		context.writer.dup();
		context.writer.loadThis();
		context.writer.swap();
		context.writer.storeInstanceField(eventField);
		context.writer.returnObjectFromFunction();
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

	private double compileUrgency(CodeGenerator codeGen, PUrgencySlot urgencySlot) {
		// urgency_slot =
		// {empty}
		// | {urg} urgency urgency_val semicolons;
		double urgency = RuntimeHelpers.DEFAULT_URGENCY;
		if (urgencySlot instanceof AUrgUrgencySlot) {
			PUrgencyVal val = ((AUrgUrgencySlot) urgencySlot).getUrgencyVal();
			CompilerContext context = codeGen.createUrgency();
			context.writer.sequencePoint(((AUrgUrgencySlot) urgencySlot).getUrgency().getLine());
			// urgency_val =
			// {num} P.number
			// | {id} identifier;
			if (val instanceof ANumUrgencyVal) {
				urgency = ParseHelpers
						.getLiteralDoubleValue(((ANumUrgencyVal) val).getNumberLiteral());
				context.writer.loadDoubleConstant(urgency);
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
		return urgency;
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
