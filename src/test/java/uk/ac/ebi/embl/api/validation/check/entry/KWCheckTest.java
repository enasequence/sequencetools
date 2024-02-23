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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class KWCheckTest {

  private KWCheck check;
  private Entry entry;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();

    // FileName.KEYWORD_DATACLASS, FileName.CON_NO_KEYWORDS
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.CON_NO_KEYWORDS, new DataRow("HTG"), new DataRow("FLI_CDNA"));
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.KEYWORD_DATACLASS,
        new DataRow("TSA", "TSA", "TSA"),
        new DataRow("TSA", "TRANSCRIPTOMESHOTGUNASSEMBLY", "Transcriptome Shotgun Assembly"),
        new DataRow("WGS", "WGS", "WGS"));
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.DATACLASS,
        new DataRow("TSA"),
        new DataRow("TLS"),
        new DataRow("STD"),
        new DataRow("CON"),
        new DataRow("PAT"),
        new DataRow("TPX"),
        new DataRow("HTG"),
        new DataRow("WGS"));
    check = new KWCheck();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoKeywords() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDEline() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDataclass() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDataclasswithKeywords() {
    entry.addKeyword(new Text("STD"));
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoKeywordwithDataclass() {
    entry.setDataClass("STD");
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_differentKeywordDataclass() {
    entry.setDataClass("STD");
    entry.addKeyword(new Text("WGS"));
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("KWCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_multipleKeywordDataclass() {
    entry.setDataClass("STD");
    entry.addKeyword(new Text("WGS"));
    entry.addKeyword(new Text("TSA"));
    entry.addKeyword(new Text("MGA"));
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("KWCheck_2", Severity.ERROR));
    assertEquals(1, result.count("KWCheck_5", Severity.ERROR));
  }

  @Test
  public void testCheck_KeywordDataclasswithNoCase() {
    entry.setDataClass("STD");
    entry.addKeyword(new Text("std"));
    entry.addKeyword(new Text("WGS"));
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("KWCheck_1", Severity.ERROR));
    assertEquals(1, result.count("KWCheck_5", Severity.ERROR));
  }

  @Test
  public void testCheck_CONwithNotAllowedKeywords() {
    entry.setDataClass("CON");
    entry.addKeyword(new Text("HTG"));
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("KWCheck_3", Severity.ERROR));
  }

  @Test
  public void testCheck_CONwithNoKeywords() {
    entry.setDataClass("CON");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("KWCheck_3", Severity.ERROR));
  }

  @Test
  public void testCheck_CONwithAllowedKeywords() {
    entry.setDataClass("CON");
    entry.addKeyword(new Text("complete genome"));
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("KWCheck_3", Severity.ERROR));
  }

  @Test
  public void testCheck_withAllowedTPAKeywords() {
    entry.setDataClass("CON");
    entry.addKeyword(new Text("TPA"));
    entry.addKeyword(new Text("Third Party Annotation"));
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withNotAllowedTPAKeywords() {
    entry.setDataClass("MGA");
    entry.addKeyword(new Text("TPA"));
    entry.addKeyword(new Text("Third Party Annotation"));
    assertFalse(check.check(entry).isValid());
  }

  @Test
  public void testCheck_WGSwithoutKeywords() {
    entry.setDataClass("WGS");
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("KWCheck_4", Severity.ERROR));
  }

  @Test
  public void testCheck_WGSwithKeywords() {
    entry.setDataClass("WGS");
    entry.addKeyword(new Text("WGS"));
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_WGSwithoutValidKeywords() {
    entry.setDataClass("WGS");
    entry.addKeyword(new Text("TPX"));
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("KWCheck_4", Severity.ERROR));
  }

  @Test
  public void testCheck_DEinValidKeywords1() {
    entry.setDataClass("TSA");
    entry.setDescription(new Text("TPA: Bos taurus contig xy, transcriptiome shotgun assembly."));
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("KWCheck_6", Severity.ERROR));
  }

  /*@Test
  	public void testCheck_DEinValidKeywords2()
  	{
  		entry.setDataClass("WGS");
  		entry.setDescription(new Text("TPA: Bos taurus contig xy, transcriptiome shotgun assembly."));
  		ValidationResult result = check.check(entry);
  		assertTrue(!result.isValid());
  		assertEquals(1, result.count("KWCheck_6", Severity.ERROR));
  	}
  */
  @Test
  public void testCheck_DEValidKeywords1() {
    entry.setDataClass("TSA");
    entry.addKeyword(new Text("TPA"));
    entry.addKeyword(new Text("Third Party Annotation"));
    entry.setDescription(new Text("TPA: Bos taurus contig xy, transcriptiome shotgun assembly."));
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_DEValidKeywords2() {
    entry.setDataClass("TSA");
    entry.addKeyword(new Text("TPA"));
    entry.addKeyword(new Text("Third Party Data"));
    entry.setDescription(new Text("TPA: Bos taurus contig xy, transcriptiome shotgun assembly."));
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }
}
