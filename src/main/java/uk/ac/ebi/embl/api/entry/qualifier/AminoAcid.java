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
package uk.ac.ebi.embl.api.entry.qualifier;

import java.io.Serializable;

/** An amino acid. */
public class AminoAcid implements Serializable {

  private static final long serialVersionUID = 8855241920193212734L;

  /** Constructs an amino acid. */
  protected AminoAcid(String name, Character letter, String abbreviation) {
    this.name = name;
    this.letter = letter;
    this.abbreviation = abbreviation;
  }

  /** The JCBN amino acid name. */
  private String name;

  /**
   * Returns the JCBN amino acid name.
   *
   * @return the JCBN amino acid name.
   */
  public String getName() {
    return name;
  }

  /** The JCBN amino acid letter. */
  private Character letter;

  /**
   * Returns the JCBN amino acid letter.
   *
   * @return the JCBN amino acid letter.
   */
  public Character getLetter() {
    return letter;
  }

  /** The JCBN amino acid abbreviation. */
  private String abbreviation;

  /**
   * Returns the JCBN amino acid abbreviation.
   *
   * @return the JCBN amino acid abbreviation.
   */
  public String getAbbreviation() {
    return abbreviation;
  }
}
