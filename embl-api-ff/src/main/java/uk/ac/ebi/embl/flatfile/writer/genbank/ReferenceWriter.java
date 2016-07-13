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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the REFERENCE lines.
 */
public class ReferenceWriter extends FlatFileWriter {

	protected Reference reference;

	public ReferenceWriter(Entry entry, Reference reference, WrapType wrapType) {
		super(entry, wrapType);
		setWrapChar(WrapChar.WRAP_CHAR_SEMICOLON);
		this.reference = reference;
	}

	public boolean write(Writer writer) throws IOException {
		StringBuilder block =  new StringBuilder();
		if (reference.getReferenceNumber() != null) {			
			block.append(Integer.toString(reference.getReferenceNumber()));
		}
		boolean isFirstLocation = true;
		for (Location location : reference.getLocations().getLocations()) {
			if (isFirstLocation) {
				isFirstLocation = false;				
				block.append("  ");
				block.append("(bases ");
			}
			else {
				block.append("; ");
			}
			block.append(location.getBeginPosition().toString());
			block.append(" to ");
			block.append(location.getEndPosition().toString());
		}
		if (!isFirstLocation) {
			block.append(")");
		}
    	writeBlock(writer, GenbankPadding.REFERENCE_PADDING, 
    			GenbankPadding.BLANK_PADDING, block.toString());
		return true;
	}
}

