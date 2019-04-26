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
import java.util.List;

import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.AssemblyWriter;
import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the AS lines.
 */
public class ASWriter extends FlatFileWriter  {

	public ASWriter(Entry entry) {
		super(entry);	
	}
        
	public boolean write(Writer writer) throws IOException {
		List<Assembly> assemblies = entry.getAssemblies();
		if (assemblies == null ||
			assemblies.size() == 0) {
			return false;
		}
		new AHWriter(entry).write(writer);
		new AssemblyWriter(entry,
				EmblPadding.AS_PADDING		
		).write(writer);
		return true;
	}
}
