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

public class SequenceGlyph extends HorizontalCompositeGlyph {
    public SequenceGlyph(Canvas canvas) {
        super(canvas);
    }

    private String sequence;

    @Override
	protected void initGlyph() {
        for (int i = 0 ; i < sequence.length() ; ++i) {
            BaseGlyph baseGlyph = new BaseGlyph(getCanvas());
            baseGlyph.setText(sequence.substring(i, i+1));
            addGlyph(baseGlyph);
        }
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }
}
