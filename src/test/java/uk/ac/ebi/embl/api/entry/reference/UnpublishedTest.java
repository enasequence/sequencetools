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
package uk.ac.ebi.embl.api.entry.reference;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.EntryFactory;

public class UnpublishedTest {

  private Unpublished unpublished;

  @Before
  public void setUp() throws Exception {
    unpublished = new Unpublished();
  }

  @Test
  public void testHashCode() {
    unpublished.hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(unpublished, unpublished);
    assertEquals(unpublished, new Unpublished());
    Unpublished unpublished2 = new Unpublished();
    unpublished.setTitle("title");
    assertNotEquals(unpublished, unpublished2);
    unpublished2.setTitle("title");
    assertEquals(unpublished, unpublished2);
    unpublished.setConsortium("consortium");
    assertNotEquals(unpublished, unpublished2);
    unpublished2.setConsortium("consortium");
    assertEquals(unpublished, unpublished2);
    unpublished.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertNotEquals(unpublished, unpublished2);
    unpublished2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(unpublished, unpublished2);
    unpublished.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertNotEquals(unpublished, unpublished2);
    unpublished2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(unpublished, unpublished2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", unpublished);
    assertNotEquals(null, unpublished);
  }

  @Test
  public void testToString() {
    assertNotNull(unpublished.toString());
    assertNotNull(new Unpublished("t").toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, unpublished.compareTo(unpublished));
    assertEquals(0, unpublished.compareTo(new Unpublished()));
    Unpublished unpublished2 = new Unpublished();
    unpublished.setTitle("title");
    // null < not null
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.setTitle("title");
    assertEquals(0, unpublished.compareTo(unpublished2));
    unpublished.setConsortium("consortium");
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.setConsortium("consortium");
    assertEquals(0, unpublished.compareTo(unpublished2));
    unpublished.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(0, unpublished.compareTo(unpublished2));
    unpublished.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(0, unpublished.compareTo(unpublished2));
  }
}
