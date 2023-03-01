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

import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file AC lines. Accession number
 * ranges will not be expanded.
 */
public class ACReader extends MultiLineBlockReader {
	
	public ACReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_SPACE);
	}

	@Override
	public String getTag() {
		return EmblTag.AC_TAG;
	}
	
	@Override
	protected void read(String block) {
		String idLinePrimaryAccession=entry.getPrimaryAccession();
		boolean noPrimaryAccession = (block.charAt(0) == ';');
		boolean isFirstAccession = true;
		for (String accession : FlatFileUtils.split(block, ";")) {
			if (isFirstAccession) {
				if (noPrimaryAccession) {
					Text secAccession = new Text(accession, getOrigin());
					entry.addSecondaryAccessions(Utils.expandRanges(secAccession));
				} else {
					if (idLinePrimaryAccession != null
							&& !idLinePrimaryAccession.equalsIgnoreCase(accession))
						error("AC.1");
					else {
						if (!accession.equals("XXX")) {
							entry.setPrimaryAccession(accession);
						}
					}
				}
				isFirstAccession = false;
			} else {
				Text secAccession = new Text(accession, getOrigin());
				entry.addSecondaryAccessions(Utils.expandRanges(secAccession));
			}
		}

	}	
}
