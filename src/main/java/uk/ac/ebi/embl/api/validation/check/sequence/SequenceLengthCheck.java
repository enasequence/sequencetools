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
package uk.ac.ebi.embl.api.validation.check.sequence;

import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.ValidationScope.Group;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("sequence length must not be shorter than 100 bps" +
		"Sequence length must not be shorter than 10 and must not be greater than 1000 for GSS dataclass entries" +
		"Sequence length must not be shorter than 200 for TSA dataclass entries")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class SequenceLengthCheck extends EntryValidationCheck
{
	private final static int MIN_SEQUENCE_LENGTH = 100;
	private final static int MIN_SEQUENCE_LENGTH_ASSEMBLY = 20;
	private final static int MIN_SEQUENCE_LENGTH_GSS = 10;
	private final static int MAX_SEQUENCE_LENGTH_GSS = 1000;
	private final static int MIN_SEQUENCE_LENGTH_TSA = 200;
	private final static String SEQUENCE_LENGTH_MESSAGE_ID = "SequenceLengthCheck";
	private final static String CURATOR_MESSAGE_ID = "SequenceLengthCheck2";
	private final static String SEQUENCE_LENGTH_GSS_MESSAGE_ID = "SequenceLengthCheck3";
	private final static String SEQUENCE_LENGTH_TSA_MESSAGE_ID = "SequenceLengthCheck4";
	private final static String SEQUENCE_LENGTH_INCRNA_MESSAGE_ID = "SequenceLengthCheck5";
	private final static String SEQUENCE_LENGTH_MISMATCH_MESSAGE_ID = "SequenceLengthCheck6";

	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		SequenceExistsCheck sequenceExistsCheck = new SequenceExistsCheck();

		sequenceExistsCheck.setEmblEntryValidationPlanProperty(getEmblEntryValidationPlanProperty());

		if (entry == null)
		{
			return result;
		}
		String dataclass = entry.getDataClass();

		if(dataclass != null && (entry.getDataClass().equals(Entry.PAT_DATACLASS) || 
				entry.getDataClass().equals(Entry.PRT_DATACLASS)))
		{
			return result;
		}
		if (sequenceExistsCheck.check(entry).isValid())
		{
			long length = entry.getSequence().getLength();

			if(entry.getIdLineSequenceLength()>0 && entry.getIdLineSequenceLength() != length) {
				reportError(entry.getOrigin(), SEQUENCE_LENGTH_MISMATCH_MESSAGE_ID, entry.getIdLineSequenceLength(), length);
			}

			if(length<200&&SequenceEntryUtils.isQualifierWithValueAvailable(Qualifier.NCRNA_CLASS_QUALIFIER_NAME, "lncRNA", entry))
				
				reportWarning(entry.getOrigin(), SEQUENCE_LENGTH_INCRNA_MESSAGE_ID);
			
			if (dataclass != null && dataclass.equals(Entry.GSS_DATACLASS))
			{
				if (length < MIN_SEQUENCE_LENGTH_GSS || length > MAX_SEQUENCE_LENGTH_GSS)
				{
					reportError(entry.getOrigin(), SEQUENCE_LENGTH_GSS_MESSAGE_ID, MIN_SEQUENCE_LENGTH_GSS, MAX_SEQUENCE_LENGTH_GSS, entry.getSubmitterAccession());
				}

			} else if (dataclass != null && dataclass.equals(Entry.TSA_DATACLASS))
			{
				if (length < MIN_SEQUENCE_LENGTH_TSA)
				{
					entry.setDelete(true);
					reportError(entry.getOrigin(), SEQUENCE_LENGTH_TSA_MESSAGE_ID, MIN_SEQUENCE_LENGTH_TSA, entry.getSubmitterAccession());
				}

			} else
			{
				int minSeqLength=MIN_SEQUENCE_LENGTH;
				
				if(getEmblEntryValidationPlanProperty().validationScope.get().isInGroup(Group.ASSEMBLY))
				{
					minSeqLength=MIN_SEQUENCE_LENGTH_ASSEMBLY;
				}
				if (entry.getSequence().getLength() < minSeqLength)
				{
					if (SequenceEntryUtils.isFeatureAvailable(Feature.ncRNA_FEATURE_NAME, entry) && entry.getSequence().getLength() < minSeqLength)

					{
						return result;
					}
					List<Qualifier> satelliteQualifierList = SequenceEntryUtils.getQualifiers(Qualifier.SATELLITE_QUALIFIER_NAME, entry);
					if (satelliteQualifierList != null)
					{
						for (Qualifier sateliteQualifier : satelliteQualifierList)
						{
							if (sateliteQualifier.getValue().contains("microsatellite"))
							{
								return result;
							}
						}
					}
					reportError(result, entry, SEQUENCE_LENGTH_MESSAGE_ID, minSeqLength,entry.getSubmitterAccession()==null?entry.getPrimaryAccession():entry.getSubmitterAccession());
				}
			}
		}

		return result;
	}

	/**
	 * Adds error to the result.
	 * 
	 * @param result
	 *            a reference to validation result
	 */
	private ValidationMessage<Origin> reportError(ValidationResult result, Entry entry, String messageId,Object...params)
	{
		ValidationMessage<Origin> message = EntryValidations.createMessage(entry.getOrigin(), Severity.ERROR, messageId,params);
		String curatorComment = ValidationMessageManager.getString(CURATOR_MESSAGE_ID);
		message.appendCuratorMessage(curatorComment);
		result.append(message);
		return message;
	}

}
