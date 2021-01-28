/*******************************************************************************
 * Copyright 2012-2013 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.location.LocationToStringCoverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@Description("Sequence has been added to the anticodon value : \"{0}\""
		+ "Illegal amino acid \"{0}\" changed to legal amino acid \"{1}\"")
public class AnticodonQualifierFix extends EntryValidationCheck
{
	private final static String SEQUENCE_FIX_ID = "AnticodonQualifierFix_1";
	private final static String AMINO_ACID_FIX_ID = "AnticodonQualifierFix_2";

	

	public AnticodonQualifierFix()
	{
	}

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if (entry.getSequence() == null)
		{
			return result;
		}
		ArrayList<Qualifier> anticodonQualifiers = (ArrayList<Qualifier>) SequenceEntryUtils.getQualifiers(Qualifier.ANTICODON_QUALIFIER_NAME, entry);
		if (anticodonQualifiers.isEmpty())
			return result;
		AnticodonQualifier qualifier =null;
		for (Qualifier anticodonQualifier : anticodonQualifiers)
		{
			
			qualifier = new AnticodonQualifier(anticodonQualifier.getValue());
			
			try
			{
				CompoundLocation<Location> location = qualifier.getLocations();
				String pos=LocationToStringCoverter.renderCompoundLocation(location);
				String aminoAcid=qualifier.getAminoAcid()!=null?qualifier.getAminoAcid().getAbbreviation():null;//valid amino acid(e.g. aa: Sec) from the anticodon (e.g. aa :SeC) value
				String aminoAcidString=qualifier.getAminoAcidString();//from the original anticodon value(e.g. aa :SeC)
				SegmentFactory factory = new SegmentFactory();
				Segment segment = (entry.getSequence()!=null && entry.getSequence().getSequenceByte()!=null)?factory.createSegment(entry.getSequence(), location):null;
				if(segment==null)
					continue;
				String sequenceString = new String(segment.getSequenceByte());
				String anticodonValue = qualifier.getValue(pos,aminoAcid,sequenceString);
								
				if(aminoAcid!=null&&!aminoAcidString.equals(aminoAcid))
				{
					reportMessage(Severity.FIX, anticodonQualifier.getOrigin(), AMINO_ACID_FIX_ID, aminoAcidString, aminoAcid);
				}
				if(qualifier.getSequence()==null)
				{
					reportMessage(Severity.FIX, anticodonQualifier.getOrigin(), SEQUENCE_FIX_ID, anticodonValue);
				}
				anticodonQualifier.setValue(anticodonValue);
			
			} catch (ValidationException e)
			{
              reportException(result, e,qualifier);
			}
			catch(SQLException | IOException e)
			{
				throw new ValidationEngineException(e);
			}

		}
		return result;
	}

	private void reportException(ValidationResult result, ValidationException exception,Qualifier qualifier)
	{
		ValidationMessage<Origin> message = exception.getValidationMessage();
		message.append(qualifier.getOrigin());
		result.append(message);
	}
}
