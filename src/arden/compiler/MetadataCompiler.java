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

import java.util.Date;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.node.*;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.RuntimeHelpers;

/**
 * Collects metadata from the source tree.
 * 
 * @author Daniel Grunwald
 * 
 */
final class MetadataCompiler extends DepthFirstAdapter {
	final MaintenanceMetadata maintenance = new MaintenanceMetadata();
	final LibraryMetadata library = new LibraryMetadata();
	double priority = RuntimeHelpers.DEFAULT_PRIORITY;
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
	// {vrsn} arden version version_number semicolons
	// | {empty};
	@Override
	public void caseAVrsnArdenVersionSlot(AVrsnArdenVersionSlot node) {
		if (usedFileNameForMlmName)
			throw new RuntimeCompilerException("Cannot use 'filename' with Arden version 2");
		maintenance.setArdenVersion(node.getVersionNumber().getText());
	}

	@Override
	public void caseAEmptyArdenVersionSlot(AEmptyArdenVersionSlot node) {
		maintenance.setArdenVersion("1");
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
	// | {pri} priority number_literal semicolons;
	@Override
	public void caseAEmptyPrioritySlot(AEmptyPrioritySlot node) {
		// keep default priority of 50
	}

	@Override
	public void caseAPriPrioritySlot(APriPrioritySlot node) {
		priority = ParseHelpers.getLiteralDoubleValue(node.getNumberLiteral());
	}
}
