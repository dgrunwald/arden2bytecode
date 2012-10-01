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

package arden.runtime;

import arden.runtime.events.EvokeEvent;
import arden.runtime.events.UndefinedEvokeEvent;

/**
 * Base class for compiled logic etc. The compiler creates derived classes. An
 * instance of the derived class will be created whenever the MLM is executed.
 * 
 * @author Daniel Grunwald
 */
public abstract class MedicalLogicModuleImplementation {
	// All derived classes are expected to have a constructor taking:
	// (ExecutionContext context, MedicalLogicModule self, ArdenValue[]
	// arguments)
	// None of the arguments may be null.

	/** Executes the logic block. */
	public abstract boolean logic(ExecutionContext context);

	/**
	 * Executes the action block.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	public abstract ArdenValue[] action(ExecutionContext context);

	/** Gets the urgency. */
	public double getUrgency() {
		return RuntimeHelpers.DEFAULT_URGENCY;
	}
	
	/** Gets the maintenance metadata 
	 * (not declared abstract to stay downwards compatible with existing MLMs) */
	public MaintenanceMetadata getMaintenanceMetadata() {
		return null;
	}
	
	/**
	 * Gets the library metadata
	 */
	public LibraryMetadata getLibraryMetadata() {
		return null;
	}
	
	/**
	 * Gets the priority
	 */
	public double getPriority() {
		return RuntimeHelpers.DEFAULT_PRIORITY;
	}
	
	/**
	 * Gets the event when this MLM should be invoked
	 */
	public EvokeEvent getEvokeEvent(ExecutionContext context) {
		return new UndefinedEvokeEvent();
	}
	
	/**
	 * Gets a Variable that is declared in the Medical Logic Module.
	 * This method should be overridden by the MLMs ByteCode.
	 */
	public ArdenValue getValue(String name) {
		return ArdenNull.INSTANCE;
	}
}
