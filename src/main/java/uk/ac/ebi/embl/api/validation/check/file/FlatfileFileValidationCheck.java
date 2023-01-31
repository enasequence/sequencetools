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

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;

import java.io.BufferedReader;
import java.io.PrintWriter;

@Description("")
public class FlatfileFileValidationCheck extends FileValidationCheck
{

	public FlatfileFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo)
	{
		super(options, sharedInfo);
	}

	@Override
	public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		EmblEntryValidationPlan validationPlan;
		fixedFileWriter =null;
		ValidationResult validationResult = new ValidationResult();
		Origin origin =null;

		try(BufferedReader fileReader= CommonUtil.bufferedReaderFromFile(submissionFile.getFile()); PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			boolean isGenbankFile = isGenbank(submissionFile.getFile());
			clearReportFile(getReportFile(submissionFile));
			if(!isGenbankFile && !validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FLATFILE))
			{
				addErrorAndReport(validationResult,submissionFile, "InvalidFileFormat","flatfile");
				return validationResult;
			}
		Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;
		EntryReader entryReader = isGenbankFile?new GenbankEntryReader(fileReader):
				new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());
		ValidationResult parseResult = entryReader.read();
		validationResult.append(parseResult);
		
		while(entryReader.isEntry())
		{
			if(!parseResult.isValid())
			{
				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
				addMessageStats(parseResult.getMessages());
			}

			Entry entry = entryReader.getEntry();
			origin =entry.getOrigin();
			if(options.context.get() == Context.sequence && !validateSequenceCountForTemplate(validationResult, submissionFile)) {
				return validationResult;
			}
			if(getOptions().context.get()==Context.genome)
            {
    			getOptions().getEntryValidationPlanProperty().sequenceNumber.set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get()+1);
             	if(entry.getSequence() == null || entry.getSequence().getSequenceByte() == null)
            	{
            		entryReader.read();
            		continue;
            	}
            	else  {
					collectContigInfo(entry);
				}
            }

			if(StringUtils.isBlank(entry.getSubmitterAccession()) && getOptions().context.get() == Context.genome) {
				addErrorAndReport(validationResult, submissionFile, "EntryNameRequired");
				return validationResult;
			}

			getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
			getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);

            if(Context.sequence == options.context.get()) {
				if(entry.getDataClass() == null || entry.getDataClass().isEmpty()) {
					entry.setDataClass(Entry.STD_DATACLASS);
				}
			} else {
				entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
			}

			checkChromosomeTopology(entry);

        	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
        	appendHeader(entry);
			addSubmitterSeqIdQual(entry);
        	ValidationResult planResult = validationPlan.execute(entry);
        	validationResult.append(planResult);

        	if(null != entry.getSubmitterAccession()) {
				addEntryName(entry.getSubmitterAccession());
				int assemblyLevel = getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
				AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
				sharedInfo.flatfileInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
			}

			if(!planResult.isValid())
			{
				getReporter().writeToFile(getReportFile(submissionFile), planResult);
				addMessageStats(planResult.getMessages(Severity.ERROR));
			}
			else
			{
				assignProteinAccessionAndWriteToFile(entry, fixedFileWriter, submissionFile, false);
			}
			parseResult = entryReader.read();
			validationResult.append(parseResult);
			sharedInfo.sequenceCount++;
		}
		}catch(ValidationEngineException e)
		{
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeMapDB(getComponentAGPRowsMapDB(), getAnnotationDB());
			throw e;
		} catch (Exception ex) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, ex.getMessage(),origin);
			closeMapDB(getComponentAGPRowsMapDB(), getAnnotationDB());
			throw new ValidationEngineException(ex.getMessage(),ex);
		}

		if(validationResult.isValid())
          registerFlatfileInfo();
		return validationResult;
	}

	@Override
	public ValidationResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	private void registerFlatfileInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeMapObject(sharedInfo.flatfileInfo,options.processDir.get(),AssemblySequenceInfo.flatfilefileName);
	}
}
