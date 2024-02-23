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

public class LocalBaseTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testBase() {
    Base base = new LocalBase(2L);
    assertEquals(Long.valueOf(2), base.getBeginPosition());
    assertEquals(Long.valueOf(2), base.getEndPosition());
    assertEquals(1, base.getLength());
    assertFalse(base.isComplement());
  }

  @Test
  public void testComplement() {
    Base base = new LocalBase(null, true);
    assertTrue(base.isComplement());
    base.setComplement(false);
    assertFalse(base.isComplement());
    base.setComplement(true);
    assertTrue(base.isComplement());
  }

  @Test
  public void testBase_Null() {
    Base base = new LocalBase(null);
    assertNull(base.getBeginPosition());
    assertNull(base.getEndPosition());
    assertEquals(0, base.getLength());
  }

  @Test
  public void testSetBeginPosition() {
    Base base = new LocalBase(null);
    assertNull(base.getBeginPosition());
    assertNull(base.getEndPosition());
    base.setBeginPosition(2L);
    assertEquals(Long.valueOf(2), base.getBeginPosition());
    assertEquals(Long.valueOf(2), base.getEndPosition());
    assertEquals(1, base.getLength());
  }

  @Test
  public void testSetEndPosition() {
    Base base = new LocalBase(null);
    assertNull(base.getBeginPosition());
    assertNull(base.getEndPosition());
    base.setEndPosition(2L);
    assertEquals(Long.valueOf(2), base.getBeginPosition());
    assertEquals(Long.valueOf(2), base.getEndPosition());
    assertEquals(1, base.getLength());
  }

  @Test
  public void testSetPosition() {
    Base base = new LocalBase(null);
    assertNull(base.getBeginPosition());
    assertNull(base.getEndPosition());
    base.setPosition(2L);
    assertEquals(Long.valueOf(2), base.getBeginPosition());
    assertEquals(Long.valueOf(2), base.getEndPosition());
  }

  @Test
  public void testToString() {
    assertNotNull(new LocalBase(null).toString());
    assertNotNull(new LocalBase(2L).toString());
  }

  @Test
  public void testBaseHashCode() {
    new LocalBase(2L).hashCode();
  }

  @Test
  public void testEquals() {
    Base location1 = new LocalBase(2L);
    assertEquals(location1, location1);
    Base location2 = new LocalBase(2L);
    assertEquals(location1, location2);
    assertEquals(location2, location1);
    assertNotEquals(location1, new RemoteBase("y", 1, 2L));
    assertNotEquals(location1, new RemoteBase("x", 2, 2L));
    assertNotEquals(location1, new RemoteBase("x", 1, 3L));
    assertNotEquals(location1, new RemoteBase("x", 1, 2L, true));
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", new LocalBase(2L));
  }
}
