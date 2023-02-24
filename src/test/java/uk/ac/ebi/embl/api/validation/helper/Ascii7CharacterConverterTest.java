package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Ascii7CharacterConverterTest {

    private final Ascii7CharacterConverter converter = new Ascii7CharacterConverter();

    @Test
    public void testUTF8Conversion() {
        // Remove diacritics
        assertEquals("olivia", converter.convert("ölivia"));
        assertEquals("Rafael", converter.convert("Ráfáél"));
        assertEquals("This is a funny String", converter.convert("Tĥïŝ ĩš â fůňnŷ Šťŕĭńġ"));
        assertEquals("M?kki B", converter.convert("Mύkki B"));
        assertEquals("???", converter.convert("���"));;
        assertEquals("ou est ton esprit d'aventure", converter.convert("où est ton esprit d'aventure"));
        assertEquals("Wer sieht die Stabe vorubergehen?", converter.convert("Wer sieht die Stäbe vorübergehen?"));
        assertEquals("??? ???? ??? ???????????", converter.convert("где твой дух приключений"));
        assertEquals("????", converter.convert("شوبو"));
        assertEquals("????????????", converter.convert("母语教师领衔在线直播课程"));
    }

    @Test
    public void testAscii7ControlCharacterReplacement() {
        for (char c = (char) 0; c <= (char) 31; c++) {
            if (c == '\t') {
                assertEquals("Character: " + (int) c, "\t", converter.convert(String.valueOf(c)));
            } else if (c == '\n') {
                assertEquals("Character: " + (int) c, "\n", converter.convert(String.valueOf(c)));
            } else {
                assertEquals("Character: " + (int) c, "", converter.convert(String.valueOf(c)));
            }
        }
        char del = 0x7F;
        assertEquals("", converter.convert(String.valueOf(del)));
    }

    @Test
    public void testAscii7PrintableCharacterConversion() {
        String str = "\n\t  \" !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\"";
        assertEquals(str, converter.convert(str));
    }

    @Test
    public void testAscii8ControlCharacterRemoval() {
        for (char c = (char) 128; c <= (char) 160; c++) {
            assertEquals(c + ":" + (int) c, "", converter.convert(String.valueOf(c)));
        }
    }

    @Test
    public void testAscii8SymbolCharacterReplacement() {
        for (char c = (char) 161; c <= (char) 191; c++) {
            assertEquals(c + ":" + (int) c, "?", converter.convert(String.valueOf(c)));
        }
    }

    @Test
    public void testAscii8PrintableCharacterConversion() {
        String str = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
        String expected = "AAAAAAACEEEEIIIIENOOOOO?OUUUUY?saaaaaaaceeeeiiiienooooo?ouuuuypy";
        assertEquals(expected, converter.convert(str));
    }

}