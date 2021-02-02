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
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.reader.ReferenceReader;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.sql.SQLException;

import static uk.ac.ebi.embl.api.validation.helper.Utils.getExceptionCause;

@Description("")
public class MasterEntryValidationCheck extends FileValidationCheck
{

	public MasterEntryValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	@Override
	public ValidationPlanResult check() throws ValidationEngineException
	{
		ValidationPlanResult planResult;
		try {
			if(getOptions().getEntryValidationPlanProperty() != null && getOptions().getEntryValidationPlanProperty().validationScope.get() != ValidationScope.NCBI_MASTER) {
				getOptions().getEntryValidationPlanProperty().validationScope.set(ValidationScope.ASSEMBLY_MASTER);
			}
        	getOptions().getEntryValidationPlanProperty().fileType.set(FileType.MASTER);
			if(!getOptions().isWebinCLI)
			{
				EraproDAOUtils utils = new EraproDAOUtilsImpl(getOptions().eraproConnection.get());
				masterEntry = utils.getMasterEntry(getOptions().analysisId.get(), getAnalysisType());
				if(masterEntry != null && StringUtils.isNotBlank(masterEntry.getComment().getText())) {
					StringWriter strWriter = new StringWriter();
					FlatFileWriter.writeBlock(strWriter, "", "", masterEntry.getComment().getText(),
							WrapType.EMBL_WRAP, WrapChar.WRAP_CHAR_BREAK, EmblPadding.CC_PADDING.length());
					String comment = strWriter.toString().trim();
					if( comment.length()-1 == comment.lastIndexOf("\n")) {
						comment = comment.substring(0,comment.length()-1);
					}
					masterEntry.setComment(new Text(comment));
				}
			}
			else
			{
				//webin-cli
				if(!getOptions().assemblyInfoEntry.isPresent())
					throw new ValidationEngineException("SubmissionOption assemblyInfoEntry must be given to generate master entry", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
				if(!getOptions().source.isPresent())
					throw new ValidationEngineException("SubmissionOption source must be given to generate master entry", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
				masterEntry = getMasterEntry(getAnalysisType(), getOptions().assemblyInfoEntry.get(), getOptions().source.get());
			}

			if(Context.transcriptome == options.context.get() && masterEntry != null) {
				addTranscriptomeInfo(masterEntry);
			}

			EmblEntryValidationPlan validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
			planResult=validationPlan.execute(masterEntry);

			if(!planResult.isValid())
			{
				planResult.setHasError(true);
				getReporter().writeToFile(Paths.get(getOptions().reportDir.get(), "MASTER.report"), planResult);
				addMessageKeys(planResult.getMessages());
			}
			else
			{
				if(!getOptions().isWebinCLI)
				new EmblEntryWriter(masterEntry).write(new PrintWriter(getOptions().processDir.get()+File.separator+masterFileName));
			}
		} catch (SQLException e) {
			StringBuilder msg = new StringBuilder();

			msg.append(e.getSQLState()); msg.append("\t");
			msg.append(e.getErrorCode()); msg.append("\t");
			msg.append(e.getMessage()); msg.append("\n");

			throw new ValidationEngineException(getExceptionCause(e.getCause(), msg).toString(), ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
		} catch (IOException e) {
			throw new ValidationEngineException(e);
		}
		return planResult;
	}

	@Override
	public ValidationPlanResult check(SubmissionFile file) throws ValidationEngineException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Entry getMasterEntry(AnalysisType analysisType,AssemblyInfoEntry infoEntry,SourceFeature source) throws ValidationEngineException
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
			masterEntry.getSequence().setMoleculeType(infoEntry.getMoleculeType()==null?"genomic DNA":infoEntry.getMoleculeType());
		}
		if(masterEntry.getSequence().getTopology() == null )
			masterEntry.getSequence().setTopology(Topology.LINEAR);
		source.setMasterLocation();
		masterEntry.setStatus(Entry.Status.getStatus(2));//assembly new entries status should always be private
		masterEntry.addProjectAccession(new Text(infoEntry.getProjectId()));
		masterEntry.addXRef(new XRef("BioSample", infoEntry.getBiosampleId()));
		if(infoEntry.isTpa())
		{
            EntryUtils.setKeyWords(masterEntry);
		}
		masterEntry.addFeature(source);
		if(getOptions().context.get()==Context.genome)
			masterEntry.setDescription(new Text(SequenceEntryUtils.generateMasterEntryDescription(source, AnalysisType.SEQUENCE_ASSEMBLY , infoEntry.isTpa())));

		if (StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAddress())
				&& StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAuthors())) {
			masterEntry.removeReferences();
			masterEntry.addReference(new ReferenceReader().getReference(options.assemblyInfoEntry.get().getAuthors(),
					options.assemblyInfoEntry.get().getAddress(), options.assemblyInfoEntry.get().getDate()));
		}
		return masterEntry;
	}
	
	public Entry getMasterEntry()
	{
		return masterEntry;
	}
	
	private void addTranscriptomeInfo(Entry masterEntry)
	{
		masterEntry.getSequence().setMoleculeType("transcribed RNA");
		masterEntry.setStatus(Entry.Status.getStatus(2));
	}

}
