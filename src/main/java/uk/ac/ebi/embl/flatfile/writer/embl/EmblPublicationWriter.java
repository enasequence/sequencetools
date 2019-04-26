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
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the publication lines.
 */
public class EmblPublicationWriter extends FlatFileWriter {

    private Publication publication;

    public EmblPublicationWriter(Entry entry, Publication publication, WrapType wrapType) {
        super(entry, wrapType);
        this.publication = publication;
    }
    
    public boolean write(Writer writer) throws IOException {
    	boolean writeBlock = false;
        writeBlock |=
        	new RXWriter(entry, publication, wrapType).write(writer);
        writeBlock |=
        	new RGWriter(entry, publication, wrapType).write(writer);
        writeBlock |=
        	new RAWriter(entry, publication, wrapType).write(writer);
        writeBlock |=
        	new RTWriter(entry, publication, wrapType).write(writer);
        writeBlock |=
        	new RLWriter(entry, publication, wrapType).write(writer);
        return writeBlock;
    }
}
