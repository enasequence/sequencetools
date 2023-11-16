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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class ProteinIdRemovalFixTest {

  private Entry entry;
  private Feature cdsFeature;
  private FeatureFactory featureFactory;
  private ProteinIdRemovalFix check;
  private EntryFactory entryFactory;
  private EmblEntryValidationPlanProperty property;
  private EntryDAOUtils entryDAOUtils;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryDAOUtils = createMock(EntryDAOUtils.class);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    check = new ProteinIdRemovalFix();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_noEntry() throws ValidationEngineException {
    ValidationResult validationResult = check.check(null);
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
    assertEquals(0, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_noFeatures() throws ValidationEngineException {
    ValidationResult validationResult = check.check(entryFactory.createEntry());
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
    assertEquals(0, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_noAnalysisIDandAssemblyLevel() throws ValidationEngineException {
    entry.addFeature(cdsFeature);
    ValidationResult validationResult = check.check(entryFactory.createEntry());
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
    assertEquals(0, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_noNewproteinID() throws ValidationEngineException, SQLException {

    entry.addFeature(cdsFeature);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.analysis_id.set("ERZ00001");
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
    assertEquals(0, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_withNewproteinID() throws ValidationEngineException, SQLException {

    cdsFeature.addQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "MCI00001");
    entry.addFeature(cdsFeature);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.analysis_id.set("ERZ00001");
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_withUpdateproteinID() throws ValidationEngineException, SQLException {

    cdsFeature.addQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "MCI00001");
    entry.addFeature(cdsFeature);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.analysis_id.set("ERZ00001");
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }

  @Test
  public void testCheck_nonAssemblyWithproteinID() throws ValidationEngineException, SQLException {

    cdsFeature.addQualifier(Qualifier.PROTEIN_ID_QUALIFIER_NAME, "MCI00001");
    entry.addFeature(cdsFeature);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.analysis_id.set("ERZ00001");
    check.setEmblEntryValidationPlanProperty(property);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.getMessages("ProteinIdRemovalFix_1", Severity.FIX).size());
  }
}
