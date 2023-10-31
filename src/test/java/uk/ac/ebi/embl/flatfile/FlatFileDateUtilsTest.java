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
package uk.ac.ebi.embl.flatfile;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.Test;

public class FlatFileDateUtilsTest {
  public static final String[] DAYS_AS_STRING = {
    "06-JAN-1971", "02-OCT-1972", "22-SEP-2016", "10-NOV-2022"
  };

  public static final Date[] DAYS_AS_DATE = {
    FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 6)),
    FlatFileDateUtils.getDate(LocalDate.of(1972, 10, 2)),
    FlatFileDateUtils.getDate(LocalDate.of(2016, 9, 22)),
    FlatFileDateUtils.getDate(LocalDate.of(2022, 11, 10))
  };

  public static final String[] YEARS_AS_STRING = {"1971", "1972", "2016", "2022"};

  public static final Date[] YEARS_AS_DATE = {
    FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 1)),
    FlatFileDateUtils.getDate(LocalDate.of(1972, 1, 1)),
    FlatFileDateUtils.getDate(LocalDate.of(2016, 1, 1)),
    FlatFileDateUtils.getDate(LocalDate.of(2022, 1, 1))
  };

  @Test
  public void testGetDay() {
    for (int i = 0; i < DAYS_AS_STRING.length; i++) {
      assertEquals(DAYS_AS_DATE[i], FlatFileDateUtils.getDay(DAYS_AS_STRING[i]));
    }
  }

  @Test
  public void testGetYear() {
    for (int i = 0; i < YEARS_AS_STRING.length; i++) {
      assertEquals(YEARS_AS_DATE[i], FlatFileDateUtils.getYear(YEARS_AS_STRING[i]));
    }
  }

  @Test
  public void testFormatAsDay() {
    for (int i = 0; i < DAYS_AS_STRING.length; i++) {
      assertEquals(DAYS_AS_STRING[i], FlatFileDateUtils.formatAsDay(DAYS_AS_DATE[i]));
    }
  }

  @Test
  public void testFormatAsYear() {
    for (int i = 0; i < YEARS_AS_STRING.length; i++) {
      assertEquals(YEARS_AS_STRING[i], FlatFileDateUtils.formatAsYear(YEARS_AS_DATE[i]));
    }
  }

  @Test
  public void testGetDate() {
    LocalDate localDate = LocalDate.of(1971, 1, 6);
    Date date = FlatFileDateUtils.getDate(localDate);
    assertEquals(
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.getTime());
  }

  @Test
  public void testGetLocalDate() {
    Date date = new GregorianCalendar(1971, 1, 6).getTime();
    LocalDate localDate = FlatFileDateUtils.getLocalDate(date);
    assertEquals(
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.getTime());
  }

  @Test
  public void testGetLocalDateFromSqlDate() {
    Date sqlDate = new java.sql.Date(FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 1)).getTime());
    LocalDate localDate = FlatFileDateUtils.getLocalDate(sqlDate);
    assertEquals(
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        sqlDate.getTime());
  }
}
