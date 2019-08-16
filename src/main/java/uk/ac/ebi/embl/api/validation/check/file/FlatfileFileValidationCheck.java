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
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid =true;
		EmblEntryValidationPlan validationPlan=null;
		fixedFileWriter =null;
		Origin origin =null;
		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			clearReportFile(getReportFile(submissionFile));
			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FLATFILE))
			{
				ValidationResult result = new ValidationResult();
				valid = false;
				result.append(FlatFileValidations.message(Severity.ERROR, "InvalidFileFormat","flatfile"));
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), result);
				addMessagekey(result);
				return valid;
			}
		Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;
		EmblEntryReader emblReader = new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());
		ValidationResult parseResult = emblReader.read();
		
		while(emblReader.isEntry())
		{
			if(!parseResult.isValid())
			{
				valid = false;
				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
				addMessagekey(parseResult);
			}
			parseResult=new ValidationResult();
			Entry entry = emblReader.getEntry();
			origin =entry.getOrigin();
			if(getOptions().context.get()==Context.genome)
            {
				if (entry.getSubmitterAccession() == null)
					entry.setSubmitterAccession(entry.getPrimaryAccession());
    			getOptions().getEntryValidationPlanProperty().sequenceNumber.set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get()+1);
             	if(entry.getSequence()==null||entry.getSequence().getSequenceBuffer()==null)
            	{  emblReader.read();
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
				throw new ValidationEngineException("Entry name can not be null for genome context, please check the AC * line.");
			} else {
				getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
			}
        	getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
        	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
        	appendHeader(entry);
        	ValidationPlanResult planResult=validationPlan.execute(entry);

        	if(null != entry.getSubmitterAccession()) {
				addEntryName(entry.getSubmitterAccession());
				int assemblyLevel = getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
				AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
				FileValidationCheck.flatfileInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
			}

			if(!planResult.isValid())
			{
				valid = false;
				getReporter().writeToFile(getReportFile(submissionFile), planResult);
				for(ValidationResult result: planResult.getResults())
				{
					if(!result.isValid())
					addMessagekey(result);
				}
			}
			else
			{
				if(fixedFileWriter!=null)
				new EmblEntryWriter(entry).write(fixedFileWriter);
			}
			emblReader.read();
			sequenceCount++;
		}
		}catch(ValidationEngineException e)
		{
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw e;
		} catch (Exception ex) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, ex.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw new ValidationEngineException(ex.getMessage(),ex);
		}

		if(valid)
          registerFlatfileInfo();
		return valid;
	}
	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	public void getAnnotationFlatfile() throws ValidationEngineException
	{
		for(SubmissionFile submissionFile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;

			try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter annotationOnyFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getAbsolutePath()+".annotationOnly"))))
			{
				EmblEntryReader emblReader = new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());
				emblReader.read();
				while(emblReader.isEntry())
				{
					Entry entry=emblReader.getEntry();
					if(entry.getSequence()==null||entry.getSequence().getSequenceBuffer()==null)
					{
						EmblEntryWriter writer = new EmblEntryWriter(entry);
						writer.write(annotationOnyFileWriter);
						setHasAnnotationOnlyFlatfile(true);
					}
					emblReader.read();
				}
			}
			 catch (IOException e) {
				throw new ValidationEngineException(e.getMessage(), e);
			 }
			if(isHasAnnotationOnlyFlatfile())
			{
				SubmissionFile annotationonlysf=null;
				if(submissionFile.getFixedFile()!=null)
					annotationonlysf=new SubmissionFile(FileType.ANNOTATION_ONLY_FLATFILE,new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly"),new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly.fixed"),submissionFile.getReportFile());
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
