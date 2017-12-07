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

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;

public class QualifierMatcher extends FlatFileMatcher {
   private boolean htmlEntValidationEnabled = true;

   public QualifierMatcher(FlatFileLineReader reader) {
      super(reader, PATTERN);
   }

   public QualifierMatcher(FlatFileLineReader reader, boolean executeHtmlEntityValidation) {
		super(reader, PATTERN);
		htmlEntValidationEnabled = executeHtmlEntityValidation;
	}
	 Pattern htmlEntityRegexPattern = Pattern.compile("&(?:\\#(?:([0-9]+)|[Xx]([0-9A-Fa-f]+))|([A-Za-z0-9]+));?");
	private static final Pattern PATTERN = Pattern.compile(
		"\\/([a-zA-Z1-9-_]+)\\s*=?(.*)?");

	private static final int GROUP_QUALIFIER_NAME = 1;
	private static final int GROUP_QUALIFIER_VALUE = 2;

	public Qualifier getQualifier() throws UnsupportedEncodingException {
		QualifierFactory qualifierFactory = new QualifierFactory();
		String qualifierName = getString(GROUP_QUALIFIER_NAME);
		String qualifierValue = getString(GROUP_QUALIFIER_VALUE);
		int nofQuotes=StringUtils.countMatches(qualifierValue, "\"");
		Qualifier qualifier=qualifierFactory.createQualifier(qualifierName);
		if (qualifierValue != null)
		{
			if (!qualifier.isValueQuoted() && nofQuotes != 0)
			{
				if (!qualifier.getName().equals(Qualifier.COMPARE_QUALIFIER_NAME) )
					error("FT.10", qualifierName, qualifierValue);
			} else if (qualifier.isValueQuoted())
			{
				if (nofQuotes == 0)
				{
					if( !qualifier.getName().equals(Qualifier.ANTICODON_QUALIFIER_NAME))
					error("FT.10", qualifierName, qualifierValue);
				} else
				{
					if (qualifierValue.indexOf('"') != 0 && qualifierValue.lastIndexOf('"') != qualifierValue.length() - 1)

					{
						error("FT.10", qualifierName, qualifierValue);
					} else if (nofQuotes % 2 != 0)
					{
						error("FT.10", qualifierName, qualifierValue);
					}
				 }
			}
			if (htmlEntValidationEnabled) {
            Matcher m = htmlEntityRegexPattern.matcher(qualifierValue);
            if (m.find())
            {
            	error("FT.13", qualifierName, qualifierValue);
            }
			}
			qualifierValue = FlatFileUtils.trimLeft(qualifierValue, '"');
			qualifierValue = FlatFileUtils.trimRight(qualifierValue, '"');
			
			qualifier.setValue(qualifierValue);
			return qualifier;
		} else
		{
			return qualifier;
		}
	}
}
