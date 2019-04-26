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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class MoleculeTypeAndFeatureCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private MoleculeTypeAndFeatureCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();

		entry = entryFactory.createEntry();
		Sequence sequence = sequenceFactory.createSequence();
		entry.setSequence(sequence);

		DataSetHelper.createAndAdd(FileName.MOLTYPE_FEATURE, new DataRow("rRNA", "rRNA"),new DataRow("tmRNA", "tmRNA") );
		check = new MoleculeTypeAndFeatureCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		entry.getSequence().setMoleculeType("rRNA");
		entry.addFeature(featureFactory.createFeature("rRNA"));
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMoleculeType() {
		entry.getSequence().setMoleculeType(null);
		entry.addFeature(featureFactory.createFeature("rRNA"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoSequence() {
		entry.setSequence(null);
		entry.addFeature(featureFactory.createFeature("rRNA"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Valid() {
		entry.getSequence().setMoleculeType("rRNA");
		entry.addFeature(featureFactory.createFeature("rRNA"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoFeature() {
		entry.getSequence().setMoleculeType("tmRNA");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("MoleculeTypeAndFeatureCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongFeature() {
		entry.getSequence().setMoleculeType("rRNA");
		entry.addFeature(featureFactory.createFeature("tmRNA"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("MoleculeTypeAndFeatureCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		entry.getSequence().setMoleculeType("rRNA");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages("MoleculeTypeAndFeatureCheck-1", Severity.ERROR);
		assertEquals("Feature rRNA is required when molecule type is rRNA.",messages.iterator().next().getMessage());
	}

	@Test
	public void testCDSJoinFail() {
		entry.getSequence().setMoleculeType(Sequence.MRNA_MOLTYPE);
        Feature cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
        CompoundLocation<Location> join = new Join<Location>();
        join.addLocation(new LocationFactory().createLocalRange(1l,3l));
        join.addLocation(new LocationFactory().createLocalRange(4l,5l));
        cdsFeature.setLocations(join);
        entry.addFeature(cdsFeature);

		ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(1, result.count("MoleculeTypeAndFeatureCheck-2", Severity.ERROR));

        cdsFeature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
        result = check.check(entry);
        assertTrue(result.isValid());
    }
	@Test
	public void testCDSComplementFail() {
		entry.getSequence().setMoleculeType(Sequence.MRNA_MOLTYPE);
		Feature cdsFeature = featureFactory
				.createFeature(Feature.CDS_FEATURE_NAME);
		CompoundLocation<Location> order = new Order<Location>();
		order.addLocation(new LocationFactory().createLocalRange(1l, 3l));
		order.addLocation(new LocationFactory().createLocalRange(4l, 5l));
		cdsFeature.setLocations(order);
		cdsFeature.getLocations().setComplement(true);
		entry.addFeature(cdsFeature);
		ValidationResult result = check.check(entry);
		assertTrue(!result.isValid());
		assertEquals(1,result.count("MoleculeTypeAndFeatureCheck-3", Severity.ERROR));

	}

}
