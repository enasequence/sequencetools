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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LocalRangeTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testRange() {
    Range range = new LocalRange(2L, 3L);
    assertEquals(new Long(2), range.getBeginPosition());
    assertEquals(new Long(3), range.getEndPosition());
    assertEquals(2, range.getLength());
    assertFalse(range.isComplement());
  }

  @Test
  public void testRange_Null() {
    Range range = new LocalRange(null, null);
    assertNull(range.getBeginPosition());
    assertNull(range.getEndPosition());
    assertEquals(0, range.getLength());
  }

  @Test
  public void testSetPosition() {
    Range range = new LocalRange(null, null);
    assertNull(range.getBeginPosition());
    assertNull(range.getEndPosition());
    range.setBeginPosition(2L);
    range.setEndPosition(3L);
    assertEquals(new Long(2), range.getBeginPosition());
    assertEquals(new Long(3), range.getEndPosition());
    assertEquals(2, range.getLength());
  }

  @Test
  public void testToString() {
    assertNotNull(new LocalRange(null, null).toString());
    assertNotNull(new LocalRange(2L, 3L).toString());
  }

  @Test
  public void testRangeHashCode() {
    new LocalRange(1L, 2L).hashCode();
  }

  @Test
  public void testEquals() {
    Range location1 = new LocalRange(2L, 3L);
    assertTrue(location1.equals(location1));
    Range location2 = new LocalRange(2L, 3L);
    assertTrue(location1.equals(location2));
    assertTrue(location2.equals(location1));
    assertFalse(location1.equals(new RemoteRange("y", 1, 2L, 3L)));
    assertFalse(location1.equals(new RemoteRange("x", 2, 2L, 3L)));
    assertFalse(location1.equals(new RemoteRange("x", 1, 3L, 3L)));
    assertFalse(location1.equals(new RemoteRange("x", 1, 2L, 4L)));
    assertFalse(location1.equals(new RemoteRange("x", 1, 2L, 3L, true)));
  }

  @Test
  public void testEquals_WrongObject() {
    assertFalse(new LocalRange(2L, 3L).equals(new String()));
  }
}
