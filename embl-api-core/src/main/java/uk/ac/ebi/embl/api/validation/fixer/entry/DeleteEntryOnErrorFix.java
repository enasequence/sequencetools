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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("delete Entry on error")
public class DeleteEntryOnErrorFix extends EntryValidationCheck
{

	private final static String FIX_ID = "DeleteEntryOnErrorFix";

	public DeleteEntryOnErrorFix()
	{
	}

	/*
	 * it will report the fixer message, if entry is set to be deleted.
	 */
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry.isDelete())
		{
			reportMessage(Severity.FIX, entry.getOrigin(), FIX_ID, entry.getPrimaryAccession());
		}
		return result;
	}

}
