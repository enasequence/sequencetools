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

public class GridGlyph extends CompositeGlyph {
    public GridGlyph(Canvas canvas) {
        super(canvas);
        setColor(new Color(0xE5, 0xE5, 0xE5));
        setBgColor(BackgroundGlyph.BACKGROUND_COLOR);
    }

    private int height;
    
    @Override
	protected void initGlyph() {
    	int x = 0;
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getBgColor());
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setFill(true);
        rectGlyph.setHeight(getHeight());
        rectGlyph.setWidth(getWidth());
        addGlyph(rectGlyph);
        for ( ; x <= getWidth() ; x += getCanvas().getColumnWidth()) {
        	LineGlyph lineGlyph = new LineGlyph(getCanvas());
            lineGlyph.setColor(getColor());
            lineGlyph.setBeginPoint(new GlyphPoint(x, 0));
            lineGlyph.setEndPoint(new GlyphPoint(x, getHeight()));
            addGlyph(lineGlyph);
        }
    }

	@Override
	public int getHeight() {
		return height;
	}
    
	public void setHeight(int height) {
		this.height = height;
	}
		
	@Override
	public int getWidth() {
        return getCanvas().getColumnCount() * 
        	   getCanvas().getColumnWidth();
    }	
}
