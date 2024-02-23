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
package uk.ac.ebi.embl.api.entry.location;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RemoteRangeTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testRange() {
    Range Range = new RemoteRange("x", 1, 2L, 3L);
    assertEquals(Long.valueOf(2), Range.getBeginPosition());
    assertEquals(Long.valueOf(3), Range.getEndPosition());
    assertEquals(2, Range.getLength());
    assertFalse(Range.isComplement());
  }

  @Test
  public void testAccession() {
    RemoteRange range = new RemoteRange("x", 1, 2L, 3L);
    assertEquals("x", range.getAccession());
    assertEquals(Integer.valueOf(1), range.getVersion());
  }

  @Test
  public void testComplement() {
    Range range = new RemoteRange(null, null, null, null, true);
    assertTrue(range.isComplement());
    range.setComplement(false);
    assertFalse(range.isComplement());
    range.setComplement(true);
    assertTrue(range.isComplement());
  }

  @Test
  public void testRange_Null() {
    Range range = new RemoteRange("x", 1, null, null);
    assertNull(range.getBeginPosition());
    assertNull(range.getEndPosition());
    assertEquals(0, range.getLength());
  }

  @Test
  public void testSetPosition() {
    Range range = new RemoteRange("x", 1, null, null);
    assertNull(range.getBeginPosition());
    assertNull(range.getEndPosition());
    range.setBeginPosition(2L);
    range.setEndPosition(3L);
    assertEquals(Long.valueOf(2), range.getBeginPosition());
    assertEquals(Long.valueOf(3), range.getEndPosition());
    assertEquals(2, range.getLength());
  }

  @Test
  public void testToString() {
    assertNotNull(new RemoteRange("x", 1, null, null).toString());
    assertNotNull(new RemoteRange("x", 1, 2L, 3L).toString());
  }

  @Test
  public void testRangeHashCode() {
    new RemoteRange("x", 1, 1L, 2L).hashCode();
  }

  @Test
  public void testEquals() {
    Range location1 = new RemoteRange("x", 1, 2L, 3L);
    assertEquals(location1, location1);
    Range location2 = new RemoteRange("x", 1, 2L, 3L);
    assertEquals(location1, location2);
    assertEquals(location2, location1);
    Range location3 = new RemoteRange("x", 1, 2L, 3L);
    assertEquals(location1, location3);
    assertEquals(location3, location1);
    assertNotEquals(location1, new RemoteRange("y", 1, 2L, 3L));
    assertNotEquals(location1, new RemoteRange("x", 2, 2L, 3L));
    assertNotEquals(location1, new RemoteRange("x", 1, 3L, 3L));
    assertNotEquals(location1, new RemoteRange("x", 1, 2L, 4L));
    assertNotEquals(location1, new RemoteRange("x", 1, 2L, 3L, true));
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", new RemoteRange("x", 1, 1L, 2L));
  }
}
