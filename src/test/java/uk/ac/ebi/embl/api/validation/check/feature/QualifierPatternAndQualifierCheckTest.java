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
package uk.ac.ebi.embl.api.validation.check.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierPatternAndQualifierCheckTest {

  private Feature feature;
  private QualifierPatternAndQualifierCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");

    DataRow dataRow = new DataRow("proviral", "note", ".*endogenous retrovirus$");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_PATTERN_QUALIFIER, dataRow);
    check = new QualifierPatternAndQualifierCheck();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifiers() {
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_NoNote() {
    feature.setSingleQualifier("proviral");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_NoMacronuclear() {
    feature.setSingleQualifierValue("note", "X endogenous retrovirus");
    feature.setSingleQualifier("proviral");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_NoNoteValue() {
    feature.setSingleQualifier("note");
    feature.setSingleQualifier("proviral");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck() {
    feature.setSingleQualifierValue("note", "X endogenous retrovirus");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("QualifierPatternAndQualifierCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_Message() {
    feature.setSingleQualifierValue("note", "X endogenous retrovirus");

    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("QualifierPatternAndQualifierCheck", Severity.ERROR);
    assertEquals(
        "Qualifier proviral must exist when qualifier note value matches the pattern .*endogenous retrovirus$.",
        messages.iterator().next().getMessage());
  }
}
