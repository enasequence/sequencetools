package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.apache.commons.lang.StringUtils;


public class SubmitterAccessionFix {

    /** Remove and replace unwanted characters from the submitter accession.
     *
     * @param submitterAccession the submitter accession
     * @return submitter accession with unwanted characters removed or replaced
     */
    public static String fix(String submitterAccession) {
        if (submitterAccession != null) {
            // Remove whitespace, ''', '"' characters.
            submitterAccession = submitterAccession.replaceAll("[\\s'\\\"]", "");
            // Replace '\', '/', '|', '=', ';', ',' characters with a '_' character.
            submitterAccession = submitterAccession.replaceAll("[\\\\/\\|\\=\\;\\,]", "_");
            // Remove leading and trailing '_' characters.
            submitterAccession = StringUtils.strip(submitterAccession, "_");
            // Coalesce '_' characters.
            submitterAccession = submitterAccession.replaceAll("_+", "_");
        }
        return submitterAccession;
    }
}
