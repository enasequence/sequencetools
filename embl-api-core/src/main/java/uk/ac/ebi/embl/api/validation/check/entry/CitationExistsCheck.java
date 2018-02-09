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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Citation information missing"
		+ "Reference Author (RA) missing for RN [{0}]"
		+ "Reference Location (RL) missing for RN [{0}]")

@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_CONTIG,ValidationScope.ASSEMBLY_SCAFFOLD,ValidationScope.ASSEMBLY_CHROMOSOME,ValidationScope.EMBL_TEMPLATE,ValidationScope.ASSEMBLY_TRANSCRIPTOME})

public class CitationExistsCheck extends EntryValidationCheck {

	private final static String REFERENCE_MISSING_MESSAGE_ID = "CitationExistsCheck_1";
	private static String REFERENCE_AUTHOR_MESSAGE_ID = "CitationExistsCheck_2";
	private static String REFERENCE_LOCATION_MESSAGE_ID = "CitationExistsCheck_3";
		
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		if (entry.getReferences().isEmpty())
		{
			reportError(entry.getOrigin(), REFERENCE_MISSING_MESSAGE_ID);
		} else
		{
			for (Reference ref : entry.getReferences())
			{
				Publication publication= ref.getPublication();
				
				if (!ref.isLocationExists())
				{
					reportError(ref.getOrigin(), REFERENCE_LOCATION_MESSAGE_ID,ref.getReferenceNumber());
				}

				if (!ref.isAuthorExists() || publication.getAuthors().isEmpty())
				{
					if(ref.getPublication().getConsortium()==null || ref.getPublication().getConsortium().length()==0)
					reportError(ref.getOrigin(), REFERENCE_AUTHOR_MESSAGE_ID,ref.getReferenceNumber());
				}
			
			}
		}

		return result;
	}

}
