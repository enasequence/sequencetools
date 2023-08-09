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
package uk.ac.ebi.embl.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;

public class AccessionMatcherTest {

  private final Pattern PREFIX_PATTERN = Pattern.compile("^([A-Z]+)\\d+\\w+$");

  private final List<String> validNonWGSAccessions =
      Arrays.asList("AB12345", "AB123456", "AB12345678", "A12345", "A123456");

  private final List<String> inValidNonWGSAccessions =
      Arrays.asList(
          null,
          "",
          "A12345678", // 1 letter 8 digit
          "AB1234567", // 7 digits
          "AB123456789", // >8 digits
          "AB1234" // <5 digits
          );

  private final List<String> validWGSAccessions =
      Arrays.asList(
          "AABBCC01123456789", // NEW FORMAT
          "AABBCC011234567",
          "AABBCC0112345678",
          "AABBCC01123456789",
          "AABBCC01S123456789", // with S
          // OLD FORMAT
          "AABB01123456",
          "AABB011234567",
          "AABB0112345678",
          "AABB01S12345678",
          "AABB01S123456",
          "AABB01S1234567",
          "AABB01S12345678");

  private final List<String> inValidWGSAccessions =
      Arrays.asList(
          null,
          "",
          "AABBCC01123456", // <7 digits
          "AABBCC0112345", // <7 digits
          "AABBCC011234567890", // >9digits
          "AABBCC0112345678901", // >9digits
          "AABB01S123456789", // with S, 4L 2D S 9D
          "AABBCC1S123456789", // with S, 6L 1D S 9D
          "AABB01123456789", // > 8 digit
          "AABB0112345", // < 6 digit
          "AAB01123456", // 3 letters
          "100001123456", // No letters
          "AABC1S123456" // 1 digit version
          );

  private final List<String> validWGSMaster =
      Arrays.asList(
          "AABBCC01000000000", // NEW FORMAT
          "AABBCC0100000000",
          "AABBCC010000000",
          // OLD FORMAT
          "AABB01000000",
          "AABB010000000",
          "AABB0100000000");

  private final List<String> inValidWGSMaster =
      Arrays.asList(
          null,
          "",
          "AABBCC01000000", // <7 digits
          "AABBCC010000000000", // >9digits
          "AABBCC01S000000000", // with S
          "AABBCC01123456789", // with S, 6L 1D S 9D
          "AABB01000001", // non zero
          "AABB0100000", // < 6 digit
          "AAB01000000", // 3 letters
          "1000000000000" // No letters
          );

  @Test
  public void testProteinIds() {
    // OLD: 3L 5D . D+
    // NEW: 3L 7D . D+
    assertEquals("VAA12345", AccessionMatcher.getProteinAccession("VAA12345.01"));
    assertEquals("VAB1234567", AccessionMatcher.getProteinAccession("VAB1234567.123456"));
    assertEquals(
        "VAB0000000",
        AccessionMatcher.getProteinAccession("VAB0000000.0")); // TODO: make it invalid
    assertEquals("VAB1234567", AccessionMatcher.getProteinAccession("VAB1234567.1"));

    assertEquals("01", AccessionMatcher.getProteinVersion("VAA12345.01"));
    assertEquals("123456", AccessionMatcher.getProteinVersion("VAB1234567.123456"));
    assertEquals("0", AccessionMatcher.getProteinVersion("VAB0000000.0"));
    assertEquals("1", AccessionMatcher.getProteinVersion("VAB1234567.1"));

    assertNull(AccessionMatcher.getProteinAccession(""));
    assertNull(AccessionMatcher.getProteinAccession(null));
    assertNull(AccessionMatcher.getProteinAccession("VAH12345")); // no version
    assertNull(AccessionMatcher.getProteinAccession("VAH12345."));
    assertNull(AccessionMatcher.getProteinAccession("VAH12345.a")); // non numeric version
    assertNull(AccessionMatcher.getProteinAccession("VAH1234567."));
    assertNull(AccessionMatcher.getProteinAccession("VAH1234.01")); // <5 digits
    assertNull(AccessionMatcher.getProteinAccession("VA12345.12")); // 2 letters
    assertNull(AccessionMatcher.getProteinAccession("VAB123456.1")); // 6 digits
  }

  @Test // ALL NON WGS
  public void isSeqAccession() {
    // OLD: 1-2L 5-6D
    // NEW: 2L 8D
    validNonWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isSeqAccession(x)));
    inValidNonWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isSeqAccession(x)));
  }

  @Test // ALL WGS
  public void isWgsSeqAccession() {
    // NEW: 6L 2D S? 7-9D
    // OLD: 4L 2D S? 6-8D
    validWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isWgsSeqAccession(x)));
    inValidWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isWgsSeqAccession(x)));
  }

  @Test
  public void isWgsMasterAccession() {
    // NEW: 6L 2D 7-9D
    // OLD: 4L 2D 6-8D
    validWGSMaster.forEach(x -> assertTrue(AccessionMatcher.isMasterAccession(x)));
    inValidWGSMaster.forEach(x -> assertFalse(AccessionMatcher.isMasterAccession(x)));
  }

  @Test
  public void getProteinIdMatcher() {}

  @Test
  public void anySeqMatcher() {

    validNonWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x)));
    validWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x)));
    validWGSMaster.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x)));

    inValidNonWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isPrimaryAcc(x)));
    inValidWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isPrimaryAcc(x)));

    // with version
    validNonWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x + ".12")));
    validWGSAccessions.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x + ".0")));
    validWGSMaster.forEach(x -> assertTrue(AccessionMatcher.isPrimaryAcc(x + ".1234")));

    inValidNonWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isPrimaryAcc(x + ".12")));
    inValidWGSAccessions.forEach(x -> assertFalse(AccessionMatcher.isPrimaryAcc(x + ".01")));
    assertTrue(AccessionMatcher.isPrimaryAcc("AB123456.01"));
    assertTrue(AccessionMatcher.isPrimaryAcc("AB123456.00"));
    assertFalse(AccessionMatcher.isPrimaryAcc("AB123456."));
    assertFalse(AccessionMatcher.isPrimaryAcc("AB123456.ab"));
    assertFalse(AccessionMatcher.isPrimaryAcc("AB123456.1b"));
  }

  @Test
  public void getAccessionPrefix() {
    comparePrefix(validWGSAccessions, Entry.WGS_DATACLASS);
    comparePrefix(validWGSMaster, Entry.SET_DATACLASS);
    comparePrefix(validNonWGSAccessions, Entry.STD_DATACLASS);

    inValidWGSAccessions.forEach(
        x -> assertNull(AccessionMatcher.getAccessionPrefix(x, Entry.WGS_DATACLASS)));
    inValidWGSMaster.forEach(
        x -> assertNull(AccessionMatcher.getAccessionPrefix(x, Entry.SET_DATACLASS)));
    inValidNonWGSAccessions.forEach(
        x -> assertNull(AccessionMatcher.getAccessionPrefix(x, Entry.STD_DATACLASS)));
  }

  @Test
  public void getSplittedAccession() {
    AccessionMatcher.Accession accession = AccessionMatcher.getSplittedAccession(null);
    assertNull(accession);

    accession = AccessionMatcher.getSplittedAccession("");
    assertNull(accession);

    // Invalid
    accession = AccessionMatcher.getSplittedAccession("ABC123");
    assertNull(accession);

    // STD old 1,5
    accession = AccessionMatcher.getSplittedAccession("A12345");
    assertEquals("A", accession.prefix);
    assertEquals("12345", accession.number);
    assertNull(accession.version);
    assertNull(accession.s);

    // STD old 2,6
    accession = AccessionMatcher.getSplittedAccession("AB123456");
    assertEquals("AB", accession.prefix);
    assertEquals("123456", accession.number);
    assertNull(accession.version);
    assertNull(accession.s);

    // STD new 2, 8
    accession = AccessionMatcher.getSplittedAccession("AB12345678");
    assertEquals("AB", accession.prefix);
    assertEquals("12345678", accession.number);
    assertNull(accession.version);
    assertNull(accession.s);

    // WGS old 4 2 6
    accession = AccessionMatcher.getSplittedAccession("ABCD01123456");
    assertEquals("ABCD", accession.prefix);
    assertEquals("123456", accession.number);
    assertEquals("01", accession.version);
    assertEquals("", accession.s);

    // WGS old 4 2 S 6
    accession = AccessionMatcher.getSplittedAccession("ABCD01S123456");
    assertEquals("ABCD", accession.prefix);
    assertEquals("123456", accession.number);
    assertEquals("01", accession.version);
    assertEquals("S", accession.s);

    // WGS new 6 2 S 9
    accession = AccessionMatcher.getSplittedAccession("ABCDEF11S123456789");
    assertEquals("ABCDEF", accession.prefix);
    assertEquals("123456789", accession.number);
    assertEquals("11", accession.version);
    assertEquals("S", accession.s);
  }

  private void comparePrefix(List<String> accns, String dataClass) {
    for (String accn : accns) {
      String prefix = "failMe";
      Matcher m = PREFIX_PATTERN.matcher(accn);
      if (m.matches()) {
        prefix = m.group(1);
      }
      assertEquals(prefix, AccessionMatcher.getAccessionPrefix(accn, dataClass));
    }
  }
}
