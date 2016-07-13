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

public class AcidGlyph extends CompositeGlyph {
    public AcidGlyph(Canvas canvas) {
        super(canvas);
        setBgColor(Color.green);
        setTextColor(Color.black);
    }

    private Character aminoAcid;
    private boolean alternativeStartCodon;
    
    private final static HashMap<Character, Color> bgColorMap = 
    	new HashMap<Character, Color>();
    
    static {
    	bgColorMap.put('A', new Color(0xC1, 0xFF, 0xC1));
    	bgColorMap.put('B', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('C', new Color(0xF0, 0xE6, 0x8C));
    	bgColorMap.put('D', new Color(0xFF, 0xD7, 0x00));
    	bgColorMap.put('E', new Color(0xFF, 0xB9, 0x0F));
    	bgColorMap.put('F', new Color(0xAF, 0xEE, 0xEE));
    	bgColorMap.put('G', new Color(0x3C, 0xB3, 0x71));
    	bgColorMap.put('H', new Color(0x00, 0xCE, 0xD1));
    	bgColorMap.put('I', new Color(0xAD, 0xFF, 0x2F));
    	bgColorMap.put('J', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('K', new Color(0xF0, 0x80, 0x80));
    	bgColorMap.put('L', new Color(0xC0, 0xFF, 0x3E));
    	bgColorMap.put('M', new Color(0x00, 0xFF, 0x00));
    	bgColorMap.put('N', new Color(0xEE, 0xAE, 0xEE));
    	bgColorMap.put('O', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('P', new Color(0x00, 0xFF, 0x7F));
    	bgColorMap.put('Q', new Color(0xFF, 0xE1, 0xFF));
    	bgColorMap.put('R', new Color(0xBC, 0x8F, 0x8F));
    	bgColorMap.put('S', new Color(0xAB, 0x82, 0xFF));
    	bgColorMap.put('T', new Color(0xE0, 0x66, 0xFF));
    	bgColorMap.put('U', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('V', new Color(0x9B, 0xCD, 0x9B));
    	bgColorMap.put('W', new Color(0x87, 0xCE, 0xEB));
    	bgColorMap.put('X', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('Y', new Color(0x87, 0xCE, 0xFA));
    	bgColorMap.put('Z', new Color(0xF5, 0xF5, 0xF5));
    	bgColorMap.put('*', new Color(0xFF, 0x00, 0x00));     	
    }
           
    private int height = 20;
    private int columnCount = 3;

    @Override
	protected void initGlyph() {
        if (aminoAcid != null) {
        	Color bgColor = bgColorMap.get(aminoAcid);
        	if (bgColor != null) {
        		if (alternativeStartCodon) {
        			RectGlyph rectGlyph = new RectGlyph(getCanvas());
	            	rectGlyph.setColor(bgColor);
	            	rectGlyph.setFill(true);
	            	rectGlyph.setHeight(getHeight());
	            	rectGlyph.setWidth(getWidth());
	            	addGlyph(rectGlyph);        
	            	rectGlyph = new RectGlyph(getCanvas());
	            	rectGlyph.setColor(new Color(0x00, 0x00, 0x00));
	            	rectGlyph.setFill(false);
	            	rectGlyph.setHeight(getHeight());
	            	rectGlyph.setWidth(getWidth() - 1);
	            	addGlyph(rectGlyph);
        		}
        		else {
	            	RectGlyph rectGlyph = new RectGlyph(getCanvas());
	            	rectGlyph.setColor(bgColor);
	            	rectGlyph.setFill(true);
	            	rectGlyph.setHeight(getHeight());
	            	rectGlyph.setWidth(getWidth());
	            	addGlyph(rectGlyph);        
        		}
        	}
            TextGlyph textGlyph = new TextGlyph(getCanvas());
            textGlyph.setColor(getTextColor());
            textGlyph.setText(aminoAcid.toString());
            textGlyph.setFont(Glyph.DEFAULT_FONT);
            textGlyph.setPoint(new GlyphPoint(
                (getWidth() - textGlyph.getWidth()) / 2,
                (getHeight() - textGlyph.getHeight()) / 2));
            addGlyph(textGlyph);
        }
        else {
        	addGlyph(new HorizontalSpacerGlyph(getCanvas(), getWidth()));
        }
   }

    @Override
	public int getHeight() {
        return height;
    }

    @Override
	public int getWidth() {
        return getCanvas().getColumnWidth() * getColumnCount();
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

	public Character getAminoAcid() {
		return aminoAcid;
	}

	public void setAminoAcid(Character aminoAcid) {
		this.aminoAcid = aminoAcid;
	}
    
    public int getColumnCount() {
        return columnCount;
    }

	public void setAlternativeStartCodon(boolean alternativeStartCodon) {
		this.alternativeStartCodon = alternativeStartCodon;
	}
}
