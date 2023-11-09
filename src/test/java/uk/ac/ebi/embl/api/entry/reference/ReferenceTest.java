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

public class ReferenceTest {

  private Reference reference;

  @Before
  public void setUp() {
    reference = new Reference(null, null);
  }

  @Test
  public void testReference() {
    assertNull(reference.getPublication());
    assertNull(reference.getReferenceNumber());
    assertNotNull(reference.getLocations());
    reference.setReferenceNumber(1);
    Publication publication = new Publication();
    reference.setPublication(publication);
    assertEquals(Integer.valueOf(1), reference.getReferenceNumber());
    assertEquals(publication, reference.getPublication());
  }

  @Test
  public void testHashCode() {
    reference.hashCode();
    new Reference(new Unpublished("x"), 2).hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(reference, reference);
    assertEquals(reference, new Reference());
    Reference reference2 = new Reference();
    reference.setReferenceNumber(1);
    assertNotEquals(reference, reference2);
    reference2.setReferenceNumber(1);
    assertEquals(reference, reference2);
    reference.setComment("comment");
    assertNotEquals(reference, reference2);
    reference2.setComment("comment");
    assertEquals(reference, reference2);
    Publication publication = new Publication();
    reference.setPublication(publication);
    assertNotEquals(reference, reference2);
    reference2.setPublication(publication);
    assertEquals(reference, reference2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", new Reference(null, null));
  }

  @Test
  public void testToString() {
    assertNotNull(reference.toString());
    assertNotNull(new Reference(new Unpublished("x"), 1).toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, reference.compareTo(reference));
    assertEquals(0, reference.compareTo(new Reference()));
    Reference reference2 = new Reference();
    reference.setReferenceNumber(1);
    assertTrue(reference.compareTo(reference2) > 0);
    reference2.setReferenceNumber(1);
    assertEquals(0, reference.compareTo(reference2));
    reference.setComment("comment");
    assertTrue(reference.compareTo(reference2) > 0);
    reference2.setComment("comment");
    assertEquals(0, reference.compareTo(reference2));
    Publication publication = new Publication();
    reference.setPublication(publication);
    assertTrue(reference.compareTo(reference2) > 0);
    reference2.setPublication(publication);
    assertEquals(0, reference.compareTo(reference2));
  }
}
