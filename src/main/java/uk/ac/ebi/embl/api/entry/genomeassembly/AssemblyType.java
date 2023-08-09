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
package uk.ac.ebi.embl.api.entry.genomeassembly;

public enum AssemblyType {
  CLONEORISOLATE("CLONE OR ISOLATE", "clone or isolate"),
  PRIMARYMETAGENOME("PRIMARY METAGENOME", "primary metagenome"),
  BINNEDMETAGENOME("BINNED METAGENOME", "binned metagenome"),
  CLINICALISOLATEASSEMBLY("CLINICAL ISOLATE ASSEMBLY", "clinical isolate assembly"),
  COVID_19_OUTBREAK("COVID-19 OUTBREAK", "COVID-19 outbreak"),
  METAGENOME_ASSEMBLEDGENOME(
      "METAGENOME-ASSEMBLED GENOME (MAG)", "Metagenome-Assembled Genome (MAG)"),
  ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME(
      "ENVIRONMENTAL SINGLE-CELL AMPLIFIED GENOME (SAG)",
      "Environmental Single-Cell Amplified Genome (SAG)");
  String value;
  String fixedValue;

  AssemblyType(String value) {
    this.value = value;
  }

  AssemblyType(String value, String fixedValue) {
    this.value = value;
    this.fixedValue = fixedValue;
  }

  public String getValue() {
    return value;
  }

  public String getFixedValue() {
    return fixedValue;
  }
}
