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
package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.validation.ValidationResult;

public class PhyloMarkerTemplatePreProcessor implements TemplatePreProcessor {
  public ValidationResult process(TemplateVariables variablesMap) {
    ValidationResult validationResult = new ValidationResult();
    String markerToken = variablesMap.getTokenValue("MARKER");
    if (markerToken != null && !markerToken.isEmpty()) {
      if (markerToken.equals("actin")) variablesMap.addToken("PP_GENE", "act");
      else if (markerToken.equals("alpha tubulin")) variablesMap.addToken("PP_GENE", "tuba");
      else if (markerToken.equals("beta tubulin")) variablesMap.addToken("PP_GENE", "tubb");
      else if (markerToken.equals("translation elongation factor 1 alpha"))
        variablesMap.addToken("PP_GENE", "tef1a");
      else if (markerToken.equals("calmodulin")) variablesMap.addToken("PP_GENE", "CaM");
      else if (markerToken.equals("RNA polymerase II large subunit 1"))
        variablesMap.addToken("PP_GENE", "RPB1");
      else if (markerToken.equals("RNA polymerase II large subunit 2"))
        variablesMap.addToken("PP_GENE", "RPB2");
      else if (markerToken.equals("Glyceraldehyde 3-phosphate dehydrogenase"))
        variablesMap.addToken("PP_GENE", "GAPDH");
      else if (markerToken.equals("Histone H3")) variablesMap.addToken("PP_GENE", "H3");
    }
    return validationResult;
  }
}
