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
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentMap;

import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.ByteBufferUtils;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.fasta.reader.FastaFileReader;
import uk.ac.ebi.embl.fasta.reader.FastaLineReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

@Description("")
public class FastaFileValidationCheck extends FileValidationCheck
{

	public FastaFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid=true;
		fixedFileWriter =null;
		ConcurrentMap sequenceMap =null;
		Origin origin =null;
		if(getSequenceDB()!=null)
			sequenceMap= getSequenceDB().hashMap("map").createOrOpen();

		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			clearReportFile(getReportFile(submissionFile));

			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FASTA))
			{
				ValidationResult result = new ValidationResult();
				valid = false;
				result.append(FlatFileValidations.message(Severity.ERROR, "InvalidFileFormat","fasta"));
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), result);
				addMessagekey(result);
				return valid;
			}
			FastaFileReader reader = new FastaFileReader( new FastaLineReader( fileReader));
			ValidationResult parseResult = reader.read();
			EmblEntryValidationPlan validationPlan =null;
		
			while(reader.isEntry())
			{
				if(!parseResult.isValid())
				{
					valid = false;
					getReporter().writeToFile(getReportFile(submissionFile), parseResult);
					addMessagekey(parseResult);
				}
				parseResult=new ValidationResult();
				Entry entry=reader.getEntry();
				origin=entry.getOrigin();
				if(getOptions().context.get()==Context.genome)
				{
	    			getOptions().getEntryValidationPlanProperty().sequenceNumber.set(new Integer(getOptions().getEntryValidationPlanProperty().sequenceNumber.get()+1));
					collectContigInfo(entry);
					if(entry.getSubmitterAccession()!=null && getSequenceDB()!=null)
					{
						sequenceMap.put(entry.getSubmitterAccession().toUpperCase(), ByteBufferUtils.string(entry.getSequence().getSequenceBuffer()));
					}
				}
            	getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
            	getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.FASTA);
            	validationPlan=new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
            	appendHeader(entry);
				ValidationPlanResult planResult=validationPlan.execute(entry);
            	addEntryName(entry.getSubmitterAccession(),getOptions().getEntryValidationPlanProperty().validationScope.get(),entry.getSequence().getLength(),uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FASTA);
				if(!planResult.isValid())
				{
					valid = false;
    				getReporter().writeToFile(getReportFile(submissionFile), planResult);
					for(ValidationResult result: planResult.getResults())
					{
						addMessagekey(result);
					}
				}
				else
				{
					if(fixedFileWriter!=null)
					new EmblEntryWriter(entry).write(getFixedFileWriter(submissionFile));
				}
				parseResult= reader.read();
				sequenceCount++;
			}
			if(getSequenceDB()!=null)
			{
				getSequenceDB().commit();
			}
			if(getContigDB()!=null)
			{
				getContigDB().commit();
			}
		} catch (ValidationEngineException e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeDB(getSequenceDB(), getContigDB());
			throw e;
		}
		catch (Exception e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeDB(getSequenceDB(), getContigDB());
			throw new ValidationEngineException(e.getMessage(), e);
		}

		if(valid)
			registerFastaInfo();
		return valid;	
	}
	private void registerFastaInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.fastaInfo,options.processDir.get(),AssemblySequenceInfo.fastafileName);
	}

	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
}
