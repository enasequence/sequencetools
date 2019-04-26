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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
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
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.entry.OrganismAndPermittedQualifierCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class OrganismAndPermittedQualifierCheckTest {

	private Entry entry;
	private SourceFeature source;
	private OrganismAndPermittedQualifierCheck check;
	private TaxonHelper taxonHelper;
	EntryFactory entryFactory;
	FeatureFactory featureFactory;
	EmblEntryValidationPlanProperty property;
	
	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory=new EntryFactory();
		featureFactory=new FeatureFactory();
		entry=entryFactory.createEntry();
		property=new EmblEntryValidationPlanProperty();
		source=featureFactory.createSourceFeature();
		taxonHelper = createMock(TaxonHelper.class);
		property.taxonHelper.set(taxonHelper);
		DataRow dataRow = new DataRow("virion", "Viruses,Viroids");
        DataSetHelper.createAndAdd(FileName.ORG_PERMITTED_QUALIFIER, dataRow);
		check = new OrganismAndPermittedQualifierCheck();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		check.check(entry);
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		entry.addFeature(source);
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoVirion() {
		source.setSingleQualifierValue("organism", "Deltavirus");
		entry.addFeature(source);
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoOrganism() {
		source.setSingleQualifier("virion");
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("OrganismAndPermittedQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoOrganismValue() {
		source.setSingleQualifier("organism");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("OrganismAndPermittedQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongOrganism() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
		expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Bacteria", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE);
		replay(taxonHelper);

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("OrganismAndPermittedQualifierCheck3", Severity.ERROR));
	}

	@Test
	public void testCheck() {
		source.setSingleQualifierValue("organism", "Deltavirus");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.FALSE);
		replay(taxonHelper);	
		
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_Message() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Bacteria", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE);
		replay(taxonHelper);		

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "OrganismAndPermittedQualifierCheck3", Severity.ERROR);
		assertEquals(
				"Organism \"Bacteria\" not belongs to \"Viruses, Viroids\".Qualifier \"virion\" is only permitted when organism belongs to \"Viruses, Viroids\".",
				messages.iterator().next().getMessage());
	}

	@Test
	public void testCheck_withValidProduct() throws SQLException {
		Feature feature=featureFactory.createFeature("feature");
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME,"16S ribosomal RNA");
		entry.addFeature(feature);
		source.setSingleQualifierValue("organism", "Deltavirus");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);
		property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
		check.setEmblEntryValidationPlanProperty(property);
		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages("OrganismAndPermittedQualifierCheck2").size()==0);
		assertTrue(result.isValid());
	}
	@Test
	public void testCheck_withValidGene() throws SQLException {
		Feature feature=featureFactory.createFeature("feature");
		feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME,"rRNA");
		entry.addFeature(feature);
		source.setSingleQualifierValue("organism", "Deltavirus");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);
		property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
		check.setEmblEntryValidationPlanProperty(property);		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages("OrganismAndPermittedQualifierCheck2").size()==0);
		assertTrue(result.isValid());
	}
	
	@Test
	public void testCheck_withInValidProduct() throws SQLException {
		Feature feature=featureFactory.createFeature("feature");
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME,"16S RNA");
		entry.addFeature(feature);
		source.setSingleQualifierValue("organism", "Deltavirus");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);
		property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
		check.setEmblEntryValidationPlanProperty(property);		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages("OrganismAndPermittedQualifierCheck2").size()==1);
		assertTrue(result.isValid());
	}
	
	@Test
	public void testCheck_withInValidProductandGene() throws SQLException {
		Feature feature=featureFactory.createFeature("feature");
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME,"16S RNA");
		feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME,"rNA");
		entry.addFeature(feature);
		source.setSingleQualifierValue("organism", "Deltavirus");
		source.setSingleQualifier("virion");
		entry.addFeature(source);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
        expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[]{"Viruses", "Viroids"})).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);
		property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
		check.setEmblEntryValidationPlanProperty(property);		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages("OrganismAndPermittedQualifierCheck2").size()==1);
		assertTrue(result.isValid());
	}

}
