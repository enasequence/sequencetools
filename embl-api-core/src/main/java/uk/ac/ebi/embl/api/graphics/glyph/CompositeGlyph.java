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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;

public class CompositeGlyph extends AbstractCompositeGlyph {
    public CompositeGlyph(Canvas canvas) {
    	super(canvas);
    }

    private Color color;
    private Color bgColor;
    private Color textColor;
    
    protected Vector<Glyph> glyphs = new Vector<Glyph>();
    
    @Override
	protected void drawTranslatedGlyph(Graphics2D g) {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}

    	if (getLabel() != null) {
    		drawLabel(g, getLabel());
    	}       

    	for(Glyph glyph : glyphs) {
            glyph.drawGlyph(g);
        }
    }

    @Override
	public int getHeight() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        if (glyphs.size() == 0) {
            return 0;
        }
        int height = 0;
        for (Glyph glyph : glyphs) {
            if (glyph.getHeight() > height) {
                height = glyph.getHeight();
            }
        }
        if (getLabel() != null) {
        	height = Math.max(height,  getLabelHeight());
        }
        return height;
    }

    @Override
	public int getWidth() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        if (glyphs.size() == 0) {
            return 0;
        }
        int width = 0;
        for (Glyph glyph : glyphs) {
            if (glyph.getWidth() > width) {
                width = glyph.getWidth();                
            }
        }
        return width;
    }
    
    @Override    
    public void addGlyph(Glyph glyph) {
        glyphs.add(glyph);
    }

    public void addGlyphFront(Glyph glyph) {
        glyphs.add(0, glyph);
    }
                
    public void setColor(Color color) {
        this.color = color;
    }
   
    public Color getColor() {
        return color;
    }
      
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getBgColor() {
        return bgColor;
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }    
}
