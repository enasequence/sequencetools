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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Description("")
public class FlatfileFileValidationCheck extends FileValidationCheck
{

	public FlatfileFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	@Override
	public ValidationPlanResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		ValidationPlanResult validationResult = new ValidationPlanResult();
		EmblEntryValidationPlan validationPlan;
		fixedFileWriter =null;
		Origin origin =null;
		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			boolean isGenbankFile = isGenbank(submissionFile.getFile());
			clearReportFile(getReportFile(submissionFile));
			if (!isGenbankFile && !validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FLATFILE)) {
				validationResult.append(reportError(getReportFile(submissionFile), "flatfile"));
				validationResult.setHasError(true);
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
				validationResult.setHasError(true);
				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
				addMessageKeys(parseResult.getMessages());
			}

			Entry entry = entryReader.getEntry();
			origin =entry.getOrigin();
			entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
			if(getOptions().context.get()==Context.genome)
            {
            	if(entry.getSubmitterAccession() == null) {
					if (entry.getPrimarySourceFeature() == null || entry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME) == null) {
						entry.setSubmitterAccession(entry.getPrimaryAccession());
					} else {
						entry.setSubmitterAccession(entry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME));
					}
				}
    			getOptions().getEntryValidationPlanProperty().sequenceNumber.set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get()+1);
             	if(entry.getSequence()==null||entry.getSequence().getSequenceBuffer()==null)
            	{  entryReader.read();
            		continue;
            	}
            	else if(isHasAnnotationOnlyFlatfile())
            		collectContigInfo(entry);
            }
            if(Context.sequence == options.context.get()) {
				if(entry.getDataClass() == null || entry.getDataClass().isEmpty())
					entry.setDataClass(Entry.STD_DATACLASS);
			} else {
				entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
			}

			if(StringUtils.isBlank(entry.getSubmitterAccession()) && getOptions().context.get() == Context.genome) {
				throw new ValidationEngineException("Entry name can not be null for genome context, please check the AC * line.", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			} else {
				getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
			}

			Sequence.Topology chrListToplogy = getTopology(entry.getSubmitterAccession());
			if (chrListToplogy != null) {
			  if (entry.getSequence().getTopology() != null
				  && entry.getSequence().getTopology() != chrListToplogy) {
				throw new ValidationEngineException(
					String.format(
						"The topology in the ID line \'%s\' conflicts with the topology specified in the chromsome list file \'%s\'.",
						entry.getSequence().getTopology(), chrListToplogy), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			  }
			  entry.getSequence().setTopology(chrListToplogy);
			}

			getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
        	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
        	appendHeader(entry);
        	ValidationPlanResult planResult=validationPlan.execute(entry);
			validationResult.append(planResult);
        	if(null != entry.getSubmitterAccession()) {
				addEntryName(entry.getSubmitterAccession());
				int assemblyLevel = getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
				AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
				FileValidationCheck.flatfileInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
			}

			if(!planResult.isValid())
			{
				validationResult.setHasError(true);
				getReporter().writeToFile(getReportFile(submissionFile), planResult);
				addMessageKeys(planResult.getMessages());
			}
			else
			{
				if(fixedFileWriter!=null)
				new EmblEntryWriter(entry).write(fixedFileWriter);
			}
			parseResult = entryReader.read();
			validationResult.append(parseResult);
			sequenceCount++;
		}
		}catch(ValidationEngineException e)
		{
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw e;
		} catch (IOException ex) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, ex.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw new ValidationEngineException(ex);
		}

		if(validationResult.isValid())
          registerFlatfileInfo();
		return validationResult;
	}
	@Override
	public ValidationPlanResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	public void getAnnotationFlatfile() throws ValidationEngineException
	{
		for(SubmissionFile submissionFile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			boolean isGenbankFile = isGenbank(submissionFile.getFile());
			Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;

			try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter annotationOnyFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getAbsolutePath()+".annotationOnly"))))
			{
				EntryReader entryReader = isGenbankFile?new GenbankEntryReader(fileReader):
						new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());

				entryReader.read();
				while(entryReader.isEntry())
				{
					Entry entry=entryReader.getEntry();
					if(entry.getSequence() == null || entry.getSequence().getSequenceBuffer() == null)
					{
						EmblEntryWriter writer = new EmblEntryWriter(entry);
						writer.write(annotationOnyFileWriter);
						setHasAnnotationOnlyFlatfile(true);
					}
					entryReader.read();
				}
			}
			 catch (IOException e) {
				throw new ValidationEngineException( e);
			 }
			if(isHasAnnotationOnlyFlatfile())
			{
				SubmissionFile annotationonlysf=null;
				if(submissionFile.getFixedFile()!=null)
					annotationonlysf=new SubmissionFile(FileType.ANNOTATION_ONLY_FLATFILE,new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly"),new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly"+SequenceEntryUtils.FIXED_FILE_SUFFIX),submissionFile.getReportFile());
				else
					annotationonlysf=new SubmissionFile(FileType.ANNOTATION_ONLY_FLATFILE,new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly"),null,submissionFile.getReportFile());
                options.submissionFiles.get().addFile(annotationonlysf);
			}
			
		}
	}
	private void registerFlatfileInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.flatfileInfo,options.processDir.get(),AssemblySequenceInfo.flatfilefileName);
	}
}
