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

import java.util.Stack;

import arden.codegenerator.MethodWriter;

/**
 * Holds the context for the compilation of a method.
 * 
 * @author Daniel Grunwald
 */
final class CompilerContext {
	public final CodeGenerator codeGenerator;
	public final MethodWriter writer;
	public final int executionContextVariable;
	public final int selfMLMVariable;
	public final int argumentsVariable;
	private int nextFreeVariable;
	/** Stack of currently active 'it' variables */
	private Stack<Integer> itVariables = new Stack<Integer>();
	/** Stack of 'it' variables that are free for reuse */
	private Stack<Integer> freeItVariables = new Stack<Integer>();

	public CompilerContext(CodeGenerator codeGenerator, MethodWriter writer, int parameters) {
		this.codeGenerator = codeGenerator;
		this.writer = writer;
		if (parameters >= 1)
			executionContextVariable = 1;
		else
			executionContextVariable = -1;
		if (parameters >= 2)
			selfMLMVariable = 2;
		else
			selfMLMVariable = -1;
		if (parameters >= 3)
			argumentsVariable = 3;
		else
			argumentsVariable = -1;
		nextFreeVariable = parameters + 1;
	}

	/** Allocates a new variable slot in the current Java method. */
	public int allocateVariable() {
		return nextFreeVariable++;
	}

	/** Allocates a new 'it' variable and pushes it onto stack */
	public int allocateItVariable() {
		int var = freeItVariables.empty() ? allocateVariable() : freeItVariables.pop();
		itVariables.push(var);
		return var;
	}

	/**
	 * Gets the current 'it' variable, or -1 if there is no active 'it' variable
	 */
	public int getCurrentItVariable() {
		if (itVariables.empty())
			return -1;
		return itVariables.peek();
	}

	/** Frees the current 'it' variable. */
	public void popItVariable() {
		freeItVariables.push(itVariables.pop());
	}
}
