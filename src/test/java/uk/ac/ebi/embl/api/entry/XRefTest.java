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
package uk.ac.ebi.embl.api.entry;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class XRefTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testHashCode() {
    assertNotNull(new XRef("db", "pa", "sa").hashCode());
  }

  @Test
  public void testEqualsObject() {
    XRef x1 = new XRef("db", "pa", "sa");
    XRef x2 = new XRef("db", "pa", "sa");
    XRef x3 = new XRef("x", "pa", "sa");
    XRef x4 = new XRef("db", "x", "sa");
    XRef x5 = new XRef("db", "pa", "x");

    assertEquals(x1, x1);
    assertEquals(x1, x2);
    assertEquals(x2, x1);

    assertNotEquals(x1, x3);
    assertNotEquals(x3, x1);

    assertNotEquals(x1, x4);
    assertNotEquals(x1, x5);

    x2.setDatabase("x");
    x2.setPrimaryAccession("x");
    x2.setSecondaryAccession("x");
    assertNotEquals(x1, x2);
  }

  @Test
  public void testToString() {
    assertNotNull(new XRef("db", "pa", "sa").toString());
  }
}
