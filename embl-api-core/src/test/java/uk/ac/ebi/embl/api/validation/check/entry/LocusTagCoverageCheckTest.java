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
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

import java.nio.ByteBuffer;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocusTagCoverageCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private LocationFactory locationFactory;
	private QualifierFactory qualifierFactory;
	private LocusTagCoverageCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
		locationFactory = new LocationFactory();
		qualifierFactory = new QualifierFactory();

		entry = entryFactory.createEntry();
		Sequence sequence = sequenceFactory.createSequence();
		sequence.setTopology(Sequence.Topology.LINEAR);
		entry.setSequence(sequence);

		check = new LocusTagCoverageCheck();
	}

    @Test
	public void testCheck_NoEntry() {
		ValidationResult result = check.check(null);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_NoFeatures() {
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_NoSequence() {
		entry.setSequence(null);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_NoLocusTags() {
		Feature feature = featureFactory.createFeature("feature");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_NoOverlap() {
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(20l, 30l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature2);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_SingleOverlap() {
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(10l, 30l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature2);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	/**
	 * A warning will be reported if the genome is not circular and neither of
	 * the locations cross the origin OR if the genome is circular and both
	 * locations cross the origin. In this test, the genome is circular and only
	 * the first location (1-10) has a circular boundary. So there should be no
	 * warning. Note if the sequence length is not set with the String
	 * implementation of Sequence, this test will give the opposite result since
	 * the test for boundary fails. With the new implementation of sequence, it
	 * is not necessary to set the sequence length.
	 */
	@Test
	public void testCheck_Circular() {

		Sequence sequence = entry.getSequence();
		sequence.setTopology(Sequence.Topology.CIRCULAR);
		sequence.setSequence(ByteBuffer.wrap("aaaaaaaaaa".getBytes()));
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(1l, 5l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature2);

		Feature feature3 = featureFactory.createFeature("feature3");
		Join<Location> join3 = new Join<Location>();
		join3.addLocation(locationFactory.createLocalRange(1l, 5l));
		feature3.setLocations(join3);
		feature3.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature3);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_SingleOverlapCompoundLocusTag() {
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(15l, 30l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature2);

		Feature feature3 = featureFactory.createFeature("feature3");
		Join<Location> join3 = new Join<Location>();
		join3.addLocation(locationFactory.createLocalRange(30l, 40l));
		feature3.setLocations(join3);
		feature3.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature3);

		Feature feature4 = featureFactory.createFeature("feature4");
		Join<Location> join4 = new Join<Location>();
		join4.addLocation(locationFactory.createLocalRange(40l, 45l));
		feature4.setLocations(join4);
		feature4.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature4);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_SingleOverlapComplement() {
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(15l, 30l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature2);

		Feature feature3 = featureFactory.createFeature("feature3");
		Join<Location> join3 = new Join<Location>();
		join3.addLocation(locationFactory.createLocalRange(30l, 40l));
		join3.setComplement(true);
		feature3.setLocations(join3);
		feature3.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature3);

		Feature feature4 = featureFactory.createFeature("feature4");
		Join<Location> join4 = new Join<Location>();
		join4.addLocation(locationFactory.createLocalRange(40l, 45l));
		feature4.setLocations(join4);
		feature4.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature4);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.count(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING));
	}

	@Test
	public void testCheck_Message() {
		Feature feature1 = featureFactory.createFeature("feature1");
		Join<Location> join = new Join<Location>();
		join.addLocation(locationFactory.createLocalRange(1l, 10l));
		feature1.setLocations(join);
		feature1.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
		entry.addFeature(feature1);

		Feature feature2 = featureFactory.createFeature("feature2");
		Join<Location> join2 = new Join<Location>();
		join2.addLocation(locationFactory.createLocalRange(10l, 30l));
		feature2.setLocations(join2);
		feature2.addQualifier(qualifierFactory.createQualifier(
				Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
		entry.addFeature(feature2);

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
				LocusTagCoverageCheck.MESSAGE_ID_OVERLAP_IN_LOCATIONS,
				Severity.WARNING);
		assertEquals(
				"Features sharing the locus_tag \"bod\" have locations overlapping with locus_tag \"cod\".",
				messages.iterator().next().getMessage());
	}

}
