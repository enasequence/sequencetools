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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.util.List;

import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.ContigLocationsMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file CO lines.
 */
public class COReader extends MultiLineBlockReader {

	public COReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_NOSPACE);
	}

	@Override
	public String getTag() {
		return EmblTag.CO_TAG;
	}
		
	@Override
	protected void read(String block) {
		ContigLocationsMatcher matcher = new ContigLocationsMatcher(this);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
		}
		else {
			List<Location> locations = matcher.getLocations();
			if (locations != null) {
				for (Location location : locations) {
					location.setOrigin(getOrigin());
				}
			}
			entry.getSequence().addContigs(locations);
		}
	}
}
