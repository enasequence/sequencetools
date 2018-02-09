/*******************************************************************************
 * Copyright 2012-13 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.check.entry;

import org.apache.commons.lang3.CharUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Flatfile contains non-ascii characters: \"{0}\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class AsciiCharacterCheck extends EntryValidationCheck
{
	private static String ASCII_CHARACTER_CHECK = "AsciiCharacterCheck_1";
	
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		
		if(entry==null)
			return result;
		
		if(isNonAscii(entry.getComment().getText()))
			reportError(entry.getComment().getOrigin(), ASCII_CHARACTER_CHECK,entry.getComment().getText());
		if(isNonAscii(entry.getDescription().getText()))
			reportError(entry.getDescription().getOrigin(), ASCII_CHARACTER_CHECK,entry.getDescription().getText());
		
		for(Reference reference: entry.getReferences())
		{
			if(isNonAscii(reference.getPublication().getTitle()))
					reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK,reference.getPublication().getTitle());

			for(Person author:reference.getPublication().getAuthors())
			{ 
				if(isNonAscii(author.getFirstName()))
				reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK,author.getFirstName());
				if(isNonAscii(author.getSurname()))
				reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK,author.getSurname());
			}
			
		}
		for(Feature feature:entry.getFeatures())
	    {
	    	for(Qualifier qualifier: feature.getQualifiers())
	    	{
	    		if(isNonAscii(qualifier.getValue()))
	    		  reportError(qualifier.getOrigin(), ASCII_CHARACTER_CHECK,qualifier.getValue());
	    	}
	    }
		return result;
	}
	
	
	private boolean isNonAscii(String text)
	{
		if(text==null)
			return false;

		for(int i=0; i<text.length();i++)
		{
			if(!CharUtils.isAscii(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}
}
