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

public class NonAsciiCharacterFixTest {
    private Entry entry;
    private NonAsciiCharacterFix fixer;
    private Reference reference1;
    private Person person1;

    @Before
    public void setUp() throws SQLException
    {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        EntryFactory entryFactory = new EntryFactory();
        ReferenceFactory referenceFactory = new ReferenceFactory();
        EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
        reference1 = referenceFactory.createReference();
        Submission submission = (new ReferenceFactory()).createSubmission(referenceFactory.createPublication());
        reference1.setPublication(submission);
        person1 = referenceFactory.createPerson();
        entry = entryFactory.createEntry();
        fixer = new NonAsciiCharacterFix();
        fixer.setEmblEntryValidationPlanProperty(property);
    }

    @Test
    public void testCheck_valid()
    {
        ValidationResult result = fixer.check(entry);
        assertTrue(result.isValid());
    }

    @Test
    public void testCheck_fixedComment() {
        entry.setComment(new Text("non-ascii cömment"));
        ValidationResult result = fixer.check(entry);
        assertEquals(1, result.count("AsciiCharacterFix_1", Severity.FIX));
        assertEquals(entry.getComment().getText(), "non-ascii comment"); // ö -> o
    }

    @Test
    public void testCheck_knownNotFixedChar()
    {
        entry.setComment(new Text("unknown character with ���"));
        ValidationResult result = fixer.check(entry);
        assertEquals(0, result.count(Severity.FIX)); // expected: not caught and not possible to fix
        assertTrue(result.isValid());
    }

    @Test
    public void testCheck_fixedPublication()
    {
        reference1.getPublication().setTitle("Sóme title containing non-áscíí");
        person1.setFirstName("Tom");
        person1.setSurname("Ráfáél");
        reference1.getPublication().addAuthor(person1);

        entry.addReference(reference1);
        ValidationResult result = fixer.check(entry);
        assertEquals(2, result.count("AsciiCharacterFix_1", Severity.FIX));
    }

    @Test
    public void testCheck_fixedCountryQualifier()
    {
        Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
        feature.addQualifier((new QualifierFactory()).createQualifier(Qualifier.COUNTRY_QUALIFIER_NAME,"Estonia:Põlva maakond"));
        entry.addFeature(feature);
        ValidationResult result = fixer.check(entry);
        assertEquals(1, result.count("AsciiCharacterFix_1", Severity.FIX));
    }

}
