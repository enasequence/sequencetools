package uk.ac.ebi.embl.api.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeverityTest {

    @Test
    public void testisLessSevereThan() {
        assertFalse(Severity.FIX.isLessSevereThan(Severity.FIX));
        assertTrue(Severity.FIX.isLessSevereThan(Severity.INFO));
        assertTrue(Severity.FIX.isLessSevereThan(Severity.WARNING));
        assertTrue(Severity.FIX.isLessSevereThan(Severity.ERROR));

        assertFalse(Severity.INFO.isLessSevereThan(Severity.FIX));
        assertFalse(Severity.INFO.isLessSevereThan(Severity.INFO));
        assertTrue(Severity.INFO.isLessSevereThan(Severity.WARNING));
        assertTrue(Severity.INFO.isLessSevereThan(Severity.ERROR));

        assertFalse(Severity.WARNING.isLessSevereThan(Severity.FIX));
        assertFalse(Severity.WARNING.isLessSevereThan(Severity.INFO));
        assertFalse(Severity.WARNING.isLessSevereThan(Severity.WARNING));
        assertTrue(Severity.WARNING.isLessSevereThan(Severity.ERROR));

        assertFalse(Severity.ERROR.isLessSevereThan(Severity.FIX));
        assertFalse(Severity.ERROR.isLessSevereThan(Severity.INFO));
        assertFalse(Severity.ERROR.isLessSevereThan(Severity.WARNING));
        assertFalse(Severity.ERROR.isLessSevereThan(Severity.ERROR));
    }
}
