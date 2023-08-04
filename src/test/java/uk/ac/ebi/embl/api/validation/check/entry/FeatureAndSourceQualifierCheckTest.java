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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class FeatureAndSourceQualifierCheckTest {

  private Entry entry;
  private Feature source;
  private FeatureFactory featureFactory;
  private FeatureAndSourceQualifierCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();

    entry = entryFactory.createEntry();
    source = featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
    entry.addFeature(source);

    DataRow dataRow = new DataRow("map", "STS");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.FEATURE_SOURCE_QUALIFIER, dataRow);
    check = new FeatureAndSourceQualifierCheck();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifier() {
    entry.addFeature(featureFactory.createFeature("STS"));

    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("FeatureAndSourceQualifierCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_WrongQualifier() {
    source.setSingleQualifier("max");
    entry.addFeature(featureFactory.createFeature("STS"));

    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("FeatureAndSourceQualifierCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_Valid() {
    source.setSingleQualifier("map");
    entry.addFeature(featureFactory.createFeature("STS"));

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_MultipleSource() {
    source.setSingleQualifier("map");
    Feature source2 = featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
    entry.addFeature(source2);
    entry.addFeature(featureFactory.createFeature("STS"));

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoFeature() {
    source.setSingleQualifier("map");

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_WrongFeature() {
    source.setSingleQualifier("map");
    entry.addFeature(featureFactory.createFeature("STX"));

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_Message() {
    entry.addFeature(featureFactory.createFeature("STS"));

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("FeatureAndSourceQualifierCheck-1", Severity.ERROR);
    assertEquals(
        "Source qualifier map is required when feature STS exists.",
        messages.iterator().next().getMessage());
  }
}
