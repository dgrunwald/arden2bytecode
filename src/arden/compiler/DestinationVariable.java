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

import java.lang.reflect.Modifier;

import arden.codegenerator.FieldReference;
import arden.compiler.node.TIdentifier;

/**
 * DESTINATION Variable.
 * 
 * An instance field of type String is stored in the MLM implementation class.
 * It is set in the data block where the 'x := DESTINATION y' statement occurs
 * and used for WRITE AT statements.
 */
final class DestinationVariable extends Variable {
	final FieldReference field;

	private DestinationVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}

	/**
	 * Gets the DestinationVariable for the LHSR, or creates it on demand.
	 */
	public static DestinationVariable getDestinationVariable(CodeGenerator codeGen, LeftHandSideResult lhs) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "DESTINATION variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
		Variable variable = codeGen.getVariable(ident.getText());
		if (variable instanceof DestinationVariable) {
			return (DestinationVariable) variable;
		} else {
			FieldReference mlmField = codeGen.createField(ident.getText(), String.class, Modifier.PRIVATE);
			DestinationVariable dv = new DestinationVariable(ident, mlmField);
			codeGen.addVariable(dv);
			return dv;
		}
	}
}
