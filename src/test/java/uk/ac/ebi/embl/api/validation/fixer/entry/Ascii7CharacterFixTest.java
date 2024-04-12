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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class Ascii7CharacterFixTest {

    private static final String TEXT = "This is a multi-line comment with tabs  Šťŕĭńġ\n���";
    private static final String FIXED_TEXT = "This is a multi-line comment with tabs  String\n???";
    private final Ascii7CharacterFix fix = new Ascii7CharacterFix();
    private EntryFactory entryFactory = new EntryFactory();
    private ReferenceFactory referenceFactory = new ReferenceFactory();

    @Before
    public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
        fix.setEmblEntryValidationPlanProperty(property);
    }

    private Entry entry(Publication publication) {
        Entry entry = entryFactory.createEntry();
        Reference reference = referenceFactory.createReference();
        entry.addReference(reference);
        reference.setPublication(publication);
        return entry;
    }

    private static class GetterAndSetter<T> {
        public final Supplier<T> getter;
        public final Consumer<T> setter;

        private GetterAndSetter(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public static <T> GetterAndSetter<T> of(Supplier<T> getter, Consumer<T> setter) {
            return new GetterAndSetter<>(getter, setter);
        }
    }

    private void testString(Entry entry, GetterAndSetter<String>... list) {
        for (GetterAndSetter<String> gs : list) {
            gs.setter.accept(TEXT);
        }
        ValidationResult result = fix.check(entry);
        assertEquals(list.length, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        for (GetterAndSetter<String> gs : list) {
            assertEquals(FIXED_TEXT, gs.getter.get());
        }
    }

    private void testText(Entry entry, Supplier<Text> getter, Consumer<Text> setter) {
        setter.accept(new Text(TEXT));
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
        assertTrue(result.isValid());
        assertEquals(FIXED_TEXT, getter.get().getText());
    }

    @Test
    public void testDescription() {
        Entry entry = entryFactory.createEntry();
        testText(entry, entry::getDescription, entry::setDescription);
    }

    @Test
    public void testComment() {
        Entry entry = entryFactory.createEntry();
        testText(entry, entry::getComment, entry::setComment);
    }

    @Test
    public void testQualifier() {
        for (String featureName : Arrays.asList(Feature.SOURCE_FEATURE_NAME, Feature.CDS_FEATURE_NAME)) {
            for (String qualifierName : Arrays.asList(Qualifier.NOTE_QUALIFIER_NAME)) {
                Entry entry = entryFactory.createEntry();
                Feature feature = (new FeatureFactory()).createFeature(featureName);
                Qualifier qualifier = (new QualifierFactory()).createQualifier(qualifierName);
                feature.addQualifier(qualifier);
                entry.addFeature(feature);
                testString(entry, GetterAndSetter.of(qualifier::getValue, qualifier::setValue));
            }
        }
    }

    @Test
    public void testPublication() {
        Publication publication = new Publication();
        Entry entry = entry(publication);
        Person person = referenceFactory.createPerson();
        publication.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(publication::getTitle, publication::setTitle),
                GetterAndSetter.of(publication::getConsortium, publication::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname));
    }

    @Test
    public void testArticle() {
        Article article = referenceFactory.createArticle();
        Entry entry = entry(article);
        Person person = referenceFactory.createPerson();
        article.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(article::getTitle, article::setTitle),
                GetterAndSetter.of(article::getConsortium, article::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(article::getJournal, article::setJournal),
                GetterAndSetter.of(article::getVolume, article::setVolume),
                GetterAndSetter.of(article::getIssue, article::setIssue),
                GetterAndSetter.of(article::getFirstPage, article::setFirstPage),
                GetterAndSetter.of(article::getLastPage, article::setLastPage));
    }

    @Test
    public void testBook() {
        Book book = referenceFactory.createBook();
        Entry entry = entry(book);
        Person person = referenceFactory.createPerson();
        book.addAuthor(person);
        Person editor = referenceFactory.createPerson();
        book.addEditor(editor);
        testString(entry,
                GetterAndSetter.of(book::getTitle, book::setTitle),
                GetterAndSetter.of(book::getConsortium, book::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(book::getBookTitle, book::setBookTitle),
                GetterAndSetter.of(book::getPublisher, book::setPublisher),
                GetterAndSetter.of(book::getFirstPage, book::setFirstPage),
                GetterAndSetter.of(book::getLastPage, book::setLastPage),
                GetterAndSetter.of(editor::getFirstName, editor::setFirstName),
                GetterAndSetter.of(editor::getSurname, editor::setSurname));
    }

    @Test
    public void testElectronicReference() {
        ElectronicReference electronicReference = referenceFactory.createElectronicReference();
        Entry entry = entry(electronicReference);
        Person person = referenceFactory.createPerson();
        electronicReference.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(electronicReference::getTitle, electronicReference::setTitle),
                GetterAndSetter.of(electronicReference::getConsortium, electronicReference::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(electronicReference::getText, electronicReference::setText));
    }

    @Test
    public void testPatent() {
        Patent patent = referenceFactory.createPatent();
        Entry entry = entry(patent);
        Person person = referenceFactory.createPerson();
        patent.addAuthor(person);
        patent.addApplicant("");
        testString(entry,
                GetterAndSetter.of(patent::getTitle, patent::setTitle),
                GetterAndSetter.of(patent::getConsortium, patent::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(patent::getPatentOffice, patent::setPatentOffice),
                GetterAndSetter.of(patent::getPatentNumber, patent::setPatentNumber),
                GetterAndSetter.of(patent::getPatentType, patent::setPatentType),
                GetterAndSetter.of(() -> patent.getApplicants().get(0), (str) -> patent.getApplicants().set(0, str))
        );
    }

    @Test
    public void testSubmission() {
        Submission submission = referenceFactory.createSubmission();
        Entry entry = entry(submission);
        Person person = referenceFactory.createPerson();
        submission.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(submission::getTitle, submission::setTitle),
                GetterAndSetter.of(submission::getConsortium, submission::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(submission::getSubmitterAddress, submission::setSubmitterAddress));
    }

    @Test
    public void testThesis() {
        Thesis thesis = referenceFactory.createThesis();
        Entry entry = entry(thesis);
        Person person = referenceFactory.createPerson();
        thesis.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(thesis::getTitle, thesis::setTitle),
                GetterAndSetter.of(thesis::getConsortium, thesis::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(thesis::getInstitute, thesis::setInstitute));
    }

    @Test
    public void testUnpublished() {
        Unpublished unpublished = referenceFactory.createUnpublished();
        Entry entry = entry(unpublished);
        Person person = referenceFactory.createPerson();
        unpublished.addAuthor(person);
        testString(entry,
                GetterAndSetter.of(unpublished::getTitle, unpublished::setTitle),
                GetterAndSetter.of(unpublished::getConsortium, unpublished::setConsortium),
                GetterAndSetter.of(person::getFirstName, person::setFirstName),
                GetterAndSetter.of(person::getSurname, person::setSurname),
                GetterAndSetter.of(unpublished::getJournalBlock, unpublished::setJournalBlock));
    }
}
