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
package uk.ac.ebi.embl.api.translation;

import java.util.Arrays;
import java.util.Vector;

import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.Origin;

public class SimpleTranslator extends AbstractTranslator {

    public ExtendedResult<TranslationResult> translate(byte[] sequence, Origin origin) {
    	TranslationResult translationResult = new TranslationResult();
    	ExtendedResult<TranslationResult> extendedResult = 
    		new ExtendedResult<TranslationResult>(translationResult);
    	try {
	        translateCodons(sequence, translationResult);
    	}
    	catch (ValidationException ex) {
    		extendedResult.append(ex.getValidationMessage());
    	}
    	return extendedResult;
    }

    protected void translateCodons(byte[] sequence,
    		TranslationResult translationResult) throws ValidationException {
        int bases = sequence.length;
        Vector<Codon> codons = new Vector<Codon>(bases / 3);
        for ( int i = 0 ; i + 3 <= bases ; i += 3 ) {
            Codon codon = new Codon();
            codon.setCodon(new String(Arrays.copyOfRange(sequence, i,i+3)));
            codon.setPos(i + 1);
            codonTranslator.translateOtherCodon(codon);
            codons.add(codon);
        }
        translationResult.setCodons(codons);
        translationResult.setConceptualTranslationCodons(codons.size());
    }
}
