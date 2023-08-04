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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class EC_numberCheckTest {

  private Feature feature;
  private EC_numberCheck check;
  private EntryDAOUtils entryDAOUtils;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("feature");
    check = new EC_numberCheck();
    entryDAOUtils = createMock(EntryDAOUtils.class);
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoFeature() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifiers() throws ValidationEngineException {
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_NoEcnumber() throws ValidationEngineException {
    feature.setSingleQualifier("qual1");
    feature.setSingleQualifier("qual2");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_invalidEcnumber() throws SQLException, ValidationEngineException {

    feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
    expect(entryDAOUtils.isEcnumberValid("3.6.1.15")).andReturn("N");
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(feature);
    assertTrue(result.isValid());
    assertEquals(1, result.count("EC_numberCheck_1", Severity.WARNING));
  }

  @Test
  public void testCheck_validEcnumber() throws SQLException, ValidationEngineException {
    feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
    expect(entryDAOUtils.isEcnumberValid("3.6.1.15")).andReturn("Y");
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(feature);
    assertTrue(result.isValid());
    assertEquals(0, result.count("EC_numberCheck_1", Severity.WARNING));
  }

  @Test
  public void testCheck_notExistEcnumber() throws SQLException, ValidationEngineException {
    feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
    expect(entryDAOUtils.isEcnumberValid("3.6.1.15")).andReturn(null);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(feature);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EC_numberCheck_2", Severity.ERROR));
  }
}
