package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EMBLStringEscapeUtilTest {

    @Test
    public void escapeASCIIHtmlEntities() {
        //null
        Assert.assertEquals(null, EMBLStringEscapeUtil.escapeASCIIHtmlEntities(null));
        //string with multiple spaces
        StringBuilder ip = new StringBuilder("   ");
        Assert.assertEquals("   ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //string with single space
        ip = new StringBuilder(" ");
        Assert.assertEquals(" ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //empty string
        ip = new StringBuilder("");
        Assert.assertEquals("", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //Valid html entity name &amp;
        ip = new StringBuilder("gene &amp; protein");
        Assert.assertEquals("gene & protein", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //html entity name without ending ; &amp
        ip = new StringBuilder("gene &amp protein");
        Assert.assertEquals("gene &amp protein", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //valid html entity number &#45;
        ip = new StringBuilder("gene &#45; protein");
        Assert.assertEquals("gene - protein", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //html entity number without ending ; &#45
        ip = new StringBuilder("gene &#45 protein");
        Assert.assertEquals("gene &#45 protein", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //invalid html entity number followed by a valid
        ip = new StringBuilder("gene &#45&#45; protein");
        Assert.assertEquals("gene &#45- protein", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //invalid html entity name followed by a valid
        ip = new StringBuilder("gene &gt&gt; protein &#45; organ");
        Assert.assertEquals("gene &gt> protein - organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //multiple html entity number
        ip = new StringBuilder("gene &#45; protein &#45; organ");
        Assert.assertEquals("gene - protein - organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //multiple html entity number with html entity name
        ip = new StringBuilder("gene &gt; protein &amp; &#45; organ");
        Assert.assertEquals("gene > protein & - organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //without html entity
        ip = new StringBuilder("gene & protein - organ");
        Assert.assertEquals("gene & protein - organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //without html entity
        ip = new StringBuilder("gene protein organ");
        Assert.assertEquals("gene protein organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //non ascii html entity number
        ip = new StringBuilder("gene &#127; protein - organ");
        Assert.assertEquals("gene &#127; protein - organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
        //non ascii html entity name and number
        ip = new StringBuilder("gene &#127; protein &Ccedil; organ");
        Assert.assertEquals("gene &#127; protein &Ccedil; organ", EMBLStringEscapeUtil.escapeASCIIHtmlEntities(ip).toString());
    }
}