package uk.ac.ebi.embl.flatfile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class FlatFileDateUtilsTest {
    public static final String[] DATES = {
            "06-JAN-1971",
            "02-OCT-1972",
            "22-SEP-2016",
            "10-NOV-2022"
    };

    public static final Date[] EXPECTED_DATES = {
            FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 6)),
            FlatFileDateUtils.getDate(LocalDate.of(1972, 10, 2)),
            FlatFileDateUtils.getDate(LocalDate.of(2016, 9, 22)),
            FlatFileDateUtils.getDate(LocalDate.of(2022, 11, 10))
    };

    public static final String[] YEARS = {
            "1971",
            "1972",
            "2016",
            "2022"
    };

    public static final Date[] EXPECTED_YEARS = {
            FlatFileDateUtils.getDate(LocalDate.of(1971, 1, 1)),
            FlatFileDateUtils.getDate(LocalDate.of(1972, 1, 1)),
            FlatFileDateUtils.getDate(LocalDate.of(2016, 1, 1)),
            FlatFileDateUtils.getDate(LocalDate.of(2022, 1, 1))
    };

    @Test
    public void testGetDay() {
        for (int i = 0; i < DATES.length; i++) {
            assertEquals(EXPECTED_DATES[i], FlatFileDateUtils.getDay(DATES[i]));
        }
    }

    @Test
    public void testGetYear() {
        for (int i = 0; i < YEARS.length; i++) {
            assertEquals(EXPECTED_YEARS[i], FlatFileDateUtils.getYear(YEARS[i]));
        }
    }

    @Test
    public void testGetDate() {
        LocalDate localDate = LocalDate.of(1971, 1, 6);
        Date date = FlatFileDateUtils.getDate(localDate);
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), date.getTime());
    }
}
