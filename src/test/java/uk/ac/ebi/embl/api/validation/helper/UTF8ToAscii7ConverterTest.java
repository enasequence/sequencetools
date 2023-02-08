package uk.ac.ebi.embl.api.validation.helper;


import org.junit.Assert;
import org.junit.Test;

public class UTF8ToAscii7ConverterTest {

    private final UTF8ToAscii7Converter converter = new UTF8ToAscii7Converter();

    @Test
    public void convertUTF8() {
        // Remove diacritics
        Assert.assertEquals("olivia", converter.convert("ölivia"));
        Assert.assertEquals("Rafael", converter.convert("Ráfáél"));
        Assert.assertEquals("This is a funny String", converter.convert("Tĥïŝ ĩš â fůňnŷ Šťŕĭńġ"));

        // Replace non-printable ascii characters with ?
        Assert.assertEquals("M?kki B", converter.convert("Mύkki B"));
        Assert.assertEquals("unknown  ??? character", converter.convert("unknown  ��� character"));
        Assert.assertEquals("?upp", converter.convert("∆upp"));
        Assert.assertEquals("11?cu", converter.convert("11Φcu"));
    }

    @Test
    public void convertAscii() {
        for (char c = 0; c <= 255; c++) {
            if (Integer.valueOf(c) < ' ' || c > '~') {
                //      if (Integer.valueOf(c) < 32 /* space */ || Integer.valueOf(c) > 126 /* ~ */ ) {
                // Replace non-printable ascii characters with ?
                Assert.assertEquals("Character: " + Integer.valueOf(c), "?", String.valueOf(c));
            } else {
                // Printable ASCII7 character
                Assert.assertEquals("Character: " + Integer.valueOf(c), String.valueOf(c), converter.convert(String.valueOf(c)));
            }
        }
    }
}
