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
import java.util.ArrayList;

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
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.fixer.sourcefeature.AssemblySourceQualiferFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblySourceQualiferFixTest
{

	private Entry entry;
	private AssemblySourceQualiferFix check;
	public EntryFactory entryFactory;
	public FeatureFactory featureFactory;
	public QualifierFactory qualifierFactory;
	private EntryDAOUtils entryDAOUtils;
	private EmblEntryValidationPlanProperty planProperty;
	private ArrayList<Qualifier> chromosomeQualifiers;
	private ArrayList<Qualifier> sourceMandatoryQualifiers;
	private SourceFeature sourceFeature;
	private Entry masterEntry;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

		entryFactory = new EntryFactory();
		masterEntry=entryFactory.createEntry();
		qualifierFactory = new QualifierFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		sourceFeature =featureFactory.createSourceFeature();
		chromosomeQualifiers = new ArrayList<Qualifier>();
		sourceMandatoryQualifiers = new ArrayList<Qualifier>();
		chromosomeQualifiers.add(qualifierFactory.createQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, "AB"));
		sourceMandatoryQualifiers.add(qualifierFactory.createQualifier(Qualifier.MOL_TYPE_QUALIFIER_NAME, "rRNA"));
		sourceFeature.addQualifiers(sourceMandatoryQualifiers);
		masterEntry.addFeature(sourceFeature);
		entryDAOUtils = createMock(EntryDAOUtils.class);
		planProperty=new EmblEntryValidationPlanProperty();
		planProperty.analysis_id.set("ERZ0001");
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check = new AssemblySourceQualiferFix();
		check.setEmblEntryValidationPlanProperty(planProperty);
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		check.setEntryDAOUtils(entryDAOUtils);
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures() throws ValidationEngineException
	{
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_NoPrimarySource() throws ValidationEngineException
	{
		check.setEntryDAOUtils(entryDAOUtils);
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_withPrimarySource() throws ValidationEngineException, SQLException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		Qualifier organismQualifier = qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
		sourceFeature.addQualifier(organismQualifier);
		entry.addFeature(sourceFeature);
		expect(entryDAOUtils.getChromosomeQualifiers(planProperty.analysis_id.get(),entry.getSubmitterAccession(),sourceFeature)).andReturn(chromosomeQualifiers);
		expect(entryDAOUtils.getMasterEntry(planProperty.analysis_id.get())).andReturn(masterEntry);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
	}
	
	@Test
	public void testCheck_NoDBConnection() throws ValidationEngineException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_nonAssemblyScope() throws ValidationEngineException,
			SQLException
	{
		planProperty.validationScope.set(ValidationScope.EMBL);
		check.setEmblEntryValidationPlanProperty(planProperty);
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		Qualifier organismQualifier = qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
		sourceFeature.addQualifier(organismQualifier);
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_ValidAssemblyScope()
			throws ValidationEngineException, SQLException
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		Qualifier organismQualifier = qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
		sourceFeature.addQualifier(organismQualifier);
		entry.addFeature(sourceFeature);
		expect(entryDAOUtils.getChromosomeQualifiers(planProperty.analysis_id.get(),entry.getSubmitterAccession(),sourceFeature)).andReturn(chromosomeQualifiers);
		expect(entryDAOUtils.getMasterEntry(planProperty.analysis_id.get())).andReturn(masterEntry);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
		assertEquals(1, validationResult.count("AssemblySourceQualiferFix_2", Severity.FIX));
	}

	@Test
	public void testCheck_chromosomeAssemblyScope()
			throws ValidationEngineException, SQLException
	{
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		check.setEmblEntryValidationPlanProperty(planProperty);
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
		entry.addFeature(feature);
		SourceFeature sourceFeature = featureFactory.createSourceFeature();
		Qualifier organismQualifier = qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
		sourceFeature.addQualifier(organismQualifier);
		entry.addFeature(sourceFeature);
		expect(entryDAOUtils.getMasterEntry(planProperty.analysis_id.get())).andReturn(masterEntry);
		expect(entryDAOUtils.getChromosomeQualifiers(planProperty.analysis_id.get(),entry.getSubmitterAccession(),masterEntry.getPrimarySourceFeature())).andReturn(chromosomeQualifiers);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(2, validationResult.getMessages(Severity.FIX).size());
		assertEquals(1, validationResult.count("AssemblySourceQualiferFix_2", Severity.FIX));
		assertEquals(1, validationResult.count("AssemblySourceQualiferFix_1", Severity.FIX));
	}
	
}