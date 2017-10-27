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
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChromosomeListChromosomeTypeCheckTest
{
	private ChromosomeListChromosomeTypeCheck check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
		check = new ChromosomeListChromosomeTypeCheck();
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_noChromosomeType() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("Mitocondria");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ChromosomeListMissingChromosomeTypeCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_inValidChromosomeType() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("Mitocondria");
		entry.setChromosomeType("fdggfhfhj");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ChromosomeListChromosomeTypeValidCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_validChromosomeType() throws ValidationEngineException
	{
		ChromosomeEntry entry = new ChromosomeEntry();
		entry.setAnalysisId("ERZ00000");
		entry.setObjectName("chrjkhjg");
		entry.setChromosomeName("Mitocondria");
		entry.setChromosomeType("chromosome");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("ChromosomeListChromosomeTypeValidCheck", Severity.ERROR));
	}

}
