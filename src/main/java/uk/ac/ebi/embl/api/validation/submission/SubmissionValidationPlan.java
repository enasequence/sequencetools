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
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.check.file.*;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
	public ValidationPlanResult execute() throws ValidationEngineException {
		ValidationPlanResult validationPlanResult = new ValidationPlanResult();
		try
		{
			options.init();
			FileValidationCheck.setHasAgp(options.submissionFiles.get().getFiles(FileType.AGP).size() > 0);
			//Validation Order shouldn't be changed
			if(options.context.get().getFileTypes().contains(FileType.MASTER)) {
				validationPlanResult = createMaster();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.CHROMOSOME_LIST)) {
				validationPlanResult = validateChromosomeList();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.UNLOCALISED_LIST)) {
				validationPlanResult = validateUnlocalisedList();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}

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
				validationPlanResult = validateFasta();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}

			if(options.context.get().getFileTypes().contains(FileType.FLATFILE)) {
				validationPlanResult = validateFlatfile();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}

			if(options.context.get().getFileTypes().contains(FileType.AGP))
			{
				validationPlanResult = validateAGP();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE)) {
				validationPlanResult = validateAnnotationOnlyFlatfile();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}
			if(options.context.get().getFileTypes().contains(FileType.TSV)) {
				validationPlanResult = validateTsvfile();
				if(validationPlanResult.hasError())
					return validationPlanResult;
			}

			check.validateDuplicateEntryNames();
			if(Context.genome == options.context.get()) {
				registerSequences();
				check.validateSequencelessChromosomes();

				String assemblyType = options.assemblyInfoEntry.isPresent()? options.assemblyInfoEntry.get().getAssemblyType(): null;
				throwValidationResult(uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(
							options.ignoreErrors, getSequencecount(0), getSequencecount(1), getSequencecount(2), assemblyType));

				if(!options.isWebinCLI)
				{
					if(!(AssemblyType.BINNEDMETAGENOME.getValue().equalsIgnoreCase(assemblyType) ||
					   AssemblyType.PRIMARYMETAGENOME.getValue().equalsIgnoreCase(assemblyType)	))
					writeUnplacedList();
				}
			}	else {
				writeSequenceInfo();
			}
		} finally {

			reportStats();
			if (sequenceDB != null)
				sequenceDB.close();
			if (contigDB != null)
				contigDB.close();
		}
		return validationPlanResult;
	}

	private void reportStats() throws ValidationEngineException {
		try {
			if (!options.isWebinCLI && options.context.isPresent() && options.context.get() == Context.genome && check != null && check.getMessageStats() != null)
				check.getReporter().writeToFile(Paths.get(options.reportDir.get()), check.getMessageStats());
		} catch (Exception ex) {
			throw new ValidationEngineException("Failed to write error message stats: " + ex.getMessage());
		}
	}

	static Set<String> getUnplacedEntryNames() {
		return FileValidationCheck.unplacedEntryNames;
	}

	private ValidationPlanResult createMaster() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();

		masterCheck = new MasterEntryValidationCheck(options);
		if (options.processDir.isPresent()
				&& Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, masterFlagFileName)))
				&& masterCheck.getMasterEntry() != null) {
			return planResult;
		}

		planResult = masterCheck.check();

		if (planResult.hasError()) {
			if (options.isWebinCLI) {
				throw new ValidationEngineException("Master entry validation failed.", ReportErrorType.VALIDATION_ERROR);
			}
		} else {
			flagValidation(FileType.MASTER);
		}
		return planResult;
	}

	private ValidationPlanResult validateChromosomeList() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();
		if (options.processDir.isPresent() && Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, chromosomelistFlagFileName))))
			return planResult;

		check = new ChromosomeListFileValidationCheck(options);
		for (SubmissionFile chromosomeListFile : options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST)) {
			planResult = check.check(chromosomeListFile);
			if(planResult.hasError() ){
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.CHROMOSOME_LIST, chromosomeListFile)));
				return planResult;
			}
		}

		return planResult;
	}

	private ValidationPlanResult validateFasta() throws ValidationEngineException
	{
		ValidationPlanResult planResult = new ValidationPlanResult();
		if(options.processDir.isPresent()&&Files.exists(Paths.get(String.format("%s%s%s",options.processDir.get(),File.separator,fastaFlagFileName))))
			return planResult;

			check = new FastaFileValidationCheck(options);
			for(SubmissionFile fastaFile:options.submissionFiles.get().getFiles(FileType.FASTA))
			{
				if(sequenceDB!=null)
					check.setSequenceDB(sequenceDB);
				if(contigDB!=null)
					check.setContigDB(contigDB);
				planResult = check.check(fastaFile);

				if(planResult.hasError() ){
					planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.FASTA,fastaFile)));
					return planResult;
				}
			}

		flagValidation(FileType.FASTA);
		return planResult;
	}

	private ValidationPlanResult validateFlatfile() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();
		if (options.processDir.isPresent() && Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, flatfileFlagFileName))))
			return planResult;

		check = new FlatfileFileValidationCheck(options);
		for (SubmissionFile flatfile : options.submissionFiles.get().getFiles(FileType.FLATFILE)) {
			if (sequenceDB != null)
				check.setSequenceDB(sequenceDB);
			if (contigDB != null)
				check.setContigDB(contigDB);
			planResult = check.check(flatfile);
			if (planResult.hasError()) {
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.FLATFILE, flatfile)));
				return planResult;
			}

		}
		flagValidation(FileType.FLATFILE);
		return planResult;
	}

	private ValidationPlanResult validateAGP() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();
		if (options.processDir.isPresent() && Files.exists(Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, agpFlagFileName))))
			return planResult;

		for (SubmissionFile agpFile : options.submissionFiles.get().getFiles(FileType.AGP)) {
			if (sequenceDB != null)
				agpCheck.setSequenceDB(sequenceDB);
			planResult = agpCheck.check(agpFile);
			if (planResult.hasError()) {
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.AGP, agpFile)));
				return planResult;
			}

		}
		flagValidation(FileType.AGP);
		return planResult;
	}

	private ValidationPlanResult validateUnlocalisedList() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();

		check = new UnlocalisedListFileValidationCheck(options);

		for (SubmissionFile unlocalisedListFile : options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST)) {
			planResult = check.check(unlocalisedListFile);
			if (planResult.hasError()) {
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.UNLOCALISED_LIST, unlocalisedListFile)));
				return planResult;
			}
		}
		return planResult;
	}

	private void registerSequences() throws ValidationEngineException
	{
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
		FileValidationCheck.sequenceInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.agpfileName));
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.sequenceInfo,options.processDir.get(),AssemblySequenceInfo.sequencefileName);
	}

	private ValidationPlanResult validateAnnotationOnlyFlatfile() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();

		check = new AnnotationOnlyFlatfileValidationCheck(options);
		for (SubmissionFile annotationOnlyFlatfile : options.submissionFiles.get().getFiles(FileType.ANNOTATION_ONLY_FLATFILE)) {

			if (sequenceDB != null)
				check.setSequenceDB(sequenceDB);
			planResult = check.check(annotationOnlyFlatfile);
			if (planResult.hasError()) {
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.ANNOTATION_ONLY_FLATFILE, annotationOnlyFlatfile)));
				return planResult;
			}
		}
		return planResult;
	}

	private ValidationPlanResult validateTsvfile() throws ValidationEngineException {
		ValidationPlanResult planResult = new ValidationPlanResult();

		check = new TSVFileValidationCheck(options);
		for (SubmissionFile tsvFile : options.submissionFiles.get().getFiles(FileType.TSV)) {
			planResult = check.check(tsvFile);
			if (planResult.hasError()) {
				planResult.setValidationMessage(new DefaultOrigin(getValidationErrorMessage(FileType.TSV, tsvFile)));
				return planResult;
			}
		}
		return planResult;
	}

	private String getSequenceDbname()
	{
		return ".sequence";

	}
	private String getcontigDbname()
	{
		return ".contig";

	}

	private String getValidationErrorMessage(FileType fileTpe, SubmissionFile submissionFile)  {
			return String.format("%s file validation failed : %s, Please see the error report: %s",
					fileTpe.name().toLowerCase(), submissionFile.getFile().getName() , check.getReportFile(submissionFile).toFile());
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

	private void flagValidation(FileType fileType) throws ValidationEngineException {
		try {
			if (options.isWebinCLI || !options.processDir.isPresent())
				return;

			String fileName = null;
			switch (fileType) {
				case FASTA:
					fileName = fastaFlagFileName;
					break;
				case AGP:
					fileName = agpFlagFileName;
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
			Path filePath = Paths.get(String.format("%s%s%s", options.processDir.get(), File.separator, fileName));
			if (!Files.exists(filePath))
				Files.createFile(filePath);
		} catch (IOException e) {
			throw new ValidationEngineException("Exception while writing "+fileType.name()+".validated flagfile." + e.getMessage());
		}
	}
}

