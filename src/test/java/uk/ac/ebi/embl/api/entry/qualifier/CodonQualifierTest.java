/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.entry.qualifier;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class CodonQualifierTest {

  @Before
  public void setUp() {}

  @Test
  public void testValidQualifier() throws ValidationException {
    CodonQualifier qual = new CodonQualifier("(seq:\"agt\",aa:Ser)");
    assertEquals(qual.getAminoAcid().getAbbreviation(), "Ser");
    assertEquals(qual.getAminoAcid().getLetter(), new Character('S'));
    assertEquals(qual.getCodon(), "agt");
  }

  @Test(expected = ValidationException.class)
  public void testInvalidQualifier1() throws ValidationException {
    CodonQualifier qual = new CodonQualifier("eq:\"cug\",aa:Ser)");
    qual.getCodon();
  }

  @Test(expected = ValidationException.class)
  public void testInvalidQualifier2() throws ValidationException {
    CodonQualifier qual = new CodonQualifier("eq:\"cug\",aa:Ser)");
    qual.getAminoAcid();
  }
}
