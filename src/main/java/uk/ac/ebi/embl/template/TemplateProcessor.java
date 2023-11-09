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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

public class TemplateProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProcessor.class);
  private final int maxProcessingSize = -1; // default - process all
  private TemplateInfo templateInfo;
  private TemplateEntryProcessor entryProcessor;
  private static final String MOL_TYPE = "/mol_type";

  public TemplateProcessor() {}

  public TemplateProcessor(TemplateInfo templateInfo, SubmissionOptions options) {
    this.templateInfo = templateInfo;
    this.entryProcessor = new TemplateEntryProcessor(options);
  }

  private String getMolTypeFromTemplate() throws Exception {
    String template = templateInfo.getTemplateString();
    String molType = "";
    if (template.contains(MOL_TYPE)) {
      String start = template.substring(template.indexOf(MOL_TYPE) + MOL_TYPE.length());
      if (start.contains("\"")) {
        start = start.substring(start.indexOf("\"") + 1);
        if (start.contains("\"")) molType = start.substring(0, start.indexOf("\""));
        else
          throw new Exception(
              "Found MOL TYPE in template but there is no ending '\"' (double quotes).");
      } else
        throw new Exception(
            "Found MOL TYPE in template but there is no starting '\"' (double quotes).");
    } else throw new Exception("Template is missing MOL TYPE.");
    return molType;
  }

  public TemplateProcessorResultSet process(
      TemplateVariables templateVariables, SubmissionOptions options) throws Exception {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    incrementAndDecrementTokans(templateVariables, templateInfo);
    String molType = getMolTypeFromTemplate();
    reportEntryCount(templateVariables.getSequenceNumber());
    TemplateProcessorResultSet templateProcessorResultSet =
        entryProcessor.processEntry(
            templateInfo, molType, templateVariables, options.getProjectId());
    /** check no identical records */
    //        new FieldUniquenessChecker().check(templateVariables, templateProcessorResultSetsL);
    return templateProcessorResultSet;
  }

  private void incrementAndDecrementTokans(TemplateVariables variables, TemplateInfo templateInfo)
      throws TemplateException {
    new TokenIncrementor().processIncrementAndDecrementTokens(templateInfo, variables);
  }

  private void reportEntryCount(Integer entrynumber) {
    if (entrynumber % 10000 == 0) LOGGER.info("variableKey = " + entrynumber);
  }

  public String getTemplate(String templateId) {
    StringBuilder template = new StringBuilder();
    BufferedReader buffer =
        new BufferedReader(
            new InputStreamReader(
                getClass()
                    .getClassLoader()
                    .getResourceAsStream("templates/" + templateId + ".xml")));
    buffer.lines().forEach(p -> template.append(p.trim() + "\n"));

    //        String template  =
    // IOUtils.toString(getClass().getClassLoader().getResourceAsStream("templates/" + templateId +
    // ".xml")).trim();
    return template.toString();
  }
}
