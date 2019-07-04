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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

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
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SourceFeatureQualifierCheckTest {
	private Entry entry;
	private SourceFeature source,source1;
	private FeatureFactory featureFactory;
	private QualifierFactory qualifierFactory;
	private SourceFeatureQualifierCheck check;
	private Feature feature;

	@Before
	public void setUp() throws SQLException {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		qualifierFactory = new QualifierFactory();
		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		entry.addFeature(source);

    	check = new SourceFeatureQualifierCheck();

		EmblEntryValidationPlanProperty planProperty=new EmblEntryValidationPlanProperty();
		planProperty.taxonHelper.set(new TaxonHelperImpl());
		check.setEmblEntryValidationPlanProperty(planProperty);

	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("strain");
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoSourceFeature() {
		entry.removeFeature(source);
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoGene() {
		source.addQualifier(qualifierFactory
				.createQualifier("strain", "BALB/c"));
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_MultipleSourceDifferentOrganism() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism",
				"Homo sapiens"));
		source1.addQualifier(qualifierFactory.createQualifier("organism",
				"Cloning vector pBeloBAC11"));
		source.setFocus(false);
		source1.setFocus(false);
		source.setTransgenic(false);
		source1.setTransgenic(false);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck2", Severity.ERROR));
	}

	@Test
	public void testMultipleSourceDifferentOrganismFocus() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism",
				"Homo sapiens"));
		source1.addQualifier(qualifierFactory.createQualifier("organism",
				"Cloning vector pBeloBAC11"));
		source.setFocus(true);
		source1.setFocus(false);
		source.setTransgenic(false);
		source1.setTransgenic(false);

		ValidationResult result = check.check(entry);
		assertEquals(0, result.count( Severity.ERROR));
	}

	@Test
	public void testMultipleSourceMultipleFocus() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism",
				"Homo sapiens"));
		source1.addQualifier(qualifierFactory.createQualifier("organism",
				"Cloning vector pBeloBAC11"));
		source.setFocus(true);
		source1.setFocus(false);
		source.setTransgenic(false);
		source1.setTransgenic(false);

		Feature geneFeature = featureFactory.createFeature("gene");
		geneFeature.addQualifier("gene", "b");
		geneFeature.addQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		entry.addFeature(geneFeature);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck3", Severity.ERROR));
	}

	@Test
	public void testFocusMisplacement() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism",
				"Homo sapiens"));
		source1.addQualifier(qualifierFactory.createQualifier("organism",
				"Cloning vector pBeloBAC11"));
		source.setFocus(false);
		source1.setFocus(false);
		source.setTransgenic(false);
		source1.setTransgenic(false);

		Feature geneFeature = featureFactory.createFeature("gene");
		geneFeature.addQualifier("gene", "b");
		geneFeature.addQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		entry.addFeature(geneFeature);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("FocusAllowedOnlyInPrimarySource", Severity.ERROR));
	}
	@Test
	public void testCheck_GeneWithNoPattern() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "b");
		source.addQualifier(qualifierFactory
				.createQualifier("strain", "BALB/c"));
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_multipleFocus() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("strain");
		source.addQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		source.addQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck3", Severity.ERROR));
		}
	
	@Test
	public void testCheck_multipleTransgenic() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("strain");
		source.addQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME);
		source.addQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck4", Severity.ERROR));
		}
	
	@Test
	public void testCheck_withTransgenicandFocus() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("strain");
		source.addQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		source.addQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck5", Severity.ERROR));
		}
	
	@Test
	public void testCheck_OrganismSubmittable() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism","uncultured fungus"));
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck8", Severity.ERROR));
		SourceFeature source2 = featureFactory.createSourceFeature();
		entry.addFeature(source2);
		source2.addQualifier(qualifierFactory.createQualifier("organism","Pseudendoclonium basiliense"));
		result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck8", Severity.ERROR));
		}

	@Test
	public void testMetagenomeSourceWithEnvSampleQual()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier(
				"(pos:10..12,aa:Glu,seq:tta)"));
		SourceFeature sourceFeature = featureFactory.createSourceFeature();

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester metagenome"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));

		entry.addFeature(feature);
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.ERROR).size());
	}

	@Test
	public void testMetagenomeSourceWithNoEnvSampleQual()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier(
				"(pos:10..12,aa:Glu,seq:tta)"));
		SourceFeature sourceFeature = featureFactory.createSourceFeature();

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester metagenome"));

		entry.addFeature(feature);
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.ERROR).size());
		assertEquals(1, validationResult.count("EnvSampleRequiredForMetagenome",Severity.ERROR));
	}

	@Test
	public void testInvalidMetagenomeSource()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier(
				"(pos:10..12,aa:Glu,seq:tta)"));
		SourceFeature sourceFeature = featureFactory.createSourceFeature();

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester "));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));

		entry.addFeature(feature);
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.ERROR).size());
		assertEquals(1, validationResult.count("InvalidMetagenomeSource",Severity.ERROR));
	}

	@Test
	public void testMoreThanOneMetagenomeSource()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(new AnticodonQualifier(
				"(pos:10..12,aa:Glu,seq:tta)"));
		SourceFeature sourceFeature = featureFactory.createSourceFeature();

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester metagenome"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(
				Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester metagenome"));

		sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));

		entry.addFeature(feature);
		entry.addFeature(sourceFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.ERROR).size());
		assertEquals(1, validationResult.count("MorethanOneMetagenome", Severity.ERROR));
	}

}
