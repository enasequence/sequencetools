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

import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.Origin;

/** Translates a bases to an amino acid sequences. The bases are encoded using lower 
 * case single letter JCBN abbreviations and the amino acids are encoded using upper
 * case single letter JCBN abbreviations.
 */
public abstract class AbstractTranslator {

    public void setTranslationTable(Integer translationTable) throws ValidationException {
        codonTranslator.setTranslationTable(translationTable);
    }

    protected CodonTranslator codonTranslator = new CodonTranslator();
   
    abstract public ExtendedResult<TranslationResult> translate(byte[] sequence, Origin origin);

    abstract protected void translateCodons(byte[] sequence,
    		TranslationResult translationResult) throws ValidationException;

    public ExtendedResult<TranslationResult> translate(byte[] sequence) {
        return translate(sequence, null);
    }

    public TranslationTable getTranslationTable(){
        return codonTranslator.getTranslationTable();
    }
}
