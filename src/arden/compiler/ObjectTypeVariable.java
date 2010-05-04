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
import java.util.LinkedList;

import arden.codegenerator.FieldReference;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.AIdentObjectAttributeList;
import arden.compiler.node.AListObjectAttributeList;
import arden.compiler.node.PObjectAttributeList;
import arden.compiler.node.TIdentifier;
import arden.runtime.ObjectType;

/**
 * OBJECT Variable.
 * 
 * A static field of type ObjectType is stored in the MLM implementation class.
 * It is set in the static constructor and used for NEW statements.
 */
public class ObjectTypeVariable extends Variable {
	final FieldReference field;

	public ObjectTypeVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}

	/**
	 * Gets the ObjectTypeVariable for the LHSR, or creates it on demand.
	 */
	public static void create(CodeGenerator codeGen, LeftHandSideResult lhs, PObjectAttributeList attributeList) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "OBJECT variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;

		FieldReference mlmField = codeGen.createField(ident.getText(), ObjectType.class, Modifier.PRIVATE
				| Modifier.STATIC | Modifier.FINAL);
		codeGen.addVariable(new ObjectTypeVariable(ident, mlmField));

		MethodWriter init = codeGen.getStaticInitializer();
		LinkedList<String> list = getAttributeNames(attributeList);

		// emit: mlmField = new ObjectType(name, new String[] { fieldNames });
		init.newObject(ObjectType.class);
		init.dup();
		init.loadStringConstant(ident.getText());
		init.loadIntegerConstant(list.size());
		init.newArray(String.class);
		int index = 0;
		for (String memberName : list) {
			init.dup();
			init.loadIntegerConstant(index);
			init.loadStringConstant(memberName);
			init.storeObjectToArray();
			index++;
		}
		try {
			init.invokeConstructor(ObjectType.class.getConstructor(String.class, String[].class));
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
		init.storeStaticField(mlmField);
	}

	private static LinkedList<String> getAttributeNames(PObjectAttributeList attributeList) {
		// object_attribute_list =
		// {ident} identifier
		// | {list} identifier comma object_attribute_list;
		if (attributeList instanceof AIdentObjectAttributeList) {
			LinkedList<String> list = new LinkedList<String>();
			list.add(((AIdentObjectAttributeList) attributeList).getIdentifier().getText());
			return list;
		} else {
			AListObjectAttributeList attrList = (AListObjectAttributeList) attributeList;
			LinkedList<String> list = getAttributeNames(attrList.getObjectAttributeList());
			String newIdent = attrList.getIdentifier().getText();
			for (String existing : list) {
				if (existing.equalsIgnoreCase(newIdent))
					throw new RuntimeCompilerException(attrList.getIdentifier(), "Duplicate member name: " + newIdent);
			}
			list.addFirst(newIdent);
			return list;
		}
	}
}
