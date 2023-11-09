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
package uk.ac.ebi.embl.api.entry;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class SimpleEntryFactoryTest {

  private EntryFactory factory;

  @Before
  public void setUp() {
    factory = new EntryFactory();
  }

  @Test
  public void testCreateEntry() {
    Entry result = factory.createEntry();
    assertNull(result.getDescription().getText());
    assertNotNull(result.getFeatures());
    assertTrue(result.getFeatures().isEmpty());
    assertNotNull(result.getKeywords());
    assertTrue(result.getKeywords().isEmpty());
    assertNotNull(result.getProjectAccessions());
    assertTrue(result.getProjectAccessions().isEmpty());
    assertNotNull(result.getReferences());
    assertTrue(result.getReferences().isEmpty());
    assertNotNull(result.getSecondaryAccessions());
    assertTrue(result.getSecondaryAccessions().isEmpty());
    assertNotNull(result.getXRefs());
    assertTrue(result.getXRefs().isEmpty());
    assertNull(result.getSequence());
    assertNotNull(result.getAssemblies());
    assertTrue(result.getAssemblies().isEmpty());
    result.setSequence(new SequenceFactory().createSequence());
    assertNotNull(result.getSequence().getContigs());
    assertTrue(result.getSequence().getContigs().isEmpty());
  }

  @Test
  public void testCreateAssembly() {
    Assembly result = factory.createAssembly("B001", 2, 1L, 2L, true, 3L, 4L);
    assertNotNull(result);
    assertNotNull(result.getPrimarySpan());
    assertNotNull(result.getSecondarySpan());
    assertEquals("B001", result.getPrimarySpan().getAccession());
    assertEquals(Integer.valueOf(2), result.getPrimarySpan().getVersion());
    assertEquals(Long.valueOf(1), result.getPrimarySpan().getBeginPosition());
    assertEquals(Long.valueOf(2), result.getPrimarySpan().getEndPosition());
    assertEquals(Long.valueOf(3), result.getSecondarySpan().getBeginPosition());
    assertEquals(Long.valueOf(4), result.getSecondarySpan().getEndPosition());
  }

  @Test
  public void testCreateXRef() {
    XRef xRef1 = factory.createXRef("db", "pa");
    assertNotNull(xRef1);
    assertEquals("db", xRef1.getDatabase());
    assertEquals("pa", xRef1.getPrimaryAccession());
    assertNull(xRef1.getSecondaryAccession());

    XRef xRef2 = factory.createXRef("db", "pa", "sa");
    assertNotNull(xRef2);
    assertEquals("db", xRef2.getDatabase());
    assertEquals("pa", xRef2.getPrimaryAccession());
    assertEquals("sa", xRef2.getSecondaryAccession());
  }
}
