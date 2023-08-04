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
package uk.ac.ebi.embl.api.translation;

import java.util.List;

public class Codon {

  private String codon;

  public void setCodon(String codon) {
    this.codon = codon;
  }

  public String getCodon() {
    return codon;
  }

  private Integer pos;

  protected void setPos(Integer pos) {
    this.pos = pos;
  }

  public Integer getPos() {
    return pos;
  }

  private Character aminoAcid;

  public void setAminoAcid(Character aminoAcid) {
    this.aminoAcid = aminoAcid;
  }

  public Character getAminoAcid() {
    return aminoAcid;
  }

  private UnAmbiguousCodon[] unAmbiguousCodons;

  protected void setUnAmbiguousCodons(List<UnAmbiguousCodon> codons) {
    unAmbiguousCodons = new UnAmbiguousCodon[codons.size()];
    unAmbiguousCodons = codons.toArray(unAmbiguousCodons);
  }

  public UnAmbiguousCodon[] getUnAmbiguousCodons() {
    return unAmbiguousCodons;
  }

  private boolean translationException = false;

  protected void setTranslationException(boolean translationException) {
    this.translationException = translationException;
  }

  public boolean isTranslationException() {
    return translationException;
  }

  public boolean isCodonException() {
    if (unAmbiguousCodons == null) {
      return false;
    }
    for (UnAmbiguousCodon unAmbiguousCodon : unAmbiguousCodons) {
      if (unAmbiguousCodon.isCodonException()) {
        return true;
      }
    }
    return false;
  }
}
