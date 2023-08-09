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

import java.util.*;

public class TemplateVersions {
  private final Map<Integer, TemplateInfo> versions;

  public TemplateVersions() {
    this.versions = new TreeMap();
  }

  public boolean containsVersion(Integer version) {
    return versions.containsKey(version);
  }

  public TemplateInfo getTemplate(Integer version) {
    return versions.get(version);
  }

  public void addTemplate(Integer version, TemplateInfo templateInfo) {
    versions.put(version, templateInfo);
  }

  public Set<Integer> getVersions() {
    return versions.keySet();
  }

  public TemplateInfo getLatestTemplate() {
    List<Integer> versionList = new ArrayList<Integer>(versions.keySet());
    return versions.get(
        versionList.get(
            versionList.size() - 1)); // the last element - will be the largest version no.
  }
}
