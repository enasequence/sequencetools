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

public class CircleGlyph extends SimpleGlyph {
    public CircleGlyph(Canvas canvas) {
    	super(canvas);
    }

    private GlyphPoint point = new GlyphPoint();
    private int size = 0;
    private boolean fill = false;

    @Override
    protected void drawSimpleGlyph(Graphics2D g) {    
        g.drawOval(getPoint().getX(), getPoint().getY(), 
                getSize(), getSize());
        if (isFill()) {
            g.fillOval(getPoint().getX(), getPoint().getY(), 
                    getSize(), getSize());
        }
    }

    @Override
	public int getHeight() {
    	return size;
    }

    @Override
	public int getWidth() {
    	return size;
    }

    public GlyphPoint getPoint() {
        return point;
    }

    public void setPoint(GlyphPoint glyphPoint) {
        this.point = glyphPoint;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int glyphSize) {
        this.size = glyphSize;
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean glyphFill) {
        this.fill = glyphFill;
    }
}
