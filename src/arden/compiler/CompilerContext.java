package arden.compiler;

import arden.codegenerator.MethodWriter;

/**
 * Holds the context for the compilation of a method.
 * 
 * @author Daniel Grunwald
 */
class CompilerContext {
	public final CodeGenerator codeGenerator;
	public final MethodWriter writer;
	public final int executionContextVariable = 1;

	public CompilerContext(CodeGenerator codeGenerator, MethodWriter writer) {
		this.codeGenerator = codeGenerator;
		this.writer = writer;
	}
}
