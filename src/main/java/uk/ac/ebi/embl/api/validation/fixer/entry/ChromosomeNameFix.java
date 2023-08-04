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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.apache.commons.lang.StringUtils;

public class ChromosomeNameFix {

  /** Words to remove from the chromosome name. */
  static final String[] WORDS =
      new String[] {
        "chromosome", "chrom", "chrm", "chr", "linkage-group", "linkage group", "plasmid"
      };

  /**
   * Remove and replace unwanted characters and words from the chromosome name.
   *
   * @param chromosomeName the chromosome name
   * @return chromosome name with unwanted characters and words removed or replaced
   */
  public static String fix(String chromosomeName) {
    if (chromosomeName != null) {
      // Remove whitespace.
      chromosomeName = chromosomeName.replaceAll("\\s", "");
      // Replace '\', '/', '|', '=', ';' characters with a '_' character.
      chromosomeName = chromosomeName.replaceAll("[\\\\/\\|\\=\\;]", "_");
      // Remove leading and trailing '_' characters.
      chromosomeName = StringUtils.strip(chromosomeName, "_");
      // Coalesce '_' characters.
      chromosomeName = chromosomeName.replaceAll("_+", "_");

      // Remove words case-insensitively.
      for (String word : WORDS) {
        word = word.replaceAll("\\s", "");
        chromosomeName = chromosomeName.replaceAll("(?i)" + word, "");
      }

      // Replace words case-insensitively.
      if (chromosomeName.equalsIgnoreCase("mitocondria")
          || chromosomeName.equalsIgnoreCase("mitochondria")) {
        chromosomeName = "MT";
      }
    }
    return chromosomeName;
  }
}
