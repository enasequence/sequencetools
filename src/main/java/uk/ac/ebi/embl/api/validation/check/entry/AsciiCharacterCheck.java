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
@ExcludeScope(validationScope = {ValidationScope.NCBI , ValidationScope.NCBI_MASTER})
public class AsciiCharacterCheck extends EntryValidationCheck
{
	private static String ASCII_CHARACTER_CHECK = "AsciiCharacterCheck_1";
	
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		
		if(entry==null)
			return result;
		
		if(hasNonAscii(entry.getComment().getText())) {
			entry.getComment().setText(entry.getComment().getText().replaceAll("\\u00a0", " " ));
			if(hasNonAscii(entry.getComment().getText()))
				reportError(entry.getComment().getOrigin(), ASCII_CHARACTER_CHECK, entry.getComment().getText(), "Comment");
		}
		if(hasNonAscii(entry.getDescription().getText())) {
			entry.getDescription().setText(entry.getDescription().getText().replaceAll("\\u00a0", " " ));
			if(hasNonAscii(entry.getDescription().getText()))
				reportError(entry.getDescription().getOrigin(), ASCII_CHARACTER_CHECK, entry.getDescription().getText(),"Description");
		}
		
		for(Reference reference: entry.getReferences())
		{
			if (hasNonAscii(reference.getPublication().getTitle())) {
				reference.getPublication().setTitle(reference.getPublication().getTitle().replaceAll("\\u00a0", " "));
				if (hasNonAscii(reference.getPublication().getTitle()))
					reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK, reference.getPublication().getTitle(),"References");
			}

			for(Person author:reference.getPublication().getAuthors())
			{
				if (hasNonAscii(author.getFirstName())) {
					author.setFirstName(author.getFirstName().replaceAll("\\u00a0", " "));
					if (hasNonAscii(author.getFirstName()))
						reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK, author.getFirstName(),"First name");
				}
				if(hasNonAscii(author.getSurname())) {
					author.setSurname(author.getSurname().replaceAll("\\u00a0", " "));
					if (hasNonAscii(author.getSurname()))
						reportError(reference.getOrigin(), ASCII_CHARACTER_CHECK, author.getSurname(),"Last name");
				}
			}
			
		}
		for(Feature feature:entry.getFeatures())
	    {
	    	for(Qualifier qualifier: feature.getQualifiers())
	    	{
				if(hasNonAscii(qualifier.getValue())) {
					qualifier.setValue(qualifier.getValue().replaceAll("\\u00a0", " "));
					if (hasNonAscii(qualifier.getValue()))
						reportError(qualifier.getOrigin(), ASCII_CHARACTER_CHECK, qualifier.getValue(),"Qualifier value");
				}
	    	}
	    }
		return result;
	}
	
	
	private boolean hasNonAscii(String text)
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
