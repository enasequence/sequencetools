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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.AssemblyinfoEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.TSVFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.UnlocalisedListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlan
{
	SubmissionOptions options;
	FileValidationCheck check = null;
    DB sequenceDB =null;
    public SubmissionValidationPlan(SubmissionOptions options) {
		this.options =options;
	}
	public void execute() throws ValidationEngineException {
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
    		if(check.isHasAnnotationOnlyFlatfile())
              sequenceDB=DBMaker.fileDB(options.reportDir.get()+File.separator+getSequenceDbname()).deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make();
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
		if(options.context.get().getFileTypes().contains(FileType.TSV))
			validateTsvfile();
		if(Context.genome==options.context.get())
		{
			check.validateDuplicateEntryNames();
			check.validateSequencelessChromosomes();
			throwValidationResult(uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(options.ignoreErrors, getSequencecount(0), getSequencecount(1), getSequencecount(2)));
			if(!options.isRemote)
			{
			registerSequences();
			writeUnplacedList();
			}
		} else {
			writeSequenceInfo();
		}
		if(sequenceDB!=null)
			sequenceDB.close();
		}catch(ValidationEngineException e)
		{
			try {
			if(!options.isRemote&&options.context.isPresent()&&options.context.get()==Context.genome&&check!=null&&check.getMessageStats()!=null)
				check.getReporter().writeToFile(Paths.get(options.reportDir.get()),check.getMessageStats());
			}catch(Exception ex)
			{
				throw new ValidationEngineException(e.getMessage()+"\n Failed to write error message stats: "+ex.getMessage(),e.getErrorType());
			}
			throw new ValidationEngineException(e.getMessage(),e.getErrorType());
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
	}

	private void createMaster() throws ValidationEngineException
	{
		check = new MasterEntryValidationCheck(options);
		if(!check.check())
			throw new ValidationEngineException("Master entry validation failed",ReportErrorType.VALIDATION_ERROR );
	}

	private void validateChromosomeList() throws ValidationEngineException
	{
		check = new ChromosomeListFileValidationCheck(options);
		for(SubmissionFile chromosomeListFile:options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST))
		{			
				if(!check.check(chromosomeListFile))
				throwException(FileType.CHROMOSOME_LIST,chromosomeListFile);
		}
	}

	private void validateFasta() throws ValidationEngineException
	{
		check = new FastaFileValidationCheck(options);
		for(SubmissionFile fastaFile:options.submissionFiles.get().getFiles(FileType.FASTA))
		{
			if(sequenceDB!=null)
				check.setSequenceDB(sequenceDB);
			if(!check.check(fastaFile))
				throwException(FileType.FASTA,fastaFile);
		}
	}

	private void validateFlatfile() throws ValidationEngineException
	{
		check = new FlatfileFileValidationCheck(options);
		for(SubmissionFile flatfile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			if(sequenceDB!=null)
				check.setSequenceDB(sequenceDB);
			if(!check.check(flatfile))
				throwException(FileType.FLATFILE,flatfile);
		}
	}

	private void validateAGP() throws ValidationEngineException
	{
		check = new AGPFileValidationCheck(options);
		for(SubmissionFile agpFile:options.submissionFiles.get().getFiles(FileType.AGP))
		{
			if(sequenceDB!=null)
				check.setSequenceDB(sequenceDB);
			if(!check.check(agpFile))
				throwException(FileType.AGP,agpFile);
		}
	}

	private void validateUnlocalisedList() throws ValidationEngineException
	{
		check = new UnlocalisedListFileValidationCheck(options);
		for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
		{			
			if(!check.check(unlocalisedListFile))
				throwException(FileType.UNLOCALISED_LIST,unlocalisedListFile);
		}
	}
	
	private void registerSequences() throws ValidationEngineException 
	{
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.sequenceInfo,options.reportDir.get(),AssemblySequenceInfo.sequencefileName);
		AssemblySequenceInfo.writeListObject(FileValidationCheck.chromosomeEntryNames,options.reportDir.get(),AssemblySequenceInfo.chromosomefileName);
		AssemblySequenceInfo.writeListObject(FileValidationCheck.contigEntryNames,options.reportDir.get(),AssemblySequenceInfo.contigfileName);
		AssemblySequenceInfo.writeListObject(FileValidationCheck.scaffoldEntryNames,options.reportDir.get(),AssemblySequenceInfo.scaffoldfileName);
	}

	private void writeSequenceInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeObject(FileValidationCheck.getSequenceCount(),options.reportDir.get(),AssemblySequenceInfo.sequencefileName);
	}
	
	
	private void validateAnnotationOnlyFlatfile() throws ValidationEngineException
	{
		check = new AnnotationOnlyFlatfileValidationCheck(options);
		for(SubmissionFile annotationOnlyFlatfile:options.submissionFiles.get().getFiles(FileType.ANNOTATION_ONLY_FLATFILE))
		{
			if(sequenceDB!=null)
				check.setSequenceDB(sequenceDB);
			if(!check.check(annotationOnlyFlatfile))
				throwException(FileType.ANNOTATION_ONLY_FLATFILE, annotationOnlyFlatfile);
		}
	}
	
	private void validateTsvfile() throws ValidationEngineException
	{
		check = new TSVFileValidationCheck(options);
		for(SubmissionFile tsvFile:options.submissionFiles.get().getFiles(FileType.TSV))
		{
		    	if(!check.check(tsvFile))
				throwException(FileType.TSV,tsvFile);
		}
	}
	private String getSequenceDbname()
	{
		 return ".sequence"+new SimpleDateFormat("yyMMddhhmmssMs").format(new Date());
	     
	}
	
	private void throwException(FileType fileTpe,SubmissionFile submissionFile) throws ValidationEngineException
	{
		throw new ValidationEngineException(String.format("%s file validation failed : %s, Please see the error report: %s", fileTpe.name().toLowerCase(),submissionFile.getFile().getName(),check.getReportFile(submissionFile).toFile()),ReportErrorType.VALIDATION_ERROR);
	}
	
	@SuppressWarnings("deprecation")
	private void throwValidationResult(ValidationResult result) throws ValidationEngineException
	{
		if(result==null||result.isValid())
			return;
		StringBuilder messages = new StringBuilder();
		for(ValidationMessage message:result.getMessages())
		{
			messages.append(message.getMessage()+"\n");
		}
		
		throw new ValidationEngineException(StringUtils.chopNewline(messages.toString()),ReportErrorType.VALIDATION_ERROR);
	}
	
  private long getSequencecount(int assemblyLevel)
  {
	  return FileValidationCheck.sequenceInfo.values().stream().filter(p->p.getAssemblyLevel()==0).count();
  }
  
  private void writeUnplacedList() throws IOException, ValidationEngineException
  {

	  try {
		  Files.deleteIfExists(Paths.get(options.processDir.get(),"unplaced.txt"));
	  }catch(Exception e)
	  {
		  throw new ValidationEngineException("Failed to delete unplaced file: "+e.getMessage());
	  }
	  try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(options.processDir.get()+File.separator+"unplaced.txt")))
	  {
		  oos.writeObject(FileValidationCheck.unplacedEntryNames);

	  }catch(Exception e)
	  {
		  throw new ValidationEngineException("Failed to write unplaced file: "+e.getMessage());
	  }
  }
}

