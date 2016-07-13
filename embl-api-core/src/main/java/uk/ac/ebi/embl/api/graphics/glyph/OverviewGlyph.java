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

public class OverviewGlyph extends VerticalCompositeGlyph {
    public OverviewGlyph(Canvas canvas,
    		Long sequenceLength, String accession, Integer version) {
        super(canvas);
    	this.sequenceLength = sequenceLength;
        this.accession = accession;
        this.version = version;
        setTextColor(TEXT_COLOR);
        setColor(REGION_BORDER_COLOR);
        setBgColor(REGION_COLOR);
    }

    private final static Color TEXT_COLOR = Color.WHITE;
    private final static Color REGION_BORDER_COLOR = Color.BLACK;
    private final static Color REGION_COLOR = Color.LIGHT_GRAY;
    private final static Color FOCUS_COLOR = Color.RED;
    private final static int FOCUS_HEIGHT = 30;
    private final static int REGION_HEIGHT = 20;
    private final static int MARGIN = 5;
    
    Long sequenceLength;
    private String accession;
    private Integer version;
    
    @Override
	protected void initGlyph() {
        addGlyph(new VerticalFillerGlyph(getCanvas()));		
        initOverviewRegionForwardRulerGlyph();
    	int x = 0;
    	CompositeGlyph compositeGlyph = new CompositeGlyph(getCanvas());
        // background rectangle
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(BackgroundGlyph.BACKGROUND_COLOR);
        rectGlyph.setFill(true);
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getWidth());
        rectGlyph.setHeight(FOCUS_HEIGHT);
        compositeGlyph.addGlyph(rectGlyph);
        // foreground rectangle
        rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getBgColor());
        rectGlyph.setFill(true);
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getWidth());
        rectGlyph.setHeight(REGION_HEIGHT);
        compositeGlyph.addGlyph(rectGlyph);
        // foreground rectangle border
        rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(getColor());
        rectGlyph.setFill(false);
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getWidth());
        rectGlyph.setHeight(REGION_HEIGHT);
        compositeGlyph.addGlyph(rectGlyph);
        // text
        TextGlyph textGlyph = new TextGlyph(getCanvas());
        textGlyph.setColor(getTextColor());
        textGlyph.setText(accession + "." + version);
        textGlyph.setFont(Glyph.DEFAULT_FONT);
        textGlyph.setPoint(new GlyphPoint(
            x + (getWidth() / 2) - (textGlyph.getWidth() / 2), 
            (REGION_HEIGHT - textGlyph.getHeight()) / 2));
        if (textGlyph.getWidth() < getWidth()) {
        	compositeGlyph.addGlyph(textGlyph);
        }
        initOverviewRegionFocusRectGlyph(compositeGlyph);
        addGlyph(compositeGlyph);
    }
    
    private void initOverviewRegionFocusRectGlyph(CompositeGlyph compositeGlyph) {        
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(FOCUS_COLOR);
        rectGlyph.setFill(false);
        int x = getCanvas().convertOverviewRegionPosition(getCanvas().getVisibleFeatureRegionBeginPosition());
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getCanvas().convertOverviewRegionPosition(
        		getCanvas().getVisibleFeatureRegionEndPosition()) - x);
        rectGlyph.setHeight(FOCUS_HEIGHT);
        rectGlyph.setGlyphTranslate(new GlyphTranslate(0, 1-(FOCUS_HEIGHT-REGION_HEIGHT)/2));
    	//float[] dashPattern = { 1, 1, 1, 1 };
    	//BasicStroke stroke = new BasicStroke(
    	//		1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, dashPattern, 0);
    	//rectGlyph.setStroke(stroke);        
        compositeGlyph.addGlyph(rectGlyph);                
        addGlyph(new VerticalFillerGlyph(getCanvas(), MARGIN));
    }
        
    private void initOverviewRegionForwardRulerGlyph() {
        ForwardRulerGlyph forwardRulerGlyph = new ForwardRulerGlyph(getCanvas());
        forwardRulerGlyph.setText(getCanvas().formatPosition(
        		sequenceLength));
        forwardRulerGlyph.setLabel("Overview");
        forwardRulerGlyph.setBoldLabel(true);
        CompositeGlyph compositeGlyph = new CompositeGlyph(getCanvas());
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(BackgroundGlyph.BACKGROUND_COLOR);
        rectGlyph.setFill(true);
        rectGlyph.setPoint(new GlyphPoint(0, 0));
        rectGlyph.setWidth(forwardRulerGlyph.getWidth());
        rectGlyph.setHeight(forwardRulerGlyph.getHeight());
        compositeGlyph.addGlyph(rectGlyph);
        compositeGlyph.addGlyph(forwardRulerGlyph);
        addGlyph(compositeGlyph);
    }    
    
    @Override
	public int getWidth() {
        return getCanvas().getMainPanelWidth();
    }
   
}
