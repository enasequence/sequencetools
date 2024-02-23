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

import java.util.ArrayList;
import java.util.List;

public class MutableTemplateInfo {
  public String id;
  public Integer version;
  public boolean newTemplate;
  public String name;
  public String templateString;
  public String description;
  public String example;
  public String filePath;
  public String comment;
  public List<TemplateTokenInfo> tokenInfos = new ArrayList<TemplateTokenInfo>();
  public List<TemplateSectionInfo> sectionInfos = new ArrayList<TemplateSectionInfo>();
  public List<TemplateTokenGroupInfo> groupInfo = new ArrayList<TemplateTokenGroupInfo>();
}
