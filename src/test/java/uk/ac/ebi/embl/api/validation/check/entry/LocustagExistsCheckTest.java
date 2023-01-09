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

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

import java.sql.SQLException;

public class LocustagExistsCheckTest {

	private Entry entry;
	private LocustagExistsCheck check;
	private EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() throws SQLException {
		EntryFactory entryFactory = new EntryFactory();
		planProperty=new EmblEntryValidationPlanProperty();
		planProperty.fileType.set(FileType.EMBL);
		planProperty.taxonClient.set(new TaxonomyClient());
		entry = entryFactory.createEntry();
		entry.setDataClass(Entry.WGS_DATACLASS);
		check = new LocustagExistsCheck();
		check.setEmblEntryValidationPlanProperty(planProperty);
    }

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_nonEmblFile() throws SQLException {
		planProperty.fileType.set(FileType.FASTA);
		check.setEmblEntryValidationPlanProperty(planProperty);
		ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }

	@Test
	public void testCheck_WGSFlatFileWithNoLocus_tag() {
        Feature feature=new FeatureFactory().createFeature("feature");
		entry.addFeature(feature); 
        ValidationResult validationResult = check.check(entry);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.count("LocustagExistsCheck_1", Severity.ERROR));

    }
	
	@Test
	public void testCheck_WGSFlatFileWithSourceandAssembly_gap() {
        SourceFeature source=new FeatureFactory().createSourceFeature();
        Feature assemblyGapFeature=new FeatureFactory().createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
		entry.addFeature(source);
		entry.addFeature(assemblyGapFeature);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
    }

	@Test
	public void testCheck_CONFlatFileWithNoLocus_tag() {
		entry.setDataClass(Entry.CON_DATACLASS);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }
	
	@Test
	public void testCheck_FlatFileWithLocus_tag() {
		Feature feature=new FeatureFactory().createFeature("feature");
		feature.addQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
		entry.addFeature(feature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }
	
	@Test
	public void testCheck_FlatFileWithVirusSeqenceNolocus_tag() {
       SourceFeature source=new FeatureFactory().createSourceFeature();
       source.setScientificName("Siadenovirus");
		Feature feature=new FeatureFactory().createFeature("feature");
		entry.addFeature(feature);
		entry.addFeature(source);
		ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }

}
