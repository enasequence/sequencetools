package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UTF8ToAscii7ConverterTest {

    private final UTF8ToAscii7Converter converter = new UTF8ToAscii7Converter();

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
    public void testAscii7ValidRangeConversion() {
        String str = "  \" !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\"";
        assertEquals(str, converter.convert(str));
    }

    @Test
    public void testAscii8ValidRangeConversion() {
        String expected = "AAAAAAACEEEEIIIIENOOOOO?OUUUUY?saaaaaaaceeeeiiiienooooo?ouuuuypy";
        String str = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
        assertEquals(expected, converter.convert(str));
    }


    @Test
    public void testAscii7InvalidRangeConversion() {
        for (char c = 0; c < ' '; c++) {
            assertEquals("?", converter.convert(String.valueOf(c)));
        }
        char del = 0x7F;
        assertEquals("?", converter.convert(String.valueOf(del)));
    }

    @Test
    public void testAscii8InvalidRangeConversion() {
        for (char c = '€'; c <= 'À'; c++) {
            assertEquals("?", converter.convert(String.valueOf(c)));
        }
    }
}