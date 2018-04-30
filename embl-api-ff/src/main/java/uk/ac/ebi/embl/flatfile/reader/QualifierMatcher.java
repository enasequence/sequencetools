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

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

import java.util.regex.Pattern;

public class QualifierMatcher extends FlatFileMatcher {

	public QualifierMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);
	}

	private static final Pattern PATTERN = Pattern.compile(
		"\\/([a-zA-Z1-9-_]+)\\s*=?(.*)?");

	private static final int GROUP_QUALIFIER_NAME = 1;
	private static final int GROUP_QUALIFIER_VALUE = 2;

	public Qualifier getQualifier()  {
		QualifierFactory qualifierFactory = new QualifierFactory();
		String qualifierName = getString(GROUP_QUALIFIER_NAME);
		String qualifierValue = getString(GROUP_QUALIFIER_VALUE);

		Qualifier qualifier=qualifierFactory.createQualifier(qualifierName);
		if (qualifierValue != null) {

			int nofQuotes = StringUtils.countMatches(qualifierValue, "\"");
			if (nofQuotes != 0) {
				if (qualifierValue.indexOf('"') != 0 && qualifierValue.lastIndexOf('"') != qualifierValue.length() - 1) {
					error("FT.10", qualifierName, qualifierValue);
				}
				qualifierValue = FlatFileUtils.trimLeft(qualifierValue, '"');
				qualifierValue = FlatFileUtils.trimRight(qualifierValue, '"');
				if(nofQuotes > 2 && StringUtils.countMatches(qualifierValue, "\"") > 0){
					error("FT.10", qualifierName, qualifierValue);
				}
			}
			if(!getReader().lineReader.getReaderOptions().isIgnoreParserErrors()) {
				qualifierValue = Utils.escapeASCIIHtmlEntities(qualifierValue).toString();
			}

			qualifier.setValue(qualifierValue);
			return qualifier;
		} else
		{
			return qualifier;
		}
	}
}
