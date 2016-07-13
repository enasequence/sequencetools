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

import java.awt.Graphics2D;

public class VerticalCompositeGlyph extends CompositeGlyph {
    public VerticalCompositeGlyph(Canvas canvas) {
    	super(canvas);
    }

    @Override
	protected void drawTranslatedGlyph(Graphics2D g) {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
    	if (glyphs.size() > 0 && getLabel() != null) {
    		drawLabel(g, getLabel());
    	}    	   	
        int y = 0;
        for(Glyph glyph : glyphs) {
        	g.translate(0, y);
            glyph.drawGlyph(g);
            g.translate(0, -y);
            y += glyph.getHeight();
        }
    }

    @Override
	public int getHeight() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        int height = 0;
        for(Glyph glyph : glyphs) {
            height += glyph.getHeight();
        }    
        if (glyphs.size() > 0 && getLabel() != null) {
        	height = Math.max(height,  getLabelHeight());
        }
        return height;
    }    
}
