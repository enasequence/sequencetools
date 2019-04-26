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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.fixer.sequence.SequenceBasesFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlanProperty;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SequenceBasesFixTest {
	private EntryFactory entryFactory;
	private SequenceFactory sequenceFactory;
	private FeatureFactory featureFactory;
	private LocationFactory locationFactory;
	private SequenceBasesFix check;
	public EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() throws Exception {
		planProperty=new EmblEntryValidationPlanProperty();
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
		locationFactory = new LocationFactory();
		check = new SequenceBasesFix();
		planProperty.validationScope.set(ValidationScope.EMBL);
		check.setEmblEntryValidationPlanProperty(planProperty);
	}

	@Test
	public void testCheck_NoEntry() {
		ValidationResult result = check.check(null);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	public void testCheck_NoSequence() {
		Entry entry = entryFactory.createEntry();
		entry.setSequence(null);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_sequenceWithoutNs() {
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequence();
		newsequence.setSequence(ByteBuffer.wrap("abcdefghijklmnopqrstuvwxyz".getBytes()));
		entry.setSequence(newsequence);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
        assertEquals("abcdefghijklmnopqrstuvwxyz", new String(entry.getSequence().getSequenceByte()));        
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_sequenceWithNsAtBeginAndEnd() {
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("nnnnnnnnabcdefghijklmnopqrstuvwxyznnnnnn".getBytes());
		newsequence.setTopology(Sequence.Topology.LINEAR);
		entry.setSequence(newsequence);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
        assertEquals("abcdefghijklmnopqrstuvwxyz", new String(entry.getSequence().getSequenceByte()));
		Collection<ValidationMessage<Origin>> messages = validationResult
				.getMessages("SequenceBasesFix_2", Severity.FIX);

		assertEquals(
				"Base n characters are deleted at the end and start of the sequence.",
				messages.iterator().next().getMessage());

	}

    @Test
	public void testCheck_sequenceWithNsAtEnd() {
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("abcdefghijklmnopqrstuvwxyznnnnnn".getBytes());
		newsequence.setTopology(Sequence.Topology.LINEAR);
		entry.setSequence(newsequence);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
        assertEquals("abcdefghijklmnopqrstuvwxyz", new String(entry.getSequence().getSequenceByte()));
		Collection<ValidationMessage<Origin>> messages = validationResult
				.getMessages("SequenceBasesFix_2", Severity.FIX);

		assertEquals(
				"Base n characters are deleted at the end and start of the sequence.",
				messages.iterator().next().getMessage());

	}

	@Test
	public void testCheck_sequenceWithAllNs() {
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn".getBytes());
		newsequence.setTopology(Sequence.Topology.LINEAR);
		entry.setSequence(newsequence);
		ValidationResult validationResult = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = validationResult
				.getMessages("SequenceBasesFix_1", Severity.ERROR);

        assertTrue(!messages.isEmpty());
		assertEquals("Invalid Sequence, Sequence doesn't contain any bases after deleting the n characters at the begin and end of Sequence.",
				messages.iterator().next().getMessage());
	}

}
