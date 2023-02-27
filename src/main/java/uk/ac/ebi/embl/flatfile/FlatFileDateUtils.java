/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;

public abstract class FlatFileDateUtils {

    private static DateTimeFormatter DATE_FORMATTER;

    static {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.parseCaseInsensitive();
        builder.appendPattern("dd-MMM-yyyy");
        DATE_FORMATTER = builder.toFormatter(Locale.UK);
    }

    /**
     * Returns the day given a string in format dd-MMM-yyyy or null of the string can't be parsed.
     *
     * @param str the day as string
     * @return the day or null of the string can't be parsed.
     */
    public static Date getDay(String str) {
        if (str == null) {
            return null;
        }
        try {
            return getDate(LocalDate.from(DATE_FORMATTER.parse(str)));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the year given a string in format yyyy or null of the string can't be parsed.
     *
     * @param str the year as string
     * @return the day or null of the string can't be parsed.
     */
    public static Date getYear(String str) {
        if (str == null) {
            return null;
        }
        return getDay("01-JAN-" + str);
    }

    public static Date getDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
