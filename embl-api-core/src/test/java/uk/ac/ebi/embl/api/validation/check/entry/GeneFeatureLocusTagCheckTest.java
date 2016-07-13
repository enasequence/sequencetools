/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneFeatureLocusTagCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private QualifierFactory qualifierFactory;
	private GeneFeatureLocusTagCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
        qualifierFactory = new QualifierFactory();

		entry = entryFactory.createEntry();

        check = new GeneFeatureLocusTagCheck();
	}

	@Test
	public void testCheck_NoEntry() {
        ValidationResult result = check.check(null);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoFeatures() {
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoLocusTags() {
        Feature feature = featureFactory.createFeature("feature");
        Feature feature2 = featureFactory.createFeature("feature2");
        entry.addFeature(feature);
        entry.addFeature(feature2);
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

    @Test
    public void testCheck_SameLocusAssociation() {
        Feature feature1 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME,"locus1"));
        entry.addFeature(feature1);
        Feature feature2 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
        feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME,"locus1"));
        entry.addFeature(feature2);

        ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(1, result.count(GeneFeatureLocusTagCheck.MESSAGE_ID, Severity.ERROR));
    }

    /**
     * an expected, stable association of gene qualifiers and locus tags - no errors
     */
    @Test
    public void testCheck_GeneAssociationOkay() {
        Feature feature1 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME,"locus1"));
        entry.addFeature(feature1);

        Feature feature2 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
        feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME,"locus2"));
        entry.addFeature(feature2);

        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
    }
}
