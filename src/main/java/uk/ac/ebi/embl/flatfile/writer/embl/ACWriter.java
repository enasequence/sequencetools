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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the AC lines. */
public class ACWriter extends FlatFileWriter {

  public ACWriter(Entry entry, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SEMICOLON);
  }

  public boolean write(Writer writer) throws IOException {
    StringBuilder block = new StringBuilder();
    String primaryAccession = entry.getPrimaryAccession();
    if (!isBlankString(primaryAccession)) {
      block.append(primaryAccession);
    }
    block.append(";");

    for (Text secondaryAccession : entry.getSecondaryAccessions()) {
      block.append(" ");
      block.append(secondaryAccession.getText());
      block.append(";");
    }
    writeBlock(writer, EmblPadding.AC_PADDING, block.toString());
    return true;
  }
}
