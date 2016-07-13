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
import java.util.Vector;

public class PolyGlyph extends SimpleGlyph {
    public PolyGlyph(Canvas canvas) {
    	super(canvas);
    }
    
    private Vector<GlyphPoint> points = new Vector<GlyphPoint>();
    private boolean fill = false;

    @Override
    protected void drawSimpleGlyph(Graphics2D g) {
        if (points.size() < 1) {
            return;
        }
        int[] x = new int[points.size()];
        int[] y = new int[points.size()];
        for (int i = 0 ; i < points.size() ; i++) {
             x[i] = points.get(i).getX();
             y[i] = points.get(i).getY();
        }
        g.drawPolygon(x, y, points.size());
        if (isFill()) {
            g.fillPolygon(x, y, points.size());
        }
    }
        
    @Override
	public int getHeight() {
        if (points.size() == 0) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (GlyphPoint point : points) {
            if (point.getY() > max) {
                max = point.getY();
            }
            if (point.getY() < min) {
                min = point.getY();
            }
        }
        return max - min + 1;
    }

    @Override
	public int getWidth() {
        if (points.size() == 0) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (GlyphPoint point : points) {
            if (point.getX() > max) {
                max = point.getX();
            }
            if (point.getX() < min) {
                min = point.getX();
            }
        }
        return max - min + 1;
    }
    
    public void addPoint(GlyphPoint point) {
        points.add(point);
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }    
}
