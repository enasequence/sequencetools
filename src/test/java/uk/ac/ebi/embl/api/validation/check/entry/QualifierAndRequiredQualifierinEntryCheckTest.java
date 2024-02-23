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

public class QualifierAndRequiredQualifierinEntryCheckTest {

  private Entry entry;
  private EntryFactory entryFactory;
  private FeatureFactory featureFactory;
  private QualifierAndRequiredQualifierinEntryCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    DataRow dataRow = new DataRow("map,PCR_primers", "satellite");

    GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_REQUIRED_QUALIFIER_IN_ENTRY, dataRow);
    check = new QualifierAndRequiredQualifierinEntryCheck();
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
  public void testCheck_NoQualifiers() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoRequiredQualifiers() {
    Feature feature1 = featureFactory.createFeature("feature1");
    Feature feature2 = featureFactory.createFeature("feature2");
    feature1.setSingleQualifier("satellite");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("QualifierAndRequiredQualifierinEntryCheck2", Severity.ERROR));
  }

  @Test
  public void testCheck_NoSatelite() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.setSingleQualifier("chromosome");
    entry.addFeature(feature1);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_OnlyOneOfRequired() {
    Feature feature1 = featureFactory.createFeature("feature1");
    Feature feature2 = featureFactory.createFeature("feature2");
    feature1.setSingleQualifier("satelite");
    feature2.setSingleQualifier("PCR_primers");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_AllRequired() {
    Feature feature1 = featureFactory.createFeature("feature1");
    Feature feature2 = featureFactory.createFeature("feature2");
    feature1.setSingleQualifier("PCR_primers");
    feature2.setSingleQualifier("map");
    feature1.setSingleQualifier("satellite");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_Message() {
    Feature feature1 = featureFactory.createFeature("feature1");
    Feature feature2 = featureFactory.createFeature("feature2");
    feature1.setSingleQualifier("satellite");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("QualifierAndRequiredQualifierinEntryCheck2", Severity.ERROR);
    assertEquals(
        "One of qualifiers \"map, PCR_primers\" must exist when qualifier \"satellite\" exists in any feature.",
        messages.iterator().next().getMessage());
  }
}
