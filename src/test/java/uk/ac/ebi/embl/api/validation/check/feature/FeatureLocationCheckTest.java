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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

public class FeatureLocationCheckTest {

  private FeatureLocationCheck check;
  private EntryDAOUtils entryDAOUtils;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    check = new FeatureLocationCheck();
    property = new EmblEntryValidationPlanProperty();
    check.setEntryDAOUtils(entryDAOUtils);
    check.setEmblEntryValidationPlanProperty(property);

    entryDAOUtils = createMock(EntryDAOUtils.class);
    expect(entryDAOUtils.isEntryExists("M12561")).andReturn(true);
  }

  @Test
  public void testCheck_NoFeature() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_noLocation() throws ValidationEngineException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
    ValidationResult result = check.check(intronFeature);
    assertEquals(1, result.count("FeatureLocationCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidOrderLocation() throws ValidationEngineException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
    Order<Location> intronFeatureLocation = new Order<Location>();
    LocationFactory locationFactory = new LocationFactory();
    intronFeatureLocation.addLocation(locationFactory.createLocalRange(120L, 108L));
    intronFeature.setLocations(intronFeatureLocation);
    ValidationResult intronResult = check.check(intronFeature);
    assertEquals(1, intronResult.count("FeatureLocationCheck-3", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidRemoteLocationEMBLScope() throws ValidationEngineException, SQLException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
    LocationFactory locationFactory = new LocationFactory();
    Order<Location> intronFeatureLocation = new Order<Location>();
    intronFeatureLocation.addLocation(locationFactory.createRemoteBase("M12561", 2, 2L));
    intronFeature.setLocations(intronFeatureLocation);
    ValidationResult intronResult = check.check(intronFeature);
    assertEquals(1, intronResult.count("FeatureLocationCheck-5", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidRemoteLocationNCBIScope() throws ValidationEngineException, SQLException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
    LocationFactory locationFactory = new LocationFactory();
    Order<Location> intronFeatureLocation = new Order<Location>();
    intronFeatureLocation.addLocation(locationFactory.createRemoteBase("M12561", 2, 2L));
    intronFeature.setLocations(intronFeatureLocation);
    property.validationScope.set(ValidationScope.NCBI);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult intronResult = check.check(intronFeature);
    assertEquals(0, intronResult.count());
  }
}
