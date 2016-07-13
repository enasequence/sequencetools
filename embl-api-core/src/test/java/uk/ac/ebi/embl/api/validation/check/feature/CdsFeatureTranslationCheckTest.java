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
package uk.ac.ebi.embl.api.validation.check.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class CdsFeatureTranslationCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private CdsFeatureTranslationCheck check;

	@SuppressWarnings("deprecation")
	@Before
	public void setUp() throws SQLException {
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
		Sequence sequence=sequenceFactory.createSequenceByte("gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgtcactcgg".getBytes());
		entry = entryFactory.createEntry();
		entry.setSequence(sequence);
        TaxonHelper taxonHelper = createMock(TaxonHelper.class);
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        property.taxonHelper.set(taxonHelper);
        check = new CdsFeatureTranslationCheck();
        check.setEmblEntryValidationPlanProperty(property);
    }

	public void testCheck_NoDataSet() {
        check.setEntry(entry);
        assertTrue(check.check(featureFactory.createFeature(Feature.CDS_FEATURE_NAME)).isValid());
    }

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_EmptyFeature() {
        CdsFeature cdsFeature = featureFactory.createCdsFeature();
        entry.addFeature(cdsFeature);
        check.setEntry(entry);
        ValidationResult validationResult = check.check(cdsFeature);
        assertTrue(!validationResult.isValid());//i.e. there were failures
    }

    @Test
	public void testCheck_NoTranslation() {
        CdsFeature cdsFeature = featureFactory.createCdsFeature();
        LocationFactory locationFactory = new LocationFactory();
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 12L));
        entry.setSequence(new SequenceFactory().createSequenceByte("atggagtggtaa".getBytes()));
        entry.addFeature(cdsFeature);

        check.setEntry(entry);
        ValidationResult validationResult = check.check(cdsFeature);
        assertTrue(validationResult.isValid());
        assertTrue(cdsFeature.getTranslation() == null);
    }
    
    @Test
	public void testCheck_InvalidLocation() {
        CdsFeature cdsFeature = featureFactory.createCdsFeature();
        LocationFactory locationFactory = new LocationFactory();
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(12L, 1L));
        entry.setSequence(new SequenceFactory().createSequenceByte("actgactgactgactg".getBytes()));
        entry.addFeature(cdsFeature);
        check.setEntry(entry);
        ValidationResult validationResult = check.check(cdsFeature);
        assertEquals(1, validationResult.count("Translator-19", Severity.ERROR));
        assertEquals(1, validationResult.count(Severity.ERROR));
        assertTrue(!validationResult.isValid());
        assertTrue(cdsFeature.getTranslation() == null);
    }    
}
