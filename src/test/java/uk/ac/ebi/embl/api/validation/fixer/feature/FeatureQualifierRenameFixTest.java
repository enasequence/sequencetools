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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class FeatureQualifierRenameFixTest {

  private Feature feature;
  private FeatureQualifierRenameFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");
    List<Qualifier> qualifiers = feature.getQualifiers();
    DataRow dataRow1 = new DataRow("organism", "qualifier1");
    DataRow dataRow2 = new DataRow("strain", "qualifier2");
    DataRow dataRow3 = new DataRow("label", "note");
    DataRow dataRow4 = new DataRow("country", "geo_loc_name");

    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_QUALIFIER_RENAME, dataRow1, dataRow2, dataRow3,dataRow4);
    check = new FeatureQualifierRenameFix();
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
  public void testCheck_invalidQualifierName() {
    feature.setSingleQualifier("ghfgjh");

    ValidationResult validationResult = check.check(feature);

    assertEquals(0, validationResult.count("FeatureQualifierRenameFix", Severity.FIX));
  }

  @Test
  public void testCheck_validQualifierName() {

    feature.setSingleQualifier("organism");
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("FeatureQualifierRenameFix", Severity.FIX));
  }

  @Test
  public void testCheck_labelQualifierName() {

    feature.setSingleQualifier(Qualifier.LABEL_QUALIFIER_NAME);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("FeatureQualifierRenameFix", Severity.FIX));
  }

  @Test
  public void testCheck_geo_loc_name_QualifierName() {
    feature.setSingleQualifier(Qualifier.COUNTRY_QUALIFIER_NAME);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("FeatureQualifierRenameFix", Severity.FIX));
  }
}
