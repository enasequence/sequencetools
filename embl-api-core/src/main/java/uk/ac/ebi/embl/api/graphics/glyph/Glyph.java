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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;

public abstract class Glyph {

	private Canvas canvas;
	private GlyphTranslate glyphTranslate;
	private AlphaComposite alphaComposite;	
    protected static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);	
	
	public Glyph(Canvas canvas) {
		this.canvas = canvas;
	}
	
    public Canvas getCanvas() {
		return canvas;
	}

    public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}  
     
	public GlyphTranslate getGlyphTranslate() {
		return glyphTranslate;
	}

	public void setGlyphTranslate(GlyphTranslate glyphTranslate) {
		this.glyphTranslate = glyphTranslate;
	}            
	
	public void setAlpha(float alpha) {
		this.alphaComposite = AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha);
	}

	public abstract int getHeight();    
    public abstract int getWidth();
	
    public final void drawGlyph(Graphics2D g) {
       	if (getGlyphTranslate() != null) {
    		g.translate(getGlyphTranslate().getX(), getGlyphTranslate().getY());
    	}    	
       	Composite oldComposite = null;
       	if (alphaComposite != null) {
       		oldComposite = g.getComposite();
       		g.setComposite(alphaComposite);       		
       	}
       	drawTranslatedGlyph(g);
       	if (alphaComposite != null) {
       		g.setComposite(oldComposite);       		
       	}
       	if (getGlyphTranslate() != null) {
    		g.translate(-getGlyphTranslate().getX(), -getGlyphTranslate().getY());
       	}
    }
	
    protected abstract void drawTranslatedGlyph(Graphics2D g);
}
