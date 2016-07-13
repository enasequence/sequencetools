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
import java.awt.Font;
import java.awt.Graphics2D;

public class TextGlyph extends SimpleGlyph {
    public TextGlyph(Canvas canvas) {
    	super(canvas);
        setColor(Color.black);
    }

    private GlyphPoint point = new GlyphPoint();
    private String text;
    private Font font = new Font("SansSerif", Font.PLAIN, 12);
    
    @Override
    protected void drawSimpleGlyph(Graphics2D g) {
        if (getText() == null) {
            return;
        }
        if (getFont() == null) {
            return;
        }         
        Font font = g.getFont();
        g.setFont(getFont());
        int height = g.getFontMetrics().getAscent();
        g.drawString(getText(), getPoint().getX(), 
                     getPoint().getY() + height);
        g.setFont(font);
    }

    @Override
	public int getHeight() {
    	Graphics2D g = getCanvas().getTempGraphics2D();
        Font font = g.getFont();
        g.setFont(getFont());
        int height = g.getFontMetrics().getHeight();
        g.setFont(font);
        return height;
    }
    
    @Override
	public int getWidth() {
    	Graphics2D g = getCanvas().getTempGraphics2D();
        Font font = g.getFont();
        g.setFont(getFont());
        int width = g.getFontMetrics().stringWidth(getText());
        g.setFont(font);
        return width;
    }

    public GlyphPoint getPoint() {
        return point;
    }

    public void setPoint(GlyphPoint point) {
        this.point = point;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }
}
