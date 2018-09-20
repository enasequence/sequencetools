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

import java.util.List;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.ChromosmeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
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
		List<SubmissionFile> chromosomelistSubmissionFiles=options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST);
		List<SubmissionFile> fastaSubmissionFiles=options.submissionFiles.get().getFiles(FileType.FASTA);
		List<SubmissionFile> flatfileSubmissionFiles=options.submissionFiles.get().getFiles(FileType.FLATFILE);
		List<SubmissionFile> agpSubmissionFiles=options.submissionFiles.get().getFiles(FileType.AGP);
		List<SubmissionFile> unlocalisedSubmissionFiles=options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST);
		
		for(SubmissionFile chromosomeListFile:chromosomelistSubmissionFiles)
		{
			check = new ChromosmeListFileValidationCheck(options.getEntryValidationPlanProperty());
			if(!check.check(chromosomeListFile))
				throw new ValidationEngineException("chromosome list file validation failed: "+chromosomeListFile.getFile().getName());
		}

		for(SubmissionFile fastaFile:fastaSubmissionFiles)
		{
			check = new FastaFileValidationCheck(options.getEntryValidationPlanProperty());
			if(!check.check(fastaFile))
				throw new ValidationEngineException("fasta file validation failed: "+fastaFile.getFile().getName());
		}

		for(SubmissionFile flatfile:flatfileSubmissionFiles)
		{
			check = new FlatfileFileValidationCheck(options.getEntryValidationPlanProperty());
			if(!check.check(flatfile))
				throw new ValidationEngineException("flat file validation failed: "+flatfile.getFile().getName());
		}

		for(SubmissionFile agpFile:agpSubmissionFiles)
		{
			check = new AGPFileValidationCheck(options.getEntryValidationPlanProperty());
			if(!check.check(agpFile))
				throw new ValidationEngineException("AGP file validation failed: "+agpFile.getFile().getName());
		}
		
		for(SubmissionFile unlocalisedListFile:unlocalisedSubmissionFiles)
		{
			check = new AGPFileValidationCheck(options.getEntryValidationPlanProperty());
			if(!check.check(unlocalisedListFile))
				throw new ValidationEngineException("unlocalised list file validation failed: "+unlocalisedListFile.getFile().getName());
		}
		return null;
	}
}
