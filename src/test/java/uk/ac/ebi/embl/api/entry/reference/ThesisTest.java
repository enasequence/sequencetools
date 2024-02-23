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
package uk.ac.ebi.embl.api.entry.reference;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.EntryFactory;

public class ThesisTest {

  private Thesis thesis;

  @Before
  public void setUp() throws Exception {
    thesis = new Thesis();
  }

  @Test
  public void testThesis() {
    assertNull(thesis.getInstitute());
    assertNull(thesis.getYear());
    Date date = new Date();
    thesis.setInstitute("institute");
    thesis.setYear(date);
    assertEquals(date, thesis.getYear());
    assertEquals("institute", thesis.getInstitute());
  }

  @Test
  public void testHashCode() {
    thesis.hashCode();
    new Thesis("t", new Date(), "i").hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(thesis, thesis);
    assertEquals(thesis, new Thesis());
    Thesis thesis2 = new Thesis();
    thesis.setTitle("title");
    assertNotEquals(thesis, thesis2);
    thesis2.setTitle("title");
    assertEquals(thesis, thesis2);
    thesis.setConsortium("consortium");
    assertNotEquals(thesis, thesis2);
    thesis2.setConsortium("consortium");
    assertEquals(thesis, thesis2);
    thesis.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertNotEquals(thesis, thesis2);
    thesis2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(thesis, thesis2);
    thesis.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertNotEquals(thesis, thesis2);
    thesis2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(thesis, thesis2);
    thesis.setInstitute("institute");
    assertNotEquals(thesis, thesis2);
    thesis2.setInstitute("institute");
    assertEquals(thesis, thesis2);
    thesis.setYear(new Date());
    assertNotEquals(thesis, thesis2);
    thesis2.setYear(new Date());
    assertEquals(thesis, thesis2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", thesis);
    assertNotEquals(null, thesis);
  }

  @Test
  public void testToString() {
    assertNotNull(thesis.toString());
    assertNotNull(new Thesis("t", new Date(), "i").toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, thesis.compareTo(thesis));
    assertEquals(0, thesis.compareTo(new Thesis()));
    Thesis thesis2 = new Thesis();
    thesis.setTitle("title");
    // null < not null
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.setTitle("title");
    assertEquals(0, thesis.compareTo(thesis2));
    thesis.setConsortium("consortium");
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.setConsortium("consortium");
    assertEquals(0, thesis.compareTo(thesis2));
    thesis.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(0, thesis.compareTo(thesis2));
    thesis.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(0, thesis.compareTo(thesis2));
    thesis.setInstitute("institute");
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.setInstitute("institute");
    assertEquals(0, thesis.compareTo(thesis2));
    thesis.setYear(new Date());
    assertTrue(thesis.compareTo(thesis2) > 0);
    thesis2.setYear(new Date());
    assertEquals(0, thesis.compareTo(thesis2));
  }
}
