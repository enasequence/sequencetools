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
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.MoleculeTypeAndOrganismCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class MoleculeTypeAndOrganismCheckTest {

	private Entry entry;
	private Feature source;
	private MoleculeTypeAndOrganismCheck check;
	private TaxonHelper taxonHelper;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		FeatureFactory featureFactory = new FeatureFactory();
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		entry.addFeature(source);

		Sequence sequence = sequenceFactory.createSequence();
		entry.setSequence(sequence);

		taxonHelper = createMock(TaxonHelper.class);
		property.taxonHelper.set(taxonHelper);
		DataRow dataRow = new DataRow("Deltavirus,Retro-transcribing viruses,ssRNA viruses,dsRNA viruses", "genomic RNA");
		DataSetHelper.createAndAdd(FileName.MOLTYPE_ORGANISM, dataRow);
		check = new MoleculeTypeAndOrganismCheck();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		entry.getSequence().setMoleculeType("genomic RNA");
		source.addQualifier("organism", "Deltavirus");
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoMoleculeType() {
		entry.getSequence().setMoleculeType(null);
		source.addQualifier("organism", "Deltavirus");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoSequence() {
		entry.setSequence(null);
		source.addQualifier("organism", "Deltavirus");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_OneOrganism() {
		entry.getSequence().setMoleculeType("genomic RNA");
		source.addQualifier("organism", "Deltavirus");

		expect(taxonHelper.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
		expect(taxonHelper.isChildOfAny("Deltavirus", new String[] {
						"Deltavirus", "Retro-transcribing viruses",
						"ssRNA viruses", "dsRNA viruses" })).andReturn(Boolean.TRUE);
		replay(taxonHelper);

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoOrganisms() {
		entry.getSequence().setMoleculeType("genomic RNA");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("MoleculeTypeAndOrganismCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_NoSource() {
        entry.getSequence().setMoleculeType("genomic RNA");
        entry.removeFeature(source);

		ValidationResult result = check.check(entry);
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_WrongOrganism() {
		entry.getSequence().setMoleculeType("genomic RNA");
		source.addQualifier("organism", "some organism");

		ValidationResult result = check.check(entry);
        assertEquals(0, result.getMessages().size());//just leaves if the organism is not recognized
	}

	@Test
	public void testCheck_Message() {
		entry.getSequence().setMoleculeType("genomic RNA");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "MoleculeTypeAndOrganismCheck", Severity.ERROR);
		assertEquals(
				"Organism must belong to one of \"Deltavirus, Retro-transcribing viruses, ssRNA viruses, dsRNA viruses\" when molecule type is \"genomic RNA\".",
				messages.iterator().next().getMessage());
	}

}
