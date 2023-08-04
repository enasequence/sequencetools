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

public class TemplateSectionInfo {
  private String name;
  private List<TemplateTokenInfo> depends;

  public TemplateSectionInfo() {
    this("", new ArrayList<TemplateTokenInfo>());
  }

  public TemplateSectionInfo(final String name, final List<TemplateTokenInfo> depends) {
    this.name = name;
    this.depends = depends;
  }

  public List<TemplateTokenInfo> getDepends() {
    return depends;
  }

  public String getName() {
    return name;
  }
}
