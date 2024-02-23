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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class OrganelleDETemplatePreProcessor implements TemplatePreProcessor {
  public static final String PP_ORGANELLE_TOKEN = "PP_ORGANELLE";
  public static final String ORGANELLE_TOKEN = "ORGANELLE";
  protected static final String MISSING_ORGANELLE_TOKEN_MESSAGE = "missing organelle token";
  protected static final String INVALID_ORGANELLE_TOKEN_MESSAGE = "invalid organelle token";
  private final Map<String, String> tokenMap = new HashMap<String, String>();

  public OrganelleDETemplatePreProcessor() {
    super();
    initTokenMap();
  }

  protected String lookupTokenValue(String key) {
    return tokenMap.get(key);
  }

  private void initTokenMap() {
    tokenMap.clear();
    tokenMap.put("chromatophore", "chromatophore");
    tokenMap.put("hydrogenosome", "hydrogenosome");
    tokenMap.put("mitochondrion", "mitochondrial");
    tokenMap.put("nucleomorph", "nucleomorph");
    tokenMap.put("plastid", "plastid");
    tokenMap.put("mitochondrion:kinetoplast", "kinetoplast");
    tokenMap.put("plastid:chloroplast", "chloroplast");
    tokenMap.put("plastid:apicoplast", "apicoplast");
    tokenMap.put("plastid:chromoplast", "chromoplast");
    tokenMap.put("plastid:cyanelle", "cyanelle");
    tokenMap.put("plastid:leucoplast", "leucoplast");
    tokenMap.put("plastid:proplastid", "proplastid");
  }

  @Override
  public ValidationResult process(TemplateVariables variablesMap) {
    final ValidationResult validationResult = new ValidationResult();
    String organelleTokenValue = variablesMap.getTokenValue(ORGANELLE_TOKEN);
    if (organelleTokenValue != null && !organelleTokenValue.isEmpty()) {
      final String ppOrganelleTokenValue = lookupTokenValue(organelleTokenValue);
      if (ppOrganelleTokenValue != null)
        variablesMap.addToken(PP_ORGANELLE_TOKEN, ppOrganelleTokenValue);
      else {
        ValidationMessage<Origin> message =
            ValidationMessage.message(Severity.ERROR, INVALID_ORGANELLE_TOKEN_MESSAGE);
        validationResult.append(message);
      }
    }
    return validationResult;
  }

  protected Set<String> getTokenKeys() {
    return tokenMap.keySet();
  }
}
