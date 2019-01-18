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
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;
import java.nio.file.Files;
import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

@Description("")
public class AGPFileValidationCheck extends FileValidationCheck
{

	private int i=0;
	public AGPFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		boolean valid=true;
		ValidationPlan validationPlan =null;
		fixedFileWriter=null;
		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			clearReportFile(getReportFile(submissionFile));
			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.AGP))
			{
				ValidationResult result = new ValidationResult();
				valid = false;
				result.append(FlatFileValidations.message(Severity.ERROR, "InvalidFileFormat","AGP"));
				if(getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), result);
				addMessagekey(result);
				return valid;
			}
			i=0;
			AGPFileReader reader = new AGPFileReader(new AGPLineReader(fileReader));
			ValidationResult parseResult = reader.read();
			getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.AGP);
        	while(reader.isEntry())
        	{
        		if(!parseResult.isValid())
    			{	valid = false;
    				getReporter().writeToFile(getReportFile(submissionFile), parseResult);
    				addMessagekey(parseResult);
    			}
				parseResult=new ValidationResult();
        		Entry entry =reader.getEntry();
    			getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
    			getOptions().getEntryValidationPlanProperty().assemblySequenceInfo.set(sequenceInfo);
    			getOptions().getEntryValidationPlanProperty().sequenceNumber.set(new Integer(getOptions().getEntryValidationPlanProperty().sequenceNumber.get()+1));
    			validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
            	appendHeader(entry);
    			ValidationPlanResult planResult=validationPlan.execute(entry);
            	addEntryName(entry.getSubmitterAccession(),getOptions().getEntryValidationPlanProperty().validationScope.get(),entry.getSequence().getLength());
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
					new EmblEntryWriter(entry).write(fixedFileWriter);
					if(valid)
					constructAGPSequence(entry);
					//write AGP with sequence
					//entry.setNonExpandedCON(false);
					//new EmblEntryWriter(entry).write(sequenceFixedFileWriter);
				}
				reader.read();
        	}

		}catch (Exception e) {
			if(getSequenceDB()!=null)
	               getSequenceDB().close();
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;
	}
	
	private void constructAGPSequence(Entry entry) throws ValidationEngineException
    {
		try
		{
 		ByteBuffer sequenceBuffer=ByteBuffer.wrap(new byte[new Long(entry.getSequence().getLength()).intValue()]);
 
         for(AgpRow agpRow: entry.getSequence().getSortedAGPRows())
         {
         	i++;
           	if(!agpRow.isGap())
         	  sequenceBuffer.put(contigRangeMap.get(agpRow.getComponent_id().toUpperCase()+"_"+i).getSequence());
	       	else
        	  sequenceBuffer.put(StringUtils.repeat("N".toLowerCase(), agpRow.getGap_length().intValue()).getBytes());           	
         }
         entry.getSequence().setSequence(sequenceBuffer);
         if(getOptions().context.get()==Context.genome&&getSequenceDB()!=null)
			{
        	 if(entry.getSubmitterAccession()!=null)
        	 {
				ConcurrentMap map = getSequenceDB().hashMap("map").createOrOpen();
				map.put(entry.getSubmitterAccession().toUpperCase(),new String(entry.getSequence().getSequenceByte()));
			}
			}
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		if(getSequenceDB()!=null)
		getSequenceDB().commit();  
		}
	@Override
	public boolean check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	public void getAGPEntries() throws ValidationEngineException
	{
		for( SubmissionFile submissionFile : options.submissionFiles.get().getFiles(FileType.AGP)) 
		{
			try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile()))
			{
				AGPFileReader reader = new AGPFileReader( new AGPLineReader(fileReader));

				ValidationResult result=reader.read();
				int i=1;

				while(reader.isEntry())
				{
					if(result.isValid())
					{
					agpEntryNames.add( ( (Entry) reader.getEntry() ).getSubmitterAccession().toUpperCase() );

					for(AgpRow agpRow: ((Entry)reader.getEntry()).getSequence().getSortedAGPRows())
					{
						
						if(!agpRow.isGap())
						{
							contigRangeMap.put(agpRow.getComponent_id().toUpperCase()+"_"+i,agpRow);
						}
						i++;
					}
					}
				result=reader.read();
				}

			}catch(Exception e)
			{
				throw new ValidationEngineException(e.getMessage());
			}

		}

	}
	
}
