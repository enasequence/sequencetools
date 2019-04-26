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
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the RN lines.
 */
public class RNWriter extends FlatFileWriter {

	protected Reference reference;

	public RNWriter(Entry entry, Reference reference, WrapType wrapType) {
		super(entry, wrapType);
		this.reference = reference;
	}

	public boolean write(Writer writer) throws IOException {
		writer.write(EmblPadding.RN_PADDING);
		writer.write("[");
		if (reference.getReferenceNumber() != null) {
			writer.write(Integer.toString(reference.getReferenceNumber()));
		}
		writer.write("]\n");
		return true;
	}
}
