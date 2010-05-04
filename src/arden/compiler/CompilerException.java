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

import arden.compiler.lexer.LexerException;
import arden.compiler.parser.ParserException;

/**
 * This exception is used to report compiler errors.
 * 
 * @author Daniel Grunwald
 */
public class CompilerException extends Exception {
	private static final long serialVersionUID = -4674298085134149649L;

	private final int line, pos;

	public CompilerException(ParserException innerException) {
		super(innerException);
		if (innerException.getToken() != null) {
			this.line = innerException.getToken().getLine();
			this.pos = innerException.getToken().getPos();
		} else {
			this.line = 0;
			this.pos = 0;
		}
	}

	public CompilerException(LexerException innerException) {
		super(innerException);
		this.line = 0;
		this.pos = 0;
	}

	CompilerException(RuntimeCompilerException innerException) {
		super(innerException.getMessage(), innerException);
		this.line = innerException.line;
		this.pos = innerException.pos;
	}

	public CompilerException(String message, int pos, int line) {
		super(message);
		this.line = line;
		this.pos = pos;
	}

	/** Gets the line number where the compile error occurred. */
	public int getLine() {
		return line;
	}

	/** Gets the line position (column) where the compile error occurred. */
	public int getPos() {
		return pos;
	}
}
