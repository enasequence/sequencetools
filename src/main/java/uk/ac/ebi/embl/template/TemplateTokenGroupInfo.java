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

import java.util.ArrayList;
import java.util.List;

public class TemplateTokenGroupInfo {
  private final String name;
  private final List<String> containsString;
  private final String description;
  private boolean mandatory;
  private int order;
  private boolean isSequenceGroup; // needed for sorting

  public TemplateTokenGroupInfo() {
    this("", new ArrayList<String>(), "", false);
  }

  // This is taken from TemplateProcessor to avoid the dependency
  public static final String SEQUENCE_TOKEN = "SEQUENCE";

  public TemplateTokenGroupInfo(
      final String name,
      final List<String> contains,
      final String description,
      final boolean mandatory) {

    this.name = name;
    containsString = contains;
    this.description = description;
    this.mandatory = mandatory;
    if (contains.size() == 1 && contains.get(0).equals(SEQUENCE_TOKEN)) {
      isSequenceGroup = true;
    }
  }

  public List<String> getContainsString() {
    return containsString;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public Integer getOrder() {
    return order;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public boolean isSequenceGroup() {
    return isSequenceGroup;
  }

  public void setMandatory(final boolean mandatory) {
    this.mandatory = mandatory;
  }

  public void setOrder(final int groupOrder) {
    order = groupOrder;
  }

  /**
   * sets the parent group of all its contained tokens
   *
   * @param tokenInfos
   */
  public void setParentGroups(final List<TemplateTokenInfo> tokenInfos) {
    // inefficient, perhaps should look into being provided a map of tokenInfos
    // - keep double itterator for now as
    // probably irrelevant in the face of speed as a whole
    for (final String tokenName : containsString) {
      for (final TemplateTokenInfo tokenInfo : tokenInfos) {
        if (tokenName.equals(tokenInfo.getName())) {
          tokenInfo.setParentGroup(this);
          break;
        }
      }
    }
  }
}
