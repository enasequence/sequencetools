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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgpComponentAccessionFixTest {

	private Entry entry;
	private AgpComponentAccessionFix check;
	public EntryFactory entryFactory;
	public EntryDAOUtils entryDAOUtils;
	public EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		check = new AgpComponentAccessionFix();
		entryDAOUtils = createMock(EntryDAOUtils.class);
		planProperty=new EmblEntryValidationPlanProperty();
	}

	public void testCheck_Empty() throws ValidationEngineException {
		entry.setSequence(null);
		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoAgpRows() throws ValidationEngineException {
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_withInvalidComponentID() throws ValidationEngineException, SQLException {

		AgpRow validComponentrow1=new AgpRow();
		AgpRow validGaprow1=new AgpRow();
		validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validComponentrow1.setObject_beg(1l);
		validComponentrow1.setObject_end(330l);
		validComponentrow1.setPart_number(1);
		validComponentrow1.setComponent_type_id("W");
		validComponentrow1.setComponent_beg(1l);
		validComponentrow1.setComponent_end(330l);
		validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
		validComponentrow1.setOrientation("+");

		validGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validGaprow1.setObject_beg(331);
		validGaprow1.setObject_end(354l);
		validGaprow1.setPart_number(2);
		validGaprow1.setComponent_type_id("N");
		validGaprow1.setGap_length(24l);
		validGaprow1.setGap_type("scaffold");
		validGaprow1.setLinkageevidence("paired-ends");
		entry.addAgpRow(validComponentrow1);
		entry.addAgpRow(validGaprow1);
		expect(entryDAOUtils.getSequenceInfoBasedOnEntryName("IWGSC_CSS_6DL_contig_209591", "ERZ00001",2)).andReturn(null);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		planProperty.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(planProperty);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_withValidComponentID() throws ValidationEngineException, SQLException {

		AgpRow validComponentrow1=new AgpRow();
		AgpRow validGaprow1=new AgpRow();
		validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validComponentrow1.setObject_beg(1l);
		validComponentrow1.setObject_end(330l);
		validComponentrow1.setPart_number(1);
		validComponentrow1.setComponent_type_id("W");
		validComponentrow1.setComponent_beg(1l);
		validComponentrow1.setComponent_end(330l);
		validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
		validComponentrow1.setOrientation("+");
		validGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validGaprow1.setObject_beg(331);
		validGaprow1.setObject_end(354l);
		validGaprow1.setPart_number(2);
		validGaprow1.setComponent_type_id("N");
		validGaprow1.setGap_length(24l);
		validGaprow1.setGap_type("scaffold");
		validGaprow1.setLinkageevidence("paired-ends");
		entry.addAgpRow(validComponentrow1);
		entry.addAgpRow(validGaprow1);
		ContigSequenceInfo sequenceInfo=new ContigSequenceInfo();
		sequenceInfo.setSequenceLength(400);
		sequenceInfo.setPrimaryAccession("AC00001");
		sequenceInfo.setSequenceVersion(1);
		expect(entryDAOUtils.getSequenceInfoBasedOnEntryName("IWGSC_CSS_6DL_contig_209591", "ERZ00001",2)).andReturn(sequenceInfo);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		planProperty.fileType.set(FileType.AGP);
		planProperty.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(planProperty);
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.isValid());
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
		assertEquals(1, validationResult.count("AgpComponentAccessionFix-1", Severity.FIX));
	}
}
