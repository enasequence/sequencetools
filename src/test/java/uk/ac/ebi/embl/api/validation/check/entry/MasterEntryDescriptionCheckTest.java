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

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class MasterEntryDescriptionCheckTest {

	private Entry entry;
	private MasterEntryDescriptionCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		check=new MasterEntryDescriptionCheck();
		
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}
	

	@Test
	public void testCheck_NoDescription() {
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_descriptionwithInvalidKeywords() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		entry.setDescription(new Text("Fusarium pseudograminearum genome assembly _12-15-34_1 ,contig A1A_CC1"));
		ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
		assertEquals(1, result.count("MasterEntryDescriptionCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_withValidDescription() throws SQLException {
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		entry.setSubmitterAccession("contig1");
		entry.setDescription(new Text("Fusarium pseudograminearum genome assembly _12-15-34_1"));
		ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
		assertEquals(0, result.count("MasterEntryDescriptionCheck_1", Severity.ERROR));
	}
	
}
