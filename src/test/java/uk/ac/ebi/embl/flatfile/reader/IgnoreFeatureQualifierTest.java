package uk.ac.ebi.embl.flatfile.reader;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class IgnoreFeatureQualifierTest extends TestCase {
    
    @Test
    public void testIsIgnoreWithIgnorableQualifiers(){
        
        for(String missingQualifierValue : IgnoreFeatureQualifier.MISSING_VALUE_TERMS) {
            assertTrue(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME,Qualifier.COUNTRY_QUALIFIER_NAME,missingQualifierValue));
            assertTrue(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME,Qualifier.COLLECTION_DATE_QUALIFIER_NAME,missingQualifierValue));
            assertTrue(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME,Qualifier.LAT_LON_QUALIFIER_NAME,missingQualifierValue));
        }
        assertTrue(IgnoreFeatureQualifier.isIgnore(Feature.REPEAT_REGION,Qualifier.LOCUS_TAG_QUALIFIER_NAME,"BN5_00001"));
    }

    @Test
    public void testIsIgnoreWithNotIgnorableQualifiers() {

        assertFalse(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME, Qualifier.COUNTRY_QUALIFIER_NAME, "India"));
        assertFalse(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME, Qualifier.COLLECTION_DATE_QUALIFIER_NAME, "18-May-2023"));
        assertFalse(IgnoreFeatureQualifier.isIgnore(Feature.SOURCE_FEATURE_NAME, Qualifier.LAT_LON_QUALIFIER_NAME, "6.385667 N 162.334778 W"));
        assertFalse(IgnoreFeatureQualifier.isIgnore(Feature.REPEAT_REGION, Qualifier.NOTE_QUALIFIER_NAME, "BN5_00005"));
    }
}
