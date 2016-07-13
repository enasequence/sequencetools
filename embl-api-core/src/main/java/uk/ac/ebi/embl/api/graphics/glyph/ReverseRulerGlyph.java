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

public class ReverseRulerGlyph extends RulerGlyph {
    public ReverseRulerGlyph(Canvas canvas) {
        super(canvas);
    }

    @Override
	protected void initGlyph() {
        int x = 0;
        int y = arrowHeight / 2;
        
        if (getCanvas().getVisibleColumnCount() <= moreSimplifiedColumnCount) {
	        LineGlyph lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        lineGlyph.setEndPoint(new GlyphPoint(getWidth(), y));
	        addGlyph(lineGlyph);
        }        
        else if (getCanvas().getVisibleColumnCount() <= simplifiedColumnCount) {
	        LineGlyph lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        x = getWidth();
	        lineGlyph.setEndPoint(new GlyphPoint(x, y));
	        x -= indent;
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        addGlyph(lineGlyph);
	        x -= HorizontalSpacerGlyph.DEFAULT_WIDTH;
	                
	        TextGlyph textGlyph = new TextGlyph(getCanvas());
	        textGlyph.setColor(getColor());
	        textGlyph.setFont(Glyph.DEFAULT_FONT);
	        textGlyph.setText("Reverse strand");
	        x -= textGlyph.getWidth();
	        textGlyph.setPoint(new GlyphPoint(x, -y));
	        addGlyph(textGlyph);
	        
	        lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        lineGlyph.setEndPoint(new GlyphPoint(0, y));
	        addGlyph(lineGlyph);	       
        }        
        else {        
	        // text
	        TextGlyph textGlyph = new TextGlyph(getCanvas());
	        textGlyph.setColor(getColor());
	        textGlyph.setFont(Glyph.DEFAULT_FONT);
	        textGlyph.setText(getText());
	        int textWidth = textGlyph.getWidth();
	        int textX = (getWidth() / 2) - (textWidth / 2);
	        textGlyph.setPoint(new GlyphPoint(textX, -y));
	        addGlyph(textGlyph);
	                        
	        LineGlyph lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        x = textX - HorizontalSpacerGlyph.DEFAULT_WIDTH;
	        lineGlyph.setEndPoint(new GlyphPoint(x, y));
	        addGlyph(lineGlyph);
	                
	        // strand text
	        lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        x = getWidth();
	        lineGlyph.setEndPoint(new GlyphPoint(x, y));
	        x -= indent;
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        addGlyph(lineGlyph);
	        x -= HorizontalSpacerGlyph.DEFAULT_WIDTH;
	                
	        textGlyph = new TextGlyph(getCanvas());
	        textGlyph.setColor(getColor());
	        textGlyph.setFont(Glyph.DEFAULT_FONT);
	        textGlyph.setText("Reverse strand");
	        x -= textGlyph.getWidth();
	        textGlyph.setPoint(new GlyphPoint(x, -y));
	        addGlyph(textGlyph);
	
	        x -= HorizontalSpacerGlyph.DEFAULT_WIDTH;
	        lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setEndPoint(new GlyphPoint(x, y));
	        x = textX + textWidth + HorizontalSpacerGlyph.DEFAULT_WIDTH;
	        lineGlyph.setBeginPoint(new GlyphPoint(x, y));
	        addGlyph(lineGlyph);
        }

        // arrow
        PolyGlyph polyGlyph = new PolyGlyph(getCanvas());
        polyGlyph.setColor(getColor());
        polyGlyph.setFill(true);
        polyGlyph.addPoint(new GlyphPoint(0, y));
        polyGlyph.addPoint(new GlyphPoint(arrowWidth, y + arrowHeight / 2));
        polyGlyph.addPoint(new GlyphPoint(arrowWidth, y - arrowHeight / 2));
        addGlyph(polyGlyph);            
        
        // bar
        LineGlyph lineGlyph = new LineGlyph(getCanvas());
        lineGlyph.setColor(getColor());
        lineGlyph.setBeginPoint(new GlyphPoint(getWidth(), y + arrowHeight / 2));
        lineGlyph.setEndPoint(new GlyphPoint(getWidth(), y - arrowHeight / 2));
        addGlyph(lineGlyph);        
    }
}
