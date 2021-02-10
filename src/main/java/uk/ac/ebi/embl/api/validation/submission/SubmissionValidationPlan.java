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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.TSVFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.UnlocalisedListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
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
	public ValidationResult execute() throws ValidationEngineException {
		ValidationResult validationResult = new ValidationResult();
		try
		{
			//TODO: check for a way to log INFO messages
			options.init();
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
			if(options.context.get().getFileTypes().contains(FileType.FASTA)) {
				validationResult = validateFasta();
				if(!validationResult.isValid())
					return validationResult;
			}
			  
			if(options.context.get().getFileTypes().contains(FileType.FLATFILE)) {
				validationResult = validateFlatfile();
				if(!validationResult.isValid())
					return validationResult;
			}

			if(options.context.get().getFileTypes().contains(FileType.AGP))
			{
				validationResult = validateAGP();
				if(!validationResult.isValid())
					return validationResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE)) {
				validationResult = validateAnnotationOnlyFlatfile();
				if(!validationResult.isValid())
					return validationResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.TSV)) {
				validationResult = validateTsvfile();
				if(!validationResult.isValid())
					return validationResult;
			}

			check.validateDuplicateEntryNames();
			if(Context.genome == options.context.get()) {
				registerSequences();
				check.validateSequencelessChromosomes();

				String assemblyType = options.assemblyInfoEntry.isPresent()? options.assemblyInfoEntry.get().getAssemblyType(): null;
				throwValidationResult(uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(
							options.ignoreErrors, getSequencecount(0), getSequencecount(1), getSequencecount(2), assemblyType));

				if(!options.isRemote)
				{
					if(!(AssemblyType.BINNEDMETAGENOME.getValue().equalsIgnoreCase(assemblyType) ||
							AssemblyType.PRIMARYMETAGENOME.getValue().equalsIgnoreCase(assemblyType)	||
							AssemblyType.CLINICALISOLATEASSEMBLY.getValue().equalsIgnoreCase(assemblyType)))
					writeUnplacedList();
				}
			}	else {
				writeSequenceInfo();
			}
		} catch (ValidationEngineException e) {
			try {
				if (options.reportFile.isPresent()) {
					new DefaultSubmissionReporter(new HashSet<>(Arrays.asList(Severity.ERROR, Severity.WARNING, Severity.FIX, Severity.INFO)))
							.writeToFile(options.reportFile.get(), Severity.ERROR, e.getMessage());
				}
				if (!options.isRemote && options.context.isPresent() && options.context.get() == Context.genome && check != null && check.getMessageStats() != null)
					check.getReporter().writeToFile(Paths.get(options.reportDir.get()), check.getMessageStats());
			} catch (Exception ex) {
				e = new ValidationEngineException(e.getMessage() + "\n Failed to write error message stats: " + ex.getMessage(), e);
				e.setErrorType(e.getErrorType());
			}
			throw e;
		} finally {
			if (sequenceDB != null)
				sequenceDB.close();
			if (contigDB != null)
				contigDB.close();
		}
		return validationResult;
	}

	public static Set<String> getUnplacedEntryNames() {
		return FileValidationCheck.unplacedEntryNames;
	}
	private ValidationResult createMaster() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		try
		{
			masterCheck = new MasterEntryValidationCheck(options);
			if(options.processDir.isPresent()
					&& Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,masterFlagFileName)))
					&& masterCheck.getMasterEntry() != null ) {
				return result;
			}

			result = masterCheck.check();
			if(!result.isValid()) {
				if(options.isRemote)
					throw new ValidationEngineException("Master entry validation failed",ReportErrorType.VALIDATION_ERROR );
				return result;
			}
			if(!options.isRemote)
			     flagValidation(FileType.MASTER);
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.MASTER,e,"master.dat");
		}

		return result;
	}

	private ValidationResult validateChromosomeList() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,chromosomelistFlagFileName))))
         return result;
		String fileName = null;
		try {
			check = new ChromosomeListFileValidationCheck(options);
			for (SubmissionFile chromosomeListFile : options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST)) {
				fileName= chromosomeListFile.getFile().getName();
				result = check.check(chromosomeListFile);
				if (!result.isValid()) {
					if(options.isRemote)
						throwValidationCheckException(FileType.CHROMOSOME_LIST,chromosomeListFile);
					return result;
				}
			}
		} catch (Exception e) {
			throwValidationEngineException(FileType.CHROMOSOME_LIST, e, fileName);
		}
		return result;
	}

	private ValidationResult validateFasta() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();

		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,fastaFlagFileName))))
			return result;
		String fileName=null;
		try {
			check = new FastaFileValidationCheck(options);
			for (SubmissionFile fastaFile : options.submissionFiles.get().getFiles(FileType.FASTA)) {
				fileName= fastaFile.getFile().getName();
				if (sequenceDB != null)
					check.setSequenceDB(sequenceDB);
				if (contigDB != null)
					check.setContigDB(contigDB);
				result = check.check(fastaFile);
				if (!result.isValid()) {
					if(options.isRemote)
						throwValidationCheckException(FileType.FASTA,fastaFile);
					return result;
				}
			}
			if (!options.isRemote)
				flagValidation(FileType.FASTA);
		} catch(Exception e) {
			throwValidationEngineException(FileType.FASTA,e,fileName);
		}
		return result;
	}

	private ValidationResult validateFlatfile() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,flatfileFlagFileName))))
			return result;
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
			result = check.check(flatfile);
			if(!result.isValid()) {
				if(options.isRemote)
					throwValidationCheckException(FileType.FLATFILE,flatfile);
				return result;
			}
		}
			if (!options.isRemote)
				flagValidation(FileType.FLATFILE);
		}catch(Exception e){
			throwValidationEngineException(FileType.FLATFILE,e,fileName);
		}
		return result;
	}

	private ValidationResult validateAGP() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,agpFlagFileName))))
			return result;
		String fileName=null;
		try
		{
		for(SubmissionFile agpFile:options.submissionFiles.get().getFiles(FileType.AGP))
		{
			fileName= agpFile.getFile().getName();
			if(sequenceDB!=null)
				agpCheck.setSequenceDB(sequenceDB);
			result = agpCheck.check(agpFile);
			if(!result.isValid()) {
				if(options.isRemote)
					throwValidationCheckException(FileType.AGP,agpFile);
				return result;
			}
		}
			if (!options.isRemote)
				flagValidation(FileType.AGP);

		} catch (Exception e) {
			throwValidationEngineException(FileType.AGP,e,fileName);
		}
		return result;
	}

	private ValidationResult validateUnlocalisedList() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		String fileName=null;
		try
		{
			check = new UnlocalisedListFileValidationCheck(options);

			for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
			{	fileName= unlocalisedListFile.getFile().getName();
				result = check.check(unlocalisedListFile);
				if(!result.isValid()) {
					if(options.isRemote)
						throwValidationCheckException(FileType.UNLOCALISED_LIST,unlocalisedListFile);
					return result;
				}
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.UNLOCALISED_LIST,e,fileName);
		}
		return result;
	}

	private void registerSequences() throws ValidationEngineException
	{
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.agpfileName));
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.sequenceInfo,options.processDir.get(),AssemblySequenceInfo.sequencefileName);
	}
	
	private ValidationResult validateAnnotationOnlyFlatfile() throws ValidationEngineException
	{
		String fileName=null;
		ValidationResult result = new ValidationResult();
		try
		{
			check = new AnnotationOnlyFlatfileValidationCheck(options);
			for(SubmissionFile annotationOnlyFlatfile:options.submissionFiles.get().getFiles(FileType.ANNOTATION_ONLY_FLATFILE))
			{
				fileName = annotationOnlyFlatfile.getFile().getName();

				if(sequenceDB!=null)
					check.setSequenceDB(sequenceDB);
				result = check.check(annotationOnlyFlatfile);
				if(!result.isValid()) {
					if(options.isRemote)
						throwValidationCheckException(FileType.ANNOTATION_ONLY_FLATFILE,annotationOnlyFlatfile);
					return result;
				}
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.ANNOTATION_ONLY_FLATFILE,e,fileName);
		}
		return result;
	}

	private ValidationResult validateTsvfile() throws ValidationEngineException
	{
		String fileName=null;
		ValidationResult result = new ValidationResult();
		try
		{
			check = new TSVFileValidationCheck(options);
			for(SubmissionFile tsvFile:options.submissionFiles.get().getFiles(FileType.TSV))
			{
				fileName = tsvFile.getFile().getName();
				result = check.check(tsvFile);
				if(!result.isValid()) {
					if(options.isRemote)
						throwValidationCheckException(FileType.TSV,tsvFile);
					return result;
				}
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.TSV,e,fileName);
		}
		return result;
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
		Path reportFile = fileTpe == FileType.AGP ? agpCheck.getReportFile(submissionFile) : check.getReportFile(submissionFile);
		throw new ValidationEngineException(String.format("%s file validation failed : %s, Please see the error report: %s", fileTpe.name().toLowerCase(),
				submissionFile.getFile().getName(),reportFile.toFile()),ReportErrorType.VALIDATION_ERROR);
	}

    private void throwValidationEngineException(FileType fileTpe, Exception e, String fileName) throws ValidationEngineException {
		if(options.isRemote) {
			ValidationEngineException validationEngineException = new ValidationEngineException(
					String.format("%s file validation failed for %s", fileTpe.name().toLowerCase(), fileName), e);
			validationEngineException.setErrorType(ReportErrorType.VALIDATION_ERROR);
			throw validationEngineException;
		} else throw new ValidationEngineException(e);
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
		if(options.processDir.isPresent() && Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,AssemblySequenceInfo.sequencefileName))))
			return;

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

