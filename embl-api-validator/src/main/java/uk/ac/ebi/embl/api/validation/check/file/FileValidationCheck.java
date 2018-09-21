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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporterTest;
import uk.ac.ebi.embl.api.validation.report.SubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

public abstract class FileValidationCheck {

	private SubmissionOptions options =null;
	protected List<String> chromosomeNames = new ArrayList<String>();
	protected SubmissionReporter reporter=null;

	public FileValidationCheck(SubmissionOptions options) {
		this.options =options;
	}
	public abstract boolean check(SubmissionFile file) throws ValidationEngineException;
	public boolean check() throws ValidationEngineException {
		return false;
	}

	protected SubmissionOptions getOptions() {
		return options;
	}
	
	protected AnalysisType getAnalysisType()
	{
		switch(getOptions().getEntryValidationPlanProperty().validationScope.get())
		{
		case ASSEMBLY_TRANSCRIPTOME:
			return AnalysisType.TRANSCRIPTOME_ASSEMBLY;
		case ASSEMBLY_CHROMOSOME:
		case ASSEMBLY_CONTIG:
		case ASSEMBLY_MASTER:
		case ASSEMBLY_SCAFFOLD:
			 return AnalysisType.SEQUENCE_ASSEMBLY;
		default :
			return null;
		}
	}
	
	public SubmissionReporter getReporter()
	{
		HashSet<Severity> severity = new HashSet<Severity>();
		severity.add(Severity.ERROR);
		if(reporter==null)
			return new DefaultSubmissionReporter(severity);
		return reporter;
	}
	
	private static final String REPORT_FILE_SUFFIX = ".report";
	 
	public  File getReportFile(File reportDir, String fileName) throws ValidationEngineException
	{
	      return new File( reportDir, fileName + REPORT_FILE_SUFFIX );
	}

}
