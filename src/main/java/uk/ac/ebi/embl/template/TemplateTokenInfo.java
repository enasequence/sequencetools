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

public class TemplateTokenInfo {
  private final String name;
  private final TemplateTokenType type;
  private final boolean mandatory;
  private boolean variableOnly;
  private String displayName;
  private final String description;
  private String tip;
  private int order;
  private int groupOrder;

  /** the group to which this token belongs - if any */
  private TemplateTokenGroupInfo parentGroup;

  /** if the token is of type CV, this is the name of the cv group (used to populate values) */
  private String cvName;

  /**
   * yesno tokens have their value stored in the database as stings, these statics represent them.
   */
  public static final String NO_VALUE = "no";

  public static final String YES_VALUE = "yes";

  public TemplateTokenInfo() {
    this("", TemplateTokenType.TAXON_FIELD, false, "", "", "");
  }

  public TemplateTokenInfo(
      final String name,
      final TemplateTokenType type,
      final boolean mandatory,
      final String displayName,
      final String description,
      final String tip) {
    this.name = name;
    this.type = type;
    this.mandatory = mandatory;
    this.displayName = displayName;
    this.description = description;
    this.tip = tip;
    if (displayName == null) this.displayName = name;
  }

  public String getCvName() {
    return cvName;
  }

  public String getDescription() {
    return description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getGroupOrder() {
    return groupOrder;
  }

  public String getName() {
    return name;
  }

  public Integer getOrder() {
    return order;
  }

  public TemplateTokenGroupInfo getParentGroup() {
    return parentGroup;
  }

  public String getTip() {
    return tip;
  }

  public TemplateTokenType getType() {
    return type;
  }

  public boolean isHasDescription() {
    return description != null;
  }

  public boolean isHasTip() {
    return tip != null;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public boolean isVariableOnly() {
    return variableOnly;
  }

  public void setCvName(final String cvName) {
    this.cvName = cvName;
  }

  public void setGroupOrder(final int groupOrder) {
    this.groupOrder = groupOrder;
  }

  public void setOrder(final int tokenOrder) {
    order = tokenOrder;
  }

  public void setParentGroup(final TemplateTokenGroupInfo parentGroup) {
    this.parentGroup = parentGroup;
  }

  public void setTip(final String tip) {
    this.tip = tip;
  }

  public void setVariableOnly(final boolean variableOnly) {
    this.variableOnly = variableOnly;
  }
}
