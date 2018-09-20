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

import java.io.BufferedReader;
import java.io.FileReader;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

@Description("")
public class MasterEntryValidationCheck extends FileValidationCheck
{

	public MasterEntryValidationCheck(EmblEntryValidationPlanProperty property) 
	{
		super(property);
	}	
	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid =true;
		try(BufferedReader fileReader= new BufferedReader(new FileReader(submissionFile.getFile())))
		{
		EmblEntryReader emblReader = new EmblEntryReader(fileReader,EmblEntryReader.Format.EMBL_FORMAT,submissionFile.getFile().getName());
		ValidationResult parseResult = emblReader.read();
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
