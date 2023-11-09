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
  public void setUp() {}

  @Test
  public void testGap() {
    new Gap(0L, false);
  }

  @Test
  public void testGapLong() {
    new Gap(10L, false);
  }

  @Test
  public void testGetLength() {
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, new Gap(0L, true).getLength());
    assertEquals(10, new Gap(10L, false).getLength());
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, new Gap(-10L, true).getLength());
  }

  @Test
  public void testGetSequence() {
    String _100N =
        "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"
            + "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN";
    assertEquals(_100N, new Gap(0L, true).getSequence());
    assertEquals(_100N, new Gap(-10L, true).getSequence());
    assertEquals("", new Gap(0, false).getSequence());
    assertEquals("NNNNN", new Gap(5L, false).getSequence());
    assertEquals(_100N, new Gap(100L, false).getSequence());
  }

  @Test
  public void testIsUnknownLength() {
    assertTrue(new Gap(0L, true).isUnknownLength());
    assertTrue(new Gap(-1L, true).isUnknownLength());
    assertTrue(new Gap(10L, true).isUnknownLength());

    assertFalse(new Gap(5L, false).isUnknownLength());
    assertFalse(new Gap(100L, false).isUnknownLength());
  }

  @Test
  public void testSetUnknownLength() {
    Gap gap = new Gap(0L, true);
    assertTrue(gap.isUnknownLength());
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, gap.getLength());

    gap = new Gap(10L, false);
    assertFalse(gap.isUnknownLength());
    assertEquals(10, gap.getLength());
  }

  @Test
  public void testHashCode() {
    new Gap(0L, true).hashCode();
    new Gap(10L, false).hashCode();
  }

  @Test
  public void testEquals() {
    Gap gap = new Gap(0L, true);
    assertEquals(gap, gap);
    assertEquals(gap, new Gap(0L, true));
    assertEquals(gap, new Gap(-1L, true));
    assertEquals(new Gap(10L, false), new Gap(10L, false));

    assertNotEquals(gap, new Gap(10L, false));
    assertNotEquals(new Gap(1L, false), new Gap(10L, false));
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", new Gap(0L, true));
  }

  @Test
  public void testToString() {
    new Gap(0L, true).toString();
    new Gap(10L, false).toString();
  }
}
