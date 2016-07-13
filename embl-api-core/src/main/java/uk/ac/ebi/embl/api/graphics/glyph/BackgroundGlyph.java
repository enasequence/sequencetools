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

public class BackgroundGlyph extends CompositeGlyph {
    public BackgroundGlyph(Canvas canvas) {
    	super(canvas);
    	this.width = canvas.getWidth();
        setColor(BACKGROUND_COLOR);    
        setBgColor(BACKGROUND_COLOR);
    }
        
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    	//new Color(0xFF, 0xFF, 0xDD);
    
    private int width;
    private int height;
    
    @Override
	protected void initGlyph() {
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getBgColor());
        rectGlyph.setFill(true);
        rectGlyph.setHeight(height);
        rectGlyph.setWidth(width);
        addGlyph(rectGlyph);
        rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getColor());
        rectGlyph.setFill(false);
        rectGlyph.setHeight(height);
        rectGlyph.setWidth(width);
        addGlyph(rectGlyph);
        setGlyphTranslate(new GlyphTranslate(-getCanvas().LEFT_MARGIN, 0));
    }

    @Override
	public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }    
    
    @Override
	public int getHeight() {
        return height;
    }
               
    public void setHeight(int height) {
        this.height = height;
    }
}
