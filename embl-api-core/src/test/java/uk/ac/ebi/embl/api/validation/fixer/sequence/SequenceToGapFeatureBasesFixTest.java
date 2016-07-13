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
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.fixer.sequence.SequenceToGapFeatureBasesFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SequenceToGapFeatureBasesFixTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private LocationFactory locationFactory;
	private QualifierFactory qualifierFactory;
	private SequenceToGapFeatureBasesFix check;
	private SequenceFactory sequenceFactory;

	@Before
	public void setUp() throws SQLException {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
		qualifierFactory = new QualifierFactory();
		locationFactory = new LocationFactory();
		entry = entryFactory.createEntry();
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        property.validationScope.set(ValidationScope.EMBL);
		property.minGapLength.set(7);
        check = new SequenceToGapFeatureBasesFix();
		
        check.setEmblEntryValidationPlanProperty(property);
		SequenceToGapFeatureBasesFix.GAP_ESTIMATED_LENGTH = 8;
		SequenceToGapFeatureBasesFix.GAP_ESTIMATED_LENGTH_STRING = "8";
	}

	@Test
	public void testCheck_gapEstimatedLength() {

		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnnnnaaannnnnnnnaaaaannnnnnnnaannnnnnnnaaa".getBytes()));// 4
																						// lots
																						// of
																						// n
																						// stretches

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(4, result.count(SequenceToGapFeatureBasesFix.GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.ESTIMATED_LENGTH_ID, Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
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
	public void testCheck_NoSequence() {
		entry.setSequence(null);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoLocations() {
		Feature feature = featureFactory
				.createFeature(Feature.GAP_FEATURE_NAME);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_fixNSequence() {

		entry.setSequence(sequenceFactory.createSequenceByte("aaannnnnnnnaaaaa".getBytes()));

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));

	}

	@Test
	public void testCheck_MultipleFixNSequence() {

		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnnnnaaaaaaaannnnnnnnaaaaa".getBytes()));// 2 lots
																		// of n
																		// stretches
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
       // List<Feature> gapFeatures =
       //         new ArrayList<Feature>(SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry));
        List<Feature> gapFeatures =
                new ArrayList<Feature>(SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry));
        assertEquals(2, gapFeatures.size());
        Collections.sort(gapFeatures);
        assertTrue(gapFeatures.get(0).getLocations().getLocations().get(0).getBeginPosition() == 4);
        assertTrue(gapFeatures.get(0).getLocations().getLocations().get(0).getEndPosition() == 11);
        assertTrue(gapFeatures.get(1).getLocations().getLocations().get(0).getBeginPosition() == 20);
        assertTrue(gapFeatures.get(1).getLocations().getLocations().get(0).getEndPosition() == 27);
        assertEquals(2, result.count(SequenceToGapFeatureBasesFix.GAP_MESSAGE_ID, Severity.FIX));
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
	}

	@Test
	public void testCheck_fixAndWarnNSequence() {

		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnaaaaaaaannnnnnnnaaaaa".getBytes()));// 2 lots of n
																	// stretches

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
	}
	
	@Test
	public void testCheck_fixwithmin_gap_length() throws SQLException {

		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.minGapLength.set(10);
		property.validationScope.set(ValidationScope.EMBL);
		check.setEmblEntryValidationPlanProperty(property);
		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnnnnaaaaaaaannnnnnnnnnnaaaaa".getBytes()));// 2 lots of n
																	// stretches

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
		List<Feature> gapFeatures =
	                new ArrayList<Feature>(SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry));
		assertEquals(1, gapFeatures.size());
		assertTrue(gapFeatures.get(0).getLocations().getLocations().get(0).getBeginPosition() ==20 );
        assertTrue(gapFeatures.get(0).getLocations().getLocations().get(0).getEndPosition() == 30);
	}
	
	@Test
	public void testCheck_fix_assembly_scope() throws SQLException {

		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnnnnnnnnnnnnnnaaaaaaa".getBytes()));// 2 lots of n
																	// stretches

		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.ASSEMBLY_GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
		List<Feature> assemblyGapFeatures =
	                new ArrayList<Feature>(SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry));
		assertEquals(1, assemblyGapFeatures.size());
		assertTrue(assemblyGapFeatures.get(0).getLocations().getLocations().get(0).getBeginPosition() ==4 );
        assertTrue(assemblyGapFeatures.get(0).getLocations().getLocations().get(0).getEndPosition() == 21);
	}
	
	@Test
	public void testCheck_fix_assembly_flag() throws SQLException {
		
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.isAssembly.set(true);
		property.validationScope.set(ValidationScope.EMBL);
		property.minGapLength.set(7);
		check.setEmblEntryValidationPlanProperty(property);		
		entry.setSequence(sequenceFactory
				.createSequenceByte("aaannnnnnnnaaaaaaa".getBytes()));// 2 lots of n
																	// stretches
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.count(SequenceToGapFeatureBasesFix.ASSEMBLY_GAP_MESSAGE_ID,
				Severity.FIX));
		assertEquals(1, result.count(
				SequenceToGapFeatureBasesFix.COUNT_MESSAGE_ID, Severity.FIX));
		List<Feature> assemblyGapFeatures =
	                new ArrayList<Feature>(SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry));
		assertEquals(1, assemblyGapFeatures.size());
		assertTrue(assemblyGapFeatures.get(0).getLocations().getLocations().get(0).getBeginPosition() ==4 );
        assertTrue(assemblyGapFeatures.get(0).getLocations().getLocations().get(0).getEndPosition() == 11);
	}

	
}
