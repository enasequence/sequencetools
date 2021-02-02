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
import org.mapdb.Serializer;
import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Description("")
public class AGPFileValidationCheck extends FileValidationCheck
{

	public AGPFileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}	
	public ValidationPlanResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		ValidationPlan validationPlan;
		fixedFileWriter=null;
		Origin origin =null;
		ValidationPlanResult validationResult = new ValidationPlanResult();

		try(BufferedReader fileReader= getBufferedReader(submissionFile.getFile());PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			clearReportFile(getReportFile(submissionFile));
			if (!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.AGP)) {
				validationResult.append(reportError(getReportFile(submissionFile), "AGP"));
				validationResult.setHasError(true);
				return validationResult;
			}

			AGPFileReader reader = new AGPFileReader(new AGPLineReader(fileReader));
			HashMap<String,AssemblySequenceInfo> contigInfo = new HashMap<>();
			contigInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
			contigInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
			if(contigInfo.isEmpty())
				throw new ValidationEngineException("AGP validation can't be done : Contig Info is missing");
			ValidationResult parseResult = reader.read();
			validationResult.append(parseResult);
			getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.AGP);
        	while(reader.isEntry()) {
				if (!parseResult.isValid()) {
					validationResult.setHasError(true);
					getReporter().writeToFile(getReportFile(submissionFile), parseResult);
					addMessageKeys(parseResult.getMessages());
				}

				Entry entry = reader.getEntry();
				origin = entry.getOrigin();
				entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
				if(!isHasAnnotationOnlyFlatfile()) {
					addAgpEntryName(entry.getSubmitterAccession().toUpperCase());
				}
				//set validation scope and collect unplacedEntries
				getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
				Sequence.Topology chrListToplogy = getTopology(entry.getSubmitterAccession());
				if (chrListToplogy != null) {
					entry.getSequence().setTopology(chrListToplogy);
				}
				//level 2 placed entries should be removed from unplaced set
				if (!unplacedEntryNames.isEmpty()) {
					for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
						if (agpRow.getComponent_type_id() != null && !agpRow.getComponent_type_id().equalsIgnoreCase("N")
								&& agpRow.getComponent_id() != null) {
							unplacedEntryNames.remove(agpRow.getComponent_id().toUpperCase());
						}
					}
				}

				getOptions().getEntryValidationPlanProperty().assemblySequenceInfo.set(contigInfo);
				getOptions().getEntryValidationPlanProperty().sequenceNumber.set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get() + 1);
				validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
				appendHeader(entry);
				ValidationPlanResult planResult = validationPlan.execute(entry);
				validationResult.append(planResult);

				if(null != entry.getSubmitterAccession()) {
					addEntryName(entry.getSubmitterAccession());
					int assemblyLevel = getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
					AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
					FileValidationCheck.agpInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
					contigInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
				}

				if (planResult.isValid()) {
					if (fixedFileWriter != null)
						new EmblEntryWriter(entry).write(fixedFileWriter);
					if (isHasAnnotationOnlyFlatfile())
						constructAGPSequence(entry);
				} else {
					validationResult.setHasError(true);
					addMessageKeys(planResult.getMessages());
					getReporter().writeToFile(getReportFile(submissionFile), planResult);
				}
				parseResult = reader.read();
				validationResult.append(parseResult);
        	}

		} catch (ValidationEngineException vee) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, vee.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw vee;
		}
		catch (IOException e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			closeDB(getContigDB(), getSequenceDB());
			throw new ValidationEngineException(e);
		}
		if(validationResult.isValid()) {
			registerAGPfileInfo();
		} else {
			validationResult.setHasError(true);
		}
		return validationResult;
	}

	private void constructAGPSequence(Entry entry) throws ValidationEngineException
    {
		try
		{
 		ByteBuffer sequenceBuffer=ByteBuffer.wrap(new byte[new Long(entry.getSequence().getLength()).intValue()]);
 
		ConcurrentMap contigMap =null;
		ConcurrentMap sequenceMap = null;
		if(getContigDB()!=null)
			contigMap=getContigDB().hashMap("map").createOrOpen();
		if(getSequenceDB()!=null)
		sequenceMap=getSequenceDB().hashMap("map").createOrOpen();


			for (AgpRow agpRow : entry.getSequence().getSortedAGPRows()) {
				if (!agpRow.isGap()) {

					Object sequence;
					if (agpRow.getComponent_id() != null && getContigDB() != null) {

						Object rows = contigMap.get(agpRow.getComponent_id().toLowerCase());
						if (rows != null) {
							for (AgpRow row : (List<AgpRow>) rows) {
								if (row.getObject().toLowerCase().equals(agpRow.getObject().toLowerCase())) {
									sequence = row.getSequence();
									if (sequence != null)
										sequenceBuffer.put((byte[]) sequence);
									else {
										throw new ValidationEngineException("Failed to contruct AGP Sequence. invalid component:" + agpRow.getComponent_id(), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
									}
								}
							}
						}
					}

				} else if (agpRow.getGap_length() != null)
					sequenceBuffer.put(StringUtils.repeat("N".toLowerCase(), agpRow.getGap_length().intValue()).getBytes());
			}
         entry.getSequence().setSequence(sequenceBuffer);
         if(getOptions().context.get()==Context.genome && getSequenceDB()!=null)
			{
        	 if(entry.getSubmitterAccession()!=null)
        	 {
				sequenceMap.put(entry.getSubmitterAccession().toUpperCase(),new String(entry.getSequence().getSequenceByte()));
			}
			}

		}catch(Exception e)
		{
			if(getSequenceDB()!=null)
				getSequenceDB().close();
			if(getContigDB()!=null)
				getContigDB().close();
			throw new ValidationEngineException(e);
		}
		if(getSequenceDB()!=null)
		getSequenceDB().commit();  
		}
	@Override
	public ValidationPlanResult check() throws ValidationEngineException {
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
						Entry entry = reader.getEntry();
						entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
						addAgpEntryName(entry.getSubmitterAccession().toUpperCase());

						for (AgpRow agpRow : entry.getSequence().getSortedAGPRows()) {
							if (!agpRow.isGap()) {
								if (agpRow.getComponent_id() != null && getContigDB() != null) {
									ConcurrentMap<String, Object> map = getContigDB().hashMap("map", Serializer.STRING, getContigDB().getDefaultSerializer()).createOrOpen();
									List<AgpRow> agpRows = (List<AgpRow>) map.get(agpRow.getComponent_id().toLowerCase());
									if (agpRows == null) {
										agpRows = new ArrayList<>();
									}
									agpRows.add(agpRow);
									map.put(agpRow.getComponent_id().toLowerCase(), agpRows);
								}
							}
							i++;
						}
					}
				result=reader.read();
				}

				if(getContigDB()!=null)
					getContigDB().commit();
			}catch(Exception e)
			{
				if(getContigDB()!=null)
					getContigDB().close();
				throw new ValidationEngineException(e);
			}

		}

	}
	private void registerAGPfileInfo() throws ValidationEngineException
	{
		AssemblySequenceInfo.writeMapObject(FileValidationCheck.agpInfo,options.processDir.get(),AssemblySequenceInfo.agpfileName);
	}
	
}
