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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.GenomeAssemblyValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.ChromosomeListFileReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

@Description("")
public class ChromosomeListFileValidationCheck extends FileValidationCheck
{

	public ChromosomeListFileValidationCheck(SubmissionOptions options) {
		super(options);
	}

	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid =true;
		Origin origin =null;
		try
		{
			clearReportFile(getReportFile(submissionFile));

			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.CHROMOSOME_LIST))
			{
				ValidationResult result = new ValidationResult();
				valid = false;
				result.append(FlatFileValidations.message(Severity.ERROR, "InvalidFileFormat","chromosome_list"));
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), result);
				addMessagekey(result);
				return valid;
			}
			ChromosomeListFileReader reader = new ChromosomeListFileReader(submissionFile.getFile());

			ValidationResult parseResult = reader.read();
			if(!parseResult.isValid())
			{
				valid = false;
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
				addMessagekey(parseResult);
			}
			getOptions().getEntryValidationPlanProperty().fileType.set(FileType.CHROMOSOMELIST);
			GenomeAssemblyValidationPlan plan = new GenomeAssemblyValidationPlan(getOptions().getEntryValidationPlanProperty());

			List<ChromosomeEntry> chromosomeEntries = reader.getentries();
			for(ChromosomeEntry entry : chromosomeEntries)
			{
				origin = entry.getOrigin();
				ValidationPlanResult planResult=plan.execute(entry);
				if(!planResult.isValid())
				{
					valid = false;
    				getReporter().writeToFile(getReportFile(submissionFile), planResult);
					for(ValidationResult result: planResult.getResults())
					{
						addMessagekey(result);
					}
				}
				if(entry.getObjectName()!=null)
					chromosomeNameQualifiers.put(entry.getObjectName(),entry.getQualifiers(taxonHelper.isChildOf(masterEntry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.ORGANISM_QUALIFIER_NAME), "Viruses")));
				if(entry.getChromosomeName()!=null)
					chromosomeNames.add(entry.getChromosomeName().toUpperCase());

			}

		} catch (ValidationEngineException e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			throw e;
		}
		catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage(), e);
		}
		return valid;
	}

 	public HashMap<String,List<Qualifier>> getChromosomeQualifeirs()
 	{
 		return chromosomeNameQualifiers;
 	}

	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}

}
