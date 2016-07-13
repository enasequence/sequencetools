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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.AssemblyWriter;
import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the PRIMARY lines.
 */
public class PrimaryWriter extends FlatFileWriter  {

	public PrimaryWriter(Entry entry) {
		super(entry);	
	}
        
	public boolean write(Writer writer) throws IOException {
		List<Assembly> assemblies = entry.getAssemblies();
		if (assemblies == null ||
			assemblies.size() == 0) {
			return false;
		}
		if (entry.getDataClass() != null &&
			entry.getDataClass().equals("TSA")) {			
			writer.write("PRIMARY     TSA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n");
		}
		else {
			writer.write("PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n");			
		}
		new AssemblyWriter(entry, GenbankPadding.BLANK_PADDING).write(writer);
		return true;
	}
}
