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

import static org.junit.Assert.*;

import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class NCBIQualifierCheckTest {

  private Feature feature;
  private QualifierCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");

    DataRow dataRow1 =
        new DataRow(
            "collection_date",
            "N",
            "Y",
            "Y",
            "^(?:(?:(\\d{1,2})\\s*(-)){0,1}\\s*(.*)\\s*(-)){0,1}\\s*(\\d{4})$",
            "26",
            "(null)");
    DataRow dataRow2 = new DataRow("focus", "Y", "Y", "N", "(null)", "2", "(null)");
    DataRow dataRow3 = new DataRow("rpt_type", "N", "Y", "N", "^([\\w]+)$", "76", "(null)");
    DataRow dataRow4 =
        new DataRow(
            "lat_lon",
            "N",
            "Y",
            "Y",
            "^\\s*(-{0,1}\\s*\\d{1,6}(\\.\\d{1,6}){0,1})\\s+(S|N)\\s*,{0,1}\\s+(-{0,1}\\s*\\d{1,6}(\\.\\d{1,6}){0,1})\\s+(W|E)\\s*$",
            "22",
            "(null)");
    DataRow dataRow5 =
        new DataRow(
            "protein_id", "N", "Y", "Y", "^\\s*([A-Z]{3}\\d{5})(\\.)(\\d+)\\s*$", "93", "(null)");
    DataRow dataRow6 =
        new DataRow(
            "inference",
            "N",
            "Y",
            "Y",
            "^((COORDINATES|DESCRIPTION|EXISTENCE):)?([^\\:\\(]+)(\\(same\\sspecies\\))?(:.+)?$",
            "89",
            "(null)");
    DataRow dataRow7 = new DataRow("number", "N", "Y", "N", "^[a-zA-Z0-9]+$", "72", "(null)");

    DataRow regexRow = new DataRow("collection_date", "3", "FALSE", "Oct");
    DataRow regexRow2 =
        new DataRow(
            "rpt_type", "1", "TRUE", "tandem,inverted,flanking,terminal,direct,dispersed,other");
    DataRow regexRow3 = new DataRow("lat_lon", "3", "FALSE", "N,S");
    DataRow regexRow4 = new DataRow("lat_lon", "6", "FALSE", "E,W");
    DataRow regexRow5 =
        new DataRow(
            "inference",
            "3",
            "TRUE",
            "ab initio prediction,alignment,non-experimental evidence no additional details recorded,nucleotide motif,profile,protein motif,similar to AA sequence,similar to DNA sequence,similar to RNA sequence,similar to RNA sequence{COM} EST,similar to RNA sequence{COM} mRNA,similar to RNA sequence{COM} other RNA,similar to sequence");

    DataRow artemisRow = new DataRow("color");
    DataRow artemisRow2 = new DataRow("assembly_id");

    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_QUALIFIER_VALUES,
        dataRow1,
        dataRow2,
        dataRow3,
        dataRow4,
        dataRow5,
        dataRow6,
        dataRow7);
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_REGEX_GROUPS,
        regexRow,
        regexRow2,
        regexRow3,
        regexRow4,
        regexRow5);
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.ARTEMIS_QUALIFIERS, artemisRow, artemisRow2);
    check = new QualifierCheck();
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
  public void testCheck_InvalidKey() {
    feature.setSingleQualifier("david");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("QualifierCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_InvalidArtemisKey() {
    feature.setSingleQualifier("color");

    ValidationResult result = check.check(feature);
    assertEquals(1, result.count("QualifierCheck-1", Severity.ERROR));
    assertEquals(
        "If you are using Artemis to create this file, select the 'EMBL submission' format",
        result.getMessages().iterator().next().getCuratorMessage());
  }

  @Test
  public void testCheck_NoFeatureValue() {
    feature.setSingleQualifier("focus");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_NoMandatoryFeatureValue() {
    feature.setSingleQualifier("collection_date");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
  }

  @Test
  public void testCheck_Fine1() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "21-Oct-1952");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_Fine2() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "Oct-1952");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_Fine3() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "1952");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  /* @Test
  public void testCheck_ImpossibleDate1() {
      feature.setSingleQualifier("collection_date");
      feature.setSingleQualifierValue("collection_date", "51-Oct-1952");
      ValidationResult validationResult = check.check(feature);
      assertTrue(!validationResult.isValid());
      assertTrue(validationResult.getMessages("QualifierCheck-6").size() == 1);
  }*/

  @Test
  public void testCheck_RegExFail() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "david");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-3").size() == 1);
  }

  @Test
  public void testCheck_RegExGroupFail() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "21-Bod-1952");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("QualifierCheck-4");
    assertTrue(messages.size() == 1);
  }

  @Test
  public void testCheck_RegExGroupFineCase() {
    feature.setSingleQualifier("rpt_type");
    feature.setSingleQualifierValue("rpt_type", "INVERTED");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_RegExGroupFailCase() {
    feature.setSingleQualifier("collection_date");
    feature.setSingleQualifierValue("collection_date", "21-OCT-1952"); // upper-case month group
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("QualifierCheck-4");
    assertTrue(messages.size() == 1);
  }

  @Test
  public void testCheck_mandatoryQualifier() {
    feature.setSingleQualifier("collection_date");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-2").size() == 1);
  }

  @Test
  public void testCheck_nonMandatoryQualifier() {
    feature.setSingleQualifier("focus");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_invalidLatlonLatitudeValue() {

    feature.setSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME);
    feature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "453534.54656 N 6.13 E");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-7").size() == 1);
  }

  @Test
  public void testCheck_invalidLatlonLongitudeValue() {

    feature.setSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME);
    feature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "6.13 N 453534.54656 E");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-8").size() == 1);
  }

  @Test
  public void testCheck_validLatlonQualifierValue() {

    feature.setSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME);
    feature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "6.13 N 6.13 E");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_validProteinIdVersion() {

    feature.setSingleQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME);
    feature.setSingleQualifierValue(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "CBI84061.1");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_invalidProteinIdVersion() {

    feature.setSingleQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME);
    feature.setSingleQualifierValue(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "CBI84061.0");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-9").size() == 1);
  }

  @Test
  public void testCheck_inferenceValue() {

    feature.addQualifier("inference", "non-experimental evidence, no additional details recorded");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_IntronNumberQualifier_valid1() {
    feature.addQualifier("number", "4");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_IntronNumberQualifier_valid2() {
    feature.addQualifier("number", "6B");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_IntronNumberQualifier_invalid() {
    feature.addQualifier("number", "-");
    ValidationResult validationResult = check.check(feature);
    assertTrue(!validationResult.isValid());
    assertTrue(validationResult.getMessages("QualifierCheck-3").size() == 1);
  }
}
