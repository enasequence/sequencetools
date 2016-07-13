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

public class HorizontalCompositeGlyph extends CompositeGlyph {
    public HorizontalCompositeGlyph(Canvas canvas) {
    	super(canvas);
    }
        
    @Override
	protected void drawTranslatedGlyph(Graphics2D g) {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
    	if (getLabel() != null) {
    		drawLabel(g, getLabel());
    	}
        int x = 0;
        for(Glyph glyph : glyphs) {
        	g.translate(x, 0);
            glyph.drawGlyph(g);
            g.translate(-x, 0);
            x += glyph.getWidth();
        }
    }

    @Override
	public int getWidth() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}    	
        int width = 0;
        for(Glyph glyph : glyphs) {
            width += glyph.getWidth();
        }
        return width;
    }
}
