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
package uk.ac.ebi.embl.api.entry.genomeassembly;

public class UnlocalisedEntry extends GCSEntry {
  private String chromosomeName;
  private String objectName;
  private String acc;

  public String getAcc() {
    return acc;
  }

  public void setAcc(String acc) {
    this.acc = acc;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getChromosomeName() {
    return chromosomeName;
  }

  public void setChromosomeName(String chromosomeName) {
    this.chromosomeName = chromosomeName;
  }
}
