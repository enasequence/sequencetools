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

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class SeparatorGlyph extends SimpleGlyph {

    public SeparatorGlyph(Canvas canvas) {
    	super(canvas);
    	
    }
    
    @Override
    protected void drawSimpleGlyph(Graphics2D g) {
    	LineGlyph lineGlyph = new LineGlyph(getCanvas());
    	lineGlyph.setBeginPoint(new GlyphPoint(-getCanvas().getLabelPanelWidth(), 0));
    	lineGlyph.setEndPoint(new GlyphPoint(-getCanvas().getLabelPanelWidth() + getWidth(), 0));
    	float[] dashPattern = { 3, 3, 3, 3 };
    	BasicStroke stroke = new BasicStroke(
    			1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, dashPattern, 0);
    	lineGlyph.setStroke(stroke);
    	lineGlyph.drawGlyph(g);
    }

    @Override
	public int getHeight() {
        return 1;
    }

    @Override
	public int getWidth() {
    	return getCanvas().getWidth();
    }
}
