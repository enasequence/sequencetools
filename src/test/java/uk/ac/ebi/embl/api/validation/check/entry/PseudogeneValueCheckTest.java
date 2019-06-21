package uk.ac.ebi.embl.api.validation.check.entry;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.*;

public class PseudogeneValueCheckTest {

    private Entry entry;
    private FeatureFactory featureFactory;
    private QualifierFactory qualifierFactory;
    private PseudogeneValueCheck check;

    @Before
    public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        EntryFactory entryFactory = new EntryFactory();
        SequenceFactory sequenceFactory = new SequenceFactory();
        featureFactory = new FeatureFactory();
        qualifierFactory = new QualifierFactory();

        entry = entryFactory.createEntry();

        check = new PseudogeneValueCheck();
    }

    @Test
    public void checkNullValue() {
        Feature feature1 = featureFactory.createFeature("feature1");
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME,null));
        entry.addFeature(feature1);

        ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
    }

    @Test
    public void checkInvalidValue() {
        Feature feature1 = featureFactory.createFeature("feature1");
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME,"gene1"));
        entry.addFeature(feature1);

        ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
    }

    @Test
    public void checkValidValueWithSingleQuote() {
        Feature feature1 = featureFactory.createFeature("feature1");
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME,"'unknown'"));
        entry.addFeature(feature1);

        ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
    }

    @Test
    public void checkValidValue() {
        Feature feature1 = featureFactory.createFeature("feature1");
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME,"unknown"));
        entry.addFeature(feature1);

        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.count());
    }

}