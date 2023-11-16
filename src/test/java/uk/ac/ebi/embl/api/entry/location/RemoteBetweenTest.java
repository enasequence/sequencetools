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

public class RemoteBetweenTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testBetween() {
    Between Between = new RemoteBetween("x", 1, 2L, 3L);
    assertEquals(Long.valueOf(2), Between.getBeginPosition());
    assertEquals(Long.valueOf(3), Between.getEndPosition());
    assertEquals(0, Between.getLength());
    assertFalse(Between.isComplement());
  }

  @Test
  public void testAccession() {
    RemoteBetween between = new RemoteBetween("x", 1, 2L, 3L);
    assertEquals("x", between.getAccession());
    assertEquals(Integer.valueOf(1), between.getVersion());
  }

  @Test
  public void testComplement() {
    Between between = new RemoteBetween(null, null, null, null, true);
    assertTrue(between.isComplement());
    between.setComplement(false);
    assertFalse(between.isComplement());
    between.setComplement(true);
    assertTrue(between.isComplement());
  }

  @Test
  public void testBetween_Null() {
    Between between = new RemoteBetween("x", 1, null, null);
    assertNull(between.getBeginPosition());
    assertNull(between.getEndPosition());
    assertEquals(0, between.getLength());
  }

  @Test
  public void testSetPosition() {
    Between between = new RemoteBetween("x", 1, null, null);
    assertNull(between.getBeginPosition());
    assertNull(between.getEndPosition());
    between.setBeginPosition(2L);
    between.setEndPosition(3L);
    assertEquals(Long.valueOf(2), between.getBeginPosition());
    assertEquals(Long.valueOf(3), between.getEndPosition());
    assertEquals(0, between.getLength());
  }

  @Test
  public void testToString() {
    assertNotNull(new RemoteBetween("x", 1, null, null).toString());
    assertNotNull(new RemoteBetween("x", 1, 2L, 3L).toString());
  }

  @Test
  public void testBetweenHashCode() {
    new RemoteBetween("x", 1, 1L, 2L).hashCode();
  }

  @Test
  public void testEquals() {
    Between location1 = new RemoteBetween("x", 1, 2L, 3L);
    assertEquals(location1, location1);
    Between location2 = new RemoteBetween("x", 1, 2L, 3L);
    assertEquals(location1, location2);
    assertEquals(location2, location1);
    Between location3 = new RemoteBetween("x", 1, 2L, 3L);
    assertEquals(location1, location3);
    assertEquals(location3, location1);
    assertNotEquals(location1, new RemoteBetween("y", 1, 2L, 3L));
    assertNotEquals(location1, new RemoteBetween("x", 2, 2L, 3L));
    assertNotEquals(location1, new RemoteBetween("x", 1, 3L, 3L));
    assertNotEquals(location1, new RemoteBetween("x", 1, 2L, 4L));
    assertNotEquals(location1, new RemoteBetween("x", 1, 2L, 3L, true));
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", new RemoteBetween("x", 1, 1L, 2L));
  }
}
