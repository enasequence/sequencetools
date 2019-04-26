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

import uk.ac.ebi.embl.api.contant.Constant;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.api.entry.Entry;

/** Flat file writer for the unpublished lines.
 */
public class GenbankUnpublishedWriter extends FlatFileWriter {

	public GenbankUnpublishedWriter(Entry entry, WrapType wrapType) {
		super(entry, wrapType);
	}

	public boolean write(Writer writer) throws IOException {
		writer.write(GenbankPadding.JOURNAL_PADDING);
		writer.write(Constant.JOURNAL_UNPUBLISHED_VALUE);
		writer.write("\n");			
		return true;
	}
}
