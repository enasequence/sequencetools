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

public class BasePairViewContigReverseArrowGlyph extends BasePairViewContigArrowGlyph {
    public BasePairViewContigReverseArrowGlyph(Canvas canvas) {
        super(canvas);
    }
     
    @Override
	protected void initGlyph() {
    	int x = 0;
        // line        
        LineGlyph lineGlyph = new LineGlyph(getCanvas());
        lineGlyph.setColor(getColor());
        lineGlyph.setBeginPoint(new GlyphPoint(x, 0));
        lineGlyph.setEndPoint(new GlyphPoint(x + getWidth(), 0));
        addGlyph(lineGlyph);        
        // arrow
        PolyGlyph polyGlyph = new PolyGlyph(getCanvas());
        polyGlyph.setColor(getColor());
        polyGlyph.setFill(true);
        polyGlyph.addPoint(new GlyphPoint(x, 0));
        polyGlyph.addPoint(new GlyphPoint(x + arrowWidth, arrowHeight / 2));
        polyGlyph.addPoint(new GlyphPoint(x + arrowWidth, 0));        
        addGlyph(polyGlyph);         
        // bar
        lineGlyph = new LineGlyph(getCanvas());
        lineGlyph.setColor(getColor());
        lineGlyph.setBeginPoint(new GlyphPoint(x + getWidth(), 0));
        lineGlyph.setEndPoint(new GlyphPoint(x + getWidth(), arrowHeight / 2));
        addGlyph(lineGlyph);                    
    }    
}
