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
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Description("")
public class AGPFileValidationCheck extends FileValidationCheck
{

	public AGPFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo)
	{
		super(options, sharedInfo);
	}

	public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException
	{
		ValidationPlan validationPlan;
		ValidationResult validationResult = new ValidationResult();
		fixedFileWriter=null;
		Origin origin =null;
		ConcurrentMap annotationMap = null;
		if(sharedInfo.hasAnnotationOnlyFlatfile ) {
			if (getAnnotationDB() == null) {
				throw new ValidationEngineException("Annotations are not parsed and stored in lookup db.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
			} else {
				annotationMap = getAnnotationDB().hashMap("map").createOrOpen();
			}
		}
		try(BufferedReader fileReader= CommonUtil.bufferedReaderFromFile(submissionFile.getFile());
			PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile))
		{
			clearReportFile(getReportFile(submissionFile));
			if(!validateFileFormat(submissionFile.getFile(), uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.AGP))
			{
				addErrorAndReport(validationResult,submissionFile, "InvalidFileFormat","AGP");
				return validationResult;
			}
			AGPFileReader reader = new AGPFileReader(new AGPLineReader(fileReader));
			HashMap<String,AssemblySequenceInfo> contigInfo = new HashMap<>();
			contigInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.fastafileName));
			contigInfo.putAll(AssemblySequenceInfo.getMapObject(options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
			if(contigInfo.isEmpty()) {
				addErrorAndReport(validationResult, submissionFile, "ContigInfoMissing");
				return validationResult;
			}
			ValidationResult parseResult = reader.read();
			validationResult.append(parseResult);
			getOptions().getEntryValidationPlanProperty().fileType.set(uk.ac.ebi.embl.api.validation.FileType.AGP);
        	while(reader.isEntry()) {
				if (!parseResult.isValid()) {
					getReporter().writeToFile(getReportFile(submissionFile), parseResult);
					addMessageStats(parseResult.getMessages());
				}

				Entry entry = reader.getEntry();
				origin = entry.getOrigin();
				entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));

				//set validation scope and collect unplacedEntries
				getOptions().getEntryValidationPlanProperty().validationScope.set(getValidationScope(entry.getSubmitterAccession()));
				Sequence.Topology chrListToplogy = getTopology(entry.getSubmitterAccession());
				if (chrListToplogy != null) {
					entry.getSequence().setTopology(chrListToplogy);
				}
				//level 2 placed entries should be removed from unplaced set
				if (!sharedInfo.unplacedEntryNames.isEmpty()) {
					for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
						if (agpRow.getComponent_type_id() != null && !agpRow.getComponent_type_id().equalsIgnoreCase("N")
								&& agpRow.getComponent_id() != null) {
							sharedInfo.unplacedEntryNames.remove(agpRow.getComponent_id().toUpperCase());
						}
					}
				}

				if (sharedInfo.hasAnnotationOnlyFlatfile) {
					Entry annoationEntry = (Entry) annotationMap.get(entry.getSubmitterAccession().toUpperCase());
					if (annoationEntry != null) {
						String molType = null;
						if(annoationEntry.getSequence() != null && annoationEntry.getSequence().getMoleculeType() != null){
							molType = annoationEntry.getSequence().getMoleculeType();
						}
						annoationEntry.setSequence(entry.getSequence());
						entry = annoationEntry;
						if(molType != null) {
							entry.getSequence().setMoleculeType(molType);
						}
					}
				} else {
					appendHeader(entry);
					addSubmitterSeqIdQual(entry);
				}

				getOptions().getEntryValidationPlanProperty().assemblySequenceInfo.set(contigInfo);
				getOptions().getEntryValidationPlanProperty().sequenceNumber.set(getOptions().getEntryValidationPlanProperty().sequenceNumber.get() + 1);
				validationPlan = new EmblEntryValidationPlan(getOptions().getEntryValidationPlanProperty());
				ValidationResult planResult = validationPlan.execute(entry);
				validationResult.append(planResult);

				if(null != entry.getSubmitterAccession()) {
					addEntryName(entry.getSubmitterAccession());
					int assemblyLevel = getAssemblyLevel(getOptions().getEntryValidationPlanProperty().validationScope.get());
					AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(entry.getSequence().getLength(), assemblyLevel, null);
					sharedInfo.agpInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
					contigInfo.put(entry.getSubmitterAccession().toUpperCase(), sequenceInfo);
				}

    			if(!planResult.isValid())
    			{
    				getReporter().writeToFile(getReportFile(submissionFile), planResult);
    				addMessageStats(planResult.getMessages());
    			}
    			else
				{
					if(fixedFileWriter != null) {
						new EmblEntryWriter(entry).write(fixedFileWriter);
						constructAGPSequence(entry);
						writeEntryToFile(entry, submissionFile);
					}
				}
				parseResult = reader.read();
				validationResult.append(parseResult);
        	}

		} catch (ValidationEngineException vee) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, vee.getMessage(),origin);
			throw vee;
		}
		catch (Exception e) {
			getReporter().writeToFile(getReportFile(submissionFile),Severity.ERROR, e.getMessage(),origin);
			throw new ValidationEngineException(e.getMessage(), e);
		} finally {
			closeMapDB(getContigDB(), getAnnotationDB());
		}
		if(validationResult.isValid())
	        registerAGPfileInfo();
		return validationResult;
	}

	private void constructAGPSequence(Entry entry) throws ValidationEngineException
    {
		try
		{
 		ByteBuffer sequenceBuffer=ByteBuffer.wrap(new byte[new Long(entry.getSequence().getLength()).intValue()]);
 
		ConcurrentMap contigMap =null;
		if(getContigDB()!=null) {
			contigMap = getContigDB().hashMap("map").createOrOpen();
		}

			for (AgpRow currObjectAGPRow : entry.getSequence().getSortedAGPRows()) {
				if (!currObjectAGPRow.isGap()) {

					Object sequence;
					if (currObjectAGPRow.getComponent_id() != null && getContigDB() != null) {
						//Component can be a contig/scaffold, single contig(component) can be placed in multiple agp objects(scaffold/chromosomes)
						Object seqsOfCurrRowComponent = contigMap.get(currObjectAGPRow.getComponent_id().toLowerCase());
						if (seqsOfCurrRowComponent != null) {
							for (AgpRow component : (List<AgpRow>) seqsOfCurrRowComponent) {
								//proceed only if the component belongs to the current object(AGP row)
								if (component.getObject().equalsIgnoreCase(currObjectAGPRow.getObject())) {
									sequence = component.getSequence();
									if(sequence != null) {
										sequenceBuffer.put((byte[]) sequence);
									} else {
											throw new ValidationEngineException("Failed to contruct AGP Sequence. invalid component:" + currObjectAGPRow.getComponent_id());
									}
								}
							}
						} else {
							throw new ValidationEngineException("Component not available in sequence lookup db(contigDB)"+currObjectAGPRow.getComponent_id());
						}
					} else {
						throw new ValidationEngineException("Either Component missing for current entry or sequence db(contigDB) not available."+entry.getSubmitterAccession());
					}

				} else if (currObjectAGPRow.getGap_length() != null)
					sequenceBuffer.put(StringUtils.repeat("N".toLowerCase(), currObjectAGPRow.getGap_length().intValue()).getBytes());
			}
			entry.getSequence().setSequence(sequenceBuffer);

			//check if the current object(scaffold) is placed(will be a component) on another object(could be another scaffold/chromosome)
			//if yes, construct sequence for all the objects where the current object has been placed
			List<AgpRow> agpRows = (List<AgpRow>) contigMap.get(entry.getSubmitterAccession().toLowerCase());
			if (agpRows != null) {
				for (AgpRow agpRow : agpRows) {
					agpRow.setSequence(entry.getSequence().getSequenceByte(agpRow.getComponent_beg(), agpRow.getComponent_end()));
				}
				contigMap.put(entry.getSubmitterAccession().toLowerCase(), agpRows);
			}

			getContigDB().commit();

		}catch(Exception e)
		{
			closeMapDB(getContigDB());
			throw new ValidationEngineException(e);
		}
		}

	@Override
	public ValidationResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
	}
	
	public void createContigDB() throws ValidationEngineException
	{
		for( SubmissionFile submissionFile : options.submissionFiles.get().getFiles(FileType.AGP)) 
		{
			try(BufferedReader fileReader= CommonUtil.bufferedReaderFromFile(submissionFile.getFile()))
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
		AssemblySequenceInfo.writeMapObject(sharedInfo.agpInfo,options.processDir.get(),AssemblySequenceInfo.agpfileName);
	}
	
}
