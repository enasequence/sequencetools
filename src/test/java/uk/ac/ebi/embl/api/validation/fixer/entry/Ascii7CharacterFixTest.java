package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.*;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Ascii7CharacterFixTest {

    private final static String TEXT = "This is a multi-line comment with tabs  Šťŕĭńġ\n���";
    private final static String FIXED_TEXT = "This is a multi-line comment with tabs  String\n???";
    private final Ascii7CharacterFix fix = new Ascii7CharacterFix();

    private Entry entry;
    private Reference reference;
    private Person person;

    @Before
    public void setUp() throws SQLException
    {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        fix.setEmblEntryValidationPlanProperty(property);

        EntryFactory entryFactory = new EntryFactory();
        entry = entryFactory.createEntry();

        ReferenceFactory referenceFactory = new ReferenceFactory();
        reference = referenceFactory.createReference();
        entry.addReference(reference);

        Submission submission = referenceFactory.createSubmission(referenceFactory.createPublication());
        reference.setPublication(submission);

        person = referenceFactory.createPerson();
        reference.getPublication().addAuthor(person);
    }

    @Test
    public void testFixedDescription() {
        entry.setDescription(new Text(TEXT));
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, entry.getDescription().getText());
    }

    @Test
    public void testFixedComment() {
        entry.setComment(new Text(TEXT));
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, entry.getComment().getText());
     }

    @Test
    public void testFixedTitle() {
        reference.getPublication().setTitle(TEXT);
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, reference.getPublication().getTitle());
    }

    @Test
    public void testFixedFirstName()
    {
        person.setFirstName(TEXT);
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, person.getFirstName());
    }
    @Test
    public void testFixedSurName()
    {
        person.setSurname(TEXT);
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, person.getSurname());
    }

    @Test
    public void testFixedCountryQualifier()
    {
        Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
        feature.addQualifier((new QualifierFactory()).createQualifier(Qualifier.COUNTRY_QUALIFIER_NAME, TEXT));
        entry.addFeature(feature);
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, feature.getSingleQualifier(Qualifier.COUNTRY_QUALIFIER_NAME).getValue());

    }

    @Test
    public void testFixedIsolateQualifier()
    {
        Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
        feature.addQualifier((new QualifierFactory()).createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, TEXT));
        entry.addFeature(feature);
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, feature.getSingleQualifier(Qualifier.ISOLATE_QUALIFIER_NAME).getValue());
    }
}
