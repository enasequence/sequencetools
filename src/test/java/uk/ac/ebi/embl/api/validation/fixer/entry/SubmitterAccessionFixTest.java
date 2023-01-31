package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SubmitterAccessionFixTest {

    @Test
    public void fix() {
        assertNull(SubmitterAccessionFix.fix(null));
        assertEquals("", SubmitterAccessionFix.fix(""));
        assertEquals("test1", SubmitterAccessionFix.fix("test1"));
        assertEquals("test1", SubmitterAccessionFix.fix("__t e s t 1___"));
        assertEquals("test1", SubmitterAccessionFix.fix("test1;"));
        assertEquals("test1", SubmitterAccessionFix.fix("'test'1'"));
        assertEquals("test1", SubmitterAccessionFix.fix("\"test\"1\""));
        assertEquals("t_e_s_t_1_2_3", SubmitterAccessionFix.fix("t_____\\e/s|t=1;2,3"));
        assertEquals("test_1", SubmitterAccessionFix.fix("\\/|=;'\"test\\/|=;'\"1\\/|=;'\""));
    }
}
