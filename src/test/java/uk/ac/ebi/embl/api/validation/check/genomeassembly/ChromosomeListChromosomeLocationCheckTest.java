/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.*;

public class ChromosomeListChromosomeLocationCheckTest {
  private ChromosomeListChromosomeLocationCheck check;
  Origin origin = null;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
    check = new ChromosomeListChromosomeLocationCheck();
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_inValidChromosomeLocation() throws ValidationEngineException {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setAnalysisId("ERZ00000");
    entry.setObjectName("chrjkhjg");
    entry.setChromosomeName("Mitocondria");
    entry.setChromosomeType("chromosome");
    entry.setChromosomeLocation("fdffghhj");
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("ChromosomeListChromosomeLocationValidCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_validChromosomeLocation() throws ValidationEngineException {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setAnalysisId("ERZ00000");
    entry.setObjectName("chrjkhjg");
    entry.setChromosomeName("Mitocondria");
    entry.setChromosomeType("chromosome");
    entry.setChromosomeLocation("chromatophore");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("ChromosomeListChromosomeLocationValidCheck", Severity.ERROR));
  }
}
