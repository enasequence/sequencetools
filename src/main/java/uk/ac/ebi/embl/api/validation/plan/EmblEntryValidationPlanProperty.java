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
package uk.ac.ebi.embl.api.validation.plan;

import java.sql.Connection;
import java.util.HashMap;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class EmblEntryValidationPlanProperty {
  public final ValidationPlanProperty<ValidationScope> validationScope =
      new ValidationPlanProperty<>(ValidationScope.EMBL);
  public final ValidationPlanProperty<Connection> enproConnection =
      new ValidationPlanProperty<>(null);
  public final ValidationPlanProperty<Connection> eraproConnection =
      new ValidationPlanProperty<>(null);
  public final ValidationPlanProperty<TaxonomyClient> taxonClient =
      new ValidationPlanProperty<>(null);
  public final ValidationPlanProperty<Boolean> isRemote = new ValidationPlanProperty<>(false);
  public final ValidationPlanProperty<FileType> fileType =
      new ValidationPlanProperty<>(FileType.EMBL);
  public final ValidationPlanProperty<String> analysis_id = new ValidationPlanProperty<>(null);
  public final ValidationPlanProperty<String> organism = new ValidationPlanProperty<>(null);
  public final ValidationPlanProperty<HashMap<String, AssemblySequenceInfo>> assemblySequenceInfo =
      new ValidationPlanProperty<>(new HashMap<>());
  public final ValidationPlanProperty<Integer> sequenceNumber = new ValidationPlanProperty<>(0);
  public final ValidationPlanProperty<Boolean> ncbiCon = new ValidationPlanProperty<>(false);
  public final ValidationPlanProperty<Boolean> isSourceUpdate = new ValidationPlanProperty<>(false);
  private final SubmissionOptions options;

  public EmblEntryValidationPlanProperty(SubmissionOptions options) {
    this.options = options;
  }

  public SubmissionOptions getOptions() {
    return options;
  }
}
