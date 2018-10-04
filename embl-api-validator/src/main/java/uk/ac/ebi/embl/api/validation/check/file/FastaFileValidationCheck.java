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
import java.io.PrintWriter;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.fasta.reader.FastaFileReader;
import uk.ac.ebi.embl.fasta.reader.FastaLineReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

@Description("")
public class FastaFileValidationCheck extends FileValidationCheck
{

	public FastaFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	
	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid=true;
		try(BufferedReader fileReader= new BufferedReader(new FileReader(submissionFile.getFile()));PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			FastaFileReader reader = new FastaFileReader( new FastaLineReader( fileReader));
			ValidationResult parseResult = reader.read();
			EmblEntryValidationPlan validationPlan =null;
			if(!parseResult.isValid())
			{
				valid = false;
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(getOptions().reportDir.get(), submissionFile.getFile().getName()), parseResult);
				addMessagekey(parseResult);
			}
			while(reader.isEntry())
			{
				Entry entry=reader.getEntry();
				if(getOptions().context.get()==Context.genome)
				{
					collectContigInfo(entry);
				}
            	getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession().toUpperCase()));
            	getOptions().getEntryValidationPlanProperty().fileType.set(FileType.FASTA);
            	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
            	appendHeader(entry);
				ValidationPlanResult planResult=validationPlan.execute(entry);
				if(!planResult.isValid())
				{
					valid = false;
					if(getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(getOptions().reportDir.get(), submissionFile.getFile().getName()), planResult);
					for(ValidationResult result: planResult.getResults())
					{
						addMessagekey(result);
					}
				}
				else
				{
					if(fixedFileWriter!=null)
					new EmblEntryWriter(entry).write(getFixedFileWriter(submissionFile));
				}
				reader.read();
			}
		}catch (Exception e) {
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;	
	}

	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	
	

}
