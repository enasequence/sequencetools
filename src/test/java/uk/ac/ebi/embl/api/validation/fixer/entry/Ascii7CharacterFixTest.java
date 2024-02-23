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
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class Ascii7CharacterFixTest {

  private static final String TEXT = "This is a multi-line comment with tabs  Šťŕĭńġ\n���";
  private static final String FIXED_TEXT = "This is a multi-line comment with tabs  String\n???";
  private final Ascii7CharacterFix fix = new Ascii7CharacterFix();

  private Entry entry;
  private Reference reference;
  private Person person;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
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
  public void testFixedFirstName() {
    person.setFirstName(TEXT);
    ValidationResult result = fix.check(entry);
    assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
    assertTrue(result.isValid());
    assertEquals(FIXED_TEXT, person.getFirstName());
  }

  @Test
  public void testFixedSurName() {
    person.setSurname(TEXT);
    ValidationResult result = fix.check(entry);
    assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
    assertTrue(result.isValid());
    assertEquals(FIXED_TEXT, person.getSurname());
  }

  @Test
  public void testFixedCountryQualifier() {
    Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
    feature.addQualifier(
        (new QualifierFactory()).createQualifier(Qualifier.COUNTRY_QUALIFIER_NAME, TEXT));
    entry.addFeature(feature);
    ValidationResult result = fix.check(entry);
    assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
    assertTrue(result.isValid());
    assertEquals(
        FIXED_TEXT, feature.getSingleQualifier(Qualifier.COUNTRY_QUALIFIER_NAME).getValue());
  }

  @Test
  public void testFixedIsolateQualifier() {
    Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
    feature.addQualifier(
        (new QualifierFactory()).createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, TEXT));
    entry.addFeature(feature);
    ValidationResult result = fix.check(entry);
    assertEquals(1, result.count("Ascii7CharacterFix_1", Severity.FIX));
    assertTrue(result.isValid());
    assertEquals(
        FIXED_TEXT, feature.getSingleQualifier(Qualifier.ISOLATE_QUALIFIER_NAME).getValue());
  }
}
