package arden.runtime;

import java.util.ArrayList;
import java.util.List;

public class LibraryMetadata {
	private String purpose;
	private String explanation;
	private final ArrayList<String> keywords = new ArrayList<String>();
	private String citations;
	private String links;

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
