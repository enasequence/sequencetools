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

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class MasterEntrySourceCheckTest {

	private Entry entry;
	private MasterEntrySourceCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		check=new MasterEntrySourceCheck();
		
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}
	

	@Test
	public void testCheck_NoPrimarySource() {
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_validSourcefeature() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		check.setEmblEntryValidationPlanProperty(property);
		SourceFeature source= (new FeatureFactory()).createSourceFeature();
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"dfgh");
		source.addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,"rgd");
		source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "human");
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
	}

	@Test
	public void testCheck_InvalidSourcefeature() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		check.setEmblEntryValidationPlanProperty(property);
		SourceFeature source= (new FeatureFactory()).createSourceFeature();
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"dfgh");
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"sffg");
		source.addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,"rgd");
		source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "human");
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
		assertEquals(1, result.count("MasterEntrySourceCheck_1", Severity.ERROR));

	}
	
	@Test
	public void testCheck_validSourcefeaturenotSubmittable() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		TaxonHelper taxonHelper= new TaxonHelperImpl();
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		SourceFeature source= (new FeatureFactory()).createSourceFeature();
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"dfgh");
		source.addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,"rgd");
		source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "[Desulfotomaculus] guttoideum");
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
		assertEquals(1, result.count("MasterEntrySourceCheck_2", Severity.ERROR));

	}
	@Test
	public void testCheck_validSourcefeatureSubmittable() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		TaxonHelper taxonHelper= new TaxonHelperImpl();
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		SourceFeature source= (new FeatureFactory()).createSourceFeature();
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"dfgh");
		source.addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,"rgd");
		source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Pseudendoclonium basiliense");
       // source.setTaxId(58134l);
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
	}
	
}
