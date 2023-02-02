package uk.ac.ebi.embl.api.validation.check.entries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class NonUniqueSubmitterAccessionCheckTest {
    private final NonUniqueSubmitterAccessionCheck check = new NonUniqueSubmitterAccessionCheck();

    @Test
    public void testCheck_NullEntryList() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_EmptyEntryList() {
        ArrayList<Entry> entryList = new ArrayList<>();
        ValidationResult result = check.check(entryList);
        assertTrue(result.isValid());
    }

    @Test
    public void testCheck_NoSubmitterAccession() {
        ArrayList<Entry> entryList = new ArrayList<>();
        EntryFactory entryFactory = new EntryFactory();
        Entry entry1 = entryFactory.createEntry();
        Entry entry2 = entryFactory.createEntry();
        entryList.add(entry1);
        entryList.add(entry2);
        ValidationResult result = check.check(entryList);
        assertTrue(result.isValid());
    }

    @Test
    public void testCheck_duplicateSubmitterAccession() {
        ArrayList<Entry> entryList = new ArrayList<>();
        EntryFactory entryFactory = new EntryFactory();
        Entry entry1 = entryFactory.createEntry();
        Entry entry2 = entryFactory.createEntry();
        entry1.setSubmitterAccession("test1");
        entry2.setSubmitterAccession("test1");
        entryList.add(entry1);
        entryList.add(entry2);
        ValidationResult result = check.check(entryList);
        assertTrue(!result.isValid());
        assertEquals(1, result.count("NonUniqueSubmitterAccessionCheck", Severity.ERROR));
    }

    @Test
    public void testCheck_uniqueSubmitterAccession() {
        ArrayList<Entry> entryList = new ArrayList<>();
        EntryFactory entryFactory = new EntryFactory();
        Entry entry1 = entryFactory.createEntry();
        Entry entry2 = entryFactory.createEntry();
        entry1.setSubmitterAccession("test1");
        entry2.setSubmitterAccession("test2");
        entryList.add(entry1);
        entryList.add(entry2);
        ValidationResult result = check.check(entryList);
        assertTrue(result.isValid());
    }
}
