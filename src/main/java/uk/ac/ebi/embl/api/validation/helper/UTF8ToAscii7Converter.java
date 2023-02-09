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

        // Replace diacritics.
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace all non-ASCII characters (not 0-255) with ?.
        text = text.replaceAll("[^\\x00-\\xff]", "?");

        // Replace non-printable ASCII-7 characters with ?.
        text = text.replaceAll("\\p{Cntrl}", "?");

        // Replace non-printable ASCII-8 characters (127-191) with ?.
        text = text.replaceAll("[\\x7f-\\xbf]", "?");
        char[] a = text.toCharArray();
        // Replace other ASCII-8 characters.
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == 'Æ') a[i] = 'A';
            else if (a[i] == 'Ð') a[i] = 'E';
            else if (a[i] == 'Ø') a[i] = 'O';
            else if (a[i] == '×') a[i] = '?';
            else if (a[i] == 'Þ') a[i] = '?';
            else if (a[i] == 'ß') a[i] = 's';
            else if (a[i] == 'æ') a[i] = 'a';
            else if (a[i] == 'ð') a[i] = 'e';
            else if (a[i] == '÷') a[i] = '?';
            else if (a[i] == 'ø') a[i] = 'o';
            else if (a[i] == 'þ') a[i] = 'p';
        }
        return String.valueOf(a);
    }
}

