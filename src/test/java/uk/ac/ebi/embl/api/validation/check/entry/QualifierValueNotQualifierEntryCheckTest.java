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

public class QualifierValueNotQualifierEntryCheckTest {

  private Entry entry;
  private Feature feature1, feature2;
  private Feature feature;
  private QualifierValueNotQualifierEntryCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    FeatureFactory featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    feature1 = featureFactory.createFeature("gene");
    feature2 = featureFactory.createFeature("LTR");
    entry.addFeature(feature1);
    entry.addFeature(feature2);

    DataRow dataRow = new DataRow("organelle", "gene", "5.8S rRNA");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_VALUE_NOT_QUALIFIER_ENTRY, dataRow);
    check = new QualifierValueNotQualifierEntryCheck();
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
  public void testCheck_NoOrganelle() {
    feature1.setSingleQualifierValue("gene", "5.8S rRNA");
    feature2.setSingleQualifierValue("sub_clone", "value");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("QualifierValueNotQualifierEntryCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_Organelle() {
    feature1.setSingleQualifierValue("gene", "5.8S rRNA");
    feature2.setSingleQualifierValue("organelle", "value");
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("QualifierValueNotQualifierEntryCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_NoGene() {
    feature1.setSingleQualifierValue("sub_clone", "value");
    feature2.setSingleQualifierValue("organelle", "value");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("QualifierValueNotQualifierEntryCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_NoGeneValue() {
    feature1.setSingleQualifierValue("gene", "10S rRNA");
    feature2.setSingleQualifierValue("organelle", "value");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("QualifierValueNotQualifierEntryCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_Message() {
    feature1.setSingleQualifierValue("gene", "5.8S rRNA");
    feature2.setSingleQualifierValue("organelle", "value");

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("QualifierValueNotQualifierEntryCheck", Severity.ERROR);
    assertEquals(
        "Qualifier \"organelle\" must not exist when qualifier \"gene\" has value \"5.8S rRNA\" in any feature.",
        messages.iterator().next().getMessage());
  }
}
