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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class OrganismAndRequiredQualifierCheckTest {

	private SourceFeature source;
	private OrganismAndRequiredQualifierCheck check;
	private TaxonomyClient taxonomyClient;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		source = featureFactory.createSourceFeature();

		taxonomyClient = createMock(TaxonomyClient.class);
		DataRow dataRow = new DataRow("strain,environmental_sample", "Bacteria,Archaea", "ERROR");

        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        property.taxonClient.set(taxonomyClient);
        GlobalDataSets.addTestDataSet(GlobalDataSetFile.ORGANISM_REQUIRED_QUALIFIER, dataRow);
		check = new OrganismAndRequiredQualifierCheck( );
		check.setEmblEntryValidationPlanProperty(property);
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		source.setSingleQualifierValue("organism", "Bacteria");

		expect(taxonomyClient.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.TRUE).once();
		expect(taxonomyClient.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE).once();
		replay(taxonomyClient);

		ValidationResult result = check.check(source);
        verify(taxonomyClient);
		assertEquals(1, result.count("OrganismAndRequiredQualifierCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_NoOrganism() {
		source.setSingleQualifier("strain");
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_NoOrganismValue() {
		source.setSingleQualifier("organism");
		source.setSingleQualifier("strain");

		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_WrongOrganism() {
		source.setSingleQualifierValue("organism", "Virus");
		source.setSingleQualifier("strain");

		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_WrongQualifier() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("qual");

		expect(taxonomyClient.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.TRUE).once();
		expect(taxonomyClient.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE).once();
        replay(taxonomyClient);

		ValidationResult result = check.check(source);
        verify(taxonomyClient);
		assertEquals(1, result.count("OrganismAndRequiredQualifierCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_OnlyStrain() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("strain");

		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_OnlyEnvironmentalSample() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("environmental_sample");

		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_BothQuals() {
		source.setSingleQualifierValue("organism", "Bacteria");
		source.setSingleQualifier("strain");
		source.setSingleQualifier("environmental_sample");

		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_Message() {
		source.setSingleQualifierValue("organism", "Bacteria");

		expect(taxonomyClient.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.TRUE).once();
		expect(taxonomyClient.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE).once();
        replay(taxonomyClient);

		ValidationResult result = check.check(source);
        verify(taxonomyClient);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "OrganismAndRequiredQualifierCheck", Severity.ERROR);
		assertEquals(
				"At least one of the following qualifiers \"strain, environmental_sample\" must exist when organism belongs to Bacteria.",
				messages.iterator().next().getMessage());
	}

}
