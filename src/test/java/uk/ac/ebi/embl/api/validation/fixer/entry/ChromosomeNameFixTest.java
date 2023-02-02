package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChromosomeNameFixTest {

    @Test
    public void fix() {
        assertNull(ChromosomeNameFix.fix(null));
        assertEquals("", ChromosomeNameFix.fix(""));
        assertEquals("test1", ChromosomeNameFix.fix("test1"));
        assertEquals("test1", SubmitterAccessionFix.fix("__t e s t 1___"));
        assertEquals("test1", ChromosomeNameFix.fix("test1;"));
        assertEquals("t_e_s_t_1", ChromosomeNameFix.fix("t____\\e/s|t=1"));
        assertEquals("test_1", ChromosomeNameFix.fix("\\/|=;test\\/|=;1\\/|="));
    }

    @Test
    public void fixRemoveWords() {
        for (String word : ChromosomeNameFix.WORDS) {
            assertEquals("test1", ChromosomeNameFix.fix(word + "test" + word + "1" + word));
            assertEquals("test1", ChromosomeNameFix.fix(word.toUpperCase() + "test" + word.toUpperCase() + "1" + word.toUpperCase()));
            assertEquals("test1", ChromosomeNameFix.fix(word.toLowerCase() + "test" + word.toLowerCase() + "1" + word.toLowerCase()));
        }
    }

    @Test
    public void fixReplaceWords() {
        assertEquals("MT", ChromosomeNameFix.fix("mitocondria"));
        assertEquals("MT", ChromosomeNameFix.fix("mitochondria"));
        assertEquals("MT", ChromosomeNameFix.fix("mitocondria".toUpperCase()));
        assertEquals("MT", ChromosomeNameFix.fix("mitochondria".toUpperCase()));
        assertEquals("MT", ChromosomeNameFix.fix("mitocondria".toLowerCase()));
        assertEquals("MT", ChromosomeNameFix.fix("mitochondria".toLowerCase()));
    }
}
