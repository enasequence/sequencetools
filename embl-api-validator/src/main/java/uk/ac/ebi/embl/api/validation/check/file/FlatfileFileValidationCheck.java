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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

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
		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
		Format format = options.context.get()==Context.genome?Format.ASSEMBLY_FILE_FORMAT:Format.EMBL_FORMAT;
		EmblEntryReader emblReader = new EmblEntryReader(fileReader,format,submissionFile.getFile().getName());
		ValidationResult parseResult = emblReader.read();
		
		while(emblReader.isEntry())
		{
			if(!parseResult.isValid())
			{
				valid = false;
				getReporter().writeToFile(getReportFile(getOptions().reportDir.get(), submissionFile.getFile().getName()), parseResult);
			}
			Entry entry = emblReader.getEntry();
			entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
          
			
			getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScopeandEntrynames(entry.getSubmitterAccession().toUpperCase()));
        	getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
        	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
        	appendHeader(entry);
        	  if(getOptions().context.get()==Context.genome)
              {
              	collectContigInfo(entry);
              	if(entry.getSequence()==null||entry.getSequence().getSequenceBuffer()==null)
              		continue;
              }
			ValidationPlanResult planResult=validationPlan.execute(entry);
			if(!planResult.isValid())
			{
				valid = false;
				if(getOptions().reportDir.isPresent())
					getReporter().writeToFile(getReportFile(getOptions().reportDir.get(), submissionFile.getFile().getName()), planResult);
				for(ValidationResult result: planResult.getResults())
				{
					addMessagekey(result);
				}
			}
			else
			{
				if(fixedFileWriter!=null)
				new EmblEntryWriter(entry).write(fixedFileWriter);
			}
			emblReader.read();
		}
		
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;
	}
	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	public void getAnnotationFlatfile() throws ValidationEngineException, FileNotFoundException, IOException 
	{
		for(SubmissionFile submissionFile:options.submissionFiles.get().getFiles(FileType.FLATFILE))
		{
			try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter annotationOnyFileWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(submissionFile.getFile().getAbsolutePath()+".annotationOnly"))))
			{
				EmblEntryReader emblReader = new EmblEntryReader(fileReader,EmblEntryReader.Format.EMBL_FORMAT,submissionFile.getFile().getName());
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
					
				}
			}
			if(isHasAnnotationOnlyFlatfile())
			{
				SubmissionFile annotationonlysf= new SubmissionFile(FileType.ANNOTATION_ONLY_FLATFILE,new File(submissionFile.getFile().getAbsolutePath()+".annotationOnly"));;
                options.submissionFiles.get().addFile(annotationonlysf);
			}
			
		}
	}
}
