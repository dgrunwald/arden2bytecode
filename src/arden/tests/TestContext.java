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

package arden.tests;

import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.ExecutionContext;
import arden.runtime.events.EvokeEvent;

public class TestContext extends ExecutionContext {
	StringBuilder b = new StringBuilder();
	EvokeEvent defaultEvent = null;
	ArdenTime defaultTime = null;
	
	public TestContext() {
		
	}
	
	public TestContext(EvokeEvent defaultEvent) {
		this.defaultEvent = defaultEvent;
	}
	
	public TestContext(EvokeEvent defaultEvent, ArdenTime defaultTime) {
		this(defaultEvent);
		this.defaultTime = defaultTime;
	}
	
	public TestContext(ArdenTime defaultTime) {
		this.defaultTime = defaultTime;
	}
	
	@Override
	public DatabaseQuery createQuery(String mapping) {
		return DatabaseQuery.NULL;
	}

	@Override
	public void write(ArdenValue message, String destination) {
		b.append(((ArdenString) message).value);
		b.append("\n");
	}

	public String getOutputText() {
		return b.toString();
	}
	
	@Override
	public EvokeEvent getEvent(String mapping) {
		if (defaultEvent != null) {
			return defaultEvent;
		}
		return super.getEvent(mapping);
	}
	
	@Override
	public ArdenTime getCurrentTime() {
		if (defaultTime != null) {
			return defaultTime;
		}
		return super.getCurrentTime();
	}
	
	public void setCurrentTime(ArdenTime currentTime) {
		defaultTime = currentTime;
	}
}
