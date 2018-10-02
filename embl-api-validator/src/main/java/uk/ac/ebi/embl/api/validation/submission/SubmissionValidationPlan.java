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
package uk.ac.ebi.embl.api.validation.submission;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlan
{
	SubmissionOptions options;
	public SubmissionValidationPlan(SubmissionOptions options) {
		this.options =options;
	}
	public ValidationPlanResult execute() throws ValidationEngineException {
		options.init();
		FileValidationCheck check = null;
	
		check = new MasterEntryValidationCheck(options);
		if(!check.check())
		{
			throw new ValidationEngineException("Master entry validation failed" );
		}
		for(SubmissionFile chromosomeListFile:options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST))
		{			
			check = new ChromosomeListFileValidationCheck(options);
			if(!check.check(chromosomeListFile))
				throw new ValidationEngineException("chromosome list file validation failed: "+chromosomeListFile.getFile().getName());
		}
		check.readAGPfiles();
		for(SubmissionFile fastaFile:options.submissionFiles.get().getFiles(FileType.FASTA))
		{
			check = new FastaFileValidationCheck(options);
			if(!check.check(fastaFile))
				throw new ValidationEngineException("fasta file validation failed: "+fastaFile.getFile().getName());
		}

		for(SubmissionFile flatfile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			check = new FlatfileFileValidationCheck(options);
			if(!check.check(flatfile))
				throw new ValidationEngineException("flat file validation failed: "+flatfile.getFile().getName());
		}

		for(SubmissionFile agpFile:options.submissionFiles.get().getFiles(FileType.AGP))
		{
			check = new AGPFileValidationCheck(options);
			if(!check.check(agpFile))
				throw new ValidationEngineException("AGP file validation failed: "+agpFile.getFile().getName());
		}
		
		for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
		{
			check = new AGPFileValidationCheck(options);
			if(!check.check(unlocalisedListFile))
				throw new ValidationEngineException("unlocalised list file validation failed: "+unlocalisedListFile.getFile().getName());
		}
		if(Context.genome==options.context.get())
		{
			check.validateDuplicateEntryNames();
			check.validateSequencelessChromosomes();
		}
		return null;
	}
	
	
      
}
