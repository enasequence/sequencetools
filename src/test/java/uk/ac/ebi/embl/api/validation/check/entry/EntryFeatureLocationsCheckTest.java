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

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class EntryFeatureLocationsCheckTest {

  private Entry entry;
  private EntryFeatureLocationCheck check;

  @Before
  public void setUp() {
    EntryFactory entryFactory = new EntryFactory();
    FeatureFactory featureFactory = new FeatureFactory();
    LocationFactory locationFactory = new LocationFactory();

    entry = entryFactory.createEntry();

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1l, 10l));
    locationJoin.addLocation(locationFactory.createLocalRange(15l, 25l));
    locationJoin.addLocation(locationFactory.createLocalRange(30l, 40l));
    Feature feature = featureFactory.createFeature("1");
    feature.setLocations(locationJoin);

    entry.addFeature(feature);

    entry.setSequence(
        new SequenceFactory()
            .createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes()));

    check = new EntryFeatureLocationCheck();
  }

  public void testCheck_NoSequence() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  public void testCheck_NoFeature() {
    entry.clearFeatures();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheckLocationTooLong() {
    entry.getFeatures().get(0).getLocations().getLocations().get(2).setEndPosition(150l);
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(1, validationResult.count("EntryFeatureLocationCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_Fine() {
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }
}
