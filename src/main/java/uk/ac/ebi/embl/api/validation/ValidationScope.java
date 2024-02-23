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
package uk.ac.ebi.embl.api.validation;

public enum ValidationScope {
  /** Putff (ENA) */
  EMBL(Group.SEQUENCE),
  /** Putff (NCBI) */
  NCBI(Group.SEQUENCE),
  /** Putff (NCBI master) */
  NCBI_MASTER(Group.SEQUENCE),
  /** Pipeline (Webin-CLI sequence scope) */
  EMBL_TEMPLATE(Group.SEQUENCE),
  /** Putff (patent protein) */
  EPO_PEPTIDE(Group.SEQUENCE),
  /** Putff (patent) */
  EPO(Group.SEQUENCE),
  /** TODO: remove if not used */
  INSDC(Group.SEQUENCE),
  /** TODO: remove if not used */
  EGA(Group.SEQUENCE),
  /** TODO: remove if not used */
  ARRAYEXPRESS(Group.SEQUENCE),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_MASTER(Group.ASSEMBLY),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_CONTIG(Group.ASSEMBLY),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_SCAFFOLD(Group.ASSEMBLY),
  /** Pipeline (Webin-CLI genome scope) */
  ASSEMBLY_CHROMOSOME(Group.ASSEMBLY),
  /** Pipeline (Webin-CLI transcriptome scope) */
  ASSEMBLY_TRANSCRIPTOME(Group.ASSEMBLY);

  private final Group group;

  ValidationScope(Group group) {
    this.group = group;
  }

  public boolean isInGroup(Group group) {
    return this.group == group;
  }

  public Group group() {
    return this.group;
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
    ASSEMBLY,
    SEQUENCE
  }

  public static ValidationScope getScope(FileType fileType) {
    if (fileType == FileType.GENBANK) {
      return ValidationScope.NCBI;
    }
    return ValidationScope.EMBL;
  }
}
