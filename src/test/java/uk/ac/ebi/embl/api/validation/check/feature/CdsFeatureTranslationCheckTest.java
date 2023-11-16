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

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class CdsFeatureTranslationCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private CdsFeatureTranslationCheck check;

  @SuppressWarnings("deprecation")
  @Before
  public void setUp() throws SQLException {
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    Sequence sequence =
        sequenceFactory.createSequenceByte(
            "gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgtcactcgg".getBytes());
    entry = entryFactory.createEntry();
    entry.setSequence(sequence);
    TaxonomyClient taxonClient = createMock(TaxonomyClient.class);
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.taxonClient.set(taxonClient);
    check = new CdsFeatureTranslationCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  public void testCheck_NoDataSet() {
    check.setEntry(entry);
    assertTrue(check.check(featureFactory.createFeature(Feature.CDS_FEATURE_NAME)).isValid());
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_EmptyFeature() {
    CdsFeature cdsFeature = featureFactory.createCdsFeature();
    entry.addFeature(cdsFeature);
    check.setEntry(entry);
    ValidationResult validationResult = check.check(cdsFeature);
    assertFalse(validationResult.isValid()); // i.e. there were failures
  }

  @Test
  public void testCheck_NoTranslation() {
    CdsFeature cdsFeature = featureFactory.createCdsFeature();
    LocationFactory locationFactory = new LocationFactory();
    cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 12L));
    entry.setSequence(new SequenceFactory().createSequenceByte("atggagtggtaa".getBytes()));
    entry.addFeature(cdsFeature);

    check.setEntry(entry);
    ValidationResult validationResult = check.check(cdsFeature);
    assertTrue(validationResult.isValid());
    assertEquals("MEW", cdsFeature.getTranslation());
  }

  @Test
  public void testCheck_InvalidLocation() {
    CdsFeature cdsFeature = featureFactory.createCdsFeature();
    LocationFactory locationFactory = new LocationFactory();
    cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(12L, 1L));
    entry.setSequence(new SequenceFactory().createSequenceByte("actgactgactgactg".getBytes()));
    entry.addFeature(cdsFeature);
    check.setEntry(entry);
    ValidationResult validationResult = check.check(cdsFeature);
    assertEquals(1, validationResult.count("Translator-19", Severity.ERROR));
    assertEquals(1, validationResult.count(Severity.ERROR));
    assertFalse(validationResult.isValid());
    assertNull(cdsFeature.getTranslation());
  }
}
