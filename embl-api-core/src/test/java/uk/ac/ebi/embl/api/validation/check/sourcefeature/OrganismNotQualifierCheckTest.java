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
import uk.ac.ebi.embl.api.validation.check.sourcefeature.OrganismNotQualifierCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class OrganismNotQualifierCheckTest {

	private SourceFeature source;
	private OrganismNotQualifierCheck check;
	private TaxonHelper taxonHelper;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		source = featureFactory.createSourceFeature();

		taxonHelper = createMock(TaxonHelper.class);
		DataRow dataRow = new DataRow("serotype", "Archaea,Bacteria");
        DataSet dataSet = new DataSet();
        dataSet.addRow(dataRow);
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        property.taxonHelper.set(taxonHelper);
		check = new OrganismNotQualifierCheck(dataSet);
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
        source.setSingleQualifierValue("organism", "Crenarchaeota");
		check = new OrganismNotQualifierCheck();
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
	public void testCheck_NoSerotype() {
		source.setSingleQualifierValue("organism", "Crenarchaeota");
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoOrganism() {
		source.setSingleQualifier("serotype");
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoOrganismValue() {
		source.setSingleQualifier("organism");
		source.setSingleQualifier("serotype");
		assertTrue(check.check(source).isValid());
	}
	
	
	@Test
	public void testCheck_WrongOrganism() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("serotype");
		
		expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE);
		replay(taxonHelper);
		
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck() {
		source.setSingleQualifierValue("organism", "Crenarchaeota");
		source.setSingleQualifier("serotype");
		
        expect(taxonHelper.isOrganismValid("Crenarchaeota")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Crenarchaeota", "Bacteria")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Crenarchaeota", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);

		ValidationResult result = check.check(source);
		assertEquals(1, result.count("OrganismNotQualifierCheck-2", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		source.setSingleQualifierValue("organism", "Crenarchaeota");
		source.setSingleQualifier("serotype");
		
        expect(taxonHelper.isOrganismValid("Crenarchaeota")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOf("Crenarchaeota", "Archaea")).andReturn(Boolean.FALSE);
		expect(taxonHelper.isChildOf("Crenarchaeota", "Bacteria")).andReturn(Boolean.TRUE);
		replay(taxonHelper);

		ValidationResult result = check.check(source);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "OrganismNotQualifierCheck-2", Severity.ERROR);
		assertEquals(
				"Qualifier \"serotype\" must not exist when organism belongs to \"Bacteria\".",
				messages.iterator().next().getMessage());
	}

}
