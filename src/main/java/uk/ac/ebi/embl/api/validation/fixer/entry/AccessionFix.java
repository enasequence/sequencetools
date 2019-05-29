/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Description("Entryname has been fixed from \"{0}\" to \"{1}\"")
public class AccessionFix extends EntryValidationCheck {

	private final static String FIX_ID = "SubmitterAccessionFix";
	private final static String REMOVE_MASTER_ACCESSION_FIX = "MaterAccessionRemovalFix";

	public AccessionFix() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		if(entry == null)
			return result;

		if (entry.getSubmitterAccession() != null) {
			String entryName = entry.getSubmitterAccession().trim();
			if (entryName.endsWith(";")) {
				entry.setSubmitterAccession(StringUtils.removeEnd(entryName, ";"));
				reportMessage(Severity.FIX, entry.getOrigin(), FIX_ID, entryName, entry.getSubmitterAccession());
			}
		}
		if (entry.getSecondaryAccessions() != null) {
			List<Text> masterAccnsToRemove = new ArrayList<>();
			for (Text accn : entry.getSecondaryAccessions()) {
				if (AccessionMatcher.isMasterAccession(accn.getText())) {
					masterAccnsToRemove.add(accn);
				}
			}
			if (!masterAccnsToRemove.isEmpty()) {
				entry.getSecondaryAccessions().removeAll(masterAccnsToRemove);
				reportMessage(Severity.FIX, entry.getOrigin(), REMOVE_MASTER_ACCESSION_FIX, masterAccnsToRemove, entry.getSubmitterAccession());
			}
		}

		return result;
	}

}
