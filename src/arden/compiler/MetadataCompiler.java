package arden.compiler;

import java.util.Date;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.node.AAuthorSlot;
import arden.compiler.node.ACitationsSlot;
import arden.compiler.node.ADateMlmDate;
import arden.compiler.node.ADateSlot;
import arden.compiler.node.ADtimeMlmDate;
import arden.compiler.node.AEmptyArdenVersionSlot;
import arden.compiler.node.AEmptyPrioritySlot;
import arden.compiler.node.AExpValidationCode;
import arden.compiler.node.AExplanationSlot;
import arden.compiler.node.AFnameMlmnameSlot;
import arden.compiler.node.AInstitutionSlot;
import arden.compiler.node.AKeywordsSlot;
import arden.compiler.node.ALinksSlot;
import arden.compiler.node.AMnameMlmnameSlot;
import arden.compiler.node.APriPrioritySlot;
import arden.compiler.node.AProdValidationCode;
import arden.compiler.node.APurposeSlot;
import arden.compiler.node.AResValidationCode;
import arden.compiler.node.ASpecialistSlot;
import arden.compiler.node.ATestValidationCode;
import arden.compiler.node.ATitleSlot;
import arden.compiler.node.ATwoArdenVersion;
import arden.compiler.node.ATwooneArdenVersion;
import arden.compiler.node.AVersionSlot;
import arden.compiler.node.AVrsnArdenVersionSlot;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;

final class MetadataCompiler extends DepthFirstAdapter {
	final MaintenanceMetadata maintenance = new MaintenanceMetadata();
	final LibraryMetadata library = new LibraryMetadata();
	double priority = 50;
	private boolean usedFileNameForMlmName;

	// maintenance_body =
	// title_slot
	// mlmname_slot
	// arden_version_slot
	// version_slot
	// institution_slot
	// author_slot
	// specialist_slot
	// date_slot
	// validation_slot;

	@Override
	public void caseATitleSlot(ATitleSlot node) {
		// title_slot = title text semicolons;
		if (node.getText() != null)
			maintenance.setTitle(node.getText().getText().trim());
	}

	// mlmname_slot =
	// {mname} mlmname mlmname_text semicolons
	// | {fname} filename mlmname_text semicolons;
	@Override
	public void caseAMnameMlmnameSlot(AMnameMlmnameSlot node) {
		maintenance.setMlmName(node.getMlmnameText().getText().trim());
	}

	@Override
	public void caseAFnameMlmnameSlot(AFnameMlmnameSlot node) {
		maintenance.setMlmName(node.getMlmnameText().getText().trim());
		usedFileNameForMlmName = true;
	}

	// arden_version_slot =
	// {vrsn} arden arden_version semicolons
	// | {empty};
	@Override
	public void caseAVrsnArdenVersionSlot(AVrsnArdenVersionSlot node) {
		if (usedFileNameForMlmName)
			throw new RuntimeCompilerException("Cannot use 'filename' with Arden version 2");
		node.getArdenVersion().apply(this);
	}

	@Override
	public void caseAEmptyArdenVersionSlot(AEmptyArdenVersionSlot node) {
		maintenance.setArdenVersion("1");
	}

	// arden_version =
	// {two} version two
	// | {twoone} version twopointone;
	@Override
	public void caseATwoArdenVersion(ATwoArdenVersion node) {
		maintenance.setArdenVersion("2");
	}

	@Override
	public void caseATwooneArdenVersion(ATwooneArdenVersion node) {
		maintenance.setArdenVersion("2.1");
	}

	// version_slot = version colon text semicolons;
	@Override
	public void caseAVersionSlot(AVersionSlot node) {
		if (node.getText() != null)
			maintenance.setVersion(node.getText().getText().trim());
	}

	@Override
	public void caseAInstitutionSlot(AInstitutionSlot node) {
		if (node.getText() != null)
			maintenance.setInstitution(node.getText().getText().trim());
	}

	// author_slot = author text? semicolons;
	@Override
	public void caseAAuthorSlot(AAuthorSlot node) {
		if (node.getText() != null)
			maintenance.setAuthor(node.getText().getText().trim());
	}

	// specialist_slot = specialist text semicolons;
	@Override
	public void caseASpecialistSlot(ASpecialistSlot node) {
		if (node.getText() != null)
			maintenance.setSpecialist(node.getText().getText().trim());
	}

	// date_slot = date mlm_date semicolons;
	@Override
	public void caseADateSlot(ADateSlot node) {
		// mlm_date =
		// {date} iso_date
		// | {dtime} iso_date_time;
		long date;
		if (node.getMlmDate() instanceof ADateMlmDate) {
			date = ParseHelpers.parseIsoDate(((ADateMlmDate) node.getMlmDate()).getIsoDate());
		} else {
			date = ParseHelpers.parseIsoDateTime(((ADtimeMlmDate) node.getMlmDate()).getIsoDateTime());
		}
		maintenance.setDate(new Date(date));
	}

	// validation_slot = validation validation_code semicolons;

	// validation_code =
	// {prod} production
	// | {res} research
	// | {test} testing
	// | {exp} expired;
	@Override
	public void caseAProdValidationCode(AProdValidationCode node) {
		maintenance.setValidation("production");
	}

	@Override
	public void caseAResValidationCode(AResValidationCode node) {
		maintenance.setValidation("research");
	}

	@Override
	public void caseATestValidationCode(ATestValidationCode node) {
		maintenance.setValidation("testing");
	}

	@Override
	public void caseAExpValidationCode(AExpValidationCode node) {
		maintenance.setValidation("expired");
	}

	// Library Category
	@Override
	public void caseAPurposeSlot(APurposeSlot node) {
		if (node.getText() != null)
			library.setPurpose(node.getText().getText().trim());
	}

	@Override
	public void caseAExplanationSlot(AExplanationSlot node) {
		if (node.getText() != null)
			library.setExplanation(node.getText().getText().trim());
	}

	@Override
	public void caseAKeywordsSlot(AKeywordsSlot node) {
		if (node.getText() != null) {
			for (String keyword : node.getText().getText().split(";")) {
				String w = keyword.trim();
				if (w.length() > 0)
					library.getKeywords().add(w);
			}
		}
	}

	@Override
	public void caseACitationsSlot(ACitationsSlot node) {
		if (node.getText() != null)
			library.setCitations(node.getText().getText().trim());
	}

	@Override
	public void caseALinksSlot(ALinksSlot node) {
		if (node.getText() != null)
			library.setLinks(node.getText().getText().trim());
	}

	// priority_slot =
	// {empty}
	// | {pri} priority P.number semicolons;
	@Override
	public void caseAEmptyPrioritySlot(AEmptyPrioritySlot node) {
		// keep default priority of 50
	}

	@Override
	public void caseAPriPrioritySlot(APriPrioritySlot node) {
		priority = ParseHelpers.getLiteralDoubleValue(node.getNumber());
	}
}
