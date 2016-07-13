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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.CDS_RNA_LocusFix;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnticodonQualifierFixTest
{

	private Entry entry;
	private AnticodonQualifierFix check;
	public EntryFactory entryFactory;
	public FeatureFactory featureFactory;
	public LocationFactory locationFactory;
	public QualifierFactory qualifierFactory;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

		entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		locationFactory = new LocationFactory();
		qualifierFactory = new QualifierFactory();

		entry = entryFactory.createEntry();

		Sequence sequence = new SequenceFactory()
				.createSequenceByte("tatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatttatggcaatt".getBytes());
		entry.setSequence(sequence);

		check = new AnticodonQualifierFix();
	}

	@Test
	public void testCheck_seqEmpty() throws ValidationEngineException
	{
		entry.setSequence(null);
		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());// dont make a
																// fuss, other
																// checks for
																// that
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoAnticodon() throws ValidationEngineException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_Anticodon() throws ValidationEngineException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
		assertEquals(1, validationResult.count("AnticodonQualifierFix_1",
				Severity.FIX));
	}

	@Test
	public void testCheck_AnticodonWithSequence() throws ValidationEngineException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}
	
	@Test
	public void testCheck_AnticodonWithAminoAcidNotMatch() throws ValidationEngineException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:GlU,seq:tta)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
		assertEquals(1, validationResult.count("AnticodonQualifierFix_2",
				Severity.FIX));
	}

}