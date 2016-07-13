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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file RN lines.
 */
public class RNReader extends SingleLineBlockReader {
	
	public RNReader(LineReader lineReader) {
		super(lineReader);
	}
	
	private static final Pattern PATTERN = Pattern.compile(
			"^\\s*\\[\\s*" +
			"(\\d+)" +  // reference number
			"\\s*\\]\\s*$"
			);
	
	private static final int GROUP_REFERENCE_NUMBER = 1;

	@Override
	public String getTag() {
		return EmblTag.RN_TAG;
	}
	
	@Override
	protected void read(String block) {
		FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
			return;
		}
		getCache().resetReferenceCache();
		Reference reference = getCache().getReference();
		reference.setNumberExists(true);
		entry.addReference(reference);
		int referenceNumber = matcher.getInteger(GROUP_REFERENCE_NUMBER);
		if (referenceNumber > 0) {
			reference.setReferenceNumber(referenceNumber);
		} else {
			error("RN.1", referenceNumber);
		}
		reference.setOrigin(getOrigin());
	}
}
