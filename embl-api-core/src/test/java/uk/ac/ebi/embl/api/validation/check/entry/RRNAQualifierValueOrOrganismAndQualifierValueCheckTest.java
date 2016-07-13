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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class RRNAQualifierValueOrOrganismAndQualifierValueCheckTest {

	private Entry entry;
	private Feature source;
	private Feature rRNA;
	private RRNAQualifierValueOrOrganismAndQualifierValueCheck check;
	private TaxonHelper taxonHelper;
	private FeatureFactory featureFactory;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();

		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		entry.addFeature(source);

		rRNA = featureFactory.createFeature("rRNA");

		taxonHelper = createMock(TaxonHelper.class);
		property.taxonHelper.set(taxonHelper);
		DataRow dataRow = new DataRow("16S ribosomal RNA", "product", "organelle", "mitochondrion,chloroplast","Bacteria,Archaea");
		check = new RRNAQualifierValueOrOrganismAndQualifierValueCheck(dataRow);
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		check = new RRNAQualifierValueOrOrganismAndQualifierValueCheck();

		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		source.addQualifier("organism", "Bacteria");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoRRNA() {
		source.addQualifier("organism", "Bacteria");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_WrongOrganelle_NoOrganism() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "some value");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("RRNAQualifierValueOrOrganismAndQualifierValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongOrganelle() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "some value");
		source.addQualifier("organism", "Bacteria");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Bacteria", "Bacteria", "Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);

		assertTrue(check.check(entry).isValid());
	}

    @Test
	public void testCheck_NonsenseOrganism() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "some value");
		source.addQualifier("organism", "David");//this is nonsense
		rRNA.addQualifier("product", "16S ribosomal RNA");

		expect(taxonHelper.isOrganismValid("David")).andReturn(Boolean.FALSE);
		replay(taxonHelper);

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_WrongProduct() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		source.addQualifier("organism", "Bacteria");
		rRNA.addQualifier("product", "some value");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_OnlyOrganism() {
		entry.addFeature(rRNA);
		source.addQualifier("organism", "Bacteria");
		rRNA.addQualifier("product", "16S ribosomal RNA");

        expect(taxonHelper.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Bacteria", "Bacteria","Archaea")).andReturn(Boolean.TRUE);
		replay(taxonHelper);

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_OnlyOrganelle() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_WrongOrganism() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		source.addQualifier("organism", "some organism");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_FullCondition() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		source.addQualifier("organism", "Bacteria");
		rRNA.addQualifier("product", "16S ribosomal RNA");

		// expect(
		// taxonHelper.isChildOfAny("Bacteria", new String[] { "Bacteria",
		// "Archaea" })).andReturn(Boolean.TRUE);
		// replay(taxonHelper);

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoConditions() {
		entry.addFeature(rRNA);
		rRNA.addQualifier("product", "16S ribosomal RNA");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("RRNAQualifierValueOrOrganismAndQualifierValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_NoProduct() {
		entry.addFeature(rRNA);
		source.addQualifier("organelle", "mitochondrion");
		source.addQualifier("organism", "Bacteria");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Message() {
		entry.addFeature(rRNA);
		rRNA.addQualifier("product", "16S ribosomal RNA");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "RRNAQualifierValueOrOrganismAndQualifierValueCheck", Severity.ERROR);
		assertEquals(
				"Value \"16S ribosomal RNA\" of qualifier \"product\" is only allowed when source qualifier \"organelle\" has one of values \"mitochondrion, chloroplast\" or contains the word \"plastid\" or organism belongs to one of \"Bacteria, Archaea\".",
				messages.iterator().next().getMessage());
	}

}
