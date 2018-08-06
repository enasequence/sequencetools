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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

@Description("Base 'n's are deleted at the end and start of the sequence & shift the locations of features accordingly")
@ExcludeScope(validationScope = {ValidationScope.NCBI,ValidationScope.ASSEMBLY_CONTIG,ValidationScope.ASSEMBLY_CHROMOSOME,ValidationScope.ASSEMBLY_SCAFFOLD})
public class SequenceBasesFix extends EntryValidationCheck {

	private final static String SEQUENCE_BASES_FIX_ID_1 = "SequenceBasesFix_1";
	private final static String SEQUENCE_BASES_FIX_ID_2 = "SequenceBasesFix_2";
	String beginDelSequenceStr;
	String endDelSequenceStr;

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
        beginDelSequenceStr = "";
        endDelSequenceStr = "";

		if (entry == null) {
			return result;
		}

        Sequence sequence = entry.getSequence();

		if (sequence == null||Entry.CON_DATACLASS.equals(entry.getDataClass())) {
			return result;
		}

		if (sequence.getSequenceByte() == null) {
			return result;
		}

        /**
         * not interested in proteins as they are allowed to have Ns
         */
        if(sequence.getMoleculeType() != null && sequence.getMoleculeType().equals(Entry.PROTEIN)){
            return result;
        }

		if (sequence.getTopology() != null
				&& !sequence.getTopology().equals(Sequence.Topology.CIRCULAR)) {

			// Delete the n's at the beginning and end of the Sequence

			ByteBuffer strippedSequence = removeChar(sequence);

            /**
             * bail out if there are no terminal Ns to fix
             */
            if(strippedSequence.array().length == sequence.getLength()){
                return result;
            }
            else{
            	
            	 reportMessage(Severity.FIX, sequence.getOrigin(), SEQUENCE_BASES_FIX_ID_2);
                }

            /**
             * otherwise set the new stripped sequence
             */
			entry.getSequence().setSequence(strippedSequence);
			//entry.getSequence().setLength(entry.getSequence().getSequenceByte().length);
			
			if (entry.getSequence().getLength()>0) {

				/*
				 * call the shiftLocation method of Utility class to shift the
				 * feature locations according to new sequence locations
				 */
				ArrayList<ValidationMessage> validationMessages = Utils
						.shiftLocation(entry, beginDelSequenceStr.length(),false);
                
				for (ValidationMessage validationMessage : validationMessages) {
					result.append(validationMessage);
				}
			} else {
				reportError(entry.getOrigin(), SEQUENCE_BASES_FIX_ID_1);
			}
		}

		return result;
	}

	/*
	 * Method to delete the base n's at the beginning and end of the sequence
	 */
	private ByteBuffer removeChar(Sequence sequenceObj)
	{

		byte[] sequenceByte = sequenceObj.getSequenceByte();
		int beginPosition = 0;
		int endPosition = (int) sequenceObj.getLength();
		if (sequenceByte[0] == 'n')
		{
			for (byte base : sequenceByte)
			{
				if ('n' == (char) base)
				{
					beginPosition++;
					beginDelSequenceStr+=(char)base;
				}
				else
				{
					break;
				}
			}
			
			if (beginPosition == endPosition)
			{
				return ByteBuffer.wrap(new byte[0]);
			}
		}
		if (sequenceByte[endPosition - 1] == 'n')
		{

			for (int i = endPosition - 1; i > 0; i--)
			{
				if ('n' == sequenceByte[i])
				{
					endPosition--;
				}
				else
				{
					break;
				}
			}
		}

		int length = (int) (endPosition - beginPosition);
		int offset = beginPosition;
		byte[] strippedSequence = new byte[length];
		System.arraycopy(sequenceObj.getSequenceByte(), offset, strippedSequence, 0, length);
		return ByteBuffer.wrap(strippedSequence, 0, strippedSequence.length);

	}

}
