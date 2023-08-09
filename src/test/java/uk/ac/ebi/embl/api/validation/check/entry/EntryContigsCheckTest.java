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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class EntryContigsCheckTest {

  private Entry entry;
  private LocationFactory locationFactory;
  private EntryContigsCheck check;
  private EntryDAOUtils entryDAOUtils;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    locationFactory = new LocationFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequence());
    RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00002", 1, 10L, 20L);
    RemoteRange remoteRange3 = locationFactory.createRemoteRange("A00003", 1, 10L, 20L);
    Gap gap1 = locationFactory.createGap(10);
    Gap gap2 = locationFactory.createGap(10);
    entry.getSequence().addContig(remoteRange1);
    entry.getSequence().addContig(gap1);
    entry.getSequence().addContig(remoteRange2);
    entry.getSequence().addContig(gap2);
    entry.getSequence().addContig(remoteRange3);
    check = new EntryContigsCheck();
    EmblEntryValidationPlanProperty planProperty = new EmblEntryValidationPlanProperty();
    entryDAOUtils = createMock(EntryDAOUtils.class);
    check.setEmblEntryValidationPlanProperty(planProperty);
  }

  @Test
  public void testCheck_withNoDatabaseConnection() throws ValidationEngineException {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testcheck_withDatabaseConnectionandvalidContigs()
      throws SQLException, ValidationEngineException {
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("ContigEntryCheck-1", Severity.ERROR));
  }

  @Test
  public void testcheck_withDatabaseConnectionandInvalidContigs_1()
      throws SQLException, ValidationEngineException {
    LocalRange localRange = locationFactory.createLocalRange(10L, 20L);
    entry.getSequence().addContig(localRange);
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("ContigEntryCheck-3", Severity.ERROR));
  }

  @Test
  public void testcheck_withDatabaseConnectionandInvalidContigs_2()
      throws SQLException, ValidationEngineException {
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(false);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("ContigEntryCheck-1", Severity.ERROR));
  }

  @Test
  public void testcheck_withDatabaseConnectionandvalidComplementContigs()
      throws SQLException, ValidationEngineException {
    RemoteRange remoteRange4 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    remoteRange4.setComplement(true);
    entry.getSequence().addContig(remoteRange4);
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("ContigEntryCheck-1", Severity.ERROR));
  }

  @Test
  public void testcheck_withDatabaseConnectionandvalidContigLocation()
      throws SQLException, ValidationEngineException {
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("ContigEntryCheck-2", Severity.ERROR));
  }

  @Test
  public void testcheck_withDatabaseConnectionandInvalidContigLocation()
      throws SQLException, ValidationEngineException {
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(10L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("ContigEntryCheck-2", Severity.ERROR));
  }

  @Test
  public void testcheck_withpairedEndGapatEndofCOline()
      throws SQLException, ValidationEngineException {
    Gap gap3 = locationFactory.createGap(10);
    entry.getSequence().addContig(gap3);
    FeatureFactory featureFactory = new FeatureFactory();
    Feature assemblyGapFeature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    assemblyGapFeature.addQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, "paired-ends");
    assemblyGapFeature.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME, "within-scaffold");
    assemblyGapFeature.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "11");
    entry.addFeature(assemblyGapFeature);
    Order<Location> order = new Order<Location>();
    order.addLocation(locationFactory.createLocalRange(53L, 63L));
    assemblyGapFeature.setLocations(order);
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("ContigEntryCheck-4", Severity.ERROR));
  }

  @Test
  public void testcheck_withpairedEndGapatMiddleofSequence()
      throws SQLException, ValidationEngineException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature assemblyGapFeature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    assemblyGapFeature.addQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, "paired-ends");
    assemblyGapFeature.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME, "within-scaffold");
    assemblyGapFeature.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "11");
    entry.addFeature(assemblyGapFeature);
    Order<Location> order = new Order<Location>();
    order.addLocation(locationFactory.createLocalRange(12L, 22L));
    assemblyGapFeature.setLocations(order);
    expect(entryDAOUtils.isEntryExists("A00001.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00002.1")).andReturn(true);
    expect(entryDAOUtils.isEntryExists("A00003.1")).andReturn(true);
    expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00002.1")).andReturn(30L);
    expect(entryDAOUtils.getSequenceLength("A00003.1")).andReturn(30L);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }
}
