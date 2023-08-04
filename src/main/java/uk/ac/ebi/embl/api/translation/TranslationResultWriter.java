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

import java.io.IOException;
import java.io.Writer;

public class TranslationResultWriter {

  public TranslationResultWriter(TranslationResult result) {
    this.result = result;
  }

  public TranslationResultWriter(TranslationResult result, String expectedTranslation) {
    this.result = result;
    this.expectedTranslation = expectedTranslation;
  }

  private TranslationResult result;
  private String expectedTranslation;

  private final int CODONS_PER_BLOCK = 15;
  private final char TRANSLATION_EXCEPTION_START_SYMBOL = '!';
  private final char TRANSLATION_EXCEPTION_END_SYMBOL = ' ';
  private final char CODON_EXCEPTION_START_SYMBOL = ' ';
  private final char CODON_EXCEPTION_END_SYMBOL = '!';
  private final String AMBIGUOUS_CODON_START_SYMBOL = "/|\\";
  private final String AMBIGUOUS_CODON_END_SYMBOL = "\\|/";
  private final String DIFFERENT_AMINO_ACID_START_SYMBOL = "-";
  private final String DIFFERENT_AMINO_ACID_END_SYMBOL = "-";

  public void write(Writer writer) throws IOException {
    if (result.getCodons() == null || result.getCodons().size() == 0) {
      return;
    }
    for (int begin = 0; begin <= result.getCodons().size(); begin += CODONS_PER_BLOCK) {
      int end = begin + CODONS_PER_BLOCK;
      if (end >= result.getCodons().size()) {
        end = result.getCodons().size();
      }
      writeBlock(writer, begin, end);
    }
  }

  private void writeBlock(Writer writer, int begin, int end) throws IOException {

    for (int i = begin; i < end; ++i) {
      writeCodon(writer, result.getCodons().get(i).getCodon());
    }
    writePosition(writer, begin, end);
    writer.append("\n");

    int maxUnambiguousCodons = 0;
    for (int i = begin; i < end; ++i) {
      Codon codon = result.getCodons().get(i);
      UnAmbiguousCodon[] unAmbiguousCodons = codon.getUnAmbiguousCodons();
      if (!codon.getCodon().equals("nnn") && unAmbiguousCodons.length > maxUnambiguousCodons) {
        maxUnambiguousCodons = unAmbiguousCodons.length;
      }
    }
    if (maxUnambiguousCodons > 1) {
      writeAmbiguousCodonStart(writer, begin, end);
      writer.append("\n");
      for (int j = 0; j < maxUnambiguousCodons; ++j) {
        for (int i = begin; i < end; ++i) {
          UnAmbiguousCodon[] unAmbiguousCodons = result.getCodons().get(i).getUnAmbiguousCodons();
          if (!result.getCodons().get(i).getCodon().equals("nnn")
              && unAmbiguousCodons.length > 1
              && unAmbiguousCodons.length > j) {
            writeCodon(writer, unAmbiguousCodons[j].getCodon());
          } else {
            writeCodon(writer, "   ");
          }
        }
        writer.append("\n");
        for (int i = begin; i < end; ++i) {
          UnAmbiguousCodon[] unAmbiguousCodons = result.getCodons().get(i).getUnAmbiguousCodons();
          if (!result.getCodons().get(i).getCodon().equals("nnn")
              && unAmbiguousCodons != null
              && unAmbiguousCodons.length > 1
              && unAmbiguousCodons.length > j) {
            writeUnAmbiguousAminoAcid(writer, unAmbiguousCodons[j]);
          } else {
            writer.append("     ");
          }
        }
        writer.append("\n");
      }

      writeAmbiguousCodonEnd(writer, begin, end);
      writer.append("\n");
    }

    for (int i = begin; i < end; ++i) {
      writeTranslatedAminoAcid(writer, i);
    }

    writer.append("\n");

    if (expectedTranslation != null) {
      for (int i = begin; i < end; ++i) {
        writeExpectedAminoAcid(writer, i);
      }
      writer.append("\n");
    }
  }

  private void writeCodon(Writer writer, String codon) throws IOException {
    writer.append(" ");
    writer.append(codon);
    writer.append(" ");
  }

  private void writePosition(Writer writer, int begin, int end) throws IOException {
    for (int i = 0; i < CODONS_PER_BLOCK - (end - begin); ++i) {
      writer.append("     ");
    }
    writer.append("   ");
    int pos = result.getCodons().get(end - 1).getPos() + 2;
    writer.append(String.valueOf(pos));
  }

  private void writeAmbiguousCodonStart(Writer writer, int begin, int end) throws IOException {
    for (int i = begin; i < end; ++i) {
      Codon codon = result.getCodons().get(i);
      UnAmbiguousCodon[] unAmbiguousCodons = codon.getUnAmbiguousCodons();
      writer.append(" ");
      if (!result.getCodons().get(i).getCodon().equals("nnn") && unAmbiguousCodons.length > 1) {
        writer.append(AMBIGUOUS_CODON_START_SYMBOL);
      } else {
        writer.append("   ");
      }
      writer.append(" ");
    }
  }

  private void writeAmbiguousCodonEnd(Writer writer, int begin, int end) throws IOException {
    for (int i = begin; i < end; ++i) {
      Codon codon = result.getCodons().get(i);
      UnAmbiguousCodon[] unAmbiguousCodons = codon.getUnAmbiguousCodons();
      writer.append(" ");
      if (!result.getCodons().get(i).getCodon().equals("nnn") && unAmbiguousCodons.length > 1) {
        writer.append(AMBIGUOUS_CODON_END_SYMBOL);
      } else {
        writer.append("   ");
      }
      writer.append(" ");
    }
  }

  private void writeUnAmbiguousAminoAcid(Writer writer, UnAmbiguousCodon unAmbiguousCodon)
      throws IOException {
    writer.append(" ");
    if (unAmbiguousCodon.isCodonException()) {
      writer.append(CODON_EXCEPTION_START_SYMBOL);
    } else {
      writer.append(" ");
    }
    writer.append(unAmbiguousCodon.getAminoAcid());
    if (unAmbiguousCodon.isCodonException()) {
      writer.append(CODON_EXCEPTION_END_SYMBOL);
    } else {
      writer.append(" ");
    }
  }

  private void writeTranslatedAminoAcid(Writer writer, int i) throws IOException {
    Codon codon = result.getCodons().get(i);
    boolean isConceptualCodon = (i < result.getConceptualTranslationCodons());
    if (!isConceptualCodon && i == result.getConceptualTranslationCodons()) {
      writer.append("(");
    } else {
      writer.append(" ");
    }
    boolean displayTranslationException = codon.isTranslationException();
    boolean displayCodonException =
        (codon.getUnAmbiguousCodons().length == 1 && codon.isCodonException());
    if (displayTranslationException) {
      writer.append(TRANSLATION_EXCEPTION_START_SYMBOL);
    } else if (displayCodonException) {
      writer.append(CODON_EXCEPTION_START_SYMBOL);
    } else {
      writer.append(" ");
    }
    writer.append(codon.getAminoAcid());
    if (displayTranslationException) {
      writer.append(TRANSLATION_EXCEPTION_END_SYMBOL);
    } else if (displayCodonException) {
      writer.append(CODON_EXCEPTION_END_SYMBOL);
    } else {
      writer.append(" ");
    }
    if (!isConceptualCodon && i == result.getCodons().size() - 1) {
      writer.append(")");
    } else {
      writer.append(" ");
    }
  }

  private void writeExpectedAminoAcid(Writer writer, int i) throws IOException {
    if (expectedTranslation.length() <= i) {
      return;
    }
    writer.append(" ");
    Character expectedAminoAcid = expectedTranslation.charAt(i);
    Codon codon = result.getCodons().get(i);
    if (!expectedAminoAcid.equals(codon.getAminoAcid())) {
      writer.append(DIFFERENT_AMINO_ACID_START_SYMBOL);
    } else {
      writer.append(" ");
    }
    writer.append(expectedAminoAcid);
    if (!expectedAminoAcid.equals(codon.getAminoAcid())) {
      writer.append(DIFFERENT_AMINO_ACID_END_SYMBOL);
    } else {
      writer.append(" ");
    }
    writer.append(" ");
  }
}
