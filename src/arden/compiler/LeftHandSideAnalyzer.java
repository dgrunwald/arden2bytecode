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

import arden.compiler.node.AIdDataVarList;
import arden.compiler.node.AIdIdentifierBecomes;
import arden.compiler.node.AIdIdentifierOrObjectRef;
import arden.compiler.node.AIdlDataVarList;
import arden.compiler.node.ALetIdentifierBecomes;
import arden.compiler.node.ALettoTimeBecomes;
import arden.compiler.node.ALtimeTimeBecomes;
import arden.compiler.node.ANowIdentifierBecomes;
import arden.compiler.node.AObjrefIdentifierOrObjectRef;
import arden.compiler.node.ATimeTimeBecomes;
import arden.compiler.node.ATimeofTimeBecomes;
import arden.compiler.node.Switchable;
import arden.compiler.node.TIdentifier;

/**
 * 
 * Analyzes the left-hand side of an assignment. Produces a LeftHandSideResult,
 * a high-level representation of the syntax tree on the left hand side of
 * expressions.
 * 
 * Supported productions: identifier_becomes, time_becomes, data_var_list
 * 
 * @author Daniel Grunwald
 * 
 */
final class LeftHandSideAnalyzer extends VisitorBase {
	private LeftHandSideResult result;

	public static LeftHandSideResult analyze(Switchable node) {
		LeftHandSideAnalyzer a = new LeftHandSideAnalyzer();
		node.apply(a);
		return a.result;
	}

	// identifier_becomes =
	// {id} identifier_or_object_ref assign
	// | {let} let identifier_or_object_ref be
	// | {now} now assign;
	@Override
	public void caseAIdIdentifierBecomes(AIdIdentifierBecomes node) {
		node.getIdentifierOrObjectRef().apply(this);
	}

	@Override
	public void caseALetIdentifierBecomes(ALetIdentifierBecomes node) {
		node.getIdentifierOrObjectRef().apply(this);
	}

	@Override
	public void caseANowIdentifierBecomes(ANowIdentifierBecomes node) {
		result = new LeftHandSideNow(node.getNow());
	}

	// identifier_or_object_ref =
	// {id} identifier
	// | {objref} identifier_or_object_ref dot identifier;
	@Override
	public void caseAIdIdentifierOrObjectRef(AIdIdentifierOrObjectRef node) {
		result = new LeftHandSideIdentifier(node.getIdentifier());
	}

	@Override
	public void caseAObjrefIdentifierOrObjectRef(AObjrefIdentifierOrObjectRef node) {
		node.getIdentifierOrObjectRef().apply(this);
		result = new LeftHandSideObjectMember(result, node.getIdentifier());
	}

	// time_becomes =
	// {timeof} time of identifier assign
	// | {time} time identifier assign
	// | {letto} let time of identifier be
	// | {ltime} let time identifier be
	@Override
	public void caseATimeofTimeBecomes(ATimeofTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseATimeTimeBecomes(ATimeTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseALettoTimeBecomes(ALettoTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	@Override
	public void caseALtimeTimeBecomes(ALtimeTimeBecomes node) {
		result = new LeftHandSideTimeOfIdentifier(node.getIdentifier());
	}

	// data_var_list =
	// {id} identifier
	// | {idl} identifier comma data_var_list;
	@Override
	public void caseAIdDataVarList(AIdDataVarList node) {
		addIdentifierToList(node.getIdentifier());
	}

	@Override
	public void caseAIdlDataVarList(AIdlDataVarList node) {
		addIdentifierToList(node.getIdentifier());
		node.getDataVarList().apply(this);
	}

	private void addIdentifierToList(TIdentifier identifier) {
		if (result == null) {
			result = new LeftHandSideIdentifier(identifier);
		} else if (result instanceof LeftHandSideIdentifier) {
			LeftHandSideIdentifierList list = new LeftHandSideIdentifierList();
			list.add((LeftHandSideIdentifier) result);
			list.add(new LeftHandSideIdentifier(identifier));
			result = list;
		} else {
			((LeftHandSideIdentifierList) result).add(new LeftHandSideIdentifier(identifier));
		}
	}
}
