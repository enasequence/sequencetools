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
package uk.ac.ebi.embl.agp.writer;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.AgpRow;

public class AGPRowWriter {
  private final AgpRow agpRow;
  private final Writer writer;
  private final String agpRowString = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s";

  public AGPRowWriter(AgpRow agpRow, Writer writer) {
    this.agpRow = agpRow;
    this.writer = writer;
  }

  public void write() throws IOException {
    String rowString = null;
    rowString =
        agpRow.isGap()
            ? String.format(
                agpRowString,
                agpRow.getObject(),
                agpRow.getObject_beg(),
                agpRow.getObject_end(),
                agpRow.getPart_number(),
                agpRow.getComponent_type_id(),
                agpRow.getGap_length(),
                agpRow.getGap_type(),
                agpRow.getLinkage(),
                StringUtils.join(agpRow.getLinkageevidence(), ";"))
            : String.format(
                agpRowString,
                agpRow.getObject(),
                agpRow.getObject_beg(),
                agpRow.getObject_end(),
                agpRow.getPart_number(),
                agpRow.getComponent_type_id(),
                agpRow.getComponent_id(),
                agpRow.getComponent_beg(),
                agpRow.getComponent_end(),
                agpRow.getOrientation());
    writer.write(rowString.trim());
  }
}
