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
package uk.ac.ebi.embl.api.translation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UnAmbiguousCodonTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testSetCodon() {
    UnAmbiguousCodon codon = new UnAmbiguousCodon();
    assertNull(codon.getCodon());
    codon.setCodon("aaa");
    assertEquals("aaa", codon.getCodon());
  }

  @Test
  public void testSetAminoAcid() {
    UnAmbiguousCodon codon = new UnAmbiguousCodon();
    assertNull(codon.getAminoAcid());
    codon.setAminoAcid('A');
    assertEquals(Character.valueOf('A'), codon.getAminoAcid());
  }

  @Test
  public void testSetCodonException() {
    UnAmbiguousCodon codon = new UnAmbiguousCodon();
    assertFalse(codon.isCodonException());
    codon.setCodonException(true);
    assertTrue(codon.isCodonException());
    codon.setCodonException(false);
    assertFalse(codon.isCodonException());
  }
}
