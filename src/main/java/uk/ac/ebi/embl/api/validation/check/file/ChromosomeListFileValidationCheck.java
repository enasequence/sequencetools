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

import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.GenomeAssemblyValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.ChromosomeListFileReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Description("")
public class ChromosomeListFileValidationCheck extends FileValidationCheck
{

	public ChromosomeListFileValidationCheck(SubmissionOptions options) {
		super(options);
	}

	@Override
	public ValidationPlanResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		Origin origin =null;
		ValidationPlanResult validationResult = new ValidationPlanResult();
		try
		{
			clearReportFile(getReportFile(submissionFile));

			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.CHROMOSOME_LIST))
			{
				reportError(getReportFile(submissionFile), "chromosome_list");
				return validationResult;
			}
			ChromosomeListFileReader reader = new ChromosomeListFileReader(submissionFile.getFile());

			ValidationResult parseResult = reader.read();
			validationResult.append(parseResult);
			if(!parseResult.isValid())
			{
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
				addMessageKeys(parseResult.getMessages());
			}
			getOptions().getEntryValidationPlanProperty().fileType.set(FileType.CHROMOSOMELIST);
			GenomeAssemblyValidationPlan plan = new GenomeAssemblyValidationPlan(getOptions().getEntryValidationPlanProperty());

			List<ChromosomeEntry> chromosomeEntries = reader.getentries();
			for(ChromosomeEntry entry : chromosomeEntries)
			{
				origin = entry.getOrigin();
				ValidationPlanResult planResult = plan.execute(entry);
				validationResult.append(planResult);
				if(!planResult.isValid())
				{
    				getReporter().writeToFile(getReportFile(submissionFile), planResult);
    				addMessageKeys(parseResult.getMessages());
				}
				if (entry.getObjectName() != null)
				  chromosomeNameQualifiers.put(entry.getObjectName().toUpperCase(), entry);
				if(entry.getChromosomeName()!=null)
					chromosomeNames.add(entry.getChromosomeName().toUpperCase());

			}

		} catch (IOException e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			throw new ValidationEngineException(e);
		}

		return validationResult;
	}

 	public HashMap<String,ChromosomeEntry> getChromosomeQualifeirs()
 	{
 		return chromosomeNameQualifiers;
 	}

	@Override
	public ValidationPlanResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}

}
