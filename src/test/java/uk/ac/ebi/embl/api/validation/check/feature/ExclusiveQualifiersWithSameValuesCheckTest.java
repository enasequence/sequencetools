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

public class ExclusiveQualifiersWithSameValuesCheckTest {

  private Feature feature;
  private ExclusiveQualifiersWithSameValueCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");

    DataRow dataRow = new DataRow("clone", "sub_clone");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.EXCLUSIVE_QUALIFIERS_SAME_VALUE, dataRow);

    check = new ExclusiveQualifiersWithSameValueCheck();
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
    feature.setSingleQualifierValue("qual1", "value");
    feature.setSingleQualifierValue("qual2", "value");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_OnlyFirst() {
    feature.setSingleQualifierValue("clone", "value");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_OnlySecond() {
    feature.setSingleQualifierValue("sub_clone", "value");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_2Firsts() {
    feature.addQualifier("clone", "value");
    feature.addQualifier("clone", "value");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_BothQualifiers() {
    feature.addQualifier("clone", "value");
    feature.addQualifier("sub_clone", "value");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("ExclusiveQualifiersWithSameValueCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_BothQualifiers_NullValues() {
    feature.addQualifier("clone");
    feature.addQualifier("sub_clone");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("ExclusiveQualifiersWithSameValueCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_Message() {
    feature.addQualifier("clone", "value");
    feature.addQualifier("sub_clone", "value");

    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("ExclusiveQualifiersWithSameValueCheck", Severity.ERROR);
    assertEquals(
        "Qualifiers clone and sub_clone cannot have the same value.",
        messages.iterator().next().getMessage());
  }
}
