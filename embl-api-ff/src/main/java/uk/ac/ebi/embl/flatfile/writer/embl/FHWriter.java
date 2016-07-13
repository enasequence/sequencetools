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

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.api.entry.Entry;

/** Flat file writer for the FH lines.
 */
public class FHWriter extends FlatFileWriter {
	
	public FHWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		writer.write(EmblTag.FH_TAG);
		writer.write("   Key             Location/Qualifiers\n");
		writer.write(EmblTag.FH_TAG);
		writer.write("\n");		
		return true;
	}
}
