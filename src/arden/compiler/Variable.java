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

import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;

/**
 * Represents a Variable (anything that can be named by an identifier).
 * Normal variables are DataVariables, other classes derived from Variable are used for special variable types.
 * 
 * @author Daniel Grunwald
 */
abstract class Variable {
	final String name;
	final Token definitionPosition;

	public Variable(String name, Token definitionPosition) {
		this.name = name;
		this.definitionPosition = definitionPosition;
	}

	public Variable(TIdentifier name) {
		this.name = name.getText();
		this.definitionPosition = name;
	}

	/**
	 * Emits the instructions to load the variable's value.
	 * 
	 * Stack: .. => .., value
	 */
	public void loadValue(CompilerContext context, Token errorPosition) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be read from.");
	}

	/**
	 * Emits the instructions to save a value to the variable.
	 * 
	 * Stack: .., value => ..
	 */
	public void saveValue(CompilerContext context, Token errorPosition) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be written to.");
	}

	/**
	 * Emits a call to the variable. This causes code for 'arguments' to be
	 * generated.
	 * 
	 * Stack: .. => .., returnValues[]
	 */
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be called.");
	}

	/**
	 * Emits a delayed call to the variable. This causes code for 'arguments'
	 * and 'delay' to be generated.
	 * 
	 * Stack: .. => ..
	 */
	public void callWithDelay(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		throw new RuntimeCompilerException(errorPosition, "The variable '" + name + "' cannot be called with DELAY.");
	}
}
