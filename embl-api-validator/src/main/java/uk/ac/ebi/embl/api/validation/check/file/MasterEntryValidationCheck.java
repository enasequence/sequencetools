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

import java.sql.SQLException;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

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
		try
		{
			if(!getOptions().isRemote)
			{
				EraproDAOUtils utils = new EraproDAOUtilsImpl(getOptions().eraproConnection.get());
				masterEntry=utils.getMasterEntry(getOptions().analysisId.get(), getAnalysisType());
			}
			else
			{
				//webin-cli
				masterEntry=getMasterEntry(getAnalysisType(), getOptions().assemblyInfoEntry.get(), getOptions().source.get());
			}
			getOptions().getEntryValidationPlanProperty().validationScope.set(ValidationScope.ASSEMBLY_MASTER);
        	getOptions().getEntryValidationPlanProperty().fileType.set(FileType.MASTER);
			EmblEntryValidationPlan validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
			ValidationPlanResult planResult=validationPlan.execute(masterEntry);
			if(!planResult.isValid())
			{
				valid = false;
				if(getOptions().reportDir.isPresent())
					getReporter().writeToFile(getReportFile(getOptions().reportDir.get(), getOptions().analysisId.get()+"_master" ), planResult);
				for(ValidationResult result: planResult.getResults())
				{
					addMessagekey(result);
				}
			}
			else
				setMasterEntry(masterEntry);
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

	public Entry getMasterEntry(AnalysisType analysisType,AssemblyInfoEntry infoEntry,SourceFeature source) throws SQLException
	{
		Entry masterEntry = new Entry();
		if(analysisType == null) {
			return  masterEntry;
		}
		masterEntry.setIdLineSequenceLength(1);
		SequenceFactory sequenceFactory = new SequenceFactory();
		masterEntry.setDataClass(Entry.SET_DATACLASS);
		Sequence sequence = sequenceFactory.createSequence();
		masterEntry.setSequence(sequence);
		masterEntry.setIdLineSequenceLength(1);
		if(analysisType == AnalysisType.TRANSCRIPTOME_ASSEMBLY) {
			masterEntry.getSequence().setMoleculeType("transcribed RNA");
		} else {
			masterEntry.getSequence().setMoleculeType("genomic DNA");
		}
		masterEntry.getSequence().setTopology(Topology.LINEAR);		
		source.setMasterLocation();
		masterEntry.setStatus(Entry.Status.getStatus(2));//assembly new entries status should always be private
		masterEntry.addProjectAccession(new Text(infoEntry.getStudyId()));
		masterEntry.addXRef(new XRef("BioSample", infoEntry.getBiosampleId()));
		if(infoEntry.isTpa())
		{
			masterEntry.addKeyword(new Text("Third Party Data"));
			masterEntry.addKeyword(new Text("TPA"));
			masterEntry.addKeyword(new Text("assembly"));
		}

		masterEntry.addFeature(source);
		String description=SequenceEntryUtils.generateMasterEntryDescription(source);
		masterEntry.setDescription(new Text(description));
		return masterEntry;
	}

}
