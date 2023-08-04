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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashSet;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the KW lines. */
public class KWWriter extends FlatFileWriter {

  public KWWriter(Entry entry, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SEMICOLON);
  }

  public boolean write(Writer writer) throws IOException {
    StringBuilder block = new StringBuilder();
    boolean firstKeyword = true;
    LinkedHashSet<Text> keywords = new LinkedHashSet<Text>();
    // Sort KW using keyword length.
    Collections.sort(
        entry.getKeywords(), (a, b) -> Integer.compare(a.getText().length(), b.getText().length()));
    for (Text keyword : entry.getKeywords()) {
      keywords.add(keyword);
    }
    for (Text keyword : keywords) {
      if (!firstKeyword) {
        block.append("; ");
      } else {
        firstKeyword = false;
      }
      block.append(keyword.getText());
    }
    block.append(".");
    writeBlock(writer, EmblPadding.KW_PADDING, block.toString());
    return true;
  }
}
