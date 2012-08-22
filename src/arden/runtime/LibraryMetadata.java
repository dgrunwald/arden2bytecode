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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibraryMetadata {
	private String purpose;
	private String explanation;
	private final ArrayList<String> keywords = new ArrayList<String>();
	private String citations;
	private String links;
	
	public LibraryMetadata() {
		
	}

	public LibraryMetadata(String purpose, String explanation, String[] keywords, String citations, String links) {
		super();
		this.purpose = purpose;
		this.explanation = explanation;
		this.citations = citations;
		this.links = links;
		this.keywords.addAll(Arrays.asList(keywords));
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getExplanation() {
		return explanation;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setCitations(String citations) {
		this.citations = citations;
	}

	public String getCitations() {
		return citations;
	}

	public void setLinks(String links) {
		this.links = links;
	}

	public String getLinks() {
		return links;
	}
}
