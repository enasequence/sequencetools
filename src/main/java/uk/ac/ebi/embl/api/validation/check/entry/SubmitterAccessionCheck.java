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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.fixer.entry.SubmitterAccessionFix;

public class SubmitterAccessionCheck extends EntryValidationCheck {
    private final static String SUBMITTER_ACCESSION_MISSING_MESSAGE_ID = "SubmitterAccessionCheck_1";
    private final static String SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID = "SubmitterAccessionCheck_2";
    private final static String SUBMITTER_ACCESSION_INCONSISTENT_MESSAGE_ID = "SubmitterAccessionCheck_3";
    private final static int SUBMITTER_ACCESSION_MAX_LENGTH = 50;

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        if (entry == null) {
            return result;
        }

        boolean isSubmitterAccession = entry.getSubmitterAccession() != null && !entry.getSubmitterAccession().isEmpty();

        Qualifier submitterSeqIdQualifier = null;
        boolean isSubmitterSeqIdQualifierValue = false;
        if (entry.getPrimarySourceFeature() != null) {
            submitterSeqIdQualifier = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
            isSubmitterSeqIdQualifierValue = submitterSeqIdQualifier != null && submitterSeqIdQualifier.getValue() != null && !submitterSeqIdQualifier.getValue().isEmpty();
        }

        ValidationScope validationScope = getEmblEntryValidationPlanProperty().validationScope.get();

        if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME ||
                validationScope == ValidationScope.ASSEMBLY_SCAFFOLD ||
                validationScope == ValidationScope.ASSEMBLY_CONTIG ||
                validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {

            // Make sure submitter accession has been fixed.
            if (entry.getSubmitterAccession() != null) {
                entry.setSubmitterAccession(SubmitterAccessionFix.fix(entry.getSubmitterAccession()));
            }

            if (isSubmitterSeqIdQualifierValue) {
                // Make sure submitter accession in /submitter_seqid has been fixed.
                submitterSeqIdQualifier.setValue(SubmitterAccessionFix.fix(submitterSeqIdQualifier.getValue()));

                if (isSubmitterAccession) {
                    // Make sure submitter accessions are consistent.
                    if (!entry.getSubmitterAccession().equals(submitterSeqIdQualifier.getValue())) {
                        reportError(entry.getOrigin(),
                                SUBMITTER_ACCESSION_INCONSISTENT_MESSAGE_ID,
                                entry.getSubmitterAccession(),
                                submitterSeqIdQualifier.getValue());
                    }
                } else {
                    // Set submitter accession from /submitter_seqid.
                    entry.setSubmitterAccession(submitterSeqIdQualifier.getValue());
                }
            }

            // Check submitter accession.
            if (entry.getSubmitterAccession() == null || entry.getSubmitterAccession().isEmpty()) {
                reportError(entry.getOrigin(), SUBMITTER_ACCESSION_MISSING_MESSAGE_ID);
            } else if (entry.getSubmitterAccession().length() > SUBMITTER_ACCESSION_MAX_LENGTH) {
                reportError(entry.getOrigin(), SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID, entry.getSubmitterAccession());
            }
        } else {
            if (!isSubmitterAccession && isSubmitterSeqIdQualifierValue) {
                // Set submitter accession from /submitter_seqid.
                entry.setSubmitterAccession(submitterSeqIdQualifier.getValue());
            }
        }

        return result;
    }
}
