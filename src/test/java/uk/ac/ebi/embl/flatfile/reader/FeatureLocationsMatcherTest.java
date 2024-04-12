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

import static org.junit.Assert.*;

import java.util.EnumSet;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;

public class FeatureLocationsMatcherTest {

  public enum Partiality {
    COMPOUND_5_PARTIAL,
    COMPOUND_3_PARTIAL,
    FIRST_5_PARTIAL,
    FIRST_3_PARTIAL,
    LAST_5_PARTIAL,
    LAST_3_PARTIAL
  }

  private static void testGettingPartiality(
      CompoundLocation<Location> location, EnumSet<Partiality>[] partialityList) {
    EnumSet<Partiality> partiality = EnumSet.noneOf(Partiality.class);
    for (EnumSet<Partiality> p : partialityList) {
      partiality.addAll(p);
    }

    if (partiality.contains(Partiality.COMPOUND_5_PARTIAL)) {
      assertTrue("Expecting compound 5 prime partial", location.isFivePrimePartial());
    } else {
      assertFalse("Not expecting compound 5 prime partial", location.isFivePrimePartial());
    }
    if (partiality.contains(Partiality.COMPOUND_3_PARTIAL)) {
      assertTrue("Expecting compound 3 prime partial", location.isThreePrimePartial());
    } else {
      assertFalse("Not expecting compound 3 prime partial", location.isThreePrimePartial());
    }

    if (location.getLocations().size() == 1) {
      if (partiality.contains(Partiality.FIRST_5_PARTIAL)
          && !partiality.contains(Partiality.LAST_5_PARTIAL)) {
        partiality.add(Partiality.LAST_5_PARTIAL);
      }
      if (partiality.contains(Partiality.FIRST_3_PARTIAL)
          && !partiality.contains(Partiality.LAST_3_PARTIAL)) {
        partiality.add(Partiality.LAST_3_PARTIAL);
      }
      if (partiality.contains(Partiality.LAST_5_PARTIAL)
          && !partiality.contains(Partiality.FIRST_5_PARTIAL)) {
        partiality.add(Partiality.FIRST_5_PARTIAL);
      }
      if (partiality.contains(Partiality.LAST_3_PARTIAL)
          && !partiality.contains(Partiality.FIRST_3_PARTIAL)) {
        partiality.add(Partiality.FIRST_3_PARTIAL);
      }
    }

    if (partiality.contains(Partiality.FIRST_5_PARTIAL)) {
      assertTrue(
          "Expecting first 5 prime partial", location.getLocations().get(0).isFivePrimePartial());
    } else {
      assertFalse(
          "Not expecting first 3 prime partial",
          location.getLocations().get(0).isFivePrimePartial());
    }
    if (partiality.contains(Partiality.FIRST_3_PARTIAL)) {
      assertTrue(
          "Expecting first 3 prime partial", location.getLocations().get(0).isThreePrimePartial());
    } else {
      assertFalse(
          "Not expecting first 3 prime partial",
          location.getLocations().get(0).isThreePrimePartial());
    }

    if (partiality.contains(Partiality.LAST_5_PARTIAL)) {
      assertTrue(
          "Expecting last 5 prime partial",
          location.getLocations().get(location.getLocations().size() - 1).isFivePrimePartial());
    } else {
      assertFalse(
          "Not expecting last 5 prime partial",
          location.getLocations().get(location.getLocations().size() - 1).isFivePrimePartial());
    }
    if (partiality.contains(Partiality.LAST_3_PARTIAL)) {
      assertTrue(
          "Expecting last 3 prime partial",
          location.getLocations().get(location.getLocations().size() - 1).isThreePrimePartial());
    } else {
      assertFalse(
          "Not expecting last 3 prime partial",
          location.getLocations().get(location.getLocations().size() - 1).isThreePrimePartial());
    }
  }

  private static void removePartiality(CompoundLocation<Location> location) {
    for (Location l : location.getLocations()) {
      l.setFivePrimePartial(false);
      l.setThreePrimePartial(false);
    }
    for (Location l : location.getLocations()) {
      assertFalse(l.isFivePrimePartial());
      assertFalse(l.isThreePrimePartial());
    }
  }

  private static void testSettingPartiality(CompoundLocation<Location> location) {
    boolean isCompoundFivePartial = location.isFivePrimePartial();
    boolean isCompoundThreePartial = location.isThreePrimePartial();
    boolean isFirstFivePartial = location.getLocations().get(0).isFivePrimePartial();
    boolean isFirstThreePartial = location.getLocations().get(0).isThreePrimePartial();
    boolean isLastFivePartial =
        location.getLocations().get(location.getLocations().size() - 1).isFivePrimePartial();
    boolean isLastThreePartial =
        location.getLocations().get(location.getLocations().size() - 1).isThreePrimePartial();

    // Test compound location partiality
    removePartiality(location);
    location.setFivePrimePartial(isCompoundFivePartial);
    location.setThreePrimePartial(isCompoundThreePartial);
    assertEquals(location.isFivePrimePartial(), isCompoundFivePartial);
    assertEquals(location.isThreePrimePartial(), isCompoundThreePartial);

    // Test first and last location partiality
    removePartiality(location);
    location.getLocations().get(0).setFivePrimePartial(isFirstFivePartial);
    location.getLocations().get(0).setThreePrimePartial(isFirstThreePartial);
    location
        .getLocations()
        .get(location.getLocations().size() - 1)
        .setFivePrimePartial(isLastFivePartial);
    location
        .getLocations()
        .get(location.getLocations().size() - 1)
        .setThreePrimePartial(isLastThreePartial);
    assertEquals(location.getLocations().get(0).isFivePrimePartial(), isFirstFivePartial);
    assertEquals(location.getLocations().get(0).isThreePrimePartial(), isFirstThreePartial);
    assertEquals(
        location.getLocations().get(location.getLocations().size() - 1).isFivePrimePartial(),
        isLastFivePartial);
    assertEquals(
        location.getLocations().get(location.getLocations().size() - 1).isThreePrimePartial(),
        isLastThreePartial);
    assertEquals(location.isFivePrimePartial(), isCompoundFivePartial);
    assertEquals(location.isThreePrimePartial(), isCompoundThreePartial);
  }

  /** Tests location string without partiality. */
  private static CompoundLocation<Location> test(
      String expectedLocationString, String locationString) {
    FeatureLocationParser parser = new FeatureLocationParser(null, false);
    CompoundLocation<Location> location = parser.getCompoundLocation(locationString);
    assertEquals(expectedLocationString, FeatureLocationWriter.getLocationString(location));
    return location;
  }

  /** Tests location string with partiality. */
  @SafeVarargs
  private static void test(
      String expectedLocationString, String locationString, EnumSet<Partiality>... partialityList) {
    CompoundLocation<Location> location = test(expectedLocationString, locationString);
    testGettingPartiality(location, partialityList);
    testSettingPartiality(location);
  }

  @Test
  public void testLocation() {

    // The sequence is read from the 5' prime end (left) to the 3' prime end (right)

    test("467", "467");
    test("467", "467..467");
    test("467..468", "467..468");
    test("467^468", "467^468");

    test("complement(467)", "complement(467)");
    test("complement(467)", "complement(467..467)");
    test("complement(467..468)", "complement(467..468)");
    test("complement(467^468)", "complement(467^468)");
  }

  @Test
  public void testPartiality() {
    EnumSet<Partiality> compoundAndFirstFivePrimePartial =
        EnumSet.of(Partiality.COMPOUND_5_PARTIAL, Partiality.FIRST_5_PARTIAL);
    EnumSet<Partiality> compoundAndLastThreePrimePartial =
        EnumSet.of(Partiality.COMPOUND_3_PARTIAL, Partiality.LAST_3_PARTIAL);
    EnumSet<Partiality> compoundAndFirstThreePrimePartial =
        EnumSet.of(Partiality.COMPOUND_3_PARTIAL, Partiality.FIRST_3_PARTIAL);
    EnumSet<Partiality> compoundAndLastFivePrimePartial =
        EnumSet.of(Partiality.COMPOUND_5_PARTIAL, Partiality.LAST_5_PARTIAL);
    EnumSet<Partiality> compoundThreePrimePartialAndFirstFivePrimePartial =
        EnumSet.of(Partiality.COMPOUND_3_PARTIAL, Partiality.FIRST_5_PARTIAL);
    EnumSet<Partiality> compoundFivePrimePartialAndFirstThreePrimePartial =
        EnumSet.of(Partiality.COMPOUND_5_PARTIAL, Partiality.FIRST_3_PARTIAL);
    EnumSet<Partiality> compoundFivePrimePartialAndLastThreePrimePartial =
        EnumSet.of(Partiality.COMPOUND_5_PARTIAL, Partiality.LAST_3_PARTIAL);

    test("<467", "<467", compoundAndFirstFivePrimePartial);
    test("<467", "<467..467", compoundAndFirstFivePrimePartial);
    test("<467..468", "<467..468", compoundAndFirstFivePrimePartial);
    test("<467^468", "<467^468", compoundAndFirstFivePrimePartial);
    test("join(<467,468)", "join(<467,468)", compoundAndFirstFivePrimePartial);

    test(">467", ">467", compoundAndLastThreePrimePartial);
    test(">467", "467..>467", compoundAndLastThreePrimePartial);
    test("467..>468", "467..>468", compoundAndLastThreePrimePartial);
    test("467^>468", "467^>468", compoundAndLastThreePrimePartial);
    test("join(467,>468)", "join(467,>468)", compoundAndLastThreePrimePartial);

    test(
        "<467..>468",
        "<467..>468",
        compoundAndFirstFivePrimePartial,
        compoundAndLastThreePrimePartial);
    test(
        "<467^>468",
        "<467^>468",
        compoundAndFirstFivePrimePartial,
        compoundAndLastThreePrimePartial);
    test(
        "join(<467,>468)",
        "join(<467,>468)",
        compoundAndFirstFivePrimePartial,
        compoundAndLastThreePrimePartial);

    test("complement(<467)", "complement(<467)", compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(<467)",
        "complement(<467..467)",
        compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(<467..468)",
        "complement(<467..468)",
        compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(<467^468)",
        "complement(<467^468)",
        compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(join(<467,468))",
        "complement(join(<467,468))",
        compoundThreePrimePartialAndFirstFivePrimePartial);

    test("complement(>467)", "complement(>467)", compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(>467)",
        "complement(467..>467)",
        compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(467..>468)",
        "complement(467..>468)",
        compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(467^>468)",
        "complement(467^>468)",
        compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(join(467,>468))",
        "complement(join(467,>468))",
        compoundFivePrimePartialAndLastThreePrimePartial);

    test(
        "complement(<467..>468)",
        "complement(<467..>468)",
        compoundThreePrimePartialAndFirstFivePrimePartial,
        compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(<467^>468)",
        "complement(<467^>468",
        compoundThreePrimePartialAndFirstFivePrimePartial,
        compoundFivePrimePartialAndFirstThreePrimePartial);
    test(
        "complement(join(<467,>468))",
        "complement(join(<467,>468))",
        compoundThreePrimePartialAndFirstFivePrimePartial,
        compoundFivePrimePartialAndLastThreePrimePartial);

    test("<2..3", "<2..3", compoundAndFirstFivePrimePartial);
    test("2..>3", "2..>3", compoundAndLastThreePrimePartial);
    test(
        "complement(<2..3)",
        "complement(<2..3)",
        compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(2..>3)", "complement(2..>3)", compoundFivePrimePartialAndLastThreePrimePartial);
    test("join(<2,4)", "join(<2,4)", compoundAndFirstFivePrimePartial);
    test("join(2,>4)", "join(2,>4)", compoundAndLastThreePrimePartial);
    test(
        "complement(join(<2,4))",
        "complement(join(<2,4))",
        compoundThreePrimePartialAndFirstFivePrimePartial);
    test(
        "complement(join(2,>4))",
        "complement(join(2,>4))",
        compoundFivePrimePartialAndLastThreePrimePartial);
    test(
        "complement(join(<2,>4))",
        "complement(join(<2,>4))",
        compoundThreePrimePartialAndFirstFivePrimePartial,
        compoundFivePrimePartialAndLastThreePrimePartial);
    test("join(complement(<2),4)", "join(complement(<2),4)", compoundAndFirstThreePrimePartial);
    test("join(complement(2),>4)", "join(complement(2),>4)", compoundAndLastThreePrimePartial);
    test("join(2,complement(>4))", "join(2,complement(>4))", compoundAndLastFivePrimePartial);
    test(
        "join(complement(<2),complement(4))",
        "join(complement(<2),complement(4))",
        compoundAndFirstThreePrimePartial);
    test(
        "join(complement(2),complement(>4))",
        "join(complement(2),complement(>4))",
        compoundAndLastFivePrimePartial);
    test(
        "join(complement(<2),>4)",
        "join(complement(<2),>4)",
        compoundAndFirstThreePrimePartial,
        compoundAndLastThreePrimePartial);
    test(
        "join(<2,complement(>4))",
        "join(<2,complement(>4))",
        compoundAndFirstFivePrimePartial,
        compoundAndLastFivePrimePartial);
    test(
        "join(complement(<2),complement(>4))",
        "join(complement(<2),complement(>4))",
        compoundAndFirstThreePrimePartial,
        compoundAndLastFivePrimePartial);

    // For unknown reasons the 3' prime partiality is lost for a single base if 5' partiality is
    // present.
    test("<467", "<>467");
    test("<467", "<467..>467");
    test("complement(<467)", "complement(<467..>467)");
  }

  @Test
  public void testRemoteLocation() {
    test("J00194.1:467", "J00194.1:467");
    test("J00194.1:340..565", "J00194.1:340..565");
    test("J00194.1:<340..565", "J00194.1:<340..565");
    test("J00194.1:340..>565", "J00194.1:340..>565");
    test("J00194.1:>340", "J00194.1:>340");
    test("J00194.1:>340", "J00194.1:340..>340");
  }
}
