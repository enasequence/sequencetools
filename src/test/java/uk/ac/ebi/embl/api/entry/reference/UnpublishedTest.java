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
    assertTrue(unpublished.equals(unpublished));
    assertTrue(unpublished.equals(new Unpublished()));
    Unpublished unpublished2 = new Unpublished();
    unpublished.setTitle("title");
    assertFalse(unpublished.equals(unpublished2));
    unpublished2.setTitle("title");
    assertTrue(unpublished.equals(unpublished2));
    unpublished.setConsortium("consortium");
    assertFalse(unpublished.equals(unpublished2));
    unpublished2.setConsortium("consortium");
    assertTrue(unpublished.equals(unpublished2));
    unpublished.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertFalse(unpublished.equals(unpublished2));
    unpublished2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(unpublished.equals(unpublished2));
    unpublished.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertFalse(unpublished.equals(unpublished2));
    unpublished2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(unpublished.equals(unpublished2));
  }

  @Test
  public void testEquals_WrongObject() {
    assertFalse(unpublished.equals(new String()));
    assertFalse(unpublished.equals(null));
  }

  @Test
  public void testToString() {
    assertNotNull(unpublished.toString());
    assertNotNull(new Unpublished("t").toString());
  }

  @Test
  public void testCompareTo() {
    assertTrue(unpublished.compareTo(unpublished) == 0);
    assertTrue(unpublished.compareTo(new Unpublished()) == 0);
    Unpublished unpublished2 = new Unpublished();
    unpublished.setTitle("title");
    // null < not null
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.setTitle("title");
    assertTrue(unpublished.compareTo(unpublished2) == 0);
    unpublished.setConsortium("consortium");
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.setConsortium("consortium");
    assertTrue(unpublished.compareTo(unpublished2) == 0);
    unpublished.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(unpublished.compareTo(unpublished2) == 0);
    unpublished.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(unpublished.compareTo(unpublished2) > 0);
    unpublished2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(unpublished.compareTo(unpublished2) == 0);
  }
}
