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

public class BasePairViewContigSequenceGlyph extends CompositeGlyph {
    public BasePairViewContigSequenceGlyph(Canvas canvas) {
        super(canvas);
        setTextColor(Color.white);
    }

    private String text;
    private int columnCount;
    private boolean complement;

    private int height = 20;
    
    @Override
	protected void initGlyph() {
    	if (complement) {
    		 setColor(new Color(0xCC, 0x33, 0x33));
    	}
    	else {
    		setColor(new Color(0x36, 0x8E, 0xC9));
    	}
    	
    	int x = 0;
        // rect
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getColor());
        rectGlyph.setFill(true);
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getWidth());
        rectGlyph.setHeight(height);
        addGlyph(rectGlyph);
        // text
        if (text != null) {
            TextGlyph textGlyph = new TextGlyph(getCanvas());
            textGlyph.setColor(getTextColor());
            textGlyph.setText(text);
            textGlyph.setFont(Glyph.DEFAULT_FONT);
            textGlyph.setPoint(new GlyphPoint(
                x + (getWidth() / 2) - (textGlyph.getWidth() / 2), 
                (height - textGlyph.getHeight()) / 2));
            if (textGlyph.getWidth() < getWidth()) {
            	addGlyph(textGlyph);
            }
        }
    }

    @Override
	public int getHeight() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        return height;
    }

    @Override
	public int getWidth() {
        return columnCount * getCanvas().getColumnWidth();
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }    
    
	public void setComplement(boolean complement) {
		this.complement = complement;
	}    
}
