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
package uk.ac.ebi.embl.api.validation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.LocationQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Rpt_Unit_RangeQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Tag_PeptideQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.*;

public class UtilsTest {

	private EntryFactory entryFactory;
	private SequenceFactory sequenceFactory;
	private FeatureFactory featureFactory;
	private LocationFactory locationFactory;
	private ReferenceFactory referenceFactory;

	@Before
	public void setUp() throws Exception {
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
        locationFactory = new LocationFactory();
        referenceFactory = new ReferenceFactory();
	}

	 //test for shiftLocation Method of Utils
   
	@Test
	public void testCheck_featureLocationBeginandEndwithNs() {
		int beginN = 10;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 8));
		feature1.setLocations(order1);
		Feature feature2 = featureFactory.createFeature("feature2");
		Order<Location> order2 = new Order<Location>();
		order2.addLocation(locationFactory.createLocalRange((long) 40,
				(long) 46));
		feature2.setLocations(order2);
		entry.addFeature(feature1);
		entry.addFeature(feature2);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() ==2);
		}

	@Test
	public void testCheck_featureLocationBeginandEndwithinnewSequence() {
		int beginN = 10;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 12,
				(long) 20));
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() == 0);
	}

	@Test
	public void testCheck_featureLocationBeginOREndoutofnewSequence() {
		int beginN = 10;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory
				.createLocalRange((long) 1, (long) 20));
		Feature feature2 = featureFactory.createFeature("feature2");
		Order<Location> order2 = new Order<Location>();
		order2.addLocation(locationFactory.createLocalRange((long) 20,
				(long) 36));
		feature2.setLocations(order2);
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		entry.addFeature(feature2);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() == 0);
	}

	@Test
	public void testCheck_featureLocationBeginandEndequals() {
		int beginN = 10;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory
				.createLocalRange((long) 1, (long) 11));
		Feature feature2 = featureFactory.createFeature("feature2");
		Order<Location> order2 = new Order<Location>();
		order2.addLocation(locationFactory.createLocalRange((long) 36,
				(long) 46));
		feature2.setLocations(order2);
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		entry.addFeature(feature2);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() == 2);
		//Collection<ValidationMessage> validationMessages=Utils.shiftLocation(entry, beginN);
		
	}

	@Test
	public void testCheck_GapLocation() {
		int beginN = 10;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("gap");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 40,
				(long) 46));
		Feature feature2 = featureFactory.createFeature("gap");
		Order<Location> order2 = new Order<Location>();
		order2.addLocation(locationFactory.createLocalRange((long) 1, (long) 8));
		feature2.setLocations(order2);
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		entry.addFeature(feature2);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() == 2);
		Collection<ValidationMessage> validationMessages=Utils.shiftLocation(entry, beginN,false);
		
	}

	@Test
	public void testCheck_SequencewithallNs() {
		int beginN = 40;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 20,
				(long) 26));
		Feature feature2 = featureFactory.createFeature("gap");
		Order<Location> order2 = new Order<Location>();
		order2.addLocation(locationFactory.createLocalRange((long) 14,
				(long) 30));
		feature2.setLocations(order2);
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		entry.addFeature(feature2);
		assertTrue(Utils.shiftLocation(entry, beginN,false).size() == 2);
	}

	@Test
	public void testCheck_QualifierWithInvalidLocation() {
		int beginN = 40;
		Feature feature = featureFactory.createFeature("feature1");
		AnticodonQualifier antiCodonqualifier = new AnticodonQualifier("(pos:39..300,aa:Phe)");
		TranslExceptQualifier translExceptqualifier=new TranslExceptQualifier("(pos:20..30,aa:Trp)");
		Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier("950..960");
		Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier("30..40");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 1,
				(long) 900));
		feature.setLocations(order1);
		feature.addQualifier(translExceptqualifier);
		feature.addQualifier(antiCodonqualifier);
		feature.addQualifier(rptUnitRangequalifier);
		feature.addQualifier(tagPeptidequalifier);
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(translExceptqualifier, beginN, feature).getMessageKey());
		//assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(antiCodonqualifier, beginN, feature).getMessageKey());
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(rptUnitRangequalifier, beginN, feature).getMessageKey());
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(tagPeptidequalifier, beginN, feature).getMessageKey());
	}
	@Test
	public void testCheck_QualifierWithValidLocation() {
		int beginN = 400;
		Feature feature = featureFactory.createFeature("feature1");
		AnticodonQualifier antiCodonqualifier = new AnticodonQualifier("(pos:50..300,aa:Phe)");
		TranslExceptQualifier translExceptqualifier=new TranslExceptQualifier("(pos:100..30,aa:Trp)");
		Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier("200..920");
		Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier("60..90");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 1,
				(long) 900));
		feature.setLocations(order1);
		feature.addQualifier(translExceptqualifier);
		feature.addQualifier(antiCodonqualifier);
		feature.addQualifier(rptUnitRangequalifier);
		feature.addQualifier(tagPeptidequalifier);
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(translExceptqualifier, beginN, feature).getMessageKey());
		//assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(antiCodonqualifier, beginN, feature).getMessageKey());
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(rptUnitRangequalifier, beginN, feature).getMessageKey());
		assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(tagPeptidequalifier, beginN, feature).getMessageKey());
	}

	@Test
	public void testCheck_referenceWithValidLocation() {
		int newSequenceLength = 40;
		Entry entry = entryFactory.createEntry();
		Reference reference = referenceFactory.createReference();
		Order<LocalRange> order1 = new Order<LocalRange>();
		order1.addLocation(locationFactory.createLocalRange((long) 1,
				(long) 900));
     	reference.setLocations(order1);
     	entry.addReference(reference);
		assertEquals(null,
				Utils.shiftReferenceLocation(entry, newSequenceLength));
	}

	@Test
	public void testCheck_referenceWithInValidLocation() {
		int newSequenceLength = 1;
		Entry entry = entryFactory.createEntry();
		Reference reference = referenceFactory.createReference();
		Order<LocalRange> order1 = new Order<LocalRange>();
		order1.addLocation(locationFactory.createLocalRange((long) 1,(long) 900));
     	reference.setLocations(order1);
     	entry.addReference(reference);
     	assertTrue(Utils.shiftReferenceLocation(entry, newSequenceLength)!=null);
	}

	// remove features test
	@Test
	public void testCheck_removeInvalidFeature() {
		int beginN = 40;
		boolean removeall = true;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 20,
				(long) 26));
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		assertTrue(Utils.shiftLocation(entry, beginN, removeall).size() == 1);
	}

	// feature begin and end position equal test removeall true
	@Test
	public void testCheck_featureBeginEndPositionEqual1() {
		int beginN = 40;
		boolean removeall = true;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ADFSGDFHGHJK".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 45,
				(long) 45));
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		assertTrue(Utils.shiftLocation(entry, beginN, removeall).size() == 0);
	}

	// feature begin and end position equal test removeall false
	@Test
	public void testCheck_featureBeginEndPositionEqual2() {
		int beginN = 40;
		boolean removeall = false;
		Entry entry = entryFactory.createEntry();
		Sequence newsequence = sequenceFactory.createSequenceByte("ADFSGDFHGHJK".getBytes());
		entry.setSequence(newsequence);
		Feature feature1 = featureFactory.createFeature("feature1");
		Order<Location> order1 = new Order<Location>();
		order1.addLocation(locationFactory.createLocalRange((long) 45,
				(long) 45));
		feature1.setLocations(order1);
		entry.addFeature(feature1);
		assertTrue(Utils.shiftLocation(entry, beginN, removeall).size() == 1);
	}
}
