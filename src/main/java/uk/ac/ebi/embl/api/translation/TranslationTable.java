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

import java.util.Map;

/**
 * NCBI translation table. Bases are encoded using lower case single letter JCBN abbreviations and
 * amino acids are encoded using upper case single letter JCBN abbreviations.
 */
public class TranslationTable {

  /** Constructs the translation table. */
  protected TranslationTable(
      Integer number,
      String name,
      Map<String, Character> startCodonMap,
      Map<String, Character> otherCodonMap) {
    this.number = number;
    this.name = name;
    this.startCodonMap = startCodonMap;
    this.otherCodonMap = otherCodonMap;
  }

  public static final Integer DEFAULT_TRANSLATION_TABLE = 11;
  public static final Integer PLASTID_TRANSLATION_TABLE = 11;

  /** Translation table number. */
  private Integer number;

  /** Translation table name. */
  private String name;

  /** Start codon translations. */
  private Map<String, Character> startCodonMap;

  /** Non-start codon translations. */
  private Map<String, Character> otherCodonMap;

  /**
   * Returns the translation table number.
   *
   * @return the translation table number.
   */
  public Integer getNumber() {
    return number;
  }

  /**
   * Returns the translation table name.
   *
   * @return the translation table name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the start codon translations.
   *
   * @return the start codon translations.
   */
  public Map<String, Character> getStartCodonMap() {
    return startCodonMap;
  }

  /**
   * Returns the non-start codon translations.
   *
   * @return the non-start codon translations.
   */
  public Map<String, Character> getOtherCodonMap() {
    return otherCodonMap;
  }
}
