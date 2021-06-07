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

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class MoleculeTypeAndSourceQualifierCheckTest {

	private Entry entry;
	private Feature source;
	private MoleculeTypeAndSourceQualifierCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		FeatureFactory featureFactory = new FeatureFactory();

		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		entry.addFeature(source);

		Sequence sequence = sequenceFactory.createSequence();
		entry.setSequence(sequence);

		DataRow dataRow = new DataRow(
				"tissue_type,dev_stage,isolation_source,collection_date,host,lab_host,sex,mating_type,haplotype,cultivar,ecotype,variety,breed,isolate,strain,clone,country,lat_lon,specimen_voucher,culture_collection,biomaterial,PCR_primers",
				"mRNA");
		GlobalDataSets.addTestDataSet(GlobalDataSetFile.MOLTYPE_SOURCE_QUALIFIERS, dataRow);

		DataRow dataRow1=new DataRow("genomic DNA",Qualifier.GERMLINE_QUALIFIER_NAME);
		GlobalDataSets.addTestDataSet(GlobalDataSetFile.SOURCE_QUALIFIERS_MOLTYPE_VALUES, dataRow1);

		check = new MoleculeTypeAndSourceQualifierCheck();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
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
		source.addQualifier("organism", "liver");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_noSourceQualifier() {
		entry.getSequence().setMoleculeType("mRNA");
		ValidationResult result = check.check(entry);
		assertEquals(2, result.count("MoleculeTypeAndSourceQualifierCheck",
				Severity.ERROR));
	}

	@Test
	public void testCheck_NoSource() {
		entry.getSequence().setMoleculeType("mRNA");
		entry.removeFeature(source);

		ValidationResult result = check.check(entry);
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_noRequiredQualifier() {
		entry.getSequence().setMoleculeType("mRNA");
		source.addQualifier("organism", "some organism");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.getMessages().size());
	}

	@Test
	public void testCheck_requiredQualifier() {
		entry.getSequence().setMoleculeType("mRNA");
		source.addQualifier("tissue_type", "liver");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_Message() {
		entry.getSequence().setMoleculeType("mRNA");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
				"MoleculeTypeAndSourceQualifierCheck", Severity.ERROR);
		assertEquals(
				"At least one of the Qualifiers \"tissue_type, dev_stage, isolation_source, collection_date, host, lab_host, sex, mating_type, haplotype, cultivar, ecotype, variety, breed, isolate, strain, clone, country, lat_lon, specimen_voucher, culture_collection, biomaterial, PCR_primers\" must exist in Source feature if Molecule Type matches the Value \"mRNA\".",
				messages.iterator().next().getMessage());
	}
	
	@Test
	public void testCheck_invalidMolTypeValue() {
		entry.getSequence().setMoleculeType("mRNA");
		source.addQualifier(Qualifier.GERMLINE_QUALIFIER_NAME);
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
		assertEquals(1,result.getMessages("MoleculeTypeAndSourceQualifierCheck_1", Severity.ERROR).size());
		}
	
	@Test
	public void testCheck_validMolTypeValue() {
		entry.getSequence().setMoleculeType("genomic DNA");
		source.addQualifier(Qualifier.GERMLINE_QUALIFIER_NAME);
		entry.addFeature(source);
		ValidationResult result = check.check(entry);
		assertEquals(0,result.getMessages("MoleculeTypeAndSourceQualifierCheck_1", Severity.ERROR).size());
		}

}
