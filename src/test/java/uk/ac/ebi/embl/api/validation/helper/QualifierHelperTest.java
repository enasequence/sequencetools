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
package uk.ac.ebi.embl.api.validation.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class QualifierHelperTest {

  @Test
  public void testCheckRegExProteinId() {
    Map<String, QualifierHelper.QualifierInfo> qualifierMap = QualifierHelper.getQualifierMap();

    QualifierHelper.QualifierInfo qualifierInfo = qualifierMap.get("protein_id");

    assertTrue(checkRegEx(qualifierInfo, "protein_id", "AAA12345.1").isValid());
    assertTrue(checkRegEx(qualifierInfo, "protein_id", "AAA1234567.1").isValid());
    assertFalse(checkRegEx(qualifierInfo, "protein_id", "AAA123456.1").isValid());
    assertFalse(checkRegEx(qualifierInfo, "protein_id", "AAA1234.1").isValid());
    assertFalse(checkRegEx(qualifierInfo, "protein_id", "invalid").isValid());
  }

  private ValidationResult checkRegEx(
      QualifierHelper.QualifierInfo qualifierInfo, String name, String value) {
    QualifierFactory qualifierFactory = new QualifierFactory();
    return QualifierHelper.checkRegEx(qualifierInfo, qualifierFactory.createQualifier(name, value));
  }
}
