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
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.Writer;
import java.io.IOException;

/** Flat file writer for the DR lines.
 */
public class DRWriter extends FlatFileWriter {

    public DRWriter(Entry entry) {
        super(entry);
    }

    public boolean write(Writer writer) throws IOException {
    	if (entry.getXRefs() == null) {
			return false;
		}

    	boolean writeBlock = false;
        for(XRef xref : entry.getXRefs()){
        	writer.write(EmblPadding.DR_PADDING);
        	if (!isBlankString(xref.getDatabase())) {
        		writer.write(xref.getDatabase());
        	}
        	writer.write("; ");   	
        	if (!isBlankString(xref.getPrimaryAccession())) {
        		writer.write(xref.getPrimaryAccession());
        	}
        	if (!isBlankString(xref.getSecondaryAccession())) {
        		writer.write("; ");
        		writer.write(xref.getSecondaryAccession());
        	}
        	writer.write(".\n");
        	writeBlock = true;
        }
        return writeBlock;
    }
}
