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
import java.io.FileReader;

import javax.management.OperationsException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.report.SubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

@Description("")
public class MasterEntryValidationCheck extends FileValidationCheck
{

	public MasterEntryValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	@Override
	public boolean check() throws ValidationEngineException
	{
		boolean valid =true;
		Entry entry =null;
		try
		{
		    if(!getOptions().isRemote)
		    {
		      EraproDAOUtils utils = new EraproDAOUtilsImpl(getOptions().eraproConnection.get());
		      entry=utils.getMasterEntry(getOptions().analysisId.get(), getAnalysisType());
		    }
		    else
		    {
		    	//webin-cli
		    }
		    getOptions().getEntryValidationPlanProperty().validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		    EmblEntryValidationPlan validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
		    ValidationPlanResult result=validationPlan.execute(entry);
		    getReporter().writeToFile(getReportFile(new File(getOptions().reportDir.get()),getOptions().analysisId.get()+"_master" ),result);
		    
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		return valid;
	}
	@Override
	public boolean check(SubmissionFile file) throws ValidationEngineException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
