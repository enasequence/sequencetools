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

public class MasterTSAWriter extends FlatFileWriter {

  public MasterTSAWriter(Entry entry, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_COMMA);
  }

  public boolean write(Writer writer) throws IOException {
    if (entry.getContigDataclass() == null
        || !entry.getContigDataclass().equals(Entry.TSA_DATACLASS)) {
      return false;
    }
    StringBuilder block = new StringBuilder();
    int i = 0;
    for (Text accession : entry.getContigAccessions()) {
      ++i;
      if (i > 1) {
        block.append(", ");
      }
      block.append(accession.getText());
    }
    writeBlock(writer, EmblPadding.MASTER_TSA_PADDING, block.toString());
    return (i > 0);
  }
}
