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
package uk.ac.ebi.embl.api.entry.qualifier;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.validation.ValidationException;

public class TranslTableQualifier extends Qualifier implements Serializable {
	
	private static final long serialVersionUID = 9135163661545416830L;
	
	private static final Pattern PATTERN = Pattern.compile(
		"^(\\d+)$");
	
	protected TranslTableQualifier(String value) {
		super(TRANSL_TABLE_QUALIFIER_NAME, value);
	}

    public Integer getTranslationTable() throws ValidationException {
    	if (getValue() == null) {
    		return null;
    	}
        Matcher matcher = PATTERN.matcher(getValue());
        if (!matcher.matches()){
        	throwValueException();
        }
        return Integer.parseInt(matcher.group(1));
    }	
}
