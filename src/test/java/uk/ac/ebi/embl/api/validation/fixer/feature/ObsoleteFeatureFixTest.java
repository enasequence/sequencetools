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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ObsoleteFeatureFixTest {

  private ObsoleteFeatureFix check;
  public FeatureFactory featureFactory;
  public LocationFactory locationFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    check = new ObsoleteFeatureFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_Swap() {
    Feature feature = featureFactory.createFeature("scRNA");
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("ObsoleteFeatureFix", Severity.FIX));
    assertTrue(feature.getName().equals("ncRNA"));
    assertTrue(SequenceEntryUtils.isQualifierAvailable("ncRNA_class", feature));
    assertTrue(
        SequenceEntryUtils.isQualifierWithValueAvailable(
            "ncRNA_class", "scRNA, snRNA or snoRNA", feature));
  }
}
