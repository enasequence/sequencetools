package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class SubmitterAccessionFix {

    /**
     * Fixes the submitter accession in the entry by removing and replacing unwanted characters.
     *
     * @param entry the entry
     */
    public static void fix(Entry entry) {
        String submitterAccessionFromEntry = entry.getSubmitterAccession();
        String submitterAccessionFromQualifier = null;
        Qualifier qualifier = null;
        if (entry.getPrimarySourceFeature() != null) {
            qualifier = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME);
            if (qualifier != null) {
                submitterAccessionFromQualifier = qualifier.getValue();
            }
        }

        // Get fixed submitter accession.
        // Prioritise submitter accession assigned to the entry over the submitter_seqid qualifier.
        String fixedSubmitterAccession = fix(submitterAccessionFromEntry);
        if (fixedSubmitterAccession == null) {
            fixedSubmitterAccession = fix(submitterAccessionFromQualifier);
        }

        // Fix submitter accession in the entry.
        entry.setSubmitterAccession(fixedSubmitterAccession);
        if (qualifier != null) {
            qualifier.setValue(fixedSubmitterAccession);
        }
    }

    /**
     * Fixes the submitter accession by removing and replacing unwanted characters.
     *
     * @param submitterAccession the submitter accession
     * @return the fixed submitter accession
     */
    public static String fix(String submitterAccession) {
        if (submitterAccession != null) {
            // Remove whitespace, ''', '"' characters.
            submitterAccession = submitterAccession.replaceAll("[\\s'\\\"]", "");
            // Replace '\', '/', '=', ';', ',' characters with a '_' character.
            submitterAccession = submitterAccession.replaceAll("[\\\\/\\=\\;\\,]", "_");
            // Remove leading and trailing '_' characters.
            submitterAccession = StringUtils.strip(submitterAccession, "_");
            // Coalesce '_' characters.
            submitterAccession = submitterAccession.replaceAll("_+", "_");
            /// Return null if the submitter accession is empty.
            if (submitterAccession.trim().isEmpty()) {
                return null;
            }
        }
        return submitterAccession;
    }
}
