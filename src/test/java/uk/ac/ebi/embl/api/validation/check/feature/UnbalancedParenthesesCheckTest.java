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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.validation.*;

public class UnbalancedParenthesesCheckTest {

  private Feature feature;
  private UnbalancedParenthesesCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();

    feature = featureFactory.createFeature("feature");

    check = new UnbalancedParenthesesCheck();
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
  public void testCheck_QualifierNoParentheses() {

    feature.addQualifier("gene", "Salmonella enterica subsp. enterica");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_SingleQualifierUnbalancedParentheses() {
    feature.addQualifier("gene", "Salmonella enterica subsp.[ enterica)");
    ValidationResult result = check.check(feature);

    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("UnbalancedParenthesesCheck_1", Severity.WARNING);

    assertEquals(1, messages.size());
  }

  @Test
  public void testCheck_MultipleQualifierUnbalancedParentheses() {

    feature.addQualifier("gene", "Salmonella enterica subsp.[ enterica)");
    feature.addQualifier("organism", "Salmonella [enterica subsp.[ enterica))");
    feature.addQualifier("gene", "Salmonella (enterica) subsp.[ enterica)");
    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    /* please mention the number as the number of unbalanced right and left
    parentheses(number of different types in one qualifier value) exists*/
    assertEquals(7, messages.size());
  }

  @Test
  public void testCheck_qualifierValidParantheses() {
    feature.addQualifier("gene", "Salmonella enterica subsp.(enterica)");
    feature.addQualifier("organism", "Salmonella [enterica subsp.[ enterica]]");
    ValidationResult result = check.check(feature);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    assertEquals(0, messages.size());
  }
}
