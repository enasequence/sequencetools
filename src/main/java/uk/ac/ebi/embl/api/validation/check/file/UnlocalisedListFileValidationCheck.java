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

import uk.ac.ebi.embl.api.entry.genomeassembly.UnlocalisedEntry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.GenomeAssemblyValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.UnlocalisedListFileReader;

import java.util.List;

@Description("")
public class UnlocalisedListFileValidationCheck extends FileValidationCheck
{

	public UnlocalisedListFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	@Override
	public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		ValidationResult validationResult = new ValidationResult();
		try
		{
			clearReportFile(getReportFile(submissionFile));
			UnlocalisedListFileReader reader = new UnlocalisedListFileReader(submissionFile.getFile());
			validationResult.append(reader.read());
			if(!validationResult.isValid())
			{
				getReporter().writeToFile(getReportFile(submissionFile), validationResult);
				addMessageKeys(validationResult.getMessages());
			}
			getOptions().getEntryValidationPlanProperty().fileType.set(FileType.UNLOCALISEDLIST);
			GenomeAssemblyValidationPlan plan = new GenomeAssemblyValidationPlan(getOptions().getEntryValidationPlanProperty());
			List<UnlocalisedEntry> unlocalisedEntries=reader.getentries();
			for(UnlocalisedEntry entry : unlocalisedEntries)
			{
				ValidationResult planResult=plan.execute(entry);
				planResult.append(validateValidChromosomeEntry(entry));
				planResult.append(validateValidUnlocalisedEntry(entry));
				validationResult.append(planResult);
				if(!planResult.isValid())
				{
				getReporter().writeToFile(getReportFile(submissionFile), planResult);
				addMessageKeys(planResult.getMessages());
				}
				unlocalisedEntryNames.add(entry.getObjectName().toUpperCase());
			}
		} catch (ValidationEngineException e) {
			throw e;
		}
		catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage(), e);
		}
		return validationResult;
	}
	@Override
	public ValidationResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}


	ValidationResult validateValidChromosomeEntry(UnlocalisedEntry unlocalisedEntry)
	{
		ValidationResult result = new ValidationResult();
		if(unlocalisedEntry.getChromosomeName()!=null)
		{
			if(chromosomeNames.size()!=0&&!chromosomeNames.contains(unlocalisedEntry.getChromosomeName().toUpperCase()))
			{
				ValidationMessage message = new ValidationMessage<>(Severity.ERROR, "UnlocalisedListChromosomeValidCheck",unlocalisedEntry.getChromosomeName());
				result.append(message);
			}
		}
		return result;
	}
	
	ValidationResult validateValidUnlocalisedEntry(UnlocalisedEntry unlocalisedEntry)
	{
		ValidationResult result = new ValidationResult();
		if(unlocalisedEntry.getObjectName()!=null)
		{
			if(entryNames.size()!=0&&!entryNames.contains(unlocalisedEntry.getObjectName().toUpperCase()))
			{
				ValidationMessage message = new ValidationMessage<>(Severity.ERROR, "UnlocalisedListUnlocalisedValidCheck",unlocalisedEntry.getObjectName());
				result.append(message);
			}
		}
		return result;
	}
}
