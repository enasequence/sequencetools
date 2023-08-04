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

public class CSVLine {
  private Integer lineNumber;
  private TemplateVariables entryTokenMap;

  public CSVLine(Integer lineNumber, TemplateVariables entryTokenMap) {
    this.lineNumber = lineNumber;
    this.entryTokenMap = entryTokenMap;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public TemplateVariables getEntryTokenMap() {
    if (lineNumber != null) entryTokenMap.setSequenceNumber(lineNumber);
    return entryTokenMap;
  }
}
