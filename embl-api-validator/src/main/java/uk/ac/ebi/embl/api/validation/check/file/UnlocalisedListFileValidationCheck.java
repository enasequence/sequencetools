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
package uk.ac.ebi.embl.api.validation.check.file;

import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.UnlocalisedListFileReader;

@Description("")
public class UnlocalisedListFileValidationCheck extends FileValidationCheck
{

	public UnlocalisedListFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid =true;
		try
		{
		UnlocalisedListFileReader reader = new UnlocalisedListFileReader(submissionFile.getFile());
		ValidationResult parseResult = reader.read();
		if(!parseResult.isValid())
		{
			valid = false;
		}
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;
	}
	
}
