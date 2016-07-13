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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;

public class UnpublishedMatcher extends FlatFileMatcher {

	public UnpublishedMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}

	private static final Pattern PATTERN = Pattern.compile(
			"^((Unpublished)|(Published\\s*Only\\s*in\\s*DataBase))\\s*(\\.)?\\s*"
		);
		
	public Unpublished getUnpublished(Publication publication) {
		Unpublished unpublished = null;
		if (publication != null) {
			unpublished = (new ReferenceFactory()).createUnpublished(publication);
			unpublished.setOrigin(publication.getOrigin());
		}
		else {
			unpublished = (new ReferenceFactory()).createUnpublished();
		}
		return unpublished;
	}
}
