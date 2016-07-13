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
import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.graphics.view.FeatureSummaryView;

public class FeatureSummaryGlyph extends VerticalCompositeGlyph {
    public FeatureSummaryGlyph(Canvas canvas, String featureName, Long sequenceLength) {
    	super(canvas);
    	this.featureName = featureName;
    	this.sequenceLength = sequenceLength;
    	initHistogram();

    	//    	numberOfBins = getCanvas().getMainPanelWidth() / BIN_WIDTH;
    	System.out.println("getCanvas().getMainPanelWidth():" + getCanvas().getMainPanelWidth());
    	
    	setBgColor(BACKGROUND_COLOR);
    }
    
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    private String featureName;
    private Long sequenceLength;
    
    private int histogramCount = 0;
    private int[] histogram;
    private int[] tempHistogram;    
    private int numBins = 540 /* main panel width */ / 2 /* binWIdth */;
    private int binSize;
    private int binWidth = 2; /* minimum is two because of a bug in rectangle width computation */
    
    private final int MAX_HEIGHT = 14;
    private final int MIN_HEIGHT = 2;
    private final int HEIGHT_INCREMENT = 2;

    
    private void initHistogram() {
    	if (sequenceLength < numBins) {
    		numBins = sequenceLength.intValue();
    	}
    	binSize = (int) (sequenceLength / numBins);
    	//System.out.println("sequenceLength:" + sequenceLength);
    	System.out.println("binSize:" + binSize);
    	//System.out.println("numBins:" + numBins);
    	
    	histogram =  new int[numBins];
    	tempHistogram = new int[numBins];
    }
    
    private void addHistogram(double beginPosition, double endPosition) {
		histogramCount++;    	
		//System.out.println("histogramCount:" + histogramCount);
     	
    	int startBin = (int) ((beginPosition) / binSize);
    	int endBin = (int) ((endPosition) / binSize);

    	if (startBin < 0) startBin = 0; 
    	if (endBin < 0)	endBin = 0;
    	
    	if (startBin >= numBins) startBin = numBins - 1; 
    	if (endBin >= numBins) endBin = numBins - 1; 
    	
    	if (startBin > endBin) {
    		int tempBin = startBin;
    		startBin = endBin;
    		endBin = tempBin;
    	}

    	
		//System.out.println("startBin:" + startBin);
		//System.out.println("endBin:" + endBin);
    	
    	for (int bin = startBin ; bin <= endBin ; ++bin) {
    		++histogram[bin];
    	}
    }
    
    private void addHistogram(double[] beginPosition, double[] endPosition, int size) {
		histogramCount++;

    	Arrays.fill(tempHistogram, 0);
    	
    	for (int i = 0 ; i < size ; ++i) {
        	int startBin = (int) ((beginPosition[i]) / binSize);
        	int endBin = (int) ((endPosition[i]) / binSize);

        	if (startBin < 0) startBin = 0; 
        	if (endBin < 0)	endBin = 0;
        	
        	if (startBin >= numBins) startBin = numBins - 1; 
        	if (endBin >= numBins) endBin = numBins - 1; 
        	
        	if (startBin > endBin) {
        		int tempBin = startBin;
        		startBin = endBin;
        		endBin = tempBin;
        	}

        	for (int bin = startBin ; bin <= endBin ; ++bin) {
        		++tempHistogram[bin];
        	}
    	}

		for (int bin = 0 ; bin < numBins ; ++bin) {
			if (tempHistogram[bin] > 0) {
				++histogram[bin];
			}
		}
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

	@Override
	public int getHeight() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}

		return MAX_HEIGHT + 10;
	}	
	
	
	
	public int getFeatureCount() {
		return histogramCount;
	}
	
	public String getFeatureName() {
		return featureName;
	}


	public void addFeature(Long beginPosition, Long endPosition) {		
		addHistogram(beginPosition, endPosition);
	}
	
	public void addFeature(Feature feature) {
		List<Location> locations = feature.getLocations().getLocations();
		int size = locations.size();
	    double[] beginPosition = new double[size];
	    double[] endPosition = new double[size];
	    int i = 0;
		for (Location location : locations) {
			if (location instanceof RemoteLocation) {
				continue;
			}
		    beginPosition[i] = location.getBeginPosition();
		    endPosition[i] = location.getEndPosition();
		    ++i;
		}			
		if (i == 0) {
			return;
		}
		else if (i == 1) {
			addHistogram(beginPosition[0], endPosition[0]);				
		}
		else {
			addHistogram(beginPosition, endPosition, i);		
		}
	}
		
	private Glyph initFeatureGlyph(int height, int width, Color color) {
		if (width == 1) {
			LineGlyph glyph = new LineGlyph(getCanvas());
			glyph.setBeginPoint(new GlyphPoint(0,0));
			glyph.setEndPoint(new GlyphPoint(0,height));
			glyph.setColor(color);
			glyph.setGlyphTranslate(new GlyphTranslate(0, - height + MAX_HEIGHT - 5));
			return glyph;
		}
		else {
			RectGlyph glyph = new RectGlyph(getCanvas());
			glyph.setPoint(new GlyphPoint(0,0));
			glyph.setWidth(width);
			glyph.setHeight(height);
			glyph.setColor(color);
			glyph.setFill(true);		
			glyph.setGlyphTranslate(new GlyphTranslate(0, - height + MAX_HEIGHT - 5));
			return glyph;		
		}
	}	
	
	@Override
	protected void initGlyph() {		
    
    	VerticalCompositeGlyph containerGlyph = new VerticalCompositeGlyph(getCanvas());

		HorizontalCompositeGlyph featureCompositeGlyph = new HorizontalCompositeGlyph(getCanvas());

    	for (int i = 0 ; i < numBins ; ++i) {
    		if (histogram[i] == 0) {
   				int width = 1;
   				while (i < (numBins-1) && histogram[i+1] == 0) {
   					++width;
   					++i;
   				}
    	    	featureCompositeGlyph.addGlyph(new HorizontalSpacerGlyph(getCanvas(), width * binWidth));	    	
    		}
    		else {
    			int height = MIN_HEIGHT;
   				height += (histogram[i]-1) * HEIGHT_INCREMENT;
   				if (height > MAX_HEIGHT) height = MAX_HEIGHT;
   				int width = 1;
   				while (i < (numBins-1) && histogram[i] == histogram[i+1]) {
   					++width;
   					++i;
   				}
    	    	//System.out.println("height:" + height);
    	    	//System.out.println("width:" + width);
   				   				
    	    	featureCompositeGlyph.addGlyph(initFeatureGlyph(height, width * binWidth, getColor()));    			
    		}
    	}

    	containerGlyph.addGlyph(featureCompositeGlyph);		

   //  	TextGlyph textGlyph = new TextGlyph(getCanvas());
   // 	textGlyph.setText(featureTotalCount + " " + featureName + " features");

   // 	containerGlyph.addGlyph(textGlyph);
    	
    	addGlyph(containerGlyph);		
	}
}
