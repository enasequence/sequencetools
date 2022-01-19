package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QualifierHelperTest {

    @Test
    public void testCheckRegExProteinId() {
        Map<String, QualifierHelper.QualifierInfo> qualifierMap = QualifierHelper.getQualifierMap();

        QualifierHelper.QualifierInfo qualifierInfo = qualifierMap.get("protein_id");

        assertTrue(checkRegEx(qualifierInfo, "protein_id", "AAA12345.1").isValid());
        assertTrue(checkRegEx(qualifierInfo, "protein_id", "AAA1234567.1").isValid());
        assertFalse(checkRegEx(qualifierInfo, "protein_id", "AAA123456.1").isValid());
        assertFalse(checkRegEx(qualifierInfo, "protein_id", "AAA1234.1").isValid());
        assertFalse(checkRegEx(qualifierInfo, "protein_id", "invalid").isValid());
    }

    private ValidationResult checkRegEx(QualifierHelper.QualifierInfo qualifierInfo, String name, String value) {
        QualifierFactory qualifierFactory = new QualifierFactory();
        return QualifierHelper.checkRegEx(qualifierInfo, qualifierFactory.createQualifier(name, value));
    }
}
