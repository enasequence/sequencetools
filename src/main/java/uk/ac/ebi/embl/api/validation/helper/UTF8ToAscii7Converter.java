package uk.ac.ebi.embl.api.validation.helper;

import java.text.Normalizer;

/**
 * Removes diacritics and replaces non-printable ASCII7 characters with ?
 */
public class UTF8ToAscii7Converter {

    /**
     * Removes diacritics and replaces non-printable ASCII7 characters with ?
     */
    public String convert(String text) {
        if (text == null) {
            return null;
        }

        // Remove diacritics
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace non-printable ascii characters with ?
        return text.replaceAll("[^ -~]", "?");
    }
}
