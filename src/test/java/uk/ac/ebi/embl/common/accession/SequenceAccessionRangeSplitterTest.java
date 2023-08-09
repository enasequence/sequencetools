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
package uk.ac.ebi.embl.common.accession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class SequenceAccessionRangeSplitterTest {

  @Test
  public void getAccessionRangesNullAccession() {
    List<AccessionRange> ranges = SequenceAccessionRangeSplitter.getAccessionRanges(null);
    assertEquals(0, ranges.size());
  }

  @Test
  public void getAccessionRangesEmptyAccession() {
    List<AccessionRange> ranges = SequenceAccessionRangeSplitter.getAccessionRanges("");
    assertEquals(0, ranges.size());
  }

  @Test
  public void getAccessionRangesSingleAccession() {
    List<AccessionRange> ranges = SequenceAccessionRangeSplitter.getAccessionRanges("A00001");
    assertEquals(1, ranges.size());
    assertEquals(new AccessionRange("A00001", "A00001"), ranges.get(0));
  }

  @Test
  public void getAccessionRangesSingleRange() {
    List<AccessionRange> ranges =
        SequenceAccessionRangeSplitter.getAccessionRanges("A00001-A00002");
    assertEquals(new AccessionRange("A00001", "A00002"), ranges.get(0));
  }

  @Test
  public void getAccessionRangesTwoRanges() {
    List<AccessionRange> ranges =
        SequenceAccessionRangeSplitter.getAccessionRanges("A00001-A00002,A00003-A00004");
    assertTrue(ranges.contains(new AccessionRange("A00001", "A00002")));
    assertTrue(ranges.contains(new AccessionRange("A00003", "A00004")));
  }

  @Test
  public void getAccessionRangesSingleAndRange() {
    List<AccessionRange> ranges =
        SequenceAccessionRangeSplitter.getAccessionRanges("A00001,A00003-A00004");
    assertTrue(ranges.contains(new AccessionRange("A00001", "A00001")));
    assertTrue(ranges.contains(new AccessionRange("A00003", "A00004")));
  }

  @Test
  public void getAccessionRangesRangeAndSingle() {
    List<AccessionRange> ranges =
        SequenceAccessionRangeSplitter.getAccessionRanges("A00001-A00002,A00003");
    assertTrue(ranges.contains(new AccessionRange("A00001", "A00002")));
    assertTrue(ranges.contains(new AccessionRange("A00003", "A00003")));
  }

  @Test
  public void getAccessionRangesThreeRanges() {
    List<AccessionRange> ranges =
        SequenceAccessionRangeSplitter.getAccessionRanges(
            "A00001-A00002,A00003-A00004,A00005-A00006");
    assertTrue(ranges.contains(new AccessionRange("A00001", "A00002")));
    assertTrue(ranges.contains(new AccessionRange("A00003", "A00004")));
    assertTrue(ranges.contains(new AccessionRange("A00005", "A00006")));
  }

  @Test
  public void getAccessionsNullAccession() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions(null);
    assertEquals(0, accessions.size());
  }

  @Test
  public void getAccessionsEmptyAccession() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions("");
    assertEquals(0, accessions.size());
  }

  @Test
  public void getAccessionsSingleAccession() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions("A00001");
    assertTrue(accessions.contains("A00001"));
  }

  @Test
  public void getAccessionsSingleRange() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions("A00001-A00002");
    assertTrue(accessions.contains("A00001"));
    assertTrue(accessions.contains("A00002"));
  }

  @Test
  public void getAccessionsTwoRanges() {
    List<String> accessions =
        SequenceAccessionRangeSplitter.getAccessions("A00001-A00002,A00003-A00004");
    assertTrue(accessions.contains("A00001"));
    assertTrue(accessions.contains("A00002"));
    assertTrue(accessions.contains("A00003"));
    assertTrue(accessions.contains("A00004"));
  }

  @Test
  public void getAccessionsSingleAndRange() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions("A00001,A00003-A00004");
    assertTrue(accessions.contains("A00001"));
    assertTrue(accessions.contains("A00003"));
    assertTrue(accessions.contains("A00004"));
  }

  @Test
  public void getAccessionsRangeAndSingle() {
    List<String> accessions = SequenceAccessionRangeSplitter.getAccessions("A00001-A00002,A00003");
    assertTrue(accessions.contains("A00001"));
    assertTrue(accessions.contains("A00002"));
    assertTrue(accessions.contains("A00003"));
  }

  @Test
  public void getAccessionsThreeRanges() {
    List<String> accessions =
        SequenceAccessionRangeSplitter.getAccessions("A00001-A00002,A00003-A00004,A00005-A00006");
    assertTrue(accessions.contains("A00001"));
    assertTrue(accessions.contains("A00002"));
    assertTrue(accessions.contains("A00003"));
    assertTrue(accessions.contains("A00004"));
    assertTrue(accessions.contains("A00005"));
    assertTrue(accessions.contains("A00006"));
  }

  @Test
  public void getAccessionsWgs() {
    List<String> accessions =
        SequenceAccessionRangeSplitter.getAccessions("CAQO01000001-CAQO01000100");
    assertEquals(100, accessions.size());
    assertTrue(accessions.contains("CAQO01000001"));
    assertTrue(accessions.contains("CAQO01000010"));
    assertTrue(accessions.contains("CAQO01000100"));
  }

  @Test
  public void countAccessionsNullAccession() {
    int cnt = SequenceAccessionRangeSplitter.count(null);
    assertEquals(0, cnt);
  }

  @Test
  public void countAccessionsEmptyAccession() {
    int cnt = SequenceAccessionRangeSplitter.count("");
    assertEquals(0, cnt);
  }

  @Test
  public void countAccessionsSingleAccession() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001");
    assertEquals(1, cnt);
  }

  @Test
  public void countAccessionsSingleSameRange() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001-A00001");
    assertEquals(1, cnt);
  }

  @Test
  public void countAccessionsSingleRange() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001-A00002");
    assertEquals(2, cnt);
  }

  @Test
  public void countAccessionsTwoRanges() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001-A00002,A00003-A00004");
    assertEquals(4, cnt);
  }

  @Test
  public void countAccessionsSingleAndRange() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001,A00003-A00004");
    assertEquals(3, cnt);
  }

  @Test
  public void countAccessionsRangeAndSingle() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001-A00002,A00003");
    assertEquals(3, cnt);
  }

  @Test
  public void countAccessionsThreeRanges() {
    int cnt = SequenceAccessionRangeSplitter.count("A00001-A00002,A00003-A00004,A00005-A00006");
    assertEquals(6, cnt);
  }
}
