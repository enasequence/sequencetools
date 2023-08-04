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
import java.util.Formatter;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.AssemblyWriter;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the AH lines. */
public class AHWriter extends FlatFileWriter {

  public AHWriter(Entry entry) {
    super(entry);
  }

  public static final String LOCAL_SPAN_HEADER = "LOCAL_SPAN";

  public static final String PRIMARY_IDENTIFIER_HEADER = "PRIMARY_IDENTIFIER";

  public static final String PRIMARY_SPAN_HEADER = "PRIMARY_SPAN";

  public static final String COMPLEMENT_HEADER = "COMP";

  public boolean write(Writer writer) throws IOException {
    writer.write(EmblPadding.AH_PADDING);
    Formatter formatter = new Formatter();
    String line =
        formatter
            .format(
                "%-"
                    + AssemblyWriter.LOCAL_SPAN_COLUMN_WIDTH
                    + "s%-"
                    + AssemblyWriter.PRIMARY_IDENTIFIER_COLUMN_WIDTH
                    + "s%-"
                    + AssemblyWriter.PRIMARY_SPAN_COLUMN_WIDTH
                    + "s%s",
                new Object[] {
                  LOCAL_SPAN_HEADER,
                  PRIMARY_IDENTIFIER_HEADER,
                  PRIMARY_SPAN_HEADER,
                  COMPLEMENT_HEADER
                })
            .toString();
    writer.write(line);
    writer.write("\n");
    return true;
  }
}
