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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class ExclusiveQualifiersCheckTest {

  private Feature feature;
  private ExclusiveQualifiersCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");

    DataRow dataRow1 = new DataRow("proviral", "virion");
    DataRow dataRow2 = new DataRow("pseudo", "product");

    GlobalDataSets.addTestDataSet(GlobalDataSetFile.EXCLUSIVE_QUALIFIERS, dataRow1, dataRow2);
    check = new ExclusiveQualifiersCheck();
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
  public void testCheck_OtherQualifier() {
    feature.setSingleQualifier("qual1");
    feature.setSingleQualifier("qual2");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_OnlyFirst() {
    feature.setSingleQualifier("proviral");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_OnlySecond() {
    feature.setSingleQualifier("virion");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_2Firsts() {
    feature.addQualifier("proviral");
    feature.addQualifier("proviral");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_BothQualifiers() {
    feature.setSingleQualifier("proviral");
    feature.setSingleQualifier("virion");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("ExclusiveQualifiersCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_pseudoQualifier() {
    feature.setSingleQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
    feature.setSingleQualifier(Qualifier.PRODUCT_QUALIFIER_NAME);
    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("ExclusiveQualifiersCheck2", Severity.WARNING);
    assertEquals(1, messages.size());
    assertEquals(
        "Feature annotated with pseudo should not contain a product qualifier, please add the product qualifier value to a note qualifier or add a comment for curator.",
        messages.iterator().next().getMessage());
  }

  @Test
  public void testCheck_Message() {
    feature.setSingleQualifier("proviral");
    feature.setSingleQualifier("virion");

    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("ExclusiveQualifiersCheck1", Severity.ERROR);
    assertEquals(
        "Qualifiers proviral and virion cannot exist together.",
        messages.iterator().next().getMessage());
  }
}
