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

import java.util.List;

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
import uk.ac.ebi.embl.api.validation.*;

public class CdsFeatureCheckTest
{

	private CdsFeatureCheck check;
	QualifierFactory qualifierFactory;
	FeatureFactory featureFactory;
	EntryFactory entryFactory;
	SequenceFactory sequenceFactory;
	LocationFactory locationFactory;
	Entry entry;
	Qualifier exceptionQualifier;
	Qualifier translationQualifier;
	Qualifier ribosomalSlippageQualifier;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		check = new CdsFeatureCheck();
		qualifierFactory = new QualifierFactory();
		featureFactory = new FeatureFactory();
		entryFactory = new EntryFactory();
		sequenceFactory= new SequenceFactory();
		locationFactory= new LocationFactory();
		entry = entryFactory.createEntry();
		exceptionQualifier= qualifierFactory.createQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		translationQualifier=qualifierFactory.createQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);
		ribosomalSlippageQualifier=qualifierFactory.createQualifier(Qualifier.RIBOSOMAL_SLIPPAGE);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures()
	{
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_withNoCDSfeatures()
	{
		Feature feature = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
		feature.addQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());

	}

	@Test
	public void testCheck_cdsExceptionwithNoTranslation()
	{
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("CdsFeatureCheck-1", Severity.ERROR));
	}

	@Test
	public void testCheck_cdsExceptionwithTranslation()
	{
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
	}
	
	@Test
	public void testCheck_MoltypeNull()
	{
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_mrnaCDScomplementLocation()
	{
		Sequence sequence=sequenceFactory.createSequence();
		sequence.setMoleculeType(Sequence.MRNA_MOLTYPE);
		entry.setSequence(sequence);
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.RIBOSOMAL_SLIPPAGE_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);
		Join<Location> locationJoin = new Join<Location>();
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(2),
				new Long(10)));
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(12),
				new Long(30)));
		feature.setLocations(locationJoin);
		locationJoin.setComplement(true);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("CdsFeatureCheck-2", Severity.ERROR));
	}
	
	@Test
	public void testCheck_CDScomplementLocation()
	{
		Sequence sequence=sequenceFactory.createSequence();
		sequence.setMoleculeType(Sequence.RRNA_MOLTYPE);
		entry.setSequence(sequence);
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier(Qualifier.EXCEPTION_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.RIBOSOMAL_SLIPPAGE_QUALIFIER_NAME);
		feature.addQualifier(Qualifier.TRANSLATION_QUALIFIER_NAME);
		Join<Location> locationJoin = new Join<Location>();
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(2),
				new Long(10)));
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(12),
				new Long(30)));
		feature.setLocations(locationJoin);
		locationJoin.setComplement(true);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
	}
	
	@Test
	public void testCheck_MrnawithCDSjoinLocation()
	{
		Sequence sequence=sequenceFactory.createSequence();
		sequence.setMoleculeType(Sequence.MRNA_MOLTYPE);
		entry.setSequence(sequence);
		Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		Join<Location> locationJoin = new Join<Location>();
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(2),
				new Long(10)));
		locationJoin.addLocation(locationFactory.createLocalRange(new Long(12),
				new Long(30)));
		feature.setLocations(locationJoin);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("CdsFeatureCheck-3", Severity.ERROR));
	}
}
