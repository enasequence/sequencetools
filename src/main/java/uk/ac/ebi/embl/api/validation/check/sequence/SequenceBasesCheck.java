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

import java.util.Iterator;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Invalid base: {0}.Sequence starts and/or ends with 'n' characters")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_CONTIG,ValidationScope.ASSEMBLY_SCAFFOLD,ValidationScope.ASSEMBLY_CHROMOSOME,ValidationScope.ASSEMBLY_TRANSCRIPTOME,ValidationScope.NCBI})
public class SequenceBasesCheck extends SequenceValidationCheck {

	private final static String MESSAGE_ID = "SequenceBasesCheck";
	private final static String TERMINAL_N_ID = "SequenceBasesCheck-2";

	private boolean ignoreTerminalNError = false;
	public ValidationResult check(Sequence sequence) {
		ValidationResult result = new ValidationResult();
		if (sequence == null || sequence.getSequenceByte() == null||sequence.getContigs().size()>0) {
			return result;
		}

        if(sequence.getMoleculeType() != null && sequence.getMoleculeType().equals(Entry.PROTEIN)){
            return result;//not validating amino acids here
        }

       
        byte[] sequenceByte= sequence.getSequenceByte();
        if(sequence.getTopology() != null && !sequence.getTopology().equals(Sequence.Topology.CIRCULAR))
        {

        if( !ignoreTerminalNError &&
				sequenceByte != null && sequenceByte.length != 0
				&& ('n'==sequenceByte[0] || 'n' == sequenceByte[sequenceByte.length-1]) )
        {
           	 reportError(result, TERMINAL_N_ID);
        }
        }
        
       
		/*String sequenceStr = sequence.getSequence();
		if (sequenceStr == null) {
			return result;
		}		
		for (int i = 0 ; i < sequenceStr.length() ; ++i) {
			char base = sequenceStr.charAt(i);
			if (base == 'a' ||
				base == 'c' ||
				base == 'g' ||
				base == 't' ||
				base == 'u' ||
				base == 'b' ||
				base == 'd' ||
				base == 'h' ||
				base == 'k' ||
				base == 'm' ||
				base == 'n' ||
				base == 'r' ||
				base == 's' ||
				base == 'v' ||
				base == 'w' ||
				base == 'y' ) {
				continue;
			}
			reportError(result, MESSAGE_ID, base);
		}*/
        
      for(byte base:sequenceByte)
        {
        	switch((char)base)
        	{
        	case 'a':
			case 'c':
			case 'g':
			case 't':
			case 'u':
			case 'b':
			case 'd':
			case 'h':
			case 'k':
			case 'm':
			case 'n':
			case 'r':
			case 's':
			case 'v':
			case 'w':
			case 'y':
			 break;
			default:
			reportError(result, MESSAGE_ID, (char)base);	

        	}
        }
		return result;
	}

	public void setIgnoreTerminalNError(boolean ignoreTerminalNError) {
		this.ignoreTerminalNError = ignoreTerminalNError;
	}
}
