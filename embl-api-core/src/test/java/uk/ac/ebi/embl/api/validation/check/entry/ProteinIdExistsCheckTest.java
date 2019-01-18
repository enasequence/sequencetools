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

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProteinIdExistsCheckTest {

	private Entry entry;
	private Feature cdsFeature;
	private FeatureFactory featureFactory;
	private ProteinIdExistsCheck check;
	private EntryFactory entryFactory;
	private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils;

	@Before
	public void setUp() throws SQLException {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryDAOUtils = createMock(EntryDAOUtils.class);
		entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		check = new ProteinIdExistsCheck();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_noEntry() throws ValidationEngineException {
		ValidationResult validationResult = check.check(null);
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_noFeatures() throws ValidationEngineException {
		ValidationResult validationResult = check.check(entryFactory
				.createEntry());
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_noAnalysisIDandAssemblyLevel()
			throws ValidationEngineException {
		entry.addFeature(cdsFeature);
		ValidationResult validationResult = check.check(entryFactory
				.createEntry());
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_noNewproteinID() throws ValidationEngineException,
			SQLException {

		entry.addFeature(cdsFeature);
		property = new EmblEntryValidationPlanProperty();
		property.analysis_id.set("ERZ00001");
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_withNewproteinID() throws ValidationEngineException,
			SQLException {

		cdsFeature
				.addQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "MCI00001");
		entry.addFeature(cdsFeature);
		property = new EmblEntryValidationPlanProperty();
		property.analysis_id.set("ERZ00001");
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.count("ProteinIdExistsCheck_1",
				Severity.ERROR));
	}

	
	@Test
	public void testCheck_nonAssemblyWithproteinID()
			throws ValidationEngineException, SQLException {

		cdsFeature
				.addQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "MCI00001");
		entry.addFeature(cdsFeature);
		property = new EmblEntryValidationPlanProperty();
		property.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(property);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
	}

}