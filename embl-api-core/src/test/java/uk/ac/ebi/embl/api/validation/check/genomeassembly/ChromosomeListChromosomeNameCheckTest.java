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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

public class ChromosomeListChromosomeNameCheckTest
{
	private ChromosomeListChromosomeNameCheck check;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		check = new ChromosomeListChromosomeNameCheck();
		check.setEmblEntryValidationPlanProperty(new EmblEntryValidationPlanProperty());
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoChromsomeName() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ChromosomeListMissingNameCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidChromosome_name() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("fdfghfghhjhgjkjklkljsdffdghjgkjhksdffgd");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ChromosomeListNameLengthCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidChromosome_name1() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("fdfg;dffgd");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ChromosomeListNameRegexCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_chromosomeNameFix() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("Mitocondria");
		check.check(entry);
		assertEquals("MT",entry.getChromosomeName());
	}
	
}
