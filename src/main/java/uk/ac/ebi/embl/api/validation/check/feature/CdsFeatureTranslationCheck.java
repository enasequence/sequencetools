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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.RepositoryException;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.translation.CdsTranslator;
import uk.ac.ebi.embl.api.translation.TranslationResult;
import uk.ac.ebi.embl.api.translation.TranslationTable;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Runs the translator and returns results")
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    })
public class CdsFeatureTranslationCheck extends FeatureValidationCheck {

  protected CdsTranslator translator;
  protected Entry entry;

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  @SuppressWarnings("deprecation")
  public ValidationResult check(Feature feature) {
    translator = new CdsTranslator(getEmblEntryValidationPlanProperty());
    result = new ExtendedResult<TranslationReportInfo>();
    if (entry == null || feature == null) return result;
    if (Entry.CON_DATACLASS.equals(entry.getDataClass())
        && (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null))
      return result;
    try {
      if (entry.getSequence() != null
          && entry.getSequence().getSequenceByte() != null
          && entry.getSequence().getLength() != 0) {
        if (feature instanceof CdsFeature) {
          CdsFeature cdsFeature = (CdsFeature) feature;
          ExtendedResult<TranslationResult> validationResult =
              translator.translate(cdsFeature, entry);
          // TODO: get markedForDeletion flag from translator, add a fixer message and delete the
          // current feature
          TranslationResult translationResult = validationResult.getExtension();

          /** set the translation report on all messages */
          TranslationReportInfo translationInfo = new TranslationReportInfo();
          if (translationResult != null) { // can be null if bailed out before
            translationInfo.setTranslationResult(translationResult);
            translationInfo.setCDSFeature(cdsFeature);
            translationInfo.setTranslatonTable(translator.getTranslationTable());
          }

          for (ValidationMessage<Origin> message : validationResult.getMessages()) {
            FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
            result.append(message);
          }

          /** only add the translation report if there is an error or a warning */
          if (result.count(Severity.ERROR) != 0 || result.count(Severity.WARNING) != 0) {
            ((ExtendedResult<TranslationReportInfo>) result).setExtension(translationInfo);
          }
        }
      }
    } catch (RepositoryException e) {
      e.printStackTrace();
    }

    return result;
  }

  public class TranslationReportInfo {
    private TranslationResult translationResult;
    private CdsFeature cdsFeature;
    private TranslationTable translationTable;

    public void setTranslationResult(TranslationResult translationResult) {
      this.translationResult = translationResult;
    }

    public void setCDSFeature(CdsFeature cdsFeature) {
      this.cdsFeature = cdsFeature;
    }

    public void setTranslatonTable(TranslationTable translationTable) {
      this.translationTable = translationTable;
    }

    public TranslationResult getTranslationResult() {
      return translationResult;
    }

    public CdsFeature getCdsFeature() {
      return cdsFeature;
    }

    public TranslationTable getTranslationTable() {
      return translationTable;
    }
  }

  /*    public class TranslationReportSet {
      List<ExtendedResult<TranslationReportInfo>> translationReports;

      public TranslationReportSet() {
          this.translationReports = new ArrayList<ExtendedResult<TranslationReportInfo>>();
      }

      public void addReport(ExtendedResult<TranslationReportInfo> report){
          translationReports.add(report);
      }

      public List<ExtendedResult<TranslationReportInfo>> getTranslationReports() {
          return translationReports;
      }
  }*/
}
