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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class RLReaderTest extends EmblReaderTest {

  public void testRead_Book() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   SPECIFIC EUKARYOTIC GENES:117-132;\n"
            + "RL   Munksgaard, Copenhagen (1979).\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Book);
    Book book = (Book) reference.getPublication();
    assertEquals(3, book.getEditors().size());
    assertEquals("Engberg", book.getEditors().get(0).getSurname());
    assertEquals("Klenow", book.getEditors().get(1).getSurname());
    assertEquals("Leick", book.getEditors().get(2).getSurname());
    assertEquals("J.", book.getEditors().get(0).getFirstName());
    assertEquals("H.", book.getEditors().get(1).getFirstName());
    assertEquals("V.", book.getEditors().get(2).getFirstName());
    assertEquals("SPECIFIC EUKARYOTIC GENES", book.getBookTitle());
    assertEquals("117", book.getFirstPage());
    assertEquals("132", book.getLastPage());
    assertEquals("Munksgaard, Copenhagen", book.getPublisher());
    assertEquals(FlatFileDateUtils.getYear("1979"), book.getYear());
  }

  public void testRead_BookNoBookTitle() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   :117-132;\n"
            + "RL   Munksgaard, Copenhagen (1979).\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.16", Severity.ERROR));
  }

  public void testRead_BookNoFirstPage() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   SPECIFIC EUKARYOTIC GENES:-132;\n"
            + "RL   Munksgaard, Copenhagen (1979).\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.17", Severity.ERROR));
  }

  public void testRead_BookNoLastPage() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   SPECIFIC EUKARYOTIC GENES:117-;\n"
            + "RL   Munksgaard, Copenhagen (1979).\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.18", Severity.ERROR));
  }

  public void testRead_BookNoPublisher() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   SPECIFIC EUKARYOTIC GENES:117-132;\n"
            + "RL   (1979).\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.19", Severity.ERROR));
  }

  public void testRead_BookNoYear() throws IOException {
    initLineReader(
        "RL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n"
            + "RL   SPECIFIC EUKARYOTIC GENES:117-132;\n"
            + "RL   Munksgaard, Copenhagen ().\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.20", Severity.ERROR));
  }

  public void testRead_Article() throws IOException {
    initLineReader("RL   J.  Biol. Chem. 273_II( 48_V ):\n" + "RL   322X81 -32Y287(1998).\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("J. Biol. Chem.", article.getJournal());
    assertEquals("273_II", article.getVolume());
    assertEquals("48_V", article.getIssue());
    assertEquals("322X81", article.getFirstPage());
    assertEquals("32Y287", article.getLastPage());
    assertEquals(FlatFileDateUtils.getYear("1998"), article.getYear());
  }

  public void testReadArticle_NoIssue() throws IOException {
    initLineReader("RL   J.  Biol. Chem. 273_II:\n" + "RL   322X81 -32Y287(1998).\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("J. Biol. Chem.", article.getJournal());
    assertEquals("273_II", article.getVolume());
    assertNull(article.getIssue());
    assertEquals("322X81", article.getFirstPage());
    assertEquals("32Y287", article.getLastPage());
    assertEquals(FlatFileDateUtils.getYear("1998"), article.getYear());
  }

  public void testReadArticle_NoVolume() throws IOException {
    initLineReader("RL   Journal ( 48_V ):\n" + "RL   322X81 -32Y287(1998).\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("Journal", article.getJournal());
    assertNull(article.getVolume());
    assertEquals("48_V", article.getIssue());
    assertEquals("322X81", article.getFirstPage());
    assertEquals("32Y287", article.getLastPage());
    assertEquals(FlatFileDateUtils.getYear("1998"), article.getYear());
  }

  public void testReadArticle_NoPage() throws IOException {
    initLineReader("RL   J.  Biol. Chem. 273_II( 48_V ):\n" + "RL   (1998).\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("J. Biol. Chem.", article.getJournal());
    assertEquals("273_II", article.getVolume());
    assertEquals("48_V", article.getIssue());
    assertNull(article.getFirstPage());
    assertNull(article.getLastPage());
    assertEquals(FlatFileDateUtils.getYear("1998"), article.getYear());
  }

  public void testReadArticle_NoYear() throws IOException {
    initLineReader("RL   J.  Biol. Chem. 273_II( 48_V ):\n" + "RL   322X81 -32Y287.\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("J. Biol. Chem.", article.getJournal());
    assertEquals("273_II", article.getVolume());
    assertEquals("48_V", article.getIssue());
    assertEquals("322X81", article.getFirstPage());
    assertEquals("32Y287", article.getLastPage());
    assertNull(article.getYear());
  }

  public void testReadArticle_ElectronicPublicationPage() throws IOException {
    initLineReader("RL   Mol. Syst. Biol. 2:2006.0007-2006.0007(2006).");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Article);
    Article article = (Article) reference.getPublication();
    assertEquals("Mol. Syst. Biol.", article.getJournal());
    assertEquals("2", article.getVolume());
    assertEquals("2006.0007", article.getFirstPage());
    assertEquals("2006.0007", article.getLastPage());
    assertEquals(FlatFileDateUtils.getYear("2006"), article.getYear());
  }

  public void testRead_Patent() throws IOException {
    initLineReader(
        "RL   Patent number EPO238993-A/3, 10-SEP-1998.\n"
            + "RL   BAYER   AG  ;\n"
            + "RL   GOOGLE AG;\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Patent);
    Patent patent = (Patent) reference.getPublication();
    assertEquals("EP", patent.getPatentOffice());
    assertEquals("O238993", patent.getPatentNumber());
    assertEquals("A", patent.getPatentType());
    assertEquals(Integer.valueOf(3), patent.getSequenceNumber());
    assertEquals(FlatFileDateUtils.getDay("10-SEP-1998"), patent.getDay());
    assertTrue(patent.getApplicants().size() == 2);
    assertEquals("BAYER AG", patent.getApplicants().get(0));
    assertEquals("GOOGLE AG", patent.getApplicants().get(1));
  }

  public void testRead_PatentNoPatentOffice() throws IOException {
    initLineReader(
        "RL   Patent number O238993-A/3, 10-SEP-1998.\n"
            + "RL   BAYER   AG  ;\n"
            + "RL   GOOGLE AG;\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.5", Severity.ERROR));
  }

  public void testRead_PatentNoPatentNumber() throws IOException {
    initLineReader(
        "RL   Patent number EP-A/3, 10-SEP-1998.\n" + "RL   BAYER   AG  ;\n" + "RL   GOOGLE AG;\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.6", Severity.ERROR));
  }

  public void testRead_PatentNoPatentType() throws IOException {
    initLineReader(
        "RL   Patent number EPO238993-/3, 10-SEP-1998.\n"
            + "RL   BAYER   AG  ;\n"
            + "RL   GOOGLE AG;\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.7", Severity.ERROR));
  }

  public void testRead_PatentNoSequenceNumber() throws IOException {
    initLineReader(
        "RL   Patent number EPO238993-A/, 10-SEP-1998.\n"
            + "RL   BAYER   AG  ;\n"
            + "RL   GOOGLE AG;\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.8", Severity.ERROR));
  }

  public void testRead_PatentNoDay() throws IOException {
    initLineReader(
        "RL   Patent number EPO238993-A/3, .\n" + "RL   BAYER   AG  ;\n" + "RL   GOOGLE AG;\n");
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("RL.9", Severity.ERROR));
  }

  public void testRead_Thesis() throws IOException {
    initLineReader("RL   Thesis (1998), Universitaet Muenchen\n" + "RL   near  Berlin");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Thesis);
    Thesis thesis = (Thesis) reference.getPublication();
    assertEquals(FlatFileDateUtils.getYear("1998"), thesis.getYear());
    assertEquals("Universitaet Muenchen near Berlin", thesis.getInstitute());
  }

  public void testRead_ThesisNoYear() throws IOException {
    initLineReader("RL   Thesis (), Universitaet Muenchen\n" + "RL   near  Berlin");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Thesis);
    Thesis thesis = (Thesis) reference.getPublication();
    assertNull(thesis.getYear());
    assertEquals("Universitaet Muenchen near Berlin", thesis.getInstitute());
  }

  public void testRead_ThesisNoInstitute() throws IOException {
    initLineReader("RL   Thesis (1998)");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Thesis);
    Thesis thesis = (Thesis) reference.getPublication();
    assertEquals(FlatFileDateUtils.getYear("1998"), thesis.getYear());
    assertNull(thesis.getInstitute());
  }

  public void testRead_SubmissionWithPublication() throws IOException {
    initLineReader(
        "RL   Submitted (10-SEP-1998) to the EMBL/GenBank/DDBJ databases.\n"
            + "RL   Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n"
            + "RL   Berkeley, CA 94720, USA\n");
    Reference reference = lineReader.getCache().getReference();
    Publication publication = lineReader.getCache().getPublication();
    publication.setTitle("title");
    publication.setConsortium("consortium");
    publication.addAuthor((new ReferenceFactory()).createPerson("surname", "firstName"));
    publication.addXRef((new EntryFactory()).createXRef("UniProtKB", "A00001"));
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Submission);
    Submission submission = (Submission) reference.getPublication();
    assertEquals(FlatFileDateUtils.getDay("10-SEP-1998"), submission.getDay());
    assertEquals(
        "Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",
        submission.getSubmitterAddress());
    assertTrue(publication.equals(reference.getPublication()));
  }

  public void testRead_SubmissionWithoutPublication() throws IOException {
    initLineReader(
        "RL   Submitted (10-SEP-1998) to the EMBL/GenBank/DDBJ databases.\n"
            + "RL   Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n"
            + "RL   Berkeley, CA 94720, USA\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Submission);
    Submission submission = (Submission) reference.getPublication();
    assertEquals(FlatFileDateUtils.getDay("10-SEP-1998"), submission.getDay());
    assertEquals(
        "Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",
        submission.getSubmitterAddress());
  }

  public void testRead_SubmissionNoDate() throws IOException {
    initLineReader(
        "RL   Submitted () to the EMBL/GenBank/DDBJ databases.\n"
            + "RL   Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n"
            + "RL   Berkeley, CA 94720, USA\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Submission);
    Submission submission = (Submission) reference.getPublication();
    assertNull(submission.getDay());
    assertEquals(
        "Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",
        submission.getSubmitterAddress());
  }

  public void testRead_SubmissionNoAddress() throws IOException {
    initLineReader("RL   Submitted (10-SEP-1998) to the EMBL/GenBank/DDBJ databases.\n");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Submission);
    Submission submission = (Submission) reference.getPublication();
    assertEquals(FlatFileDateUtils.getDay("10-SEP-1998"), submission.getDay());
    assertNull(submission.getSubmitterAddress());
  }

  public void testRead_UnpublishedWithPublication() throws IOException {
    initLineReader("RL   Unpublished.");

    Reference reference = lineReader.getCache().getReference();
    Publication publication = lineReader.getCache().getPublication();
    ReferenceFactory referenceFactory = new ReferenceFactory();
    EntryFactory entryFactory = new EntryFactory();
    publication.setTitle("title");
    publication.setConsortium("consortium");
    publication.addAuthor(referenceFactory.createPerson("surname", "firstName"));
    publication.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));
    reference.setPublication(publication);
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Unpublished);
    assertTrue(publication.equals(reference.getPublication()));
  }

  public void testRead_UnpublishedWithoutPublication() throws IOException {
    initLineReader("RL   Unpublished.");

    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof Unpublished);
  }

  public void testRead_ElectronicReferenceWithPublication() throws IOException {
    initLineReader("RL   (er) dah dah");
    Reference reference = lineReader.getCache().getReference();
    Publication publication = lineReader.getCache().getPublication();
    ReferenceFactory referenceFactory = new ReferenceFactory();
    EntryFactory entryFactory = new EntryFactory();
    publication.setTitle("title");
    publication.setConsortium("consortium");
    publication.addAuthor(referenceFactory.createPerson("surname", "firstName"));
    publication.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));
    reference.setPublication(publication);
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof ElectronicReference);
    assertTrue(publication.equals(reference.getPublication()));
    assertEquals("dah dah", ((ElectronicReference) reference.getPublication()).getText());
  }

  public void testRead_ElectronicReferenceWithoutPublication() throws IOException {
    initLineReader("RL   (er) dah dah");
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(reference.getPublication());
    assertTrue(reference.getPublication() instanceof ElectronicReference);
    assertEquals("dah dah", ((ElectronicReference) reference.getPublication()).getText());
  }

  public void testRead_FormatError() throws IOException {
    initLineReader("RL   Blah blah");

    ValidationResult result = (new RLReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
  }
}
