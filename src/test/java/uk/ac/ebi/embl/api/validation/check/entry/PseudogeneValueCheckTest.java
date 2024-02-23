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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class PseudogeneValueCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private QualifierFactory qualifierFactory;
  private PseudogeneValueCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();

    entry = entryFactory.createEntry();

    check = new PseudogeneValueCheck();
  }

  @Test
  public void checkNullValue() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, null));
    entry.addFeature(feature1);

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(
        1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
  }

  @Test
  public void checkInvalidValue() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature1);

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(
        1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
  }

  @Test
  public void checkValidValueWithSingleQuote() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "'unknown'"));
    entry.addFeature(feature1);

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(
        1, result.count(PseudogeneValueCheck.PSEUDOGENE_INVALID_VALUE_CHECK, Severity.ERROR));
  }

  @Test
  public void checkValidValue() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "unknown"));
    entry.addFeature(feature1);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count());
  }
}
