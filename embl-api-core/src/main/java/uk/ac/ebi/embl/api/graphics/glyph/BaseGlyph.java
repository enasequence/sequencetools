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

import java.util.HashMap;

public class BaseGlyph extends CompositeGlyph {
    public BaseGlyph(Canvas canvas) {
        super(canvas);
        setBgColor(Color.green);
        setTextColor(Color.black);
    }

    private String text;

    private final static HashMap<String, Color> bgColorMap =
        new HashMap<String, Color>();

    static {
    	bgColorMap.put("T", new Color(0xEE, 0xA2, 0xAD));
    	bgColorMap.put("A", new Color(0x90, 0xEE, 0x90));
    	bgColorMap.put("C", new Color(0xB0, 0xC4, 0xDE));
    	bgColorMap.put("G", new Color(0xFF, 0xEC, 0x8B));
    };
        
    private int height = 20;

    @Override
	protected void initGlyph() {
        Color bgColor = bgColorMap.get(text);
        if (bgColor != null) {
            RectGlyph rectGlyph = new RectGlyph(getCanvas());
            rectGlyph.setColor(bgColor);
            rectGlyph.setFill(true);
            rectGlyph.setHeight(getHeight());
            rectGlyph.setWidth(getWidth());        
            addGlyph(rectGlyph);   
        }
        if (text != null) {
            TextGlyph textGlyph = new TextGlyph(getCanvas());
            textGlyph.setColor(getTextColor());
            textGlyph.setText(text);
            textGlyph.setFont(Glyph.DEFAULT_FONT);
            textGlyph.setPoint(new GlyphPoint(
                (getWidth() - textGlyph.getWidth()) / 2, 
                (getHeight() - textGlyph.getHeight()) / 2));
            addGlyph(textGlyph);
        }
   }

    @Override
	public int getHeight() {
        return height;
    }

    @Override
	public int getWidth() {
    	return getCanvas().getColumnWidth();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
