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

import java.util.*;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;

public final class GenomeUtils {

  public static final Long COVID_19_OUTBREAK_GENOME_MAX_SIZE = 31000L; // bp

  private GenomeUtils() {}

  /** Calculates the genome size. Placed sequences must be in uppercase. */
  public static Long calculateGenomeSize(
      Map<String, AssemblySequenceInfo> sequenceInfo, Set<String> agpPlacedComponents)
      throws ValidationEngineException {
    long genomeSize = 0;
    for (Map.Entry<String, AssemblySequenceInfo> entry : sequenceInfo.entrySet()) {
      AssemblySequenceInfo info = entry.getValue();

      if (info.getAssemblyLevel() == 0 || info.getAssemblyLevel() == 1) {
        if (!agpPlacedComponents.contains(entry.getKey().toUpperCase())) {
          genomeSize += info.getSequenceLength();
        }
      } else if (info.getAssemblyLevel() == 2) {
        genomeSize += info.getSequenceLength();
      } else
        throw new ValidationEngineException(
            "Unexpected assembly level",
            ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
    }
    return (genomeSize);
  }
}
