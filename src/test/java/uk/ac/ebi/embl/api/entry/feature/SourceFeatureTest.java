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
package uk.ac.ebi.embl.api.entry.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.Order;

public class SourceFeatureTest {

  private SourceFeature feature;

  @Before
  public void setUp() throws Exception {
    feature = new SourceFeature();
  }

  @Test
  public void testSourceFeature() {
    assertEquals(SourceFeature.SOURCE_FEATURE_NAME, feature.getName());
    assertTrue(feature.getLocations() instanceof Order<?>);
    assertTrue(feature.getQualifiers().isEmpty());
    assertTrue(feature.getXRefs().isEmpty());
    assertNotNull(feature.getLocations());
  }

  @Test
  public void testGetScientificName() {
    assertNull(feature.getScientificName());
    feature.setScientificName("org2");
    assertEquals("org2", feature.getScientificName());
  }

  @Test
  public void testSetScientificName() {
    assertNull(feature.getScientificName());
    feature.setScientificName("org2");
    assertEquals("org2", feature.getScientificName());
    feature.setScientificName(null);
    assertNull(feature.getScientificName());
  }

  @Test
  public void testTrangenic() {
    assertFalse(feature.isTransgenic());
    feature.setTransgenic(true);
    assertTrue(feature.isTransgenic());
    feature.setTransgenic(true);
    assertTrue(feature.isTransgenic());
    feature.setTransgenic(false);
    assertFalse(feature.isTransgenic());
    feature.setTransgenic(false);
    assertFalse(feature.isTransgenic());
  }

  @Test
  public void testEquals() {
    assertEquals(feature, feature);
    assertEquals(feature, new SourceFeature());
    assertNotEquals("", feature);
  }

  @Test
  public void testEquals_WrongObject() {
    assertNotEquals("", feature);
  }

  @Test
  public void testToString() {
    assertNotNull(feature.toString());
  }
}
