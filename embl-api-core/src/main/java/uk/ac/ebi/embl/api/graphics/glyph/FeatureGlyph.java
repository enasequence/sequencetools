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
import java.util.List;
import java.util.Vector;

import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;

// Remote segments will not be drawn.

public class FeatureGlyph extends VerticalCompositeGlyph implements TextAlignGlyph {
    public FeatureGlyph(Canvas canvas, Long beginPosition, Long endPosition) {
    	super(canvas);
    	this.beginPosition = beginPosition;
    	this.endPosition = endPosition;
		setGlyphTranslate(new GlyphTranslate(
			getCanvas().convertFeatureRegionPosition(beginPosition), 0));
    	featureWidth = getCanvas().getFeatureRegionRangeWidth(beginPosition, endPosition);
    	setColor(COLOR);
    	setBgColor(BACKGROUND_COLOR);
    	setTextColor(TEXT_COLOR);
    }
    
    private static final Color COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final int FEATURE_HEIGHT = 6;
    private static final int TEXT_TICK_HEIGHT = 3;
    private static final int TEXT_TICK_WIDTH = 3;
    private static final int FEATURE_ARROW_WIDTH = 4;
    private static final int MIN_INTRON_WIDTH = 3;
    
    private Long beginPosition;
    private Long endPosition;
    private boolean showLeftArrow = false;
    private boolean showRightArrow = false;
    private boolean showTextOnly = false;
    private boolean showContinueArrow = false;
    private boolean showPreferMiddleText = false;
    private int featureWidth;
    private int featureHeight = FEATURE_HEIGHT;
    private List<Location> locations;
    private String topText;
    private String middleText;
    private String bottomText;    
    private boolean allowRightAlign = true;
    private boolean isAligned = false;
    private HorizontalCompositeGlyph topTextCompositeGlyph;
    private HorizontalCompositeGlyph bottomTextCompositeGlyph;
    private TextGlyph middleTextGlyph;
    
    private class Segment {    	
    	public Segment(Long beginPosition, Long endPosition, boolean complement) {
			this.beginPosition = beginPosition;
			this.endPosition = endPosition;
			this.complement = complement;
			leftArrow = complement;
			rightArrow = !complement;
		}
		public Long beginPosition;
		public Long endPosition;
		public boolean complement;
		public boolean prevSegmentRemote = false;
		public boolean nextSegmentRemote = false;
		public boolean nonLinearSegment = false;
		public int intronWidth;
		public boolean leftArrow = false;
		public boolean rightArrow = false;
    }

	@Override
	public int getWidth() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        int	width = super.getWidth();
		return width;
	}
	
	public int getFeatureWidth() {
		return featureWidth;
	}	
		
	@Override
	protected void initGlyph() {
		
		initMiddleTextGlyph();				
		initTopTextGlyph();
		
		if (!showTextOnly) {
			Vector<Segment> segments = new Vector<Segment>();
			int nonLinearSegmentCount = getSegments(segments);
	    	if (segments.size() < 1) {
	    		initNonSegmentedFeatureGlyph();
	    	}
	    	else {
	    		initSegmentedFeatureGlyph(segments, nonLinearSegmentCount);
	    	}	    	
    	}    	
		initBottomTextGlyph();
    }
    
	private void initTopTextGlyph() {
		if (topText != null) {
			HorizontalCompositeGlyph compositeGlyph = new HorizontalCompositeGlyph(getCanvas()); 
			TextGlyph textGlyph = new TextGlyph(getCanvas());
			textGlyph.setText(topText);
			textGlyph.setColor(getTextColor());
			compositeGlyph.addGlyph(textGlyph);
			addGlyph(compositeGlyph);
			addGlyph(new VerticalSpacerGlyph(getCanvas()));
			topTextCompositeGlyph = compositeGlyph;
		}
	}
	
	private void initMiddleTextGlyph() {          
    	if (showPreferMiddleText && middleText == null) {
    		middleText = (topText != null) ? topText : bottomText;
    	}
    	if (middleText != null) {
    		TextGlyph textGlyph = new TextGlyph(getCanvas());
    		textGlyph.setColor(Color.WHITE);
    		textGlyph.setText(middleText);
    		textGlyph.setFont(Glyph.DEFAULT_FONT);
            textGlyph.setPoint(new GlyphPoint(
	                featureWidth / 2 - textGlyph.getWidth() / 2, 
	                (featureHeight - textGlyph.getHeight()) / 2));    		
    		if (textGlyph.getWidth() < featureWidth) {
    			middleTextGlyph = textGlyph;
        		if (showPreferMiddleText) {
        			topText = null;
        			bottomText = null;
        		}    			
    		}
    	}
	}
	
	private void initBottomTextGlyph() {
		if (bottomText != null) {
			HorizontalCompositeGlyph compositeGlyph = new HorizontalCompositeGlyph(getCanvas());
			TextGlyph textGlyph = new TextGlyph(getCanvas());
			textGlyph.setText(bottomText);
			textGlyph.setColor(getTextColor());
			compositeGlyph.addGlyph(textGlyph);
			addGlyph(compositeGlyph);
			bottomTextCompositeGlyph = compositeGlyph;		
		}
		addGlyph(new VerticalSpacerGlyph(getCanvas()));
	}
	
	private void initNonSegmentedFeatureGlyph() {
    	HorizontalCompositeGlyph featureCompositeGlyph = 
    		new HorizontalCompositeGlyph(getCanvas());
    	int rectWidth = featureWidth;
    	int totalArrowWidth = 
    			((showLeftArrow) ? FEATURE_ARROW_WIDTH : 0) + 
    			((showRightArrow) ? FEATURE_ARROW_WIDTH : 0);
    	if (totalArrowWidth > rectWidth) {
    		showLeftArrow = false;
    		showRightArrow = false;
    	}
    	else {
    		rectWidth -= totalArrowWidth;
    	}
    	if (showLeftArrow) {
    		featureCompositeGlyph.addGlyph(initLeftArrowGlyph());	
    	}

    	featureCompositeGlyph.addGlyph(initFeatureRectGlyph(rectWidth,
    			beginPosition, endPosition, null));

    	if (showRightArrow) {
    		featureCompositeGlyph.addGlyph(initRightArrowGlyph());
    	}
    	
    	CompositeGlyph featureDecoratorCompositeGlyph = new CompositeGlyph(getCanvas());
    	featureDecoratorCompositeGlyph.addGlyph(featureCompositeGlyph);
    	    	
    	// Decorate feature with middle text.
        if (middleTextGlyph != null) {
        	featureDecoratorCompositeGlyph.addGlyph(middleTextGlyph);
        }
    	
    	addGlyph(featureDecoratorCompositeGlyph);		
	}
	
	private void initSegmentedFeatureGlyph(Vector<Segment> segments, int nonLinearSegmentCount) {
		CompositeGlyph featureDecoratorCompositeGlyph = new CompositeGlyph(getCanvas());		
    	HorizontalCompositeGlyph linearCompositeGlyph = new HorizontalCompositeGlyph(getCanvas()); 		
    	TilingGlyph nonLinearCompositeGlyph = null;
		if (nonLinearSegmentCount > 0) {
    		nonLinearCompositeGlyph = new TilingGlyph(getCanvas(), featureWidth);
    		nonLinearCompositeGlyph.setAddGlyphsToEnd(true);
    		nonLinearCompositeGlyph.setSortGlyphs(false);
    	}
		for (int i = 0 ; i < segments.size() ; ++i) {
			Segment segment = segments.get(i);
			if (i == 0) {
	    		linearCompositeGlyph.setGlyphTranslate(new GlyphTranslate(
            			getCanvas().getFeatureRegionLocationDistance(beginPosition, 
            					segments.get(i).beginPosition), 0));
			}
			else if (i > 0 && segments.get(i).nonLinearSegment) {
				nonLinearCompositeGlyph.addGlyph(linearCompositeGlyph);
				// Start constructing new line.
				linearCompositeGlyph = new HorizontalCompositeGlyph(getCanvas());
				linearCompositeGlyph.setGlyphTranslate(new GlyphTranslate(
            			getCanvas().getFeatureRegionLocationDistance(beginPosition, 
            					segments.get(i).beginPosition), 0));
			}
						
			// Draw segment.
	    	int rectWidth = 0;
    		rectWidth = getCanvas().getFeatureRegionRangeWidth(
    			segment.beginPosition, 
    			segment.endPosition);
	    	int totalArrowWidth = 
	    			((showLeftArrow && segment.leftArrow) ? FEATURE_ARROW_WIDTH : 0) + 
	    			((showRightArrow && segment.rightArrow) ? FEATURE_ARROW_WIDTH : 0);
	    	if (totalArrowWidth > rectWidth) {
	    		segment.leftArrow = false;
	    		segment.rightArrow = false;
	    	}
	    	else {
	    		rectWidth -= totalArrowWidth;
	    	}
	    	if (showLeftArrow && segment.leftArrow) {
	    		linearCompositeGlyph.addGlyph(initLeftArrowGlyph());	
	    	}
	    	Glyph featureCompositeGlyph = initFeatureRectGlyph(rectWidth, 
	    			segment.beginPosition, segment.endPosition, segment.complement);
    		linearCompositeGlyph.addGlyph(featureCompositeGlyph);
    		
	    	if (showRightArrow && segment.rightArrow) {
	    		linearCompositeGlyph.addGlyph(initRightArrowGlyph());	
	    	}  			
			// Draw connector.
			if (i < segments.size() - 1) {
				if (!segments.get(i+1).nonLinearSegment) {
    				int firstLineWidth = segment.intronWidth / 2;
    				int secondLineWidth = segment.intronWidth - firstLineWidth; 
    				linearCompositeGlyph.addGlyph(
							initConnectorGlyph(firstLineWidth, secondLineWidth));
				}
			} 			
		}
		
		if (nonLinearSegmentCount > 0) {
			nonLinearCompositeGlyph.addGlyph(linearCompositeGlyph);
		}
				
		if (nonLinearSegmentCount > 0) {
			// Draw a rectangle around non linear features.
			VerticalCompositeGlyph verticalCompositeGlyph = new VerticalCompositeGlyph(getCanvas());			
			verticalCompositeGlyph.addGlyph(new VerticalSpacerGlyph(getCanvas()));
			verticalCompositeGlyph.addGlyph(nonLinearCompositeGlyph);
			CompositeGlyph rectCompositeGlyph = new CompositeGlyph(getCanvas());
			rectCompositeGlyph.addGlyph(verticalCompositeGlyph);
			RectGlyph rectGlyph = new RectGlyph(getCanvas());
	    	rectGlyph.setColor(getBgColor());
	    	rectGlyph.setFill(false);
	    	rectGlyph.setHeight(verticalCompositeGlyph.getHeight());
	    	rectGlyph.setWidth(verticalCompositeGlyph.getWidth());    			
	    	rectCompositeGlyph.addGlyph(rectGlyph);
	    	featureDecoratorCompositeGlyph.addGlyph(rectCompositeGlyph);
		}
		else {    		
    		featureDecoratorCompositeGlyph.addGlyph(linearCompositeGlyph);		
		}
    	addGlyph(featureDecoratorCompositeGlyph);		
	}
	
	private PolyGlyph initLeftArrowGlyph() {
		PolyGlyph arrow = new PolyGlyph(getCanvas());
		arrow.addPoint(new GlyphPoint(0, featureHeight / 2));
		arrow.addPoint(new GlyphPoint(FEATURE_ARROW_WIDTH-1, 0));
		arrow.addPoint(new GlyphPoint(FEATURE_ARROW_WIDTH-1, featureHeight));
		arrow.setFill(true);
		arrow.setColor(getBgColor());
		return arrow;		
	}
	
	private PolyGlyph initRightArrowGlyph() {
		PolyGlyph arrow = new PolyGlyph(getCanvas());
		arrow.addPoint(new GlyphPoint(0, 0));
		arrow.addPoint(new GlyphPoint(FEATURE_ARROW_WIDTH-1, featureHeight / 2));
		arrow.addPoint(new GlyphPoint(0, featureHeight));
		arrow.setFill(true);
		arrow.setColor(getBgColor());
		return arrow;
	}
		
	private Glyph initFeatureRectGlyph(int rectWidth, 
			Long segmentBeginPosition, Long segmentEndPosition, Boolean segmentComplement) {
				
    	RectGlyph rectGlyph = new RectGlyph(getCanvas());
    	rectGlyph.setColor(getBgColor());
    	rectGlyph.setFill(true);
    	rectGlyph.setHeight(featureHeight);
    	rectGlyph.setWidth(rectWidth);
    	return rectGlyph;
	}

	private HorizontalCompositeGlyph initConnectorGlyph(int firstLineWidth, int secondLineWidth) {	
		HorizontalCompositeGlyph compositeGlyph = new HorizontalCompositeGlyph(getCanvas());
		LineGlyph line = new LineGlyph(getCanvas());
		line.setBeginPoint(new GlyphPoint(0, featureHeight / 2));
		line.setEndPoint(new GlyphPoint(firstLineWidth, 0));
		line.setColor(getBgColor());    				
		compositeGlyph.addGlyph(line);
		line = new LineGlyph(getCanvas());
		line.setBeginPoint(new GlyphPoint(0, 0));
		line.setEndPoint(new GlyphPoint(secondLineWidth, featureHeight / 2));    				
		line.setColor(getBgColor());
		compositeGlyph.addGlyph(line); 
		return compositeGlyph;
	}
	
	private int getSegments(Vector<Segment> segments) {
		int nonLinearSegmentCount = 0;		
		if (locations != null && locations.size() > 1) {
			int i = -1;			
    		boolean remoteSegment = false;			
			for (Location location : locations) {
				if (location instanceof RemoteLocation) {
					remoteSegment = true;
					if (i > -1) {
						segments.get(i).nextSegmentRemote = remoteSegment;
					}
					continue;
				}
				if (!getCanvas().isVisibleFeatureRegionRange(location.getBeginPosition(), 
						location.getEndPosition())) {
					continue;
				}
				if (i == -1) {
					// First segment.
			    	segments.add(new Segment(location.getBeginPosition(), 
							location.getEndPosition(), location.isComplement()));
					++i;
					segments.get(i).prevSegmentRemote = remoteSegment;
				}
				else {
					// Other segments.
					boolean nonLinearSegment = 
						(location.getBeginPosition().compareTo(segments.get(i).endPosition) < 0);
					if (nonLinearSegment) {
						++nonLinearSegmentCount;							
						// Draw new segment.
						segments.add(new Segment(location.getBeginPosition(), 
								location.getEndPosition(), location.isComplement()));
						++i;
						segments.get(i).prevSegmentRemote = remoteSegment;						
						segments.get(i).nonLinearSegment = true;							
						segments.get(i).leftArrow = location.isComplement();
						segments.get(i).rightArrow = !location.isComplement();						
					}
					else {
						int intronWidth = 0;
						// Segments can be joined to together with no intron.						
						if (location.getBeginPosition() > segments.get(i).endPosition + 1) {
							intronWidth = getCanvas().getFeatureRegionLocationDistance(
								segments.get(i).endPosition + 1,
								location.getBeginPosition());
						}
						// Draw segments if they are separated by at least MAX_INTRON_WIDTH.
						if (intronWidth >= MIN_INTRON_WIDTH) {
							segments.get(i).intronWidth = intronWidth;
							// Draw new segment.
							segments.add(new Segment(location.getBeginPosition(), 
									location.getEndPosition(), location.isComplement()));
							++i;
							segments.get(i).prevSegmentRemote = remoteSegment;
						}
						else {
							// Add to existing segment.						
							segments.get(i).endPosition = location.getEndPosition();
							segments.get(i).leftArrow = 
								segments.get(i).leftArrow || location.isComplement();
							segments.get(i).rightArrow = 
								segments.get(i).rightArrow || !location.isComplement();						
						}
					}
				}
				remoteSegment = false;
			}			
		}
		return nonLinearSegmentCount;
	}
	
	
	public void setAlign(Align align) {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
    	if (isAligned) {
    		return;    		
    	}
    	isAligned = true;
    	if (!allowRightAlign) {
    		align = Align.LEFT;
    	}
		if (topTextCompositeGlyph != null) {
			int topTextHeight = topTextCompositeGlyph.getHeight(); 
			if (align.equals(Align.RIGHT)) {
				// Right align top text.
				topTextCompositeGlyph.setGlyphTranslate(new GlyphTranslate(
					- topTextCompositeGlyph.getWidth() - TEXT_TICK_WIDTH, 0));
				// Add tick.
	    		LineGlyph line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(TEXT_TICK_WIDTH - 1, 0));
	    		line.setGlyphTranslate(new GlyphTranslate(0, topTextHeight - TEXT_TICK_HEIGHT));
	    		topTextCompositeGlyph.addGlyph(line);
	    		line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(0, TEXT_TICK_HEIGHT));
	    		line.setGlyphTranslate(new GlyphTranslate(0, topTextHeight - TEXT_TICK_HEIGHT));	    		
	    		topTextCompositeGlyph.addGlyph(line);				
			}
			else {
				topTextCompositeGlyph.setGlyphTranslate(null);
				// Add tick.
	    		LineGlyph line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(TEXT_TICK_WIDTH - 1, 0));
	    		line.setGlyphTranslate(new GlyphTranslate(0, topTextHeight - TEXT_TICK_HEIGHT));
	    		topTextCompositeGlyph.addGlyphFront(line);
	    		line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(0, TEXT_TICK_HEIGHT));
	    		line.setGlyphTranslate(new GlyphTranslate(0, topTextHeight - TEXT_TICK_HEIGHT));	    		
	    		topTextCompositeGlyph.addGlyphFront(line);		
			}			
		}
		if (bottomTextCompositeGlyph != null) {
			if (align.equals(Align.RIGHT)) {
				// Right align bottom text.
				bottomTextCompositeGlyph.setGlyphTranslate(new GlyphTranslate(
					- bottomTextCompositeGlyph.getWidth() - TEXT_TICK_WIDTH, 0));
				// Add tick.
	    		LineGlyph line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(TEXT_TICK_WIDTH - 1, 0));
	    		line.setGlyphTranslate(new GlyphTranslate(0, TEXT_TICK_HEIGHT + 2));
	    		bottomTextCompositeGlyph.addGlyph(line);
	    		line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(0, -TEXT_TICK_HEIGHT));
	    		line.setGlyphTranslate(new GlyphTranslate(0, TEXT_TICK_HEIGHT + 2));	    		
	    		bottomTextCompositeGlyph.addGlyph(line);				
				
			}
			else {
				bottomTextCompositeGlyph.setGlyphTranslate(null);
				// Add tick.
	    		LineGlyph line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(TEXT_TICK_WIDTH - 1, 0));
	    		line.setGlyphTranslate(new GlyphTranslate(0, TEXT_TICK_HEIGHT + 2));
	    		bottomTextCompositeGlyph.addGlyphFront(line);
	    		line = new LineGlyph(getCanvas()); 
	    		line.setBeginPoint(new GlyphPoint(0, 0));
	    		line.setEndPoint(new GlyphPoint(0, -TEXT_TICK_HEIGHT));
	    		line.setGlyphTranslate(new GlyphTranslate(0, TEXT_TICK_HEIGHT + 2));	    		
	    		bottomTextCompositeGlyph.addGlyphFront(line);				
			}
		}
	}
	
	
	public boolean isAllowRightAlign() {
		return allowRightAlign;
	}

	public void setAllowRightAlign(boolean allowRightAlign) {
		this.allowRightAlign = allowRightAlign;
	}	
		
    public void setLocations(List<Location> locations) {
    	this.locations = locations;
    }
    
	public int getFeatureHeight() {
		return featureHeight;
	}

	public void setFeatureHeight(int featureHeight) {
		this.featureHeight = featureHeight;
	}

	public boolean isShowLeftArrow() {
		return showLeftArrow;
	}

	public void setShowLeftArrow(boolean showLeftArrow) {
		this.showLeftArrow = showLeftArrow;
	}

	public boolean isShowRightArrow() {
		return showRightArrow;
	}

	public void setShowRightArrow(boolean rightArrow) {
		this.showRightArrow = rightArrow;
	}
	
	public String getTopText() {
		return topText;
	}

	public void setTopText(String topText) {
		this.topText = topText;
	}

	public String getBottomText() {
		return bottomText;
	}

	public void setBottomText(String bottomText) {
		this.bottomText = bottomText;
	}

	public String getMiddleText() {
		return middleText;
	}

	public void setMiddleText(String middleText) {
		this.middleText = middleText;
	}

	public boolean isShowTextOnly() {
		return showTextOnly;
	}

	public void setShowTextOnly(boolean showTextOnly) {
		this.showTextOnly = showTextOnly;
	}

	public boolean isShowPreferMiddleText() {
		return showPreferMiddleText;
	}

	public void setShowPreferMiddleText(boolean preferMiddleText) {
		this.showPreferMiddleText = preferMiddleText;
	}

	public boolean isShowContinueArrow() {
		return showContinueArrow;
	}

	public void setShowContinueArrow(boolean showContinueArrow) {
		this.showContinueArrow = showContinueArrow;
	}	
}
