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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
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
	DB contigDB =null;
    String fastaFlagFileName ="fasta.validated";
    String agpFlagFileName ="agp.validated";
    String flatfileFlagFileName ="flatfile.validated";
    String chromosomelistFlagFileName ="chromosomelist.validated";
    String masterFlagFileName ="master.validated";


    AGPFileValidationCheck agpCheck=null;
    MasterEntryValidationCheck masterCheck = null;

	public SubmissionValidationPlan(SubmissionOptions options) {
		this.options =options;
		}
	public void execute() throws ValidationEngineException {
		try
		{
			options.init();
			if (options.processDir.isPresent() && (Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, "fasta.loaded")))
					|| Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, "flatfile.loaded")))
					|| Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, "agp.loaded"))))) {
				throw new ValidationEngineException("Can not re-execute the pipeline for the assembly already processed");
			}

			FileValidationCheck.setHasAgp(options.submissionFiles.get().getFiles(FileType.AGP).size() > 0);
			//Validation Order shouldn't be changed
			if(options.context.get().getFileTypes().contains(FileType.MASTER))
				createMaster();
			if(options.context.get().getFileTypes().contains(FileType.CHROMOSOME_LIST))
				validateChromosomeList();
			if(options.context.get().getFileTypes().contains(FileType.UNLOCALISED_LIST))
				validateUnlocalisedList();
			if (options.context.get().getFileTypes().contains(FileType.AGP)) {
				agpCheck = new AGPFileValidationCheck(options);
				if (FileValidationCheck.isHasAgp()) {
					contigDB = DBMaker.fileDB(options.reportDir.get() + File.separator + getcontigDbname()).fileDeleteAfterClose().closeOnJvmShutdown().make();
					agpCheck.setContigDB(contigDB);
				}
			}
			if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE))
			{
				FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options);
				check.getAnnotationFlatfile();
				if(FileValidationCheck.isHasAnnotationOnlyFlatfile()) {
					sequenceDB = DBMaker.fileDB(options.reportDir.get() + File.separator + getSequenceDbname()).fileDeleteAfterClose().closeOnJvmShutdown().make();
					agpCheck.getAGPEntries();
				}
			}

			if(options.context.get().getFileTypes().contains(FileType.FASTA))
			  validateFasta();
			  
			if(options.context.get().getFileTypes().contains(FileType.FLATFILE))
				validateFlatfile();

			if(options.context.get().getFileTypes().contains(FileType.AGP))
			{
				validateAGP();
			}
			if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE))
				validateAnnotationOnlyFlatfile();
			if(options.context.get().getFileTypes().contains(FileType.TSV))
				validateTsvfile();

			check.validateDuplicateEntryNames();
			if(Context.genome == options.context.get()) {
				registerSequences();
				check.validateSequencelessChromosomes();

				if (options.assemblyInfoEntry.isPresent() && !AssemblyType.PRIMARYMETAGENOME.getValue().equalsIgnoreCase(options.assemblyInfoEntry.get().getAssemblyType())) {
					throwValidationResult(uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(
							options.ignoreErrors, getSequencecount(0), getSequencecount(1), getSequencecount(2)));
				}

				if(!options.isRemote)
				{
					if(!AssemblyType.BINNEDMETAGENOME.getValue().equalsIgnoreCase(options.assemblyInfoEntry.get().getAssemblyType())&&
					   !AssemblyType.PRIMARYMETAGENOME.getValue().equalsIgnoreCase(options.assemblyInfoEntry.get().getAssemblyType())	)
					writeUnplacedList();
				}
			}	else {
				writeSequenceInfo();
			}
		}catch(ValidationEngineException e)
		{
			try {
				if(!options.isRemote && options.context.isPresent() && options.context.get() == Context.genome && check!=null&&check.getMessageStats()!=null)
					check.getReporter().writeToFile(Paths.get(options.reportDir.get()),check.getMessageStats());
			}catch(Exception ex)
			{
				e = new ValidationEngineException(e.getMessage()+"\n Failed to write error message stats: "+ex.getMessage(),e);
				e.setErrorType(e.getErrorType());
			}
			throw e;
		}finally {
			if (sequenceDB != null)
				sequenceDB.close();
			if (contigDB != null)
				contigDB.close();
		}
	}

	public static Set<String> getUnplacedEntryNames() {
		return FileValidationCheck.unplacedEntryNames;
	}
	private void createMaster() throws ValidationEngineException
	{
		try
		{
			masterCheck = new MasterEntryValidationCheck(options);
			if(options.processDir.isPresent()
					&& Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,masterFlagFileName)))
					&& masterCheck.getMasterEntry() != null ) {
				return;
			}

			if(!masterCheck.check())
				throw new ValidationEngineException("Master entry validation failed",ReportErrorType.VALIDATION_ERROR );
			else if(!options.isRemote)
			     flagValidation(FileType.MASTER);
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.MASTER,e,"master.dat");
		}
	}

	private void validateChromosomeList() throws ValidationEngineException
	{
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,chromosomelistFlagFileName))))
         return;
		String fileName=null;

		try
		{
			check = new ChromosomeListFileValidationCheck(options);
			for(SubmissionFile chromosomeListFile:options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST))
			{
				fileName= chromosomeListFile.getFile().getName();

				if(!check.check(chromosomeListFile))
					throwValidationCheckException(FileType.CHROMOSOME_LIST,chromosomeListFile);
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.CHROMOSOME_LIST,e,fileName);
		}
	}

	private void validateFasta() throws ValidationEngineException
	{
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,fastaFlagFileName))))
			return;

		String fileName=null;

		try
		{
			check = new FastaFileValidationCheck(options);
			for(SubmissionFile fastaFile:options.submissionFiles.get().getFiles(FileType.FASTA))
			{
				fileName= fastaFile.getFile().getName();
				if(sequenceDB!=null)
					check.setSequenceDB(sequenceDB);
				if(contigDB!=null)
					check.setContigDB(contigDB);
				if(!check.check(fastaFile))
					throwValidationCheckException(FileType.FASTA,fastaFile);
				else if(!options.isRemote)
				     flagValidation(FileType.FASTA);

			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.FASTA,e,fileName);
		}
	}

	private void validateFlatfile() throws ValidationEngineException
	{
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,flatfileFlagFileName))))
			return;
		String fileName=null;

		try
		{
			check = new FlatfileFileValidationCheck(options);
			for(SubmissionFile flatfile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
			{
				fileName= flatfile.getFile().getName();
				if(sequenceDB!=null)
					check.setSequenceDB(sequenceDB);
				if(contigDB!=null)
					check.setContigDB(contigDB);
				if(!check.check(flatfile))
					throwValidationCheckException(FileType.FLATFILE,flatfile);
				else if(!options.isRemote)
				     flagValidation(FileType.FLATFILE);
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.FLATFILE,e,fileName);
		}
	}

	private void validateAGP() throws ValidationEngineException
	{
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,agpFlagFileName))))
			return;
		String fileName=null;
		try
		{
			for(SubmissionFile agpFile:options.submissionFiles.get().getFiles(FileType.AGP))
			{
				fileName= agpFile.getFile().getName();
				if(sequenceDB!=null)
					agpCheck.setSequenceDB(sequenceDB);
				if(!agpCheck.check(agpFile))
					throwValidationCheckException(FileType.AGP,agpFile);
				else if(!options.isRemote)
				     flagValidation(FileType.AGP);
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.AGP,e,fileName);
		}
	}

	private void validateUnlocalisedList() throws ValidationEngineException
	{
		String fileName=null;
		try
		{
			check = new UnlocalisedListFileValidationCheck(options);

			for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
			{	fileName= unlocalisedListFile.getFile().getName();
			if(!check.check(unlocalisedListFile))
				throwValidationCheckException(FileType.UNLOCALISED_LIST,unlocalisedListFile);
			}
		}catch(ValidationEngineException e)
		{
			throwValidationEngineException(FileType.UNLOCALISED_LIST,e,fileName);
		}
	}

	private void registerSequences() throws ValidationEngineException
	{
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.agpfileName));
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.sequenceInfo,options.processDir.get(),AssemblySequenceInfo.sequencefileName);
	}
	
	private void validateAnnotationOnlyFlatfile() throws ValidationEngineException
	{
		String fileName=null;

		try
		{
			check = new AnnotationOnlyFlatfileValidationCheck(options);
			for(SubmissionFile annotationOnlyFlatfile:options.submissionFiles.get().getFiles(FileType.ANNOTATION_ONLY_FLATFILE))
			{
				fileName = annotationOnlyFlatfile.getFile().getName();

				if(sequenceDB!=null)
					check.setSequenceDB(sequenceDB);
				if(!check.check(annotationOnlyFlatfile))
					throwValidationCheckException(FileType.ANNOTATION_ONLY_FLATFILE, annotationOnlyFlatfile);
			}
		}catch(ValidationEngineException e)
		{
			throwValidationEngineException(FileType.ANNOTATION_ONLY_FLATFILE,e,fileName);
		}
	}

	private void validateTsvfile() throws ValidationEngineException
	{
		String fileName=null;

		try
		{
			check = new TSVFileValidationCheck(options);
			for(SubmissionFile tsvFile:options.submissionFiles.get().getFiles(FileType.TSV))
			{
				fileName = tsvFile.getFile().getName();
				if(!check.check(tsvFile))
					throwValidationCheckException(FileType.TSV,tsvFile);
			}
		}catch(ValidationEngineException e)
		{
			throwValidationEngineException(FileType.TSV,e,fileName);
		}
	}
	private String getSequenceDbname()
	{
		return ".sequence";

	}
	private String getcontigDbname()
	{
		return ".contig";

	}

	private void throwValidationCheckException(FileType fileTpe,SubmissionFile submissionFile) throws ValidationEngineException
	{
		throw new ValidationEngineException(String.format("%s file validation failed : %s, Please see the error report: %s", fileTpe.name().toLowerCase(),submissionFile.getFile().getName(),check.getReportFile(submissionFile).toFile()),ReportErrorType.VALIDATION_ERROR);
	}

    private void throwValidationEngineException(FileType fileTpe, Exception e, String fileName) throws ValidationEngineException {
        ValidationEngineException validationEngineException = new ValidationEngineException(
                String.format("%s file validation failed for %s", fileTpe.name().toLowerCase(), fileName), e);
        validationEngineException.setErrorType(ReportErrorType.VALIDATION_ERROR);
        throw validationEngineException;
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
		return FileValidationCheck.sequenceInfo.values().stream().filter(p->p.getAssemblyLevel()==assemblyLevel).count();
	}

	private void writeUnplacedList() throws ValidationEngineException
	{

		try {
			Files.deleteIfExists(Paths.get(options.processDir.get(),"unplaced.txt"));
		}catch(Exception e)
		{
			throw new ValidationEngineException("Failed to delete unplaced file: "+e.getMessage(), e);
		}
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(options.processDir.get()+File.separator+"unplaced.txt")))
		{
			oos.writeObject(FileValidationCheck.unplacedEntryNames);

		}catch(Exception e)
		{
			throw new ValidationEngineException("Failed to write unplaced file: "+e.getMessage(), e);
		}
	}

	private void writeSequenceInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeObject(FileValidationCheck.getSequenceCount(),options.processDir.get(),AssemblySequenceInfo.sequencefileName);
	}

	private void flagValidation(FileType fileType) throws IOException
	{
		if(!options.processDir.isPresent())
			return;

		String fileName =null;
		switch(fileType)
		{
		case FASTA:
			fileName=fastaFlagFileName;
			break;
		case AGP:
			fileName=agpFlagFileName;
			break;
		case FLATFILE:
			fileName = flatfileFlagFileName;
			break;
		case CHROMOSOME_LIST:
			fileName = chromosomelistFlagFileName;
			break;
		case MASTER:
			fileName = masterFlagFileName;
			break;
		default:
			break;
		}
		Path filePath= Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,fileName));
		if( !Files.exists(filePath))
    	Files.createFile(filePath);
	}
}

