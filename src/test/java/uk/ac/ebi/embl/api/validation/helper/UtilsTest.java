package uk.ac.ebi.embl.api.validation.helper;


import org.junit.Assert;
import org.junit.Test;

public class UtilsTest  {

    @Test
    public void removeAccents() {
        Assert.assertEquals("olivia", Utils.convertToAscii("ölivia"));
        Assert.assertEquals("Rafael", Utils.convertToAscii("Ráfáél"));
        Assert.assertEquals("This is a funny String", Utils.convertToAscii("Tĥïŝ ĩš â fůňnŷ Šťŕĭńġ"));

        Assert.assertEquals("M?kki B", Utils.convertToAscii("Mύkki B"));
        Assert.assertEquals("unknown  ??? character", Utils.convertToAscii("unknown  ��� character"));
        Assert.assertEquals("?upp", Utils.convertToAscii("∆upp"));
        Assert.assertEquals("11?cu", Utils.convertToAscii("11Φcu"));
    }



}