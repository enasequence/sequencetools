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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import java.sql.SQLException;
import uk.ac.ebi.embl.api.entry.genomeassembly.GCSEntry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public abstract class GenomeAssemblyValidationCheck<GCSEntry>
    implements EmblEntryValidationCheck<GCSEntry> {

  protected ValidationResult result;
  private boolean isPopulated;
  private EmblEntryValidationPlanProperty property;
  private EntryDAOUtils entryDAOUtils;
  private EraproDAOUtils eraproDAOUtils;

  @Override
  public ValidationResult check(GCSEntry object) throws ValidationEngineException {
    // TODO Auto-generated method stub
    return result;
  }

  protected GenomeAssemblyValidationCheck() {
    result = new ValidationResult();
  }

  public boolean isPopulated() {
    return isPopulated;
  }

  public void setPopulated() {
    // override to do any processing of data post-population
    isPopulated = true;
  }

  /**
   * Creates an error validation message for the feature and adds it to the validation result.
   *
   * @param origin the origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportError(
      Origin origin, String messageKey, Object... params) {
    return reportMessage(Severity.ERROR, origin, messageKey, params);
  }

  /**
   * Creates a warning validation message for the feature and adds it to the validation result.
   *
   * @param origin the origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportWarning(
      Origin origin, String messageKey, Object... params) {
    return reportMessage(Severity.WARNING, origin, messageKey, params);
  }

  /**
   * Creates a validation message for the feature and adds it to the validation result.
   *
   * @param severity message severity
   * @param origin the origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportMessage(
      Severity severity, Origin origin, String messageKey, Object... params) {
    ValidationMessage<Origin> message =
        EntryValidations.createMessage(origin, severity, messageKey, params);
    message.getMessage();
    //        System.out.println("message = " + message.getMessage());
    result.append(message);
    return message;
  }

  @Override
  public void setEmblEntryValidationPlanProperty(EmblEntryValidationPlanProperty property)
      throws SQLException {
    this.property = property;
  }

  @Override
  public EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty() {
    return property;
  }

  @Override
  public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils) {
    this.entryDAOUtils = entryDAOUtils;
  }

  @Override
  public EntryDAOUtils getEntryDAOUtils() {
    // TODO Auto-generated method stub
    return entryDAOUtils;
  }

  @Override
  public EraproDAOUtils getEraproDAOUtils() {
    // TODO Auto-generated method stub
    return eraproDAOUtils;
  }

  @Override
  public void setEraproDAOUtils(EraproDAOUtils eraproDAOUtils) {
    this.eraproDAOUtils = eraproDAOUtils;
  }
}
