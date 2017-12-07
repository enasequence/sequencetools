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
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoltypeExistsCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private SequenceFactory sequenceFactory;
	private MoltypeExistsCheck check;
	private EntryFactory entryFactory;
	private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils;

	@Before
	public void setUp() throws SQLException {
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		sequenceFactory= new SequenceFactory();
		entry = entryFactory.createEntry();
		check = new MoltypeExistsCheck();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_noEntry() throws ValidationEngineException 
	{
		ValidationResult validationResult = check.check(null);
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_noFeatures() throws ValidationEngineException 
	{
		ValidationResult validationResult = check.check(entryFactory.createEntry());
		assertTrue(validationResult.isValid());
	}
	
	@Test
	public void testCheck_noSequence() throws ValidationEngineException 
	{
		ValidationResult validationResult = check.check(entryFactory.createEntry());
		assertTrue(validationResult.isValid());
	}

	@Test
	public void testCheck_noMoltype() throws ValidationEngineException 
	{
		entry.setSequence(sequenceFactory.createSequence());
		SourceFeature source=featureFactory.createSourceFeature();
		entry.addFeature(source);
		ValidationResult validationResult = check.check(entry);
		assertEquals(1, validationResult.count("MoltypeExistsCheck",Severity.ERROR));
	}
	
	@Test
	public void testCheck_withSourceMoltype() throws ValidationEngineException 
	{
		entry.setSequence(sequenceFactory.createSequence());
		SourceFeature source=featureFactory.createSourceFeature();
		source.addQualifier(Qualifier.MOL_TYPE_QUALIFIER_NAME,"genomeDNA");
		entry.addFeature(source);
		ValidationResult validationResult = check.check(entry);
		assertEquals(0, validationResult.count("MoltypeExistsCheck",Severity.ERROR));
	}
	
	@Test
	public void testCheck_withSequenceMoltype() throws ValidationEngineException 
	{
		Sequence sequence=sequenceFactory.createSequence();
		sequence.setMoleculeType("genomeDNA");
		entry.setSequence(sequence);
		SourceFeature source=featureFactory.createSourceFeature();
		entry.addFeature(source);
		ValidationResult validationResult = check.check(entry);
		assertEquals(0, validationResult.count("MoltypeExistsCheck",Severity.ERROR));
	}


}