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

public class BasePairViewContigGlyph extends VerticalCompositeGlyph {
    public BasePairViewContigGlyph(Canvas canvas) {
        super(canvas);
    }

    private String text;
    private int columnCount;
    private boolean complement;
    
    @Override
	protected void initGlyph() {
    	BasePairViewContigForwardArrowGlyph forwardArrowGlyph = new BasePairViewContigForwardArrowGlyph(getCanvas());
    	forwardArrowGlyph.setColumnCount(columnCount);
        addGlyph(forwardArrowGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        BasePairViewContigSequenceGlyph contigSequenceGlyph = new BasePairViewContigSequenceGlyph(getCanvas());
        contigSequenceGlyph.setText(text);
        contigSequenceGlyph.setColumnCount(columnCount);
        contigSequenceGlyph.setComplement(complement);
        addGlyph(contigSequenceGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        BasePairViewContigReverseArrowGlyph reverseArrowGlyph = new BasePairViewContigReverseArrowGlyph(getCanvas());
        reverseArrowGlyph.setColumnCount(columnCount);
        addGlyph(reverseArrowGlyph);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }    

	public void setComplement(boolean complement) {
		this.complement = complement;
	}    
}
