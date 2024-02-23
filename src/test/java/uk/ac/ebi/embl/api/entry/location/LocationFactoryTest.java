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

public class LocationFactoryTest {

  private LocationFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = new LocationFactory();
  }

  @Test
  public void testCreateLocalRange() {
    LocalRange location = factory.createLocalRange(1L, 3L);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(3), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateLocalRange_Complement() {
    LocalRange location = factory.createLocalRange(3L, 1L, true);
    assertEquals(Long.valueOf(3), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertTrue(location.isComplement());
  }

  @Test
  public void testCreateLocalBase() {
    LocalBase location = factory.createLocalBase(2L);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(2), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateLocalBetween() {
    LocalBetween location = factory.createLocalBetween(3L, 4L);
    assertEquals(Long.valueOf(3), location.getBeginPosition());
    assertEquals(Long.valueOf(4), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateRemoteRange() {
    RemoteRange location = factory.createRemoteRange("B0001", 2, 1L, 3L);
    assertEquals("B0001", location.getAccession());
    assertEquals(Integer.valueOf(2), location.getVersion());
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(3), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateRemoteRange_Complement() {
    RemoteRange location = factory.createRemoteRange("B0001", 2, 1L, 3L, true);
    assertEquals("B0001", location.getAccession());
    assertEquals(Integer.valueOf(2), location.getVersion());
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(3), location.getEndPosition());
    assertTrue(location.isComplement());
  }

  @Test
  public void testCreateRemoteBase() {
    RemoteBase location = factory.createRemoteBase("B0001", 2, 2L);
    assertEquals("B0001", location.getAccession());
    assertEquals(Integer.valueOf(2), location.getVersion());
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(2), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateRemoteBetween() {
    RemoteBetween location = factory.createRemoteBetween("B0001", 2, 3L, 4L);
    assertEquals("B0001", location.getAccession());
    assertEquals(Integer.valueOf(2), location.getVersion());
    assertEquals(Long.valueOf(3), location.getBeginPosition());
    assertEquals(Long.valueOf(4), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  @Test
  public void testCreateGap_Unknown() {
    Gap gap = factory.createUnknownGap(100);
    assertEquals(Gap.DEFAULT_UNKNOWN_LENGTH, gap.getLength());
    assertTrue(gap.isUnknownLength());
  }

  @Test
  public void testCreateGap() {
    Gap gap = factory.createGap(10);
    assertEquals(10, gap.getLength());
    assertFalse(gap.isUnknownLength());
  }
}
