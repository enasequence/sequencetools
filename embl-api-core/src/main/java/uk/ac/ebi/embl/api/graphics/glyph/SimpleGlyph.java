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

public abstract class SimpleGlyph extends Glyph {
    public SimpleGlyph(Canvas canvas) {
    	super(canvas);
    }

    private Color color = Color.BLACK;
    
    @Override
	protected void drawTranslatedGlyph(Graphics2D g) {
        boolean isColor = (getColor() != null);
        Color color = g.getColor();
        if (isColor) {
            g.setColor(getColor());
        }
        drawSimpleGlyph(g);
        if (isColor) {
            g.setColor(color);
        }
    }
    
    abstract protected void drawSimpleGlyph(Graphics2D g);
    
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
