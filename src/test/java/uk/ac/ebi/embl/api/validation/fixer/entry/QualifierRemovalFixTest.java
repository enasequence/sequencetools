package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;

import static org.junit.Assert.*;

public class QualifierRemovalFixTest {
    public EntryFactory entryFactory;
    private Entry entry;
    private QualifierRemovalFix qualifierRemovalFix;
    FeatureFactory featureFactory;
    QualifierFactory qualifierFactory;
    
    @Before
    public void setUp() {
        ValidationMessageManager
                .addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        entryFactory = new EntryFactory();
        entry = entryFactory.createEntry();
        qualifierRemovalFix = new QualifierRemovalFix();
        featureFactory = new FeatureFactory();
        qualifierFactory = new QualifierFactory();
    }

    @Test
    public void testRemoveQualifierWithSourceFeature() {
        
        Feature feature = getFeature(Feature.SOURCE_FEATURE_NAME);
        Qualifier citatQualifier = getQualifier(Qualifier.CITATION_QUALIFIER_NAME);
        Qualifier compareQualifier = getQualifier(Qualifier.COMPARE_QUALIFIER_NAME);
        feature.addQualifier(citatQualifier);
        feature.addQualifier(compareQualifier);
        entry.addFeature(feature);
        
        assertEquals(entry.getFeatures().get(0).getQualifiers().size(),2);
        qualifierRemovalFix.check(entry);
        assertTrue(entry.getFeatures().get(0).getQualifiers().isEmpty());
    }

    @Test
    public void testRemoveQualifierWithOldSequenceFeature() {

        Feature feature = getFeature(Feature.OLD_SEQUENCE_FEATURE_NAME);
        Qualifier citatQualifier = getQualifier(Qualifier.CITATION_QUALIFIER_NAME);
        Qualifier compareQualifier = getQualifier(Qualifier.COMPARE_QUALIFIER_NAME);
        feature.addQualifier(citatQualifier);
        feature.addQualifier(compareQualifier);
        entry.addFeature(feature);

        assertEquals(entry.getFeatures().get(0).getQualifiers().size(),2);
        qualifierRemovalFix.check(entry);
        assertEquals(entry.getFeatures().get(0).getQualifiers().size(),2);
    }
    
    private Feature getFeature(String featureName){
        return featureFactory.createFeature(featureName);
    }
    
    private Qualifier getQualifier(String qualifierName){
        return qualifierFactory.createQualifier(qualifierName);
    }
}
