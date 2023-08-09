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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemplateVariables {
  private static final long serialVersionUID = 1L;
  private int sequenceNumber;
  private String sequenceName;
  private final Map<String, String> variables;

  public TemplateVariables() {
    variables = new HashMap<String, String>();
  }

  public TemplateVariables(int sequenceNumber, Map<String, String> variables) {
    this.sequenceNumber = sequenceNumber;
    this.variables = variables;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public TemplateVariables(Map<String, String> entryTokenMap) {
    this.variables = entryTokenMap;
  }

  public void setTokenValue(String key, String value) {
    if (variables.containsKey(key)) variables.put(key, value);
  }

  public boolean containsToken(String name) {
    return variables.containsKey(name);
  }

  public String getTokenValue(String tokenName) {
    return variables.get(tokenName);
  }

  public void addToken(String tokenName, String value) {
    variables.put(tokenName, value);
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public Set<String> getTokenNames() {
    return variables.keySet();
  }

  public boolean isEmpty() {
    return variables.isEmpty();
  }

  public void removeToken(String name) {
    variables.remove(name);
  }
}
