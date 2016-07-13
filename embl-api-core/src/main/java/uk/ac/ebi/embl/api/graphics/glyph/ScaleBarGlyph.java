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

import uk.ac.ebi.embl.api.graphics.view.Region;

public class ScaleBarGlyph extends CompositeGlyph {
    public ScaleBarGlyph(Canvas canvas, Region region) {
        super(canvas);
        this.region = region;
        setColor(Color.black);
        setTextColor(Color.black);
    }
    
    private Region region;
    private String leftText;
    private String rightText;

    private final int scaleBarHeight = 4;
    private final int tickHeight = 4;
    
    @Override
	protected void initGlyph() {
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getColor());
        rectGlyph.setHeight(scaleBarHeight);
        rectGlyph.setWidth(getWidth());
        addGlyph(rectGlyph);
        for (int x = getCanvas().getColumnWidth() ; x < getWidth() ; x += 2 * getCanvas().getColumnWidth()) {
            rectGlyph = new RectGlyph(getCanvas());
            rectGlyph.setColor(getColor());
            rectGlyph.setPoint(new GlyphPoint(x, 0));
            rectGlyph.setHeight(scaleBarHeight);
            rectGlyph.setWidth(getCanvas().getColumnWidth());
            rectGlyph.setFill(true);
            addGlyph(rectGlyph);
        }
        // left tick
        int y = scaleBarHeight + tickHeight;
        LineGlyph lineGlyph = new LineGlyph(getCanvas());
        lineGlyph.setColor(getColor());
        lineGlyph.setEndPoint(new GlyphPoint(0, y));
        addGlyph(lineGlyph);
        
        // right tick
        if (region.equals(Region.BASE_PAIR_REGION)) {
	        lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setBeginPoint(new GlyphPoint(getWidth() - getCanvas().getColumnWidth(), 0));
	        lineGlyph.setEndPoint(new GlyphPoint(getWidth() - getCanvas().getColumnWidth(), y));
	        addGlyph(lineGlyph);
        }
        else if (region.equals(Region.FEATURE_REGION)) {
        	int rightMargin = getCanvas().getFeatureRegionRangeWidth(
        			getCanvas().getVisibleFeatureRegionEndPosition(),
        			getCanvas().getVisibleFeatureRegionEndPosition());
	        lineGlyph = new LineGlyph(getCanvas());
	        lineGlyph.setColor(getColor());
	        lineGlyph.setBeginPoint(new GlyphPoint(getWidth() - rightMargin, 0));
	        lineGlyph.setEndPoint(new GlyphPoint(getWidth() - rightMargin, y));
	        addGlyph(lineGlyph);
        }
        
        // left text
        y += VerticalSpacerGlyph.DEFAULT_HEIGHT;
        TextGlyph textGlyph = new TextGlyph(getCanvas());
        textGlyph.setColor(getTextColor());
        textGlyph.setFont(Glyph.DEFAULT_FONT);
        textGlyph.setText(leftText);
        textGlyph.setPoint(new GlyphPoint(0, y));
        addGlyph(textGlyph);
        
        // right text
        textGlyph = new TextGlyph(getCanvas());
        textGlyph.setColor(getTextColor());
        textGlyph.setFont(Glyph.DEFAULT_FONT);
        textGlyph.setText(rightText);
        textGlyph.setPoint(new GlyphPoint(getWidth() - textGlyph.getWidth(), y));
        addGlyph(textGlyph);
    }

    @Override
	public int getHeight() {
        TextGlyph textGlyph = new TextGlyph(getCanvas());
        textGlyph.setFont(Glyph.DEFAULT_FONT);
        int textHeight = textGlyph.getHeight();
        return scaleBarHeight + tickHeight + VerticalSpacerGlyph.DEFAULT_HEIGHT + textHeight;
    }

    @Override
	public int getWidth() {
    	return getCanvas().getVisibleColumnCount() * 
 	           getCanvas().getColumnWidth();
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public String getRightText() {
        return rightText;
    }
}
