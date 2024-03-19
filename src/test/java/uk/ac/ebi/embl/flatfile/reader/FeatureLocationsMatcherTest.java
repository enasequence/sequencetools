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

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;

public class FeatureLocationsMatcherTest extends TestCase {

  private static String test(String locationString) {
    FeatureLocationsMatcher matcher = new FeatureLocationsMatcher(null, false);
    matcher.match(locationString);
    return FeatureLocationWriter.getLocationString(matcher.getCompoundLocation());
  }

  private static boolean leftPartial(String locationString) {
    FeatureLocationsMatcher matcher = new FeatureLocationsMatcher(null, false);
    matcher.match(locationString);
    return matcher.getCompoundLocation().isLeftPartial();
  }

  private static boolean rightPartial(String locationString) {
    FeatureLocationsMatcher matcher = new FeatureLocationsMatcher(null, false);
    matcher.match(locationString);
    return matcher.getCompoundLocation().isRightPartial();
  }

  @Test
  public void testLocation() {

    // The sequence is read from the 5' prime end (left) to the 3' prime end (right)

    assertEquals("467", test("467"));
    assertEquals("467", test("467..467"));
    assertEquals("467..468", test("467..468"));
    assertEquals("467^468", test("467^468"));

    assertEquals("<467", test("<467"));
    assertEquals("<467", test("<467..467"));
    assertEquals("<467..468", test("<467..468"));
    assertEquals("<467^468", test("<467^468"));

    assertEquals(">467", test(">467"));
    assertEquals(">467", test("467..>467"));
    assertEquals("467..>468", test("467..>468"));
    assertEquals("467^>468", test("467^>468"));

    // Note that 3' prime end (right) partiality is lost.
    assertEquals("<467", test("<>467"));
    // Note that 3' prime end (right) partiality is lost.
    assertEquals("<467", test("<467..>467"));
    assertEquals("<467..>468", test("<467..>468"));
    assertEquals("<467^>468", test("<467^>468"));

    assertEquals("complement(467)", test("complement(467)"));
    assertEquals("complement(467)", test("complement(467..467)"));
    assertEquals("complement(467..468)", test("complement(467..468)"));
    assertEquals("complement(467^468)", test("complement(467^468)"));

    assertEquals("complement(<467)", test("complement(<467)"));
    assertEquals("complement(<467)", test("complement(<467..467)"));
    assertEquals("complement(<467..468)", test("complement(<467..468)"));
    assertEquals("complement(<467^468)", test("complement(<467^468)"));

    assertEquals("complement(>467)", test("complement(>467)"));
    assertEquals("complement(>467)", test("complement(467..>467)"));
    assertEquals("complement(467..>468)", test("complement(467..>468)"));
    assertEquals("complement(467^>468)", test("complement(467^>468)"));

    // Note that 3' prime end (right) partiality is lost.
    assertEquals("complement(<467)", test("complement(<>467)"));
    // Note that 3' prime end (right) partiality is lost.
    assertEquals("complement(<467)", test("complement(<467..>467)"));
    assertEquals("complement(<467..>468)", test("complement(<467..>468)"));
    assertEquals("complement(<467^>468)", test("complement(<467^>468"));

    String location = "complement(2807037..>2807081)";
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "complement(<2807037..2807081)";
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(join(2182966..2183014,2183124..>2183128))";
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(join(<2182966..2183014,2183124..2183128))";
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));
  }

  @Test
  public void testPartiality() {
    /*
    <2..3 → 5’ partial
    2..>3 → 3’ partial
    complement(<2..3) → 3’ partial
    complement(2..>3) → 5’ partial
    join(<2,4) → 5’ partial
    join(2,>4) → 3’ partial
    complement(join(<2,4)) → 3’ partial
    complement(join(2,>4)) → 5’partial
    join(complement(<2),4) → 3’ partial
    join(2,complement(>4)) → 5’ partial
    join(complement(<2),complement(4)) → 3’ partial
    join(complement(2),complement(>4)) → 5’ partial
     */

    String location = "<2..3"; //  → 5’ partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "2..>3"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(<2..3)"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(2..>3)"; //  → 5’ partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "join(<2,4)"; //  → 5’ partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "join(2,>4)"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(join(<2,4))"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "complement(join(2,>4))"; //  → 5’partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "join(complement(<2),4)"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "join(2,complement(>4))"; //  → 5’ partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));

    location = "join(complement(<2),complement(4))"; //  → 3’ partial
    assertEquals(location, test(location));
    assertFalse(leftPartial(location));
    assertTrue(rightPartial(location));

    location = "join(complement(2),complement(>4))"; //  → 5’ partial
    assertEquals(location, test(location));
    assertTrue(leftPartial(location));
    assertFalse(rightPartial(location));
  }

  @Test
  public void testRemoteLocation() {
    assertEquals("J00194.1:467", test("J00194.1:467"));
    assertEquals("J00194.1:340..565", test("J00194.1:340..565"));
    assertEquals("J00194.1:<340..565", test("J00194.1:<340..565"));
    assertEquals("J00194.1:340..>565", test("J00194.1:340..>565"));
    assertEquals("J00194.1:>340", test("J00194.1:>340"));
    assertEquals("J00194.1:>340", test("J00194.1:340..>340"));
  }
}
