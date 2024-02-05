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
package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public abstract class EmblEntryValidationCheck<E> implements ValidationCheck<E> {

  protected EmblEntryValidationPlanProperty property;
  private EntryDAOUtils entryDAOUtils;
  private EraproDAOUtils eraproDAOUtils;

  @Override
  public final void setEmblEntryValidationPlanProperty(EmblEntryValidationPlanProperty property) {
    this.property = property;
  }

  @Override
  public final EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty() {
    return property;
  }

  public boolean isIgnoreError() {
    return (property != null
        && property.getOptions() != null
        && property.getOptions().ignoreErrors);
  }

  @Override
  public EntryDAOUtils getEntryDAOUtils() {
    return entryDAOUtils;
  }

  @Override
  public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils) {
    this.entryDAOUtils = entryDAOUtils;
  }

  @Override
  public EraproDAOUtils getEraproDAOUtils() {
    return eraproDAOUtils;
  }

  @Override
  public void setEraproDAOUtils(EraproDAOUtils eraproDAOUtils) {
    this.eraproDAOUtils = eraproDAOUtils;
  }
}
