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

import java.util.Date;

import arden.runtime.events.EvokeEvent;
import arden.runtime.events.MappedEvokeEvent;

/**
 * Describes the environment in which a Medical Logic Module is executed.
 * 
 * @author Daniel Grunwald
 * 
 */
public abstract class ExecutionContext {
	/**
	 * Creates a database query using a mapping clause. The DatabaseQuery object
	 * can be used to limit the number of results produced.
	 * 
	 * @param mapping
	 *            The contents of the mapping clause (=text between { and }).
	 *            The meaning is implementation-defined. The Arden language
	 *            specification uses mapping clauses like
	 *            "medication_cancellation where class = gentamicin".
	 * 
	 * @return This method may not return Java null. Instead, it can return
	 *         DatabaseQuery.NULL, a query that will always produce an empty
	 *         result set.
	 */
	public DatabaseQuery createQuery(String mapping) {
		return DatabaseQuery.NULL;
	}

	/** Gets a value represents the message of a MESSAGE variable. */
	public ArdenValue getMessage(String mapping) {
		return new ArdenString(mapping);
	}
	
	/** Gets an event defined with the EVENT{mapping} statement */
	public EvokeEvent getEvent(String mapping) {
		return new MappedEvokeEvent(mapping);
	}

	/**
	 * Called by write statements.
	 * 
	 * @param message
	 *            The message to be written.
	 * @param destination
	 *            The mapping clause describing the message destination.
	 */
	public void write(ArdenValue message, String destination) {
	}

	/**
	 * Retrieves another MLM.
	 * 
	 * @param name
	 *            The name of the requested MLM.
	 * @param institution
	 *            The institution of the requested MLM.
	 * @return The requested MLM.
	 */
	public ArdenRunnable findModule(String name, String institution) {
		throw new RuntimeException("findModule not implemented");
	}

	/**
	 * Retrieves an interface implementation.
	 * 
	 * @param mapping
	 *            The mapping clause of the interface.
	 * @return The interface implementation.
	 */
	public ArdenRunnable findInterface(String mapping) {
		throw new RuntimeException("findInterface not implemented");
	}

	/**
	 * Calls another MLM using a delay.
	 * 
	 * @param mlm
	 *            The MLM that should be called. This will be an instance
	 *            returned from findModule() or findInterface().
	 * @param arguments
	 *            The arguments being passed. Can be null if no arguments were
	 *            specified.
	 * @param delay
	 *            The delay for calling the MLM (as ArdenDuration).
	 */
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
		throw new RuntimeException("callWithDelay not implemented");
	}

	private ArdenTime eventtime = new ArdenTime(new Date());

	/** Gets the eventtime. */
	public ArdenTime getEventTime() {
		return eventtime;
	}

	/** Gets the triggertime. */
	public ArdenTime getTriggerTime() {
		return eventtime;
	}

	/** Gets the current time. */
	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}
