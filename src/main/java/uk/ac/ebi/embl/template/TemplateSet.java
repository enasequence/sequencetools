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
import java.util.Set;

public class TemplateSet {
  private final HashMap<String, TemplateVersions> templates =
      new HashMap<String, TemplateVersions>();

  public TemplateSet() {}

  public boolean containsTemplateId(String templateId) {
    return templates.containsKey(templateId);
  }

  public TemplateVersions getTemplateVersions(String templateId) {
    return templates.get(templateId);
  }

  public void addTemplateVersions(String id, TemplateVersions versionMap) {
    templates.put(id, versionMap);
  }

  public Set<String> getTemplateIds() {
    return templates.keySet();
  }

  public TemplateVersions getTemplate(String templateId) {
    return templates.get(templateId);
  }

  public TemplateInfo getLatestTemplateInfo(String templateId) throws TemplateException {

    if (containsTemplateId(templateId)) {
      TemplateVersions versions = getTemplate(templateId);
      return versions.getLatestTemplate();
    }

    throw new TemplateException("No template found with ID : " + templateId);
  }
}
