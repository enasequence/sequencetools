package uk.ac.ebi.embl.api.validation.check.feature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.*;

public class CircularRNAQualifierCheckTest {

    private Feature feature;
    private CircularRNAQualifierCheck check;

    @Before
    public void setUp() {
        check = new CircularRNAQualifierCheck();
    }

    @Test
    public void testInvalidQualForFeature() {
        FeatureFactory featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature("gene");
        feature.addQualifier("circular_RNA");
        ValidationResult validationResult = check.check(feature);
        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.count(
                "QualifierNotAllowedInFeatureCheck", Severity.ERROR));
    }

    @Test
    public void testValidQualFormRNAFeature() {
        FeatureFactory featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature("mRNA");
        feature.addQualifier("circular_RNA");
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void testValidQualForCDSFeature() {
        FeatureFactory featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature("CDS");
        feature.addQualifier("circular_RNA");
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
    }
}