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
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class FeatureAndMoleculeTypeCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private FeatureAndMoleculeTypeCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();

		entry = entryFactory.createEntry();
		Sequence sequence = sequenceFactory.createSequence();
		entry.setSequence(sequence);

		DataRow dataRow = new DataRow("genomic DNA", "STS");
		DataSetHelper.createAndAdd(FileName.FEATURE_MOLTYPE, dataRow);
		check = new FeatureAndMoleculeTypeCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		entry.getSequence().setMoleculeType("genomic DNA");
		entry.addFeature(featureFactory.createFeature("STS"));
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMoleculeType() {
		entry.getSequence().setMoleculeType(null);
		entry.addFeature(featureFactory.createFeature("STS"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("FeatureAndMoleculeTypeCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongMoleculeType() {
		entry.getSequence().setMoleculeType("genomic RNA");
		entry.addFeature(featureFactory.createFeature("STS"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("FeatureAndMoleculeTypeCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoSequence() {
		entry.setSequence(null);
		entry.addFeature(featureFactory.createFeature("STS"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("FeatureAndMoleculeTypeCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_Valid() {
		entry.getSequence().setMoleculeType("genomic DNA");
		entry.addFeature(featureFactory.createFeature("STS"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoFeature() {
		entry.getSequence().setMoleculeType("genomic DNA");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_WrongFeature() {
		entry.getSequence().setMoleculeType("genomic DNA");
		entry.addFeature(featureFactory.createFeature("STX"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Message() {
		entry.addFeature(featureFactory.createFeature("STS"));

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages("FeatureAndMoleculeTypeCheck-1", Severity.ERROR);
		assertEquals(
				"Molecule type must have value \"genomic DNA\" when feature \"STS\" exists.",
				messages.iterator().next().getMessage());
	}

}
