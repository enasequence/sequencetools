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
package uk.ac.ebi.embl.api.graphics.glyph;

import java.util.Vector;

import uk.ac.ebi.embl.api.translation.Codon;
import uk.ac.ebi.embl.api.translation.CodonTranslator;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class TranslationGlyph extends HorizontalCompositeGlyph {

	public TranslationGlyph(Canvas canvas, Integer translationTable,
			Vector<Codon> codons, int firstColumnCount, int lastColumnCount) {
		super(canvas);
		this.translationTable = translationTable;
		this.codons = codons;
		this.firstColumnCount = firstColumnCount;
		this.lastColumnCount = lastColumnCount;
	}

	private Integer translationTable;
    private Vector<Codon> codons;
    private int firstColumnCount;
    private int lastColumnCount;

	@Override
	protected void initGlyph() {
		CodonTranslator translator = new CodonTranslator();
		try {
			translator.setTranslationTable(translationTable);
		} catch (ValidationException ex) {
			return;
		}
        for (int i = 0 ; i < codons.size() ; ++i) {
        	Codon codon = codons.get(i);
            AcidGlyph acidGlyph = new AcidGlyph(getCanvas());            
            acidGlyph.setAminoAcid(codon.getAminoAcid());
            if (i == 0) {
            	acidGlyph.setColumnCount(firstColumnCount);            
            }
            else if (i == codons.size() - 1) {
            	acidGlyph.setColumnCount(lastColumnCount);
            }
            else {
            	acidGlyph.setColumnCount(3);
            }            
            try {
            	if (codon.getCodon() != null && 
            		codon.getAminoAcid() != null) {
            		acidGlyph.setAlternativeStartCodon(
            			translator.isAlternativeStartCodon(codon));
            	}
    		} catch (ValidationException ex) {}
            addGlyph(acidGlyph);
        }
    }
}
