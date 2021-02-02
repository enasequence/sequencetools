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
package uk.ac.ebi.embl.api.validation.check.entry;

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.List;

@Description("")
public class AGPValidationCheck extends EntryValidationCheck
{
	String[] componentTypeArray={"A","D","F","G","O","P","W","N","U"};
	String[] gapTypeArray={"scaffold","contig","centromere","short_arm","heterochromatin","telomere","repeat","contamination"};
	String[] orientationArray= {"+","plus","-","minus","?","0","unknown","na","irrelevant"};
	String[] linkageEvidenceArray= {"na","paired-ends","align_genus","align_xgenus","align_trnscpt","within_clone","clone_contig","map","strobe","unspecified"};

	private final static String MESSAGE_KEY_OBJECT_NAME_ERROR = "AGPValidationCheck-1";
	private final static String MESSAGE_KEY_PART_NUMBER_ERROR = "AGPValidationCheck-2";
	private final static String MESSAGE_KEY_COMPONENT_TYPE_ID_ERROR = "AGPValidationCheck-3";
	private final static String MESSAGE_KEY_GAP_TYPE_ERROR="AGPValidationCheck-4";
	private final static String MESSAGE_KEY_ORIENTATION_ERROR="AGPValidationCheck-5";
	private final static String MESSAGE_KEY_LINKAGE_EVIDENCE_ERROR="AGPValidationCheck-6";
	private final static String MESSAGE_KEY_OBJECT_COORDINATE_ERROR="AGPValidationCheck-7";
	private final static String MESSAGE_KEY_GAP_LENGTH_ERROR="AGPValidationCheck-8";
	private final static String MESSAGE_KEY_COMPONENT_VALID_ERROR="AGPValidationCheck-9";
	private final static String MESSAGE_KEY_PART_COUNT_ERROR="AGPValidationCheck-10";
	private final static String MESSAGE_KEY_COMPONENT_COORDINATE_ERROR="AGPValidationCheck-11";
	private final static String MESSAGE_KEY_COMPONENT_RANGE_ERROR="AGPValidationCheck-12";
	private final static String MESSAGE_KEY_INVALID_ROW_ERROR="AGPValidationCheck-13";
	private final static String MESSAGE_KEY_INVALID_LINKAGE_ERROR="AGPValidationCheck-14";
	private final static String MESSAGE_KEY_SAME_COMPONENT_AND_OBJECT_ERROR="AGPValidationCheck-15";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{

		result = new ValidationResult();

		if (entry == null||entry.getSequence()==null)
		{
			return result;
		}
		
		if(!FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}
		
		if (entry.getSequence().getAgpRows().size() == 0)
		{
			
				reportError(entry.getOrigin(), MESSAGE_KEY_PART_COUNT_ERROR, entry.getSubmitterAccession());
				return result;
		}

		Integer prevPartNumber = 0;
		Long prevEnd = -1l;	
		
		for (AgpRow agpRow : entry.getSequence().getAgpRows())
		{
			String objectName = agpRow.getObject();
			Long object_begin=agpRow.getObject_beg();
			Long object_end=agpRow.getObject_end();
			Integer part_number=agpRow.getPart_number();
			String component_type_id=agpRow.getComponent_type_id();
			String component_id=agpRow.getComponent_id();
			Long component_begin=agpRow.getComponent_beg();
			Long component_end=agpRow.getComponent_end();
			String orientation=agpRow.getOrientation();
			Long gap_length=agpRow.getGap_length();
			String gap_type=agpRow.getGap_type();
			boolean hasLinkage = agpRow.hasLinkage();
			List<String> linkageEvidences=agpRow.getLinkageevidence();
			String component_acc=null;

			if(agpRow==null||!agpRow.isValid())
			{
				reportError(entry.getOrigin(),MESSAGE_KEY_INVALID_ROW_ERROR,entry.getPrimaryAccession()==null?entry.getSubmitterAccession():entry.getPrimaryAccession());
				continue;
			}
				
       // object_name missing check
			if (objectName == null || objectName.isEmpty())
			{
				reportError(agpRow.getOrigin(),	MESSAGE_KEY_OBJECT_NAME_ERROR);
			}
		//part number validation check
			if (part_number != (prevPartNumber + 1))
			{
				reportError(agpRow.getOrigin(), MESSAGE_KEY_PART_NUMBER_ERROR, entry.getSubmitterAccession());
			}
			prevPartNumber = part_number;
			//object_begin, object_end validation checks
	

			if (object_begin <= 0)
			{
				reportError(agpRow.getOrigin(), MESSAGE_KEY_OBJECT_COORDINATE_ERROR, agpRow.getObject_beg(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (object_end <= 0)
			{
				reportError(agpRow.getOrigin(),MESSAGE_KEY_OBJECT_COORDINATE_ERROR,agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (object_end < object_begin)
			{
				reportError(agpRow.getOrigin(),MESSAGE_KEY_OBJECT_COORDINATE_ERROR,	agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (prevEnd != -1 && object_begin != prevEnd + 1)
			{
				reportError(agpRow.getOrigin(),	MESSAGE_KEY_OBJECT_COORDINATE_ERROR, agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}
			prevEnd = object_end;
			//component_type check
		
			if (component_type_id == null)
			{
				reportError(agpRow.getOrigin(),	MESSAGE_KEY_COMPONENT_TYPE_ID_ERROR, component_id);
			}
			else
			{
				component_type_id = component_type_id.toUpperCase();
				if (!ArrayUtils.contains(componentTypeArray,component_type_id))
				{
					 reportError(agpRow.getOrigin(), MESSAGE_KEY_COMPONENT_TYPE_ID_ERROR, agpRow.getComponent_id());
				}
			}
			
    	if(component_type_id!=null&&agpRow.isGap())
    	{
		
			if (gap_type == null)
			{
				reportError(agpRow.getOrigin(),MESSAGE_KEY_GAP_TYPE_ERROR,entry.getSubmitterAccession());
			}
			else
			{
				gap_type = gap_type.toLowerCase();
				
				if (!ArrayUtils.contains(gapTypeArray,gap_type))
				{
					reportError(agpRow.getOrigin(),MESSAGE_KEY_GAP_TYPE_ERROR,agpRow.getGap_type(),entry.getSubmitterAccession());
				}
			}
			
			if(agpRow.hasLinkage())
			{
			if (linkageEvidences == null||linkageEvidences.isEmpty())
			{
				reportError(agpRow.getOrigin(),MESSAGE_KEY_LINKAGE_EVIDENCE_ERROR,entry.getSubmitterAccession());
			}
			else
			{
				for(String linkageEvidence:linkageEvidences)
				{
				linkageEvidence = linkageEvidence.toLowerCase();
				if (!ArrayUtils.contains(linkageEvidenceArray, linkageEvidence))
				{
					reportError(agpRow.getOrigin(),MESSAGE_KEY_LINKAGE_EVIDENCE_ERROR,entry.getSubmitterAccession());
				}
				}
			}
			}
			
			if (gap_length != object_end - object_begin + 1)
			{
				reportError(agpRow.getOrigin(),MESSAGE_KEY_GAP_LENGTH_ERROR,agpRow.getGap_length(),object_end - object_begin + 1,object_begin,object_end,entry.getSubmitterAccession());
			}
			
    	}
    	else{
			
				if (orientation == null)
				{
					reportError(agpRow.getOrigin(),MESSAGE_KEY_ORIENTATION_ERROR,agpRow.getComponent_id());
				}
					else
					{
						orientation = orientation.toLowerCase();

						if (!ArrayUtils.contains(orientationArray, orientation))
						{
							reportError(agpRow.getOrigin(),	MESSAGE_KEY_ORIENTATION_ERROR, agpRow.getComponent_id());
						}
					}

				if(component_begin!=null&&component_end!=null)
				{
					if (component_begin > component_end ||component_end - component_begin != object_end - object_begin)
					{
						reportError(agpRow.getOrigin(),MESSAGE_KEY_COMPONENT_COORDINATE_ERROR,entry.getSubmitterAccession(),agpRow.getComponent_id());
					}
				}

				if(agpRow.getComponent_id().equalsIgnoreCase(agpRow.getObject())) {
					reportError(agpRow.getOrigin(), MESSAGE_KEY_SAME_COMPONENT_AND_OBJECT_ERROR, agpRow.getComponent_id());
				} else {
					
					long sequenceLength =0l;
						if (getEmblEntryValidationPlanProperty().isRemote.get()) 
						{
							if(getEmblEntryValidationPlanProperty().assemblySequenceInfo.get().size() == 0) 
							{
								throw new ValidationEngineException("AssemblySequenceInfo must be given to validate AGP file", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
							}
						}
						if (getEmblEntryValidationPlanProperty().assemblySequenceInfo.get().size() > 0) 
						{
									if (getEmblEntryValidationPlanProperty().assemblySequenceInfo.get().get(agpRow.getComponent_id().toUpperCase())==null)
										reportError(agpRow.getOrigin(), MESSAGE_KEY_COMPONENT_VALID_ERROR, agpRow.getComponent_id());
									else
									{
										sequenceLength =  getEmblEntryValidationPlanProperty().assemblySequenceInfo.get().get(agpRow.getComponent_id().toUpperCase()).getSequenceLength();
									}
						}

					// Check that component coordinates are valid.

					if (sequenceLength!=0 && (component_begin < 1 ||
							component_begin > sequenceLength ||
							component_end > sequenceLength ||
							component_end < component_begin)) 
						reportError(agpRow.getOrigin(), MESSAGE_KEY_COMPONENT_RANGE_ERROR, agpRow.getComponent_beg(), agpRow.getComponent_end(), agpRow.getComponent_id(), entry.getSubmitterAccession());

				}
    	}
    	validateLinkageCombination(agpRow);
		}
		
		return result;
	}

	private void validateLinkageCombination(AgpRow agpRow) {

		if (agpRow.isGap()) {
			if (agpRow.hasLinkage()) {
				if (!agpRow.getGap_type().toLowerCase().equals("contamination")
						&& !agpRow.getGap_type().toLowerCase().equals("scaffold")
						&& !agpRow.getGap_type().toLowerCase().equals("repeat")) {
					reportError(agpRow.getOrigin(), MESSAGE_KEY_INVALID_LINKAGE_ERROR, agpRow.getGap_type());
				}
			}
		}
	}
	
}
