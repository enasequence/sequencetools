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

import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class MasterSourceQualifierValidator {
  private final Map<String, FeatureValidationCheck> validation = new HashMap<>();

  public MasterSourceQualifierValidator() {
    validation.put(Qualifier.COLLECTION_DATE_QUALIFIER_NAME, new CollectionDateQualifierCheck());
  }

  public boolean isValid(String qualifier, String value) {
    try {
      if (validation.get(qualifier) != null) {
        return validation.get(qualifier).isValid(value);
      } else {
        throw new UnsupportedOperationException();
      }
    } catch (Exception e) {
      return false;
    }
  }
}
