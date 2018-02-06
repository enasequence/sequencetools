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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;

import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.SourceFeatureQualifierCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

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
}
