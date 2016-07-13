/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile.writer.embl;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the entry status lines.
 */
public class STStarWriter extends FlatFileWriter {

	public STStarWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		Entry.Status status = entry.getStatus(); 
		if (entry.getStatus() == null /*||
			entry.getStatus() == Entry.Status.PUBLIC */) {
			return false;
		}		
		writer.write(EmblTag.ST_STAR_TAG);
		writer.write(" ");
		if (status == Entry.Status.PUBLIC) {
			writer.write("public");
		}
        else if (status == Entry.Status.DRAFT) {
            writer.write("draft");
            if (entry.getHoldDate() != null) {
                writeHoldDate(writer);
            }
        }
        else if (status == Entry.Status.CANCELLED) {
			writer.write("cancelled");
		}
		else if (status == Entry.Status.PRIVATE) {
			writer.write("private");
			if (entry.getHoldDate() != null) {
                writeHoldDate(writer);
			}
		}
		else if (status == Entry.Status.SUPPRESSED) {
			writer.write("suppressed");
		}
		else if (status == Entry.Status.KILLED) {
			writer.write("killed");
		}
		writer.write("\n");
		return true;
	}

    private void writeHoldDate(Writer writer) throws IOException {
        writer.write(" ");
        writer.write(DAY_FORMAT.format(entry.getHoldDate()).toUpperCase());
    }
}
