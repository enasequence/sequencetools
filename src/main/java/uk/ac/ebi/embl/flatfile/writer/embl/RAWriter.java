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
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/** Flat file writer for the RA lines.
 */
public class RAWriter extends FlatFileWriter {

    private Publication publication;

    public RAWriter(Entry entry, Publication publication, WrapType wrapType) {
        super(entry, wrapType);
        this.publication = publication;
        setWrapChar(WrapChar.WRAP_CHAR_COMMA);
    }

    public boolean write(Writer writer) throws IOException {
    	StringBuilder block = new StringBuilder();
    	writeAuthors(block, publication.getAuthors());
        block.append(";");
        writeBlock(writer, EmblPadding.RA_PADDING, block.toString());
        return true;
    }

    public static void writeAuthors(StringBuilder block, List<Person> authors) {
        boolean firstAuthor = true;
        for (Person author : authors) {
        	if (!firstAuthor) {
        		block.append(", ");
        	}
        	else {
        		firstAuthor = false;
        	}
        	if (!isBlankString(author.getSurname())) {
        		block.append(author.getSurname().trim());
        	}
        	if (!isBlankString(author.getFirstName())) {
            	block.append(" ");
        		block.append(author.getFirstName().trim());
        	}
        }
    }
}
