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

import org.apache.commons.lang.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.*;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubmissionValidationPlan
{
	private final SubmissionOptions options;

	private final FileValidationCheck.SharedInfo fileValidationCheckSharedInfo = new FileValidationCheck.SharedInfo();

	FileValidationCheck check = null;
	DB annotationDB =null;
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
			fileValidationCheckSharedInfo.hasAgp = options.submissionFiles.get().getFiles(FileType.AGP).size() > 0;
			fileValidationCheckSharedInfo.assemblyType = options.assemblyInfoEntry.map(AssemblyInfoEntry::getAssemblyType).orElse(null);
			//Validation Order shouldn't be changed
			if(options.context.get().getFileTypes().contains(FileType.MASTER))
				createMaster();
			if(options.context.get().getFileTypes().contains(FileType.CHROMOSOME_LIST))
				validateChromosomeList();
			if(options.context.get().getFileTypes().contains(FileType.UNLOCALISED_LIST))
				validateUnlocalisedList();
			if (options.context.get().getFileTypes().contains(FileType.AGP)) {
				agpCheck = new AGPFileValidationCheck(options, fileValidationCheckSharedInfo);
				if (fileValidationCheckSharedInfo.hasAgp) {
					contigDB = DBMaker.fileDB(options.reportDir.get() + File.separator + getcontigDbname()).fileDeleteAfterClose().closeOnJvmShutdown().make();
					agpCheck.setContigDB(contigDB);
					agpCheck.createContigDB();
				}
			}
			if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE))
			{
				validationResult = validateAnnotationOnlyFlatfile();
				if(!validationResult.isValid()) {
					return validationResult;
				}
			}
			if(options.context.get().getFileTypes().contains(FileType.FASTA)) {
				validationResult = validateFasta();
				if(!validationResult.isValid())
					return validationResult;
			}

			if(options.context.get().getFileTypes().contains(FileType.FLATFILE) ) {
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
			if(options.context.get().getFileTypes().contains(FileType.TSV)) {
				validationResult = validateTsvfile();
				if(!validationResult.isValid())
					return validationResult;
			}

			check.validateDuplicateEntryNames();
			if(Context.genome == options.context.get()) {
				registerSequences();
				check.validateCovid19GenomeSize();
				check.validateSequencelessChromosomes();

				String assemblyType = options.assemblyInfoEntry.map(AssemblyInfoEntry::getAssemblyType).orElse(null);
				throwValidationResult(uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(
							options.ignoreErrors, getSequencecount(0), getSequencecount(1), getSequencecount(2), assemblyType));

				if(!options.isWebinCLI && !FileValidationCheck.excludeDistribution(fileValidationCheckSharedInfo.assemblyType))
				{
					writeUnplacedList();
				}
			}	else {
				writeSequenceInfo();
			}

		} catch (ValidationEngineException e) {
			try {
				if (options.reportFile.isPresent()) {
					new DefaultSubmissionReporter(new HashSet<>(Arrays.asList(Severity.ERROR, Severity.WARNING, Severity.FIX, Severity.INFO)))
							.writeToFile(options.reportFile.get(), Severity.ERROR, e.getMessage()+" Causeed by:"+e.getCause());
				}
				if (!options.isWebinCLI && options.context.isPresent() && options.context.get() == Context.genome && check != null && check.getMessageStats() != null)
					check.getReporter().writeToFile(Paths.get(options.reportDir.get()), check.getMessageStats());
			} catch (Exception ex) {
				e = new ValidationEngineException(e.getMessage() + "\n Failed to write error message stats: " + ex.getMessage(), e);
				e.setErrorType(e.getErrorType());
			}
			throw e;
		} finally {
			FileValidationCheck.closeMapDB(contigDB, annotationDB);
			if(check != null) {
				check.flushAndCloseFileWriters();
			}
		}
		return validationResult;
	}

	public Set<String> getUnplacedEntryNames() {
		return fileValidationCheckSharedInfo.unplacedEntryNames;
	}

	private ValidationResult createMaster() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		try
		{
			masterCheck = new MasterEntryValidationCheck(options, fileValidationCheckSharedInfo);
			if(options.processDir.isPresent()
					&& Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,masterFlagFileName)))
					&& fileValidationCheckSharedInfo.masterEntry != null ) {
				return result;
			}

			result = masterCheck.check();
			if(!result.isValid()) {
				if(options.isWebinCLI)
					throw new ValidationEngineException("Master entry validation failed",ReportErrorType.VALIDATION_ERROR );
				return result;
			}
			if(!options.isWebinCLI)
			     flagValidation(FileType.MASTER);
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.MASTER.name(),e,"master.dat");
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
			check = new ChromosomeListFileValidationCheck(options, fileValidationCheckSharedInfo);
			for (SubmissionFile chromosomeListFile : options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST)) {
				fileName= chromosomeListFile.getFile().getName();
				result = check.check(chromosomeListFile);
				if (!result.isValid()) {
					if(options.isWebinCLI)
						throwValidationCheckException(FileType.CHROMOSOME_LIST,chromosomeListFile);
					return result;
				}
			}
		} catch (Exception e) {
			throwValidationEngineException(FileType.CHROMOSOME_LIST.name(), e, fileName);
		}
		return result;
	}

	private ValidationResult validateFasta() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		check = new FastaFileValidationCheck(options, fileValidationCheckSharedInfo);
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,fastaFlagFileName))))
			return result;
		String fileName=null;
		try {
			List<SubmissionFile> submissionFiles =  options.submissionFiles.get().getFiles(FileType.FASTA);
			if(!submissionFiles.isEmpty()) {
				for (SubmissionFile fastaFile : submissionFiles) {
					fileName = fastaFile.getFile().getName();
					if(fileValidationCheckSharedInfo.hasAnnotationOnlyFlatfile) {
					check.setAnnotationDB(annotationDB);
					}
					if (contigDB != null)
						check.setContigDB(contigDB);
					result = check.check(fastaFile);
					if (!result.isValid()) {
						if (options.isWebinCLI)
							throwValidationCheckException(FileType.FASTA, fastaFile);
						return result;
					}
				}
				if (!options.isWebinCLI)
					flagValidation(FileType.FASTA);
			}
		} catch(Exception e) {
			e.printStackTrace();
			throwValidationEngineException(FileType.FASTA.name(),e,fileName);
		}
		return result;
	}

	private ValidationResult validateFlatfile() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		check = new FlatfileFileValidationCheck(options, fileValidationCheckSharedInfo);
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,flatfileFlagFileName))))
			return result;
		String fileName=null;
		try
		{
			List<SubmissionFile> submissionFiles =  options.submissionFiles.get().getFiles(FileType.FLATFILE);
			if(!submissionFiles.isEmpty()) {
				for (SubmissionFile flatfile : submissionFiles) {
					fileName = flatfile.getFile().getName();
					if (contigDB != null)
						check.setContigDB(contigDB);
					result = check.check(flatfile);
					if (!result.isValid()) {
						if (options.isWebinCLI)
							throwValidationCheckException(FileType.FLATFILE, flatfile);
						return result;
					}
				}
				if (!options.isWebinCLI)
					flagValidation(FileType.FLATFILE);
			}
		}catch(Exception e){
			throwValidationEngineException(FileType.FLATFILE.name(),e,fileName);
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
			List<SubmissionFile> submissionFiles =  options.submissionFiles.get().getFiles(FileType.AGP);
			if(!submissionFiles.isEmpty()) {
				for (SubmissionFile agpFile : submissionFiles) {
					fileName = agpFile.getFile().getName();
					result = agpCheck.check(agpFile);
					if (!result.isValid()) {
						if (options.isWebinCLI)
							throwValidationCheckException(FileType.AGP, agpFile);
						return result;
					}
				}
				if (!options.isWebinCLI)
					flagValidation(FileType.AGP);
			}

		} catch (Exception e) {
			throwValidationEngineException(FileType.AGP.name(),e,fileName);
		}
		return result;
	}

	private ValidationResult validateUnlocalisedList() throws ValidationEngineException
	{
		ValidationResult result = new ValidationResult();
		String fileName=null;
		try
		{
			check = new UnlocalisedListFileValidationCheck(options, fileValidationCheckSharedInfo);
			for(SubmissionFile unlocalisedListFile:options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST))
			{	fileName= unlocalisedListFile.getFile().getName();
				result = check.check(unlocalisedListFile);
				if(!result.isValid()) {
					if(options.isWebinCLI)
						throwValidationCheckException(FileType.UNLOCALISED_LIST,unlocalisedListFile);
					return result;
				}
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.UNLOCALISED_LIST.name(),e,fileName);
		}
		return result;
	}

	private void registerSequences() throws ValidationEngineException
	{
		fileValidationCheckSharedInfo.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
		fileValidationCheckSharedInfo.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
		fileValidationCheckSharedInfo.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.agpfileName));
		AssemblySequenceInfo.writeMapObject(fileValidationCheckSharedInfo.sequenceInfo,options.processDir.get(),AssemblySequenceInfo.sequencefileName);
	}

	private ValidationResult validateAnnotationOnlyFlatfile() throws ValidationEngineException {
		String fileName = null;
		ValidationResult result = new ValidationResult();
		try {
			check = new AnnotationOnlyFlatfileValidationCheck(options, fileValidationCheckSharedInfo);
			fileValidationCheckSharedInfo.hasAnnotationOnlyFlatfile = check.hasAnnotationFlatfile();
			if (fileValidationCheckSharedInfo.hasAnnotationOnlyFlatfile) {
				annotationDB = DBMaker.fileDB(options.reportDir.get() + File.separator + getAnnoationDbname()).fileDeleteAfterClose().closeOnJvmShutdown().make();
				check.setAnnotationDB(annotationDB);
				agpCheck.setAnnotationDB(annotationDB);
			} else {
				return result;
			}

			for (SubmissionFile annotationOnlyFlatfile : options.submissionFiles.get().getFiles(FileType.FLATFILE)) {
				fileName = annotationOnlyFlatfile.getFile().getName();
				result = check.check(annotationOnlyFlatfile);
				annotationDB = check.getAnnotationDB();
				if (!result.isValid()) {
					if (options.isWebinCLI)
						throwValidationCheckException(FileType.ANNOTATION_ONLY_FLATFILE, annotationOnlyFlatfile);
					return result;
				}
			}
		} catch (Exception e) {
			throwValidationEngineException(FileType.FLATFILE.name()+"/"+FileType.ANNOTATION_ONLY_FLATFILE.name(), e, fileName);
		}
		return result;
	}

	private ValidationResult validateTsvfile() throws ValidationEngineException
	{
		String fileName=null;
		ValidationResult result = new ValidationResult();
		try
		{
			check = new TSVFileValidationCheck(options, fileValidationCheckSharedInfo);
			for(SubmissionFile tsvFile:options.submissionFiles.get().getFiles(FileType.TSV))
			{
				fileName = tsvFile.getFile().getName();
				result = check.check(tsvFile);
				if(!result.isValid()) {
					if(options.isWebinCLI)
						throwValidationCheckException(FileType.TSV,tsvFile);
					return result;
				}
			}
		}catch(Exception e)
		{
			throwValidationEngineException(FileType.TSV.name(),e,fileName);
		}
		return result;
	}
	private String getAnnoationDbname()
	{
		return ".annotation";
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

    private void throwValidationEngineException(String fileTpe, Exception e, String fileName) throws ValidationEngineException {
		if(options.isWebinCLI) {
			ValidationEngineException validationEngineException = new ValidationEngineException(
					String.format("%s file validation failed for %s", fileTpe.toLowerCase(), fileName), e);
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
		return fileValidationCheckSharedInfo.sequenceInfo.values().stream().filter(p->p.getAssemblyLevel()==assemblyLevel).count();
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
			oos.writeObject(fileValidationCheckSharedInfo.unplacedEntryNames);

		}catch(Exception e)
		{
			throw new ValidationEngineException("Failed to write unplaced file: "+e.getMessage(), e);
		}
	}

	private void writeSequenceInfo() throws ValidationEngineException
	{
		if(options.processDir.isPresent() && Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,AssemblySequenceInfo.sequencefileName))))
			return;

		AssemblySequenceInfo.writeObject(fileValidationCheckSharedInfo.sequenceCount,options.processDir.get(),AssemblySequenceInfo.sequencefileName);
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

