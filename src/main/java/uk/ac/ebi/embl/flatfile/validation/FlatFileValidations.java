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
package uk.ac.ebi.embl.flatfile.validation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.translation.TranslationResult;
import uk.ac.ebi.embl.api.translation.TranslationResultWriter;
import uk.ac.ebi.embl.api.translation.TranslationTable;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;

public class FlatFileValidations {

  public static final String FLAT_FILE_BUNDLE =
      "uk.ac.ebi.embl.flatfile.validation.FlatFileValidationMessages";
  public static final String GFF3_FLAT_FILE_BUNDLE =
      "uk.ac.ebi.embl.flatfile.validation.GFF3FlatFileValidationMessages";
  public static final String GENOMEASSEMBLY_FLAT_FILE_BUNDLE =
      "uk.ac.ebi.embl.flatfile.validation.GenomeAssemblyFlatFileValidationMessages";
  public static final String AGP_FLAT_FILE_BUNDLE =
      "uk.ac.ebi.embl.flatfile.validation.AGPFlatFileValidationMessages";

  private FlatFileValidations() { // prevent instantiations
  }

  /**
   * Creates a validation message.
   *
   * @param lineReader the flat file line reader.
   * @param severity severity of the message
   * @param messageKey a key of the message
   * @param params parameters of the message
   * @return a validation message
   */
  public static ValidationMessage<Origin> message(
      LineReader lineReader, Severity severity, String messageKey, Object... params) {
    return message(lineReader.getCurrentLineNumber(), severity, messageKey, params);
  }

  public static ValidationMessage<Origin> message(
      Severity severity, String messageKey, Object... params) {
    return ValidationMessage.message(severity, messageKey, params);
  }

  public static ValidationMessage<Origin> message(
      int lineNumber, Severity severity, String messageKey, Object... params) {
    FlatFileOrigin origin = new FlatFileOrigin(lineNumber);
    return ValidationMessage.message(severity, messageKey, params).append(origin);
  }

  public static ValidationMessage<Origin> message(
      Severity severity, String messageKey, Origin origin, Object... params) {

    return ValidationMessage.message(severity, messageKey, params).append(origin);
  }

  /**
   * Creates a validation error.
   *
   * @param lineReader the flat file line reader.
   * @param messageKey a key of the message
   * @param params parameters of the message
   * @return a validation message
   */
  public static ValidationMessage<Origin> error(
      LineReader lineReader, String messageKey, Object... params) {
    return message(lineReader, Severity.ERROR, messageKey, params);
  }

  /**
   * Creates a validation warning.
   *
   * @param lineReader the flat file line reader.
   * @param messageKey a key of the message
   * @param params parameters of the message
   * @return a validation message
   */
  public static ValidationMessage<Origin> warning(
      LineReader lineReader, String messageKey, Object... params) {
    return message(lineReader, Severity.WARNING, messageKey, params);
  }

  /**
   * Takes all the messages in a CdsFeatureTranslationCheck ExtendedResult ValidationResult object
   * and writes a report for the CDS translation. Uses extra information from the ExtendedResult.
   * Have put this here as it uses Writers from the FF package and the embl-api-core package does
   * not have access to these.
   *
   * @param cdsCheckResult
   */
  public static void setCDSTranslationReport(
      ExtendedResult<CdsFeatureTranslationCheck.TranslationReportInfo> cdsCheckResult) {

    try {
      CdsFeatureTranslationCheck.TranslationReportInfo translationInfo =
          cdsCheckResult.getExtension();
      StringWriter translationReportWriter = new StringWriter();

      TranslationResult translationResult = translationInfo.getTranslationResult();
      CdsFeature cdsFeature = translationInfo.getCdsFeature();

      if (translationResult != null && cdsFeature != null) {
        String providedTranslation = cdsFeature.getTranslation();

        new TranslationResultWriter(translationResult, providedTranslation)
            .write(translationReportWriter);
        String translationReport = translationReportWriter.toString();
        StringWriter reportString = new StringWriter();
        reportString.append("The results of the automatic translation are shown below.\n\n");
        CompoundLocation<Location> compoundLocation =
            translationInfo.getCdsFeature().getLocations();
        reportString.append("Feature location : ");
        reportString
            .append(FeatureLocationWriter.renderCompoundLocation(compoundLocation))
            .append("\n");
        reportString
            .append("Base count : ")
            .append(Integer.toString(translationResult.getBaseCount()))
            .append("\n");
        reportString
            .append("Translation length : ")
            .append(Integer.toString(translationResult.getTranslation().length()))
            .append("\n\n");

        reportString.append("Translation table info : ");
        TranslationTable translationTable = translationInfo.getTranslationTable();
        String translationTableName = "NOT SET";
        String translationTableNumber = "NOT SET";

        if (translationTable != null) {
          translationTableName = translationTable.getName();
          translationTableNumber = Integer.toString(translationTable.getNumber());
        }

        reportString.append("Table name - \"").append(translationTableName).append("\" ");
        reportString.append("Table number - \"").append(translationTableNumber).append("\"\n\n");

        if (translationReport != null && !translationReport.isEmpty()) {
          reportString.append(
              "The amino acid codes immediately below the dna triplets is the actual "
                  + "translation based on the information you provided.");

          List<Qualifier> providedtranslations =
              translationInfo.getCdsFeature().getQualifiers(Qualifier.TRANSLATION_QUALIFIER_NAME);
          if (!providedtranslations.isEmpty()) {
            reportString.append(
                "\nThe second row of amino acid codes is the translation you provided in the CDS "
                    + "feature. These translations should match.");
          }

          reportString.append("\n\n");
          reportString.append(translationReport);
        } else {
          reportString.append("No translation made\n\n");
        }

        /**
         * set in all messages and the validation result - used differently in webapp to the command
         * line tool
         */
        cdsCheckResult.setReportMessage(reportString.toString());
        for (ValidationMessage message : cdsCheckResult.getMessages()) {
          message.setReportMessage(reportString.toString());
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
