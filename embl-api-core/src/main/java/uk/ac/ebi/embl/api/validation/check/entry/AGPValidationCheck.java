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

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class AGPValidationCheck extends EntryValidationCheck
{

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



	public ValidationResult check(Entry entry) throws ValidationEngineException
	{

		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		if(!FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}
		
		if (entry.getAgpRows().size() == 0)
		{
			
				reportError(entry.getOrigin(),
							MESSAGE_KEY_PART_COUNT_ERROR,
							entry.getSubmitterAccession());
				return result;
		}

		Integer prevPartNumber = 0;
		Long prevEnd = -1l;	
		
		for (AgpRow agpRow : entry.getAgpRows())
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
			String linkageEvidence=agpRow.getLinkageevidence();
			String component_acc=null;

			if(agpRow==null||!agpRow.isValid())
			{
				reportError(entry.getOrigin(),MESSAGE_KEY_INVALID_ROW_ERROR,entry.getPrimaryAccession()==null?entry.getSubmitterAccession():entry.getPrimaryAccession());
				continue;
			}
				
       // object_name missing check
			if (objectName == null || objectName.isEmpty())
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_OBJECT_NAME_ERROR);
			}
		//part number validation check
			if (part_number != (prevPartNumber + 1))
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_PART_NUMBER_ERROR,
							entry.getSubmitterAccession());
			}
			prevPartNumber = part_number;
			//object_begin, object_end validation checks
	

			if (object_begin <= 0)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_OBJECT_COORDINATE_ERROR,
							agpRow.getObject_beg(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (object_end <= 0)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_OBJECT_COORDINATE_ERROR,
							agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (object_end < object_begin)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_OBJECT_COORDINATE_ERROR,
							agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}

			if (prevEnd != -1 && object_begin != prevEnd + 1)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_OBJECT_COORDINATE_ERROR,
							agpRow.getObject_end(),entry.getSubmitterAccession(),agpRow.getPart_number());
			}
			prevEnd = object_end;
			//component_type check
		
			if (component_type_id == null)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_COMPONENT_TYPE_ID_ERROR,
							component_id);
			}
			else
			{
				component_type_id = component_type_id.toUpperCase();
				if (
					!component_type_id.equals("A") &&
					!component_type_id.equals("D") &&
					!component_type_id.equals("F") &&
					!component_type_id.equals("G") &&
					!component_type_id.equals("O") &&
					!component_type_id.equals("P") &&
					!component_type_id.equals("W") &&
					!component_type_id.equals("N") &&
					!component_type_id.equals("U"))
				{
					 reportError(agpRow.getOrigin(), MESSAGE_KEY_COMPONENT_TYPE_ID_ERROR, agpRow.getComponent_id());
				}
			}
			
    	if(component_type_id!=null&&agpRow.isGap())
    	{
		
			if (gap_type == null)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_GAP_TYPE_ERROR,
							entry.getSubmitterAccession());
			}
			else
			{
				gap_type = gap_type.toLowerCase();
				if (
					!gap_type.equals("scaffold") &&
					!gap_type.equals("contig") &&
					!gap_type.equals("centromere") &&
					!gap_type.equals("short_arm") &&
					!gap_type.equals("heterochromatin") &&
					!gap_type.equals("telomere") &&
					!gap_type.equals("repeat"))
				{
					reportError(agpRow.getOrigin(),
								MESSAGE_KEY_GAP_TYPE_ERROR,
								agpRow.getGap_type(),
								entry.getSubmitterAccession());
				}
			}
			
			if (linkageEvidence == null)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_LINKAGE_EVIDENCE_ERROR,
							entry.getSubmitterAccession());
			}
			else
			{
				linkageEvidence = linkageEvidence.toLowerCase();
				if (
					!linkageEvidence.equals("na") &&						
					!linkageEvidence.equals("paired-ends") &&
					!linkageEvidence.equals("align_genus") &&
					!linkageEvidence.equals("align_xgenus") &&
					!linkageEvidence.equals("align_trnscpt") &&
					!linkageEvidence.equals("within_clone") &&
					!linkageEvidence.equals("clone_contig") &&
					!linkageEvidence.equals("map") &&
					!linkageEvidence.equals("strobe") &&
					!linkageEvidence.equals("unspecified"))
				{
					reportError(agpRow.getOrigin(),
								MESSAGE_KEY_LINKAGE_EVIDENCE_ERROR,
								entry.getSubmitterAccession());
				}
			}
			
			if (gap_length != object_end - object_begin + 1)
			{
				reportError(agpRow.getOrigin(),
						     MESSAGE_KEY_GAP_LENGTH_ERROR,
						     agpRow.getGap_length(),entry.getSubmitterAccession(),
						     object_end - object_begin + 1);
			}
			
    	}
    	else{
			
			if (orientation == null)
			{
				reportError(agpRow.getOrigin(),
							MESSAGE_KEY_ORIENTATION_ERROR,
							agpRow.getComponent_id());
			}
				else
				{
					orientation = orientation.toLowerCase();

					if (!orientation.equals("+") &&
							!orientation.equals("plus") &&
							!orientation.equals("-") &&
							!orientation.equals("minus") &&
							!orientation.equals("?") &&
							!orientation.equals("0") &&
							!orientation.equals("unknown") &&
							!orientation.equals("na") &&
							!orientation.equals("irrelevant"))
					{
						reportError(agpRow.getOrigin(),
									MESSAGE_KEY_ORIENTATION_ERROR,
									agpRow.getComponent_id());
					}
				}

			if(component_begin!=null&&component_end!=null)
			{
				if (component_begin > component_end ||
						component_end - component_begin != object_end - object_begin)
				{
					reportError(agpRow.getOrigin(),
								MESSAGE_KEY_COMPONENT_COORDINATE_ERROR,
								entry.getSubmitterAccession(),agpRow.getComponent_id());
				}
			}

				if(getEntryDAOUtils()==null)//if database connection is not available, then the following check doesn't work.
				{
					continue;
				}
				
				int assemblyLevel = getEmblEntryValidationPlanProperty().validationScope.get().getAssemblyLevel();

				ContigSequenceInfo contigSequenceInfo = null;
				try
				{
					contigSequenceInfo = getEntryDAOUtils().getSequenceInfoBasedOnEntryName(agpRow.getComponent_id(),
							                                                                getEmblEntryValidationPlanProperty().analysis_id.get(),
																					 assemblyLevel);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					throw new ValidationEngineException(e);
				}

				if (null == contigSequenceInfo)
				{
					reportError(agpRow.getOrigin(),
								MESSAGE_KEY_COMPONENT_VALID_ERROR,
								agpRow.getComponent_id());
				}
				else
				{
					int sequenceLength = contigSequenceInfo.getSequenceLength();

					// Check that component coordinates are valid.

					if (component_begin < 1 ||
						component_begin > sequenceLength ||
						component_end > sequenceLength ||
						component_end < component_begin)
					{
						reportError(agpRow.getOrigin(),
									MESSAGE_KEY_COMPONENT_RANGE_ERROR,
								agpRow.getComponent_beg(),agpRow.getComponent_end(),agpRow.getComponent_id(),entry.getSubmitterAccession());
					}
				}
			}
		}

		return result;
	}

}
