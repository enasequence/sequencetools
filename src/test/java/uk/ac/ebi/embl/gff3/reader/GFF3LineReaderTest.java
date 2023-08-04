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
package uk.ac.ebi.embl.gff3.reader;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 21-Sep-2010 Time: 13:25:47 To change this template
 * use File | Settings | File Templates.
 */
public class GFF3LineReaderTest extends GFF3ReaderTest {

  public void testRead() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertTrue(validationResult.isValid());
    assertEquals(1, entry.getRecords().size());
    assertEquals("SJC_S000020", entry.getRecords().get(0).getSequenceID());
    assertEquals("exonhunter", entry.getRecords().get(0).getSource());
    assertEquals("david", entry.getRecords().get(0).getType());
    assertEquals(476254, entry.getRecords().get(0).getStart());
    assertEquals(477153, entry.getRecords().get(0).getEnd());
    assertEquals(0, entry.getRecords().get(0).getPhase());
    assertEquals(123.34, entry.getRecords().get(0).getScore());
    assertEquals(1, entry.getRecords().get(0).getStrand());
    assertEquals(3, entry.getRecords().get(0).getAttributes().size());
    assertEquals("Sjp_0006480", entry.getRecords().get(0).getAttributes().get("ID"));
  }

  public void testWrongColumnNumber() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\t476254\t123.34\t+\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.1", Severity.ERROR));
  }

  public void testUnmatchedAttribute() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.8", Severity.ERROR));
  }

  public void testUnmatchedAttribute2() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name=;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.8", Severity.ERROR));
  }

  public void testReadInvalidNumbers() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\tnot a number\tnot a number\tnot a number\t+\tnot a number\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.3", Severity.ERROR));
    assertEquals(1, validationResult.count("GFF.4", Severity.ERROR));
    assertEquals(1, validationResult.count("GFF.5", Severity.ERROR));
    assertEquals(1, validationResult.count("GFF.7", Severity.ERROR));
  }

  public void testReadInvalidStrand() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\tBAD_STRAND\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.6", Severity.ERROR));
  }

  public void testReadCDSNoPhase() throws IOException {
    initLineReader(
        "SJC_S000020\texonhunter\tCDS\t476254\t477153\t123.34\t+\t.\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
    ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("GFF.9", Severity.ERROR));
  }
}
