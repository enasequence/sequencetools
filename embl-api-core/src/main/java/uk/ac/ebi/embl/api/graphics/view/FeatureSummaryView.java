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
package uk.ac.ebi.embl.api.graphics.view;

import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.graphics.glyph.FeatureSummaryGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.HSLColor;
import uk.ac.ebi.embl.api.graphics.glyph.ScaleBarGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalCompositeGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalFillerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalSpacerGlyph;
import uk.ac.ebi.embl.api.translation.TranslationTable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class FeatureSummaryView extends View { /* extends BasePairView { */

    public FeatureSummaryView(Entry entry) {
    	
    	super(1L, // visibleFeatureRegionBeginPosition, 
        		entry.getSequence().getLength(), // visibleFeatureRegionEndPosition
        		1L, // visibleBasePairRegionBeginPosition
        		entry.getSequence().getLength(), //visibleBasePairRegionEndPosition        		        		
                entry.getSequence().getLength(),   // sequenceLength
                1L, // availableSequenceBeginPosition
                entry.getSequence().getLength(), // availableSequenceEndPosition,
                null, // sequence
                TranslationTable.DEFAULT_TRANSLATION_TABLE,
                COLUMN_COUNT /* column count */);
    	
        this.entry = entry;
      }

    private static final int COLUMN_COUNT = 54;
    private static final Color PLACEMENT_COLOR = new Color(0xDDDDDD);
    private static final Color FEATURE_COLOR = new Color(0x1D8086);
    private static final int FEATURE_COLOR_START_SHADE = 24;
    private static final int FEATURE_COLOR_SHADE_STEP = -3;
    private static final int FEATURE_COLOR_MIN_SHADE = -24;

    private Entry entry;
    private VerticalCompositeGlyph featureSummaryGlyphContainer;
    private HashMap<String, FeatureSummaryGlyph> featureSummaryGlyphs;
    private FeatureSummaryGlyph assemblySummaryGlyph;


    public class FeatureSummaryComparator implements Comparator<FeatureSummaryGlyph> {
        public int compare(FeatureSummaryGlyph obj1, FeatureSummaryGlyph obj2) {
            return new Integer(obj2.getFeatureCount()).compareTo(obj1.getFeatureCount());
        }
    }    
    
    @Override
    protected void initGlyph() {
    	getCanvas().LEFT_MARGIN = 150;
        featureSummaryGlyphContainer = new VerticalCompositeGlyph(getCanvas());

        addGlyph(new VerticalFillerGlyph(getCanvas(), 5));

        ScaleBarGlyph scalebarGlyph = initFeatureRegionScalebarGlyph();        
        scalebarGlyph.setLabel("Feature (count)");
        scalebarGlyph.setBoldLabel(true);
        
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        addGlyph(featureSummaryGlyphContainer); 

        assemblySummaryGlyph = initAssemblySummaryGlyph("placement");
        featureSummaryGlyphs = new HashMap<String, FeatureSummaryGlyph>();

        for (Feature feature : entry.getFeatures()) {
        	if (feature.getName().equals("unsure")) {
        		continue;
        	}
        	if (feature.getName().equals("gap")) {
        		continue;
        	}
        	if (feature.getName().equals("assembly_gap")) {
        		continue;
        	}
            initFeatureSummaryGlyph(feature.getName()).addFeature(feature);
        }

        // Show placements 
        Long contigPosition = 1L;

        int i = 0;
        for (Location location : entry.getSequence().getContigs()) {
            ++i;
            contigPosition = addContig(location, contigPosition);
        }       
        for (Assembly assembly : entry.getAssemblies()) {
            addAssembly(assembly);
        }
              
        if (assemblySummaryGlyph.getFeatureCount() > 0 ) {
        	featureSummaryGlyphContainer.addGlyph(assemblySummaryGlyph);
            assemblySummaryGlyph.setLabel(
            		assemblySummaryGlyph.getFeatureName() + " (" +
            		assemblySummaryGlyph.getFeatureCount() + ")");
            assemblySummaryGlyph.setColor(PLACEMENT_COLOR);
        }

        HSLColor baseColor = new HSLColor( FEATURE_COLOR );
        int summaryTracks = 0;
                
        ArrayList<FeatureSummaryGlyph> list = new ArrayList<FeatureSummaryGlyph>(featureSummaryGlyphs.values());        
        java.util.Collections.sort(list, new FeatureSummaryComparator());
        for (FeatureSummaryGlyph glyph : list) {
        	featureSummaryGlyphContainer.addGlyph(glyph);
        	glyph.setLabel(
        			glyph.getFeatureName() + " (" +
        			glyph.getFeatureCount() + ")");
            ++summaryTracks;
            glyph.setColor(baseColor.adjustShade(Math.max(FEATURE_COLOR_MIN_SHADE, 
            		FEATURE_COLOR_START_SHADE + FEATURE_COLOR_SHADE_STEP * summaryTracks)));
        }        
    }

    private FeatureSummaryGlyph initAssemblySummaryGlyph(String name) {
        return new FeatureSummaryGlyph(getCanvas(), name, this.entry.getSequence().getLength());
    }

    private FeatureSummaryGlyph initFeatureSummaryGlyph(String name) {
        FeatureSummaryGlyph glyph = featureSummaryGlyphs.get(name);
          if (glyph == null) {
        	  glyph = new FeatureSummaryGlyph(getCanvas(), name, this.entry.getSequence().getLength());
              featureSummaryGlyphs.put(name, glyph);
          }
          return glyph;
    }
    
    private Long addContig(Location location, Long contigPosition) {
        Long beginPosition = contigPosition;
        Long endPosition = contigPosition + location.getLength();        
        assemblySummaryGlyph.addFeature(beginPosition, endPosition);
        return endPosition;
    }

    private void addAssembly(Assembly assembly) {
        Long beginPosition = assembly.getSecondarySpan().getBeginPosition();
        Long endPosition = assembly.getSecondarySpan().getEndPosition();
        assemblySummaryGlyph.addFeature(beginPosition, endPosition);
    }
}
