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
package uk.ac.ebi.embl.api.entry.location;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GapTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testGap() {
    new Gap(0l, false);
  }

  @Test
  public void testGapLong() {
    new Gap(10l, false);
  }

  @Test
  public void testGetLength() {
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, new Gap(0l, true).getLength());
    assertEquals(10, new Gap(10l, false).getLength());
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, new Gap(-10l, true).getLength());
  }

  @Test
  public void testGetSequence() {
    String _100N =
        "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"
            + "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN";
    assertEquals(_100N, new Gap(0l, true).getSequence());
    assertEquals(_100N, new Gap(-10l, true).getSequence());
    assertEquals("", new Gap(0, false).getSequence());
    assertEquals("NNNNN", new Gap(5l, false).getSequence());
    assertEquals(_100N, new Gap(100l, false).getSequence());
  }

  @Test
  public void testIsUnknownLength() {
    assertTrue(new Gap(0l, true).isUnknownLength());
    assertTrue(new Gap(-1l, true).isUnknownLength());
    assertTrue(new Gap(10l, true).isUnknownLength());

    assertFalse(new Gap(5l, false).isUnknownLength());
    assertFalse(new Gap(100l, false).isUnknownLength());
  }

  @Test
  public void testSetUnknownLength() {
    Gap gap = new Gap(0l, true);
    assertTrue(gap.isUnknownLength());
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, gap.getLength());

    gap = new Gap(10l, false);
    assertFalse(gap.isUnknownLength());
    assertEquals(10, gap.getLength());
  }

  @Test
  public void testHashCode() {
    new Gap(0l, true).hashCode();
    new Gap(10l, false).hashCode();
  }

  @Test
  public void testEquals() {
    Gap gap = new Gap(0l, true);
    assertTrue(gap.equals(gap));
    assertTrue(gap.equals(new Gap(0l, true)));
    assertTrue(gap.equals(new Gap(-1l, true)));
    assertTrue(new Gap(10l, false).equals(new Gap(10l, false)));

    assertFalse(gap.equals(new Gap(10l, false)));
    assertFalse(new Gap(1l, false).equals(new Gap(10l, false)));
  }

  @Test
  public void testEquals_WrongObject() {
    assertFalse(new Gap(0l, true).equals(new String()));
  }

  @Test
  public void testToString() {
    new Gap(0l, true).toString();
    new Gap(10l, false).toString();
  }
}
