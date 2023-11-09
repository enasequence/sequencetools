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

public class ElectronicReferenceTest {

  private ElectronicReference electronicReference;

  @Before
  public void setUp() {
    electronicReference = new ElectronicReference(null, null);
  }

  @Test
  public void testElectronicReference() {
    assertNull(electronicReference.getText());
    electronicReference.setText("text");
    assertEquals("text", electronicReference.getText());
  }

  @Test
  public void testHashCode() {
    electronicReference.hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(electronicReference, electronicReference);
    assertEquals(electronicReference, new ElectronicReference());
    ElectronicReference electronicReference2 = new ElectronicReference();
    electronicReference.setTitle("title");
    assertNotEquals(electronicReference, electronicReference2);
    electronicReference2.setTitle("title");
    assertEquals(electronicReference, electronicReference2);
    electronicReference.setConsortium("consortium");
    assertNotEquals(electronicReference, electronicReference2);
    electronicReference2.setConsortium("consortium");
    assertEquals(electronicReference, electronicReference2);
    electronicReference.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertNotEquals(electronicReference, electronicReference2);
    electronicReference2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(electronicReference, electronicReference2);
    electronicReference.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertNotEquals(electronicReference, electronicReference2);
    electronicReference2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(electronicReference, electronicReference2);
    electronicReference.setText("text");
    assertNotEquals(electronicReference, electronicReference2);
    electronicReference2.setText("text");
    assertEquals(electronicReference, electronicReference2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", electronicReference);
  }

  @Test
  public void testToString() {
    assertNotNull(electronicReference.toString());
    assertNotNull(new ElectronicReference("t", "txt").toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, electronicReference.compareTo(electronicReference));
    assertEquals(0, electronicReference.compareTo(new ElectronicReference()));
    ElectronicReference electronicReference2 = new ElectronicReference();
    electronicReference.setTitle("title");
    // null < not null
    assertTrue(electronicReference.compareTo(electronicReference2) > 0);
    electronicReference2.setTitle("title");
    assertEquals(0, electronicReference.compareTo(electronicReference2));
    electronicReference.setConsortium("consortium");
    assertTrue(electronicReference.compareTo(electronicReference2) > 0);
    electronicReference2.setConsortium("consortium");
    assertEquals(0, electronicReference.compareTo(electronicReference2));
    electronicReference.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(electronicReference.compareTo(electronicReference2) > 0);
    electronicReference2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(0, electronicReference.compareTo(electronicReference2));
    electronicReference.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(electronicReference.compareTo(electronicReference2) > 0);
    electronicReference2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(0, electronicReference.compareTo(electronicReference2));
    electronicReference.setText("text");
    assertTrue(electronicReference.compareTo(electronicReference2) > 0);
    electronicReference2.setText("text");
    assertEquals(0, electronicReference.compareTo(electronicReference2));
  }
}
