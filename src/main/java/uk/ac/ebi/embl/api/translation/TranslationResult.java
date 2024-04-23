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
package uk.ac.ebi.embl.api.translation;

import java.io.Serializable;
import java.util.Vector;

public class TranslationResult implements Serializable {

  private static final long serialVersionUID = -3348570245845628623L;

  private boolean fixedFivePrimePartial = false;
  private boolean fixedThreePrimePartial = false;
  private boolean fixedPseudo = false;
  private boolean fixedDegenerateStartCodon = false;

  public boolean isFixedDegenerateStartCodon() {
    return fixedDegenerateStartCodon;
  }

  public void setFixedDegenerateStartCodon(boolean fixedDegenerateStartCodon) {
    this.fixedDegenerateStartCodon = fixedDegenerateStartCodon;
  }

  public boolean isFixedPseudo() {
    return fixedPseudo;
  }

  public void setFixedPseudo(boolean fixedPseudo) {
    this.fixedPseudo = fixedPseudo;
  }

  protected void setFixedFivePrimePartial(boolean fixedFivePrimePartial) {
    this.fixedFivePrimePartial = fixedFivePrimePartial;
  }

  public boolean isFixedFivePrimePartial() {
    return fixedFivePrimePartial;
  }

  protected void setFixedThreePrimePartial(boolean fixedThreePrimePartial) {
    this.fixedThreePrimePartial = fixedThreePrimePartial;
  }

  public boolean isFixedThreePrimePartial() {
    return fixedThreePrimePartial;
  }

  private Vector<Codon> codons;

  protected void setCodons(Vector<Codon> codons) {
    this.codons = codons;
  }

  public Vector<Codon> getCodons() {
    return codons;
  }

  private String trailingBases;

  protected void setTrailingBases(String trailingBases) {
    this.trailingBases = trailingBases;
  }

  public String getTrailingBases() {
    return trailingBases;
  }

  private int conceptualTranslationCodons = 0;

  protected void setConceptualTranslationCodons(int conceptualTranslationCodons) {
    this.conceptualTranslationCodons = conceptualTranslationCodons;
  }

  public int getConceptualTranslationCodons() {
    return conceptualTranslationCodons;
  }

  public String getSequence() {
    if (codons == null) {
      return "";
    }
    StringBuilder sequence = new StringBuilder();
    for (Codon codon : codons) {
      sequence.append(codon.getCodon());
    }
    sequence.append(trailingBases);
    return sequence.toString();
  }

  /**
   * Returns the translation including stop codons and trailing bases.
   *
   * @return the translation including stop codons and trailing base
   */
  public String getTranslation() {
    if (codons == null) {
      return "";
    }
    StringBuilder translation = new StringBuilder();
    for (Codon codon : codons) {
      translation.append(codon.getAminoAcid());
    }
    return translation.toString();
  }

  /**
   * Returns the translation excluding stop codons and trailing bases.
   *
   * @return the translation excluding stop codons and trailing bases.
   */
  public String getConceptualTranslation() {
    if (codons == null) {
      return "";
    }
    StringBuilder translation = new StringBuilder();
    for (int i = 0; i < conceptualTranslationCodons; ++i) {
      Codon codon = codons.get(i);
      translation.append(codon.getAminoAcid());
    }
    return translation.toString();
  }

  private int translationLength;

  public void setTranslationLength(int length) {
    this.translationLength = length;
  }

  public int getTranslationLength() {
    return translationLength;
  }

  private int baseCount;

  public void setTranslationBaseCount(int baseCount) {
    this.baseCount = baseCount;
  }

  public int getBaseCount() {
    return baseCount;
  }
}
