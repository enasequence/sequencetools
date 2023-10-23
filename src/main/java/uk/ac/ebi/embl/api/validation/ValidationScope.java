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

import java.util.Arrays;
import java.util.List;

public enum ValidationScope {
  /** Putff (ENA) */
  EMBL(Group.PUTFF),
  /** Putff (NCBI) */
  NCBI(Group.PUTFF, Group.NCBI),
  /** Putff (NCBI master) */
  NCBI_MASTER(Group.PUTFF, Group.NCBI),
  /** Pipeline (Webin-CLI sequence scope) */
  EMBL_TEMPLATE(Group.PIPELINE),
  /** Putff (patent protein) */
  EPO_PEPTIDE(Group.EPO),
  /** Putff (patent) */
  EPO(Group.EPO),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_MASTER(Group.ASSEMBLY, Group.PIPELINE),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_CONTIG(Group.ASSEMBLY, Group.PIPELINE),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_SCAFFOLD(Group.ASSEMBLY, Group.PIPELINE),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_CHROMOSOME(Group.ASSEMBLY, Group.PIPELINE),
  /** Pipeline (Webin-CLI transcriptome scope) */
  ASSEMBLY_TRANSCRIPTOME(Group.PIPELINE);

  private final List<Group> groups;

  ValidationScope(Group... groups) {
    this.groups = Arrays.asList(groups);
  }

  public boolean isInGroup(Group group) {
    return this.groups.contains(group);
  }

  public List<Group> groups() {
    return this.groups;
  }

  public static ValidationScope get(String scope) {
    if (scope == null) return ValidationScope.EMBL;
    try {
      return valueOf(scope.toUpperCase());
    } catch (IllegalArgumentException x) {
      return null;
    }
  }

  public int getAssemblyLevel() {
    switch (this) {
      case ASSEMBLY_CHROMOSOME:
        return 2;
      case ASSEMBLY_CONTIG:
        return 0;
      case ASSEMBLY_SCAFFOLD:
        return 1;
      default:
        return -1;
    }
  }

  public enum Group {
    PIPELINE,
    PUTFF,
    ASSEMBLY,
    NCBI,
    EPO
  }

  public static ValidationScope getScope(FileType fileType) {
    if (fileType == FileType.GENBANK) {
      return ValidationScope.NCBI;
    }
    return ValidationScope.EMBL;
  }
}
