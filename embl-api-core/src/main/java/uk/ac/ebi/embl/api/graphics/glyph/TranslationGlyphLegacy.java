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

public class TranslationGlyphLegacy extends HorizontalCompositeGlyph {
    public TranslationGlyphLegacy(Canvas canvas) {
        super(canvas);
    }

    private Integer translationTable;
    private Vector<Codon> codons;
    private int visibleStartCodon = 1;

	@Override
	protected void initGlyph() {
		if (translationTable == null) return;
		if (codons == null) return;
		CodonTranslator translator = new CodonTranslator();
		try {
			translator.setTranslationTable(translationTable);
		} catch (ValidationException ex) {
			return;
		}
    	int columnCount = 0;
    	// TODO: this does not work - the maximum column count is a function
    	// of the start position as well - still the  number of codons need
    	// to be set correctly elsewere ->
    	int maxColumnCount = getCanvas().getColumnCount();
    	// <- TODO: this does not work - the maximum column count is a function
    	// of the start position as well
        for (int i = 0 ; i < codons.size() ; ++i) {
        	Codon codon = codons.get(i);
            AcidGlyph acidGlyph = new AcidGlyph(getCanvas());            
            acidGlyph.setAminoAcid(codon.getAminoAcid());
            try {
            	if (codon != null && codon.getCodon() != null && 
            		codon.getAminoAcid() != null) {
            		acidGlyph.setAlternativeStartCodon(
            			translator.isAlternativeStartCodon(codon));
            	}
    		} catch (ValidationException ex) {
    		}            
        	if (i == 0) {
            	if (visibleStartCodon == 1) {
                	columnCount = 3;
            	}
            	else if (visibleStartCodon == 2) {
                	columnCount = 1;
            	}
            	else {
                	columnCount = 2;            		
            	}
            	acidGlyph.setColumnCount(columnCount);
            }
            else {
            	columnCount += 3;
            	if (columnCount == maxColumnCount + 1) {
            		acidGlyph.setColumnCount(2);        		
            	}
            	else if (columnCount == maxColumnCount + 2) {
            		acidGlyph.setColumnCount(1);
            	}
            	else if (columnCount > maxColumnCount + 2) {
            		break;
            	}
            }
            addGlyph(acidGlyph);
        }
    }

    public int getTranslationTable() {
		return translationTable;
	}

	public void setTranslationTable(int translationTable) {
		this.translationTable = translationTable;
	}

	public Vector<Codon> getCodons() {
		return codons;
	}

	public void setCodons(Vector<Codon> codons) {
		this.codons = codons;
	}
	
	public int getVisibleStartCodon() {
		return visibleStartCodon;
	}

	public void setVisibleStartCodon(int visibleStartCodon) {
		this.visibleStartCodon = visibleStartCodon;
	}	
}
