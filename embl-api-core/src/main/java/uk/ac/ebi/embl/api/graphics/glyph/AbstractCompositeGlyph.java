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

import java.awt.Font;
import java.awt.Graphics2D;

public abstract class AbstractCompositeGlyph extends Glyph {
    public AbstractCompositeGlyph(Canvas canvas) {
    	super(canvas);
    }

    protected boolean isInit = false;        
    private String label;
    private boolean boldLabel = false;

	protected void initGlyph() {
	}
    
    public abstract void addGlyph(Glyph glyph);
	   
    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
        
	public boolean isBoldLabel() {
		return boldLabel;
	}

	public void setBoldLabel(boolean boldLabel) {
		this.boldLabel = boldLabel;
	}

	public void drawLabel(Graphics2D g, String label) {
		GlyphTranslate glyphTranslate = getGlyphTranslate();
		if (glyphTranslate != null) {
			g.translate(-glyphTranslate.getX(), -glyphTranslate.getY());	
		}
		g.translate(-getCanvas().LEFT_MARGIN, 0);
  		TextGlyph textGlyph = new TextGlyph(getCanvas());
	   	textGlyph.setText(" " + label);
	   	if (boldLabel) {
	   	    textGlyph.setFont(new Font("SansSerif", Font.BOLD, 12));
	   	}
	   	textGlyph.drawGlyph(g);
		g.translate(getCanvas().LEFT_MARGIN, 0);
		if (glyphTranslate != null) {
			g.translate(glyphTranslate.getX(), glyphTranslate.getY());	
		}
	}
	
	public int getLabelHeight() {
		if (label == null) {
			return 0;
		}
  		TextGlyph textGlyph = new TextGlyph(getCanvas());
	   	textGlyph.setText(" " + label);
	   	return textGlyph.getHeight();
	}	
}
