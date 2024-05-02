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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;

public class IndividualLocationMatcherTest extends TestCase {

  FeatureLocationParser locationParser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    locationParser = new FeatureLocationParser(null, false);
  }

  @Test
  public void testLocationMatcher_LocalBase() throws IOException {

    LocalBase location = (LocalBase) locationParser.getLocation("467");
    assertEquals(467, (long) location.getBeginPosition());
    assertEquals(467, (long) location.getEndPosition());
  }

  public void testLocationMatcher_RemoteBase() {
    RemoteBase location = (RemoteBase) locationParser.getLocation("J00194.1:467");
    assertEquals(467, (long) location.getBeginPosition());
    assertEquals(467, (long) location.getEndPosition());
    assertEquals("J00194", location.getAccession());
    assertEquals(1, (int) location.getVersion());
  }

  public void testLocationMatcher_LocalRange() {
    LocalRange location = (LocalRange) locationParser.getLocation("340..565");
    assertFalse(location.isFivePrimePartial());
    assertFalse(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
  }

  public void testLocationMatcher_RemoteRange() {
    RemoteRange location = (RemoteRange) locationParser.getLocation("J00194.1:340..565");
    assertFalse(location.isFivePrimePartial());
    assertFalse(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
    assertEquals("J00194", location.getAccession());
    assertEquals(1, (int) location.getVersion());
  }

  public void testLocationMatcher_LeftPartialLocalRange() {
    LocalRange location = (LocalRange) locationParser.getLocation("<340..565");
    assertTrue(location.isFivePrimePartial());
    assertFalse(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
  }

  public void testLocationMatcher_LeftPartialRemoteRange() {
    RemoteRange location = (RemoteRange) locationParser.getLocation("J00194.1:<340..565");
    assertTrue(location.isFivePrimePartial());
    assertFalse(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
    assertEquals("J00194", location.getAccession());
    assertEquals(1, (int) location.getVersion());
  }

  public void testLocationMatcher_RightPartialLocalRange() {
    LocalRange location = (LocalRange) locationParser.getLocation("340..>565");
    assertFalse(location.isFivePrimePartial());
    assertTrue(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
  }

  public void testLocationMatcher_RightPartialRemoteRange() {
    RemoteRange location = (RemoteRange) locationParser.getLocation("J00194.1:340..>565");
    assertFalse(location.isFivePrimePartial());
    assertTrue(location.isThreePrimePartial());
    assertEquals(340, (long) location.getBeginPosition());
    assertEquals(565, (long) location.getEndPosition());
    assertEquals("J00194", location.getAccession());
    assertEquals(1, (int) location.getVersion());
  }

  /*
  This test shows the representation difference of <50 and <50..50 in the code:
  <50     -> read as left partial local base (despite the fact the partiality is not used in junction with base location)
  <50..50 -> read as left partial local range
  In both cases, the begin and end positions are the same as expected.
   */
  public void nottestLocationMatcher_LeftAndRightPartialSyntax() {
    // local cases
    LocalBase location1 = (LocalBase) locationParser.getLocation("<50"); // NOTE read as local base
    assertTrue(location1.isFivePrimePartial());
    assertEquals(50, (long) location1.getBeginPosition());
    assertEquals(50, (long) location1.getEndPosition());

    LocalRange location2 =
        (LocalRange) locationParser.getLocation("<50..50"); // NOTE read as remote base
    assertTrue(location2.isFivePrimePartial());
    assertEquals(50, (long) location2.getBeginPosition());
    assertEquals(50, (long) location2.getEndPosition());

    LocalBase location3 = (LocalBase) locationParser.getLocation(">50");
    assertTrue(location3.isThreePrimePartial());
    assertEquals(50, (long) location3.getBeginPosition());
    assertEquals(50, (long) location3.getEndPosition());

    LocalRange location4 = (LocalRange) locationParser.getLocation("50..>50");
    assertTrue(location4.isThreePrimePartial());
    assertEquals(50, (long) location4.getBeginPosition());
    assertEquals(50, (long) location4.getEndPosition());

    // remote cases
    RemoteBase location5 = (RemoteBase) locationParser.getLocation("J00194.1:>340");
    assertFalse(location5.isFivePrimePartial());
    assertTrue(location5.isThreePrimePartial());
    assertEquals(340, (long) location5.getBeginPosition());
    assertEquals(340, (long) location5.getEndPosition());
    assertEquals("J00194", location5.getAccession());
    assertEquals(1, (int) location5.getVersion());

    RemoteRange location6 = (RemoteRange) locationParser.getLocation("J00194.1:340..>340");
    assertFalse(location6.isFivePrimePartial());
    assertTrue(location6.isThreePrimePartial());
    assertEquals(340, (long) location6.getBeginPosition());
    assertEquals(340, (long) location6.getEndPosition());
    assertEquals("J00194", location6.getAccession());
    assertEquals(1, (int) location6.getVersion());
  }
}
