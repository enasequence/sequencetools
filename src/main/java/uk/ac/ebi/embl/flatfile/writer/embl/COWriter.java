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
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the CO lines.
 */
public class COWriter extends FlatFileWriter {

	public COWriter(Entry entry, WrapType wrapType) {
		super(entry, wrapType);
		setWrapChar(WrapChar.WRAP_CHAR_COMMA);
	}

	public boolean write(Writer writer) throws IOException {
		if (!entry.hasContigs()) {
			return false;
		}
		StringBuilder block = new StringBuilder();
		block.append("join(");
		boolean firstContig = true;
		for (Location contig : entry.getSequence().getContigs()) {
			if (!firstContig) {
				block.append(",");
			}
			else {
				firstContig = false;
			}
			if (contig instanceof RemoteBase) {
				// We don't support single base rendering on the CO line. Replace any
				// single bases with a range when writing the flat file.
				RemoteBase remoteBase = (RemoteBase) contig;
				contig = new RemoteRange(
						remoteBase.getAccession(),
						remoteBase.getVersion(),
						remoteBase.getBeginPosition(),
						remoteBase.getBeginPosition());
			}
			FeatureLocationWriter.renderLocation(block, contig, false, false, false);
		}
		block.append(")");
		writeBlock(writer, EmblPadding.CO_PADDING, block.toString());
		return true;
	}
}
