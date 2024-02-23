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
import uk.ac.ebi.embl.api.entry.location.LocalRange;

public class SimpleReferenceFactoryTest {

  private ReferenceFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = new ReferenceFactory();
  }

  @Test
  public void testCreateReference() {
    Reference result = factory.createReference(null, 2);
    assertEquals(Integer.valueOf(2), result.getReferenceNumber());
    assertNull(result.getPublication());
    assertNotNull(result.getLocations());
    assertTrue(result.getLocations().getLocations().isEmpty());
  }

  @Test
  public void testCreateRange() {
    LocalRange result = factory.createRange(1L, 3L);
    assertEquals(Long.valueOf(1), result.getBeginPosition());
    assertEquals(Long.valueOf(3), result.getEndPosition());
    assertFalse(result.isComplement());
  }

  @Test
  public void testCreateArticle() {
    Article result = factory.createArticle("title", "journal");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());
    assertEquals("journal", result.getJournal());
  }

  @Test
  public void testCreateBook() {
    Book result = factory.createBook("title", "book title", "1", "10", "publisher");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());

    assertNotNull(result.getEditors());
    assertTrue(result.getEditors().isEmpty());

    assertEquals("book title", result.getBookTitle());
    assertEquals("1", result.getFirstPage());
    assertEquals("10", result.getLastPage());
    assertEquals("publisher", result.getPublisher());
  }

  @Test
  public void testCreateThesis() {
    Date date = new Date();
    Thesis result = factory.createThesis("title", date, "inst");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());

    assertEquals("inst", result.getInstitute());
    assertEquals(date, result.getYear());
  }

  @Test
  public void testCreateUnpublished() {
    Unpublished result = factory.createUnpublished("title");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());
  }

  @Test
  public void testCreateElectronicReference() {
    ElectronicReference result = factory.createElectronicReference("title", "text");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());
    assertEquals("text", result.getText());
  }

  @Test
  public void testCreateSubmission() {
    Date date = new Date();
    Submission result = factory.createSubmission("title", date, "address");
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());

    assertEquals(date, result.getDay());
    assertEquals("address", result.getSubmitterAddress());
  }

  @Test
  public void testCreatePatent() {
    Date date = new Date();
    Patent result = factory.createPatent("title", "office", "3", "type", 1, date);
    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertEquals("title", result.getTitle());

    assertNotNull(result.getApplicants());
    assertTrue(result.getApplicants().isEmpty());

    assertEquals(date, result.getDay());
    assertEquals("3", result.getPatentNumber());
    assertEquals("office", result.getPatentOffice());
    assertEquals("type", result.getPatentType());
    assertEquals(Integer.valueOf(1), result.getSequenceNumber());
  }

  @Test
  public void testCreatePerson() {
    Person person1 = factory.createPerson("surname");
    assertEquals("surname", person1.getSurname());
    assertNull(person1.getFirstName());

    Person person2 = factory.createPerson("surname", "i.");
    assertEquals("surname", person2.getSurname());
    assertEquals("i.", person2.getFirstName());
  }
}
