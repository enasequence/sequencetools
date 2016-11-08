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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.OrganismPatternAndQualifierValueCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class OrganismPatternAndQualifierValueCheckTest {

	private SourceFeature source;
	private OrganismPatternAndQualifierValueCheck check;
	private TaxonHelper taxonHelper;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		source = featureFactory.createSourceFeature();

		taxonHelper = createMock(TaxonHelper.class);
		DataRow dataRow = new DataRow("Bacteria", "sub_species", ".+\\s(subsp.)\\s", ".*");
        DataSet dataSet = new DataSet();
        dataSet.addRow(dataRow);
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        property.taxonHelper.set(taxonHelper);
		check = new OrganismPatternAndQualifierValueCheck(dataSet);
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		check = new OrganismPatternAndQualifierValueCheck();
		check.check(source);
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoSubSpecies() {
		source.addQualifier("organism", "Salmonella enterica subsp. enterica");
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoOrganism() {
		source.addQualifier("sub_species", "enterica");
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoOrganismValue() {
		source.addQualifier("organism");
		source.addQualifier("sub_species", "enterica");
		assertTrue(check.check(source).isValid());
	}
	
	@Test
	public void testCheck_WrongOrganism() {
		source.setSingleQualifierValue("organism", "Virus");
		source.addQualifier("sub_species", "enterica");
		
		expect(taxonHelper.isChildOf("Virus", "Bacteria")).andReturn(Boolean.FALSE);
		replay(taxonHelper);		
		
		assertTrue(check.check(source).isValid());
	}	

	@Test
	public void testCheck_OkPattern() {
		source.addQualifier("organism", "Salmonella enterica subsp. enterica");
		source.addQualifier("sub_species", "enterica");
		
		expect(taxonHelper.isChildOf("Salmonella enterica subsp. enterica", "Bacteria")).andReturn(Boolean.TRUE);
		replay(taxonHelper);
		
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_WrongPattern() {
		source.addQualifier("organism", "Salmonella enterica");
		source.addQualifier("sub_species", "enterica");

		expect(taxonHelper.isChildOf("Salmonella enterica", "Bacteria")).andReturn(Boolean.TRUE);
		replay(taxonHelper);		
		
		ValidationResult result = check.check(source);
		assertEquals(1, result.count("OrganismPatternAndQualifierValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		source.addQualifier("organism", "Salmonella enterica");
		source.addQualifier("sub_species", "enterica");

		expect(taxonHelper.isChildOf("Salmonella enterica", "Bacteria")).andReturn(Boolean.TRUE);
		replay(taxonHelper);		
		
		ValidationResult result = check.check(source);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "OrganismPatternAndQualifierValueCheck", Severity.ERROR);
		assertEquals(
				"If the organism belongs to the specified lineage \"Bacteria\" and qualifier \"sub_species\" exists, the pattern formed by wrapping the qualifier value \"sub_species\" with patterns \".+\\s(subsp.)\\s\" and \".*\" must match.",
				messages.iterator().next().getMessage());
	}

}
