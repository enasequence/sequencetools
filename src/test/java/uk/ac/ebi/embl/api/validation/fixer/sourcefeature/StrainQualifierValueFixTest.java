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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.sql.SQLException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StrainQualifierValueFixTest
{

	private Entry entry;
	private StrainQualifierValueFix check;
	public EntryFactory entryFactory;
	public FeatureFactory featureFactory;
	public QualifierFactory qualifierFactory;
	private TaxonomyClient taxonClient;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

		entryFactory = new EntryFactory();
		qualifierFactory = new QualifierFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		taxonClient = createMock(TaxonomyClient.class);
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.taxonClient.set(taxonClient);
		check = new StrainQualifierValueFix();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures()
	{
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_NoPrimarySource()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_primarySourcewithNoStrainQualifier()
	{
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_primarySourcewithnoTypeStrainQualifier()
	{
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		sourceFeature.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "VMP-1");
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_primarySourcewithTypeStrainQualifier()
	{
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		sourceFeature.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "VMP-1T");
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		Collection<ValidationMessage<Origin>> messages = validationResult.getMessages("StrainQualifierValueandDescriptionFix_1", Severity.FIX);
		assertEquals(1, messages.size());
	}

	
	@Test
	public void testCheck_primarySourcewithformalOrganism()
	{
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		sourceFeature.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "VMP-1(T)");
		sourceFeature.setScientificName("Transposon Tn1546");
		entry.addFeature(sourceFeature);
		expect(taxonClient.isOrganismFormal("Transposon Tn1546")).andReturn(Boolean.TRUE);
		replay(taxonClient);
		ValidationResult validationResult = check.check(entry);
		assertEquals("type strain: VMP-1", entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.STRAIN_QUALIFIER_NAME).getValue());
		assertTrue(validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
	}
}