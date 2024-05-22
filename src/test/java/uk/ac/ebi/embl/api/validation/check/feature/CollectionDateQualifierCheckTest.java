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
package uk.ac.ebi.embl.api.validation.check.feature;

import static org.junit.Assert.*;
import static uk.ac.ebi.embl.api.validation.check.feature.CollectionDateQualifierCheck.*;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class CollectionDateQualifierCheckTest {

  private static final CollectionDateQualifierCheck CHECK = new CollectionDateQualifierCheck();

  private static Feature feature(String value) {
    Feature feature = (new FeatureFactory()).createFeature("feature");
    if (value != null) {
      Qualifier qualifier =
          (new QualifierFactory()).createQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
      qualifier.setValue(value);
      feature.addQualifier(qualifier);
    }
    return feature;
  }

  @Test
  public void testValid_NoFeature() {
    assertTrue(CHECK.check(null).isValid());
  }

  @Test
  public void testValid_NoQualifier() {
    assertTrue(CHECK.check(feature(null)).isValid());
  }

  @Test
  public void testInvalidDate() {
    testInvalidDate("INVALID", "CollectionDateQualifierCheck_1");
    testInvalidDate("INVALID-INVALID", "CollectionDateQualifierCheck_1");
    testInvalidDate("INVALID-INVALID-INVALID", "CollectionDateQualifierCheck_1");
    testInvalidDate("INVALID-INVALID-INVALIDTINVALIDZ", "CollectionDateQualifierCheck_1");
    testInvalidDate("INVALID-INVALID-INVALIDTINVALID:INVALIDZ", "CollectionDateQualifierCheck_1");
  }

  @Test
  public void testInvalidDate_Future() {
    testInvalidDate("21-Oct-2050", "CollectionDateQualifierCheck_2");
    testInvalidDate("Oct-2050", "CollectionDateQualifierCheck_2");
    testInvalidDate("2050", "CollectionDateQualifierCheck_2");
    testInvalidDate("2050-10-21T11:43Z", "CollectionDateQualifierCheck_2");
    testInvalidDate("2050-10-21T11Z", "CollectionDateQualifierCheck_2");
    testInvalidDate("2050-10-21", "CollectionDateQualifierCheck_2");
    testInvalidDate("2050-10", "CollectionDateQualifierCheck_2");
  }

  @Test
  public void testInvalidDate_Day() {
    testInvalidDate("100-Oct-2022", "CollectionDateQualifierCheck_1");
    testInvalidDate("00-Oct-2022", "CollectionDateQualifierCheck_1");
    testInvalidDate("0-Oct-2022", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-100T11:43Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-100T11Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-100", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-00T11:43Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-00T11Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-00", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-0T11:43Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-0T11Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-0", "CollectionDateQualifierCheck_1");
  }

  @Test
  public void testInvalidDate_Month() {
    testInvalidDate("10-Ott-2022", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-100-10T11:43Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-00-10T11Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-0-10", "CollectionDateQualifierCheck_1");
  }

  @Test
  public void testInvalidDate_Hour() {
    testInvalidDate("2022-10-21T100:43Z", "CollectionDateQualifierCheck_1");
    testInvalidDate("2022-10-21T100Z", "CollectionDateQualifierCheck_1");
  }

  @Test
  public void testInvalidDate_Minute() {
    testInvalidDate("2022-10-21T10:100Z", "CollectionDateQualifierCheck_1");
  }

  @Test
  public void testInvalidDateRange() {
    testInvalidDate("2004/2003", "CollectionDateQualifierCheck_1"); // from date is > to date
  }

  @Test
  public void testValidDate() {
    testValidDate("21-Oct-1952", LocalDateTime.of(1952, 10, 21, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDate("23-Sep-2021", LocalDateTime.of(2021, 9, 23, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDate(
        "Oct-1952", LocalDateTime.of(1952, 10, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDate(
        "1952", LocalDateTime.of(1952, DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDate("1952-10-21T11:43Z", LocalDateTime.of(1952, 10, 21, 11, 43));
    testValidDate("1952-10-21T11Z", LocalDateTime.of(1952, 10, 21, 11, DEFAULT_MINUTE));
    testValidDate("1952-10-21", LocalDateTime.of(1952, 10, 21, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDate("1952-10", LocalDateTime.of(1952, 10, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
  }

  @Test
  public void testValidDateRange() {

    testValidDateRange(
        "21-Oct-1952/15-Feb-1953",
        LocalDateTime.of(1952, 10, 21, DEFAULT_HOUR, DEFAULT_MINUTE),
        LocalDateTime.of(1953, 2, 15, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDateRange(
        "Oct-1952/Feb-1953",
        LocalDateTime.of(1952, 10, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE),
        LocalDateTime.of(1953, 2, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDateRange(
        "1952/1953",
        LocalDateTime.of(1952, DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE),
        LocalDateTime.of(1953, DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDateRange(
        "1952-10-21T11:43Z/1952-10-21T17:43Z",
        LocalDateTime.of(1952, 10, 21, 11, 43),
        LocalDateTime.of(1952, 10, 21, 17, 43));
    testValidDateRange(
        "1952-10-21/1953-02-15",
        LocalDateTime.of(1952, 10, 21, DEFAULT_HOUR, DEFAULT_MINUTE),
        LocalDateTime.of(1953, 02, 15, DEFAULT_HOUR, DEFAULT_MINUTE));
    testValidDateRange(
        "1952-10/1953-02",
        LocalDateTime.of(1952, 10, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE),
        LocalDateTime.of(1953, 02, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE));
  }

  private static void testInvalidDate(String value, String messageKey) {
    ValidationResult validationResult = CHECK.check(feature(value));
    assertEquals(1, validationResult.count(messageKey, Severity.ERROR));
    assertFalse(validationResult.isValid());
    assertFalse(CHECK.isValid(value));
  }

  private static void testValidDate(String value, LocalDateTime expected) {
    ValidationResult validationResult = CHECK.check(feature(value));
    assertTrue(validationResult.isValid());
    assertTrue(CHECK.isValid(value));
    try {
      assertTrue(CHECK.isValidDate(value));
      CollectionDateQualifierCheck.ParseDateResult dateResult = CHECK.parseDate(value);
      assertEquals(expected, dateResult.date);
    } catch (CollectionDateQualifierCheck.FutureDateException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void testValidDateRange(
      String value, LocalDateTime expectedFrom, LocalDateTime expectedTo) {
    ValidationResult validationResult = CHECK.check(feature(value));
    assertTrue(validationResult.isValid());
    assertTrue(CHECK.isValid(value));
    try {
      assertTrue(CHECK.isValidDate(value));

      String fromValue = value.split("/")[0];
      String toValue = value.split("/")[1];

      CollectionDateQualifierCheck.ParseDateResult fromResult = CHECK.parseDate(fromValue);
      CollectionDateQualifierCheck.ParseDateResult toResult = CHECK.parseDate(toValue);

      assertEquals(expectedFrom, fromResult.date);
      assertEquals(expectedTo, toResult.date);
    } catch (CollectionDateQualifierCheck.FutureDateException ex) {
      throw new RuntimeException(ex);
    }
  }
}
