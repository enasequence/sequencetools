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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.EntryFactory;

public class SubmissionTest {

  private Submission submission;

  @Before
  public void setUp() {
    submission = new Submission(null, null, null);
  }

  @Test
  public void testSubmission() {
    assertNull(submission.getDay());
    assertNull(submission.getSubmitterAddress());
    Date date = new Date();
    submission.setDay(date);
    submission.setSubmitterAddress("address");
    assertEquals(date, submission.getDay());
    assertEquals("address", submission.getSubmitterAddress());
  }

  @Test
  public void testHashCode() {
    submission.hashCode();
    new Submission("t", new Date(), "a").hashCode();
  }

  @Test
  public void testEquals() {
    assertEquals(submission, submission);
    assertEquals(submission, new Submission());
    Submission submission2 = new Submission();
    submission.setTitle("title");
    assertNotEquals(submission, submission2);
    submission2.setTitle("title");
    assertEquals(submission, submission2);
    submission.setConsortium("consortium");
    assertNotEquals(submission, submission2);
    submission2.setConsortium("consortium");
    assertEquals(submission, submission2);
    submission.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertNotEquals(submission, submission2);
    submission2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(submission, submission2);
    submission.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertNotEquals(submission, submission2);
    submission2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(submission, submission2);
    submission.setSubmitterAddress("submitterAddress");
    assertNotEquals(submission, submission2);
    submission2.setSubmitterAddress("submitterAddress");
    assertEquals(submission, submission2);
    submission.setDay(new Date());
    assertNotEquals(submission, submission2);
    submission2.setDay(new Date());
    assertEquals(submission, submission2);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", submission);
    assertNotEquals(null, submission);
  }

  @Test
  public void testToString() {
    assertNotNull(submission.toString());
    assertNotNull(new Submission("t", new Date(), "a").toString());
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, submission.compareTo(submission));
    assertEquals(0, submission.compareTo(new Submission()));
    Submission submission2 = new Submission();
    submission.setTitle("title");
    // null < not null
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.setTitle("title");
    assertEquals(0, submission.compareTo(submission2));
    submission.setConsortium("consortium");
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.setConsortium("consortium");
    assertEquals(0, submission.compareTo(submission2));
    submission.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.addAuthor((new ReferenceFactory()).createPerson("surname", "firstname"));
    assertEquals(0, submission.compareTo(submission2));
    submission.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.addXRef((new EntryFactory()).createXRef("database", "accession"));
    assertEquals(0, submission.compareTo(submission2));
    submission.setSubmitterAddress("submitterAddress");
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.setSubmitterAddress("submitterAddress");
    assertEquals(0, submission.compareTo(submission2));
    submission.setDay(new Date());
    assertTrue(submission.compareTo(submission2) > 0);
    submission2.setDay(new Date());
    assertEquals(0, submission.compareTo(submission2));
  }
}
