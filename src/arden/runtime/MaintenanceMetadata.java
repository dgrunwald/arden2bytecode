package arden.runtime;

import java.util.Date;

public class MaintenanceMetadata {
	private String title;
	private String mlmName;
	private String ardenVersion;
	private String version;
	private String institution;
	private String author;
	private String specialist;
	private Date date;
	private String validation;

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setMlmName(String mlmName) {
		this.mlmName = mlmName;
	}

	public String getMlmName() {
		return mlmName;
	}

	public void setArdenVersion(String ardenVersion) {
		this.ardenVersion = ardenVersion;
	}

	public String getArdenVersion() {
		return ardenVersion;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getInstitution() {
		return institution;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setSpecialist(String specialist) {
		this.specialist = specialist;
	}

	public String getSpecialist() {
		return specialist;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public String getValidation() {
		return validation;
	}
}
