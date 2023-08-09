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
package uk.ac.ebi.embl.template;

import java.util.List;
import java.util.Map;

public class FASTAReaderTokenInfo {
  private Map<Integer, TemplateToken> variableTokenOrders;
  private Map<String, TemplateToken> variableTokenDisplayNames;
  private TemplateVariablesSet templateVariables;
  private TemplateVariables megaEntryConstants;
  private List<TemplateTokenInfo> allSelectedTokens;

  public FASTAReaderTokenInfo() {}

  public void setVariableTokenOrders(Map<Integer, TemplateToken> variableTokenOrders) {
    this.variableTokenOrders = variableTokenOrders;
  }

  public void setVariableTokenDisplayNames(Map<String, TemplateToken> variableTokenDisplayNames) {
    this.variableTokenDisplayNames = variableTokenDisplayNames;
  }

  public Map<Integer, TemplateToken> getVariableTokenOrders() {
    return variableTokenOrders;
  }

  public Map<String, TemplateToken> getVariableTokenDisplayNames() {
    return variableTokenDisplayNames;
  }

  public void setTemplateVariables(TemplateVariablesSet templateVariables) {
    this.templateVariables = templateVariables;
  }

  public TemplateVariablesSet getTemplateVariables() {
    return templateVariables;
  }

  public void setMegaEntryConstants(TemplateVariables megaEntryConstants) {
    this.megaEntryConstants = megaEntryConstants;
  }

  public TemplateVariables getMegaEntryConstants() {
    return megaEntryConstants;
  }

  public void setAllSelectedTokens(List<TemplateTokenInfo> allSelectedTokens) {
    this.allSelectedTokens = allSelectedTokens;
  }

  public List<TemplateTokenInfo> getAllSelectedTokens() {
    return allSelectedTokens;
  }
}
