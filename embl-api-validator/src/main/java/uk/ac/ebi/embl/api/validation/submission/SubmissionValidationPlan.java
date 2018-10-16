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

import java.nio.file.Paths;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.UnlocalisedListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlan
{
	SubmissionOptions options;
	FileValidationCheck check = null;
	
	public SubmissionValidationPlan(SubmissionOptions options) {
		this.options =options;
	}
	public boolean execute() throws ValidationEngineException {
		try
		{
		
		options.init();
		//Validation Order shouldn't be changed
		if(options.context.get().getFileTypes().contains(FileType.MASTER))
			createMaster();
		if(options.context.get().getFileTypes().contains(FileType.CHROMOSOME_LIST))
			validateChromosomeList();
		if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE))
		 { 
			FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options);
			check.getAnnotationFlatfile();
		 }
		if(options.context.get().getFileTypes().contains(FileType.AGP))
		{
			AGPFileValidationCheck check = new AGPFileValidationCheck(options);
			check.getAGPEntries();
		}
		if(options.context.get().getFileTypes().contains(FileType.FASTA))
			validateFasta();
		if(options.context.get().getFileTypes().contains(FileType.FLATFILE))
			validateFlatfile();
		if(options.context.get().getFileTypes().contains(FileType.AGP))
			validateAGP();
		if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE))
			validateAnnotationOnlyFlatfile();
		if(options.context.get().getFileTypes().contains(FileType.UNLOCALISED_LIST))
			validateUnlocalisedList();
		if(Context.genome==options.context.get())
		{
			check.validateDuplicateEntryNames();
			check.validateSequencelessChromosomes();
			if(!options.isRemote)
			registerSequences();
			if(check.getSequenceDB()!=null)
				check.getSequenceDB().close();
		}
		return true;
		}catch(Exception e)
		{
			try {
			if(!options.isRemote&&options.context.isPresent()&&options.context.get()==Context.genome&&check!=null&&check.getMessageStats()!=null)
				check.getReporter().writeToFile(Paths.get(options.reportDir.get()),check.getMessageStats());
			}catch(Exception ex)
			{
				throw new ValidationEngineException(e.getMessage()+"\n Failed to write error message stats: "+ex.getMessage());
			}
			throw new ValidationEngineException(e.getMessage());
		}
	}

	private void createMaster() throws ValidationEngineException
	{
		check = new MasterEntryValidationCheck(options);
		if(!check.check())
			throw new ValidationEngineException("Master entry validation failed" );
	}

	private void validateChromosomeList() throws ValidationEngineException
	{
		for(SubmissionFile chromosomeListFile:options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST))
		{			
			check = new ChromosomeListFileValidationCheck(options);
			if(!check.check(chromosomeListFile))
				throw new ValidationEngineException("chromosome list file validation failed: "+chromosomeListFile.getFile().getName());
		}
	}

	private void validateFasta() throws ValidationEngineException
	{
		for(SubmissionFile fastaFile:options.submissionFiles.get().getFiles(FileType.FASTA))
		{
			check = new FastaFileValidationCheck(options);
			if(!check.check(fastaFile))
				throw new ValidationEngineException("fasta file validation failed: "+fastaFile.getFile().getName());
		}
	}

	private void validateFlatfile() throws ValidationEngineException
	{
		for(SubmissionFile flatfile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			check = new FlatfileFileValidationCheck(options);
			if(!check.check(flatfile))
				throw new ValidationEngineException("flat file validation failed: "+flatfile.getFile().getName());
		}
	}

	private void validateAGP() throws ValidationEngineException
	{
		for(SubmissionFile agpFile:options.submissionFiles.get().getFiles(FileType.AGP))
		{
			check = new AGPFileValidationCheck(options);
			if(!check.check(agpFile))
				throw new ValidationEngineException("AGP file validation failed: "+agpFile.getFile().getName());
		}
	}

	private void validateUnlocalisedList() throws ValidationEngineException
	{
		for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
		{
			check = new UnlocalisedListFileValidationCheck(options);
			if(!check.check(unlocalisedListFile))
				throw new ValidationEngineException("unlocalised list file validation failed: "+unlocalisedListFile.getFile().getName());
		}
	}
	
	private void registerSequences() throws ValidationEngineException 
	{
		try
		{
			EntryDAOUtils entryDAOUtils= new EntryDAOUtilsImpl(options.enproConnection.get());
			entryDAOUtils.registerSequences(FileValidationCheck.contigs, options.analysisId.get(), 0);
			entryDAOUtils.registerSequences(FileValidationCheck.scaffolds, options.analysisId.get(),1);
			entryDAOUtils.registerSequences(FileValidationCheck.chromosomes,options.analysisId.get(),2);
		}catch(Exception e)
		{
          throw new ValidationEngineException("Assembly sequence registration failed: "+e.getMessage());
		}
	}
	
	private void validateAnnotationOnlyFlatfile() throws ValidationEngineException
	{
		for(SubmissionFile annotationOnlyFlatfile:options.submissionFiles.get().getFiles(FileType.ANNOTATION_ONLY_FLATFILE))
		{
			check = new AnnotationOnlyFlatfileValidationCheck(options);
     		if(!check.check(annotationOnlyFlatfile))
				throw new ValidationEngineException("flat file validation failed for annotation only entries : "+annotationOnlyFlatfile.getFile().getName());
		}
	}
}
