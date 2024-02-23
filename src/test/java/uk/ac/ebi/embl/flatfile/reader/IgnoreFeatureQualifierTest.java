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
package uk.ac.ebi.embl.flatfile.reader;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class IgnoreFeatureQualifierTest extends TestCase {

  @Test
  public void testIsIgnoreWithIgnorableQualifiers() {

    for (String missingQualifierValue : IgnoreFeatureQualifier.MISSING_VALUE_TERMS) {
      assertTrue(
          IgnoreFeatureQualifier.isIgnore(
              Feature.SOURCE_FEATURE_NAME,
              Qualifier.COUNTRY_QUALIFIER_NAME,
              missingQualifierValue));
      assertTrue(
          IgnoreFeatureQualifier.isIgnore(
              Feature.SOURCE_FEATURE_NAME,
              Qualifier.COLLECTION_DATE_QUALIFIER_NAME,
              missingQualifierValue));
      assertTrue(
          IgnoreFeatureQualifier.isIgnore(
              Feature.SOURCE_FEATURE_NAME,
              Qualifier.LAT_LON_QUALIFIER_NAME,
              missingQualifierValue));
    }
    assertTrue(
        IgnoreFeatureQualifier.isIgnore(
            Feature.REPEAT_REGION, Qualifier.LOCUS_TAG_QUALIFIER_NAME, "BN5_00001"));
  }

  @Test
  public void testIsIgnoreWithNotIgnorableQualifiers() {

    assertFalse(
        IgnoreFeatureQualifier.isIgnore(
            Feature.SOURCE_FEATURE_NAME, Qualifier.COUNTRY_QUALIFIER_NAME, "India"));
    assertFalse(
        IgnoreFeatureQualifier.isIgnore(
            Feature.SOURCE_FEATURE_NAME, Qualifier.COLLECTION_DATE_QUALIFIER_NAME, "18-May-2023"));
    assertFalse(
        IgnoreFeatureQualifier.isIgnore(
            Feature.SOURCE_FEATURE_NAME,
            Qualifier.LAT_LON_QUALIFIER_NAME,
            "6.385667 N 162.334778 W"));
    assertFalse(
        IgnoreFeatureQualifier.isIgnore(
            Feature.REPEAT_REGION, Qualifier.NOTE_QUALIFIER_NAME, "BN5_00005"));
  }
}
