package uk.ac.ebi.embl.flatfile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class FlatFileDateUtilsTest {
    public static final String[] DAYS_AS_STRING = {
            "06-JAN-1971",
            "02-OCT-1972",
            "22-SEP-2016",
            "10-NOV-2022"
    };

    public static final Date[] DAYS_AS_DATE = {
            FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 6)),
            FlatFileDateUtils.getDate(LocalDate.of(1972, 10, 2)),
            FlatFileDateUtils.getDate(LocalDate.of(2016, 9, 22)),
            FlatFileDateUtils.getDate(LocalDate.of(2022, 11, 10))
    };

    public static final String[] YEARS_AS_STRING = {
            "1971",
            "1972",
            "2016",
            "2022"
    };

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
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.getTime());
    }

    @Test
    public void testGetLocalDate() {
        Date date = new GregorianCalendar(1971, 1, 6).getTime();
        LocalDate localDate = FlatFileDateUtils.getLocalDate(date);
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.getTime());
    }

    @Test
    public void testGetLocalDateFromSqlDate() {
        Date sqlDate = new java.sql.Date(FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 1)).getTime());
        LocalDate localDate = FlatFileDateUtils.getLocalDate(sqlDate);
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), sqlDate.getTime());
    }
}
