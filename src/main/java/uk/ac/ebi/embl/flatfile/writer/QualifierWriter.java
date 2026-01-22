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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

/** Flat file writer for the qualifiers. */
public class QualifierWriter extends FlatFileWriter {

  private final Qualifier qualifier;
  private static final int NOTE_MAX_LINE_LENGTH = 200;

  public QualifierWriter(Entry entry, Qualifier qualifier, WrapType wrapType, String header) {
    super(entry, wrapType);
    this.qualifier = qualifier;
    this.header = header;
    if (qualifier != null) {
      wrapChar = WrapChar.WRAP_CHAR_SPACE;
      if (qualifier.getName().equals(Qualifier.REPLACE_QUALIFIER_NAME)
          || qualifier.getName().equals(Qualifier.RPT_UNIT_SEQ_QUALIFIER_NAME)
          || qualifier.getName().equals(Qualifier.PCR_PRIMERS_QUALIFIER_NAME)
          || qualifier.getName().equals(Qualifier.TRANSLATION_QUALIFIER_NAME)
          || qualifier.getName().equals(Qualifier.SEROVAR_QUALIFIER_NAME)) {
        setForceLineBreak(true);
      } else if (qualifier.getName().equals(Qualifier.NOTE_QUALIFIER_NAME)) {
        setMaximumLineLength(NOTE_MAX_LINE_LENGTH);
        setForceLineBreak(true);
      }
    }
  }

  public QualifierWriter(
      Entry entry, Qualifier qualifier, WrapType wrapType, String header, boolean wrapOnly) {
    this(entry, qualifier, wrapType, header);
    this.wrapOnly = wrapOnly;
  }

  private final String header;
  private boolean wrapOnly = false;

  public boolean write(Writer writer) throws IOException {
    if (qualifier == null) {
      return false;
    }
    StringBuilder block = new StringBuilder();
    if (wrapOnly) {
      // Used by the XmlFeatureWriter.
      if (!isBlankString(qualifier.getValue())) {
        writeBlock(writer, header, qualifier.getValue());
      }
    } else {
      block.append("/");
      if (!isBlankString(qualifier.getName())) {
        block.append(qualifier.getName());
      }
      if (qualifier.isValueQuoted()
          || qualifier.getName().equals(Qualifier.ANTICODON_QUALIFIER_NAME)) {
        block.append("=");
        block.append("\"");
        if (!isBlankString(qualifier.getValue())) {
          block.append(qualifier.getValue());
        }
        block.append("\"");
      } else if (!isBlankString(qualifier.getValue())) {
        block.append("=");
        block.append(qualifier.getValue());
      }

      writeBlock(writer, header, block.toString());
    }
    return true;
  }
}
