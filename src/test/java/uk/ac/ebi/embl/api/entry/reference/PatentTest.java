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

import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.EntryFactory;

public class PatentTest {

  private Patent patent;

  @Before
  public void setUp() {
    patent = new Patent();
  }

  @Test
  public void testPatent() {
    assertNull(patent.getPatentOffice());
    assertNull(patent.getPatentNumber());
    assertNull(patent.getPatentType());
    assertNull(patent.getSequenceNumber());
    assertNull(patent.getDay());
    assertTrue(patent.getApplicants().isEmpty());
    Date date = new Date();
    patent.setPatentOffice("patentOffice");
    patent.setPatentNumber("patentNumber");
    patent.setPatentType("patentType");
    patent.setSequenceNumber(1);
    patent.setDay(date);
    assertEquals("patentOffice", patent.getPatentOffice());
    assertEquals("patentNumber", patent.getPatentNumber());
    assertEquals("patentType", patent.getPatentType());
    assertEquals(Integer.valueOf(1), patent.getSequenceNumber());
    assertEquals(date, patent.getDay());
    assertTrue(patent.getApplicants().isEmpty());
  }

  @Test
  public void testGetApplicants() {
    assertEquals(0, patent.getApplicants().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetApplicants_UnmodifiableList() {
    patent.getApplicants().add("");
  }

  @Test
  public void testAddApplicant() {
    assertTrue(patent.addApplicant("applicant"));
    assertEquals("applicant", patent.getApplicants().get(0));
  }

  @Test
  public void testAddApplicants() {
    assertTrue(patent.addApplicants(Collections.singletonList("applicant")));
    assertEquals("applicant", patent.getApplicants().get(0));
  }

  @Test
  public void testAddApplicants_Null() {
    assertFalse(patent.addApplicants(null));
    assertTrue(patent.getApplicants().isEmpty());
  }

  @Test
  public void testRemoveApplicant() {
    assertFalse(patent.removeApplicant("applicant"));

    patent.addApplicant("applicant");
    assertEquals(1, patent.getApplicants().size());

    assertFalse(patent.removeApplicant("x"));
    assertEquals(1, patent.getApplicants().size());

    assertTrue(patent.removeApplicant("applicant"));
    assertEquals(0, patent.getApplicants().size());
  }

  @Test
  public void testHashCode() {
    patent.hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(patent, patent);
    assertEquals(patent, new Patent());
    Patent patent2 = new Patent();
    patent.setTitle("title");
    assertNotEquals(patent, patent2);
    patent2.setTitle("title");
    assertEquals(patent, patent2);
    patent.setConsortium("consortium");
    assertNotEquals(patent, patent2);
    patent2.setConsortium("consortium");
    assertEquals(patent, patent2);
    patent.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertNotEquals(patent, patent2);
    patent2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(patent, patent2);
    patent.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertNotEquals(patent, patent2);
    patent2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(patent, patent2);
    Date date = new Date();
    patent.setPatentOffice("patentOffice");
    assertNotEquals(patent, patent2);
    patent2.setPatentOffice("patentOffice");
    assertEquals(patent, patent2);
    patent.setPatentNumber("patentNumber");
    assertNotEquals(patent, patent2);
    patent2.setPatentNumber("patentNumber");
    assertEquals(patent, patent2);
    patent.setPatentType("patentType");
    assertNotEquals(patent, patent2);
    patent2.setPatentType("patentType");
    assertEquals(patent, patent2);
    patent.setSequenceNumber(1);
    assertNotEquals(patent, patent2);
    patent2.setSequenceNumber(1);
    assertEquals(patent, patent2);
    patent.setDay(date);
    assertNotEquals(patent, patent2);
    patent2.setDay(date);
    assertEquals(patent, patent2);
    patent.addApplicant("applicant");
    assertNotEquals(patent, patent2);
    patent2.addApplicant("applicant");
    assertEquals(patent, patent2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", patent);
  }

  @Test
  public void testToString() {
    assertNotNull(patent.toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, patent.compareTo(patent));
    assertEquals(0, patent.compareTo(new Patent()));
    Patent patent2 = new Patent();
    patent.setTitle("title");
    // null < not null
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setTitle("title");
    assertEquals(0, patent.compareTo(patent2));
    patent.setConsortium("consortium");
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setConsortium("consortium");
    assertEquals(0, patent.compareTo(patent2));
    patent.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(0, patent.compareTo(patent2));
    patent.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(0, patent.compareTo(patent2));
    Date date = new Date();
    patent.setPatentOffice("patentOffice");
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setPatentOffice("patentOffice");
    assertEquals(0, patent.compareTo(patent2));
    patent.setPatentNumber("patentNumber");
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setPatentNumber("patentNumber");
    assertEquals(0, patent.compareTo(patent2));
    patent.setPatentType("patentType");
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setPatentType("patentType");
    assertEquals(0, patent.compareTo(patent2));
    patent.setSequenceNumber(1);
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setSequenceNumber(1);
    assertEquals(0, patent.compareTo(patent2));
    patent.setDay(date);
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.setDay(date);
    assertEquals(0, patent.compareTo(patent2));
    patent.addApplicant("applicant");
    assertTrue(patent.compareTo(patent2) > 0);
    patent2.addApplicant("applicant");
    assertEquals(0, patent.compareTo(patent2));
  }
}
