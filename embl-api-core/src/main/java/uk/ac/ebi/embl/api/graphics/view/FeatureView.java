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
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.graphics.glyph.BackgroundGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.CompositeGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.FeatureGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.ForwardRulerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.GlyphPoint;
import uk.ac.ebi.embl.api.graphics.glyph.GlyphTranslate;
import uk.ac.ebi.embl.api.graphics.glyph.OverviewGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.RectGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.SeparatorGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.TilingGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalCompositeGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalFillerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalSpacerGlyph;
import uk.ac.ebi.embl.api.translation.TranslationTable;
import static uk.ac.ebi.embl.api.graphics.view.FeatureViewOptions.*;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class FeatureView extends BasePairView {

    public FeatureView(Entry entry,
            Long visibleFeatureRegionBeginPosition, Long visibleFeatureRegionEndPosition,
            Long visibleBasePairRegionBeginPosition, Long visibleBasePairRegionEndPosition,
            Long availableSequenceBeginPosition, Long availableSequenceEndPosition,
            Integer translationTable,
            int columnCount,
            EnumSet<FeatureViewOptions> options) {
        super(visibleFeatureRegionBeginPosition, visibleFeatureRegionEndPosition,
                visibleBasePairRegionBeginPosition, visibleBasePairRegionEndPosition,
                entry.getSequence().getLength(),   // sequenceLength
                availableSequenceBeginPosition,
                availableSequenceEndPosition,
//                new String(entry.getSequence().getSequenceByte()), // sequence
                "", // sequence
                translationTable,
                entry.getPrimaryAccession(),
                entry.getSequence().getVersion(),
                columnCount,
                getViewOptions(options));
        /*System.out.println("Inside Feature: seq:"+entry.getSequence().getSequence());
        System.out.println("Inside Feature: visibleBasePairRegionBeginPosition:"+visibleBasePairRegionBeginPosition);
        System.out.println("Inside Feature: visibleBasePairRegionEndPosition:"+visibleBasePairRegionEndPosition);
        System.out.println("Inside Feature: availableSequenceBeginPosition:"+availableSequenceBeginPosition);
        System.out.println("Inside Feature: availableSequenceEndPosition:"+availableSequenceEndPosition);
        */
        this.entry = entry;
        if (translationTable == null) {
            setTranslationTable(TranslationTable.DEFAULT_TRANSLATION_TABLE);
        }
        this.options = options;
        if (this.options == null) {
            //System.out.println("FeatureView. using default");
            this.options = getDefaultOptions();
        }
        else {
            //System.out.println("FeatureView. using given options");
        }
        //System.out.println("FeatureView: using given options");
        //System.out.println("FeatureView: ENUM SIZE:"+this.options.size());
        // TEST ->
        //this.setVisibleFeatureRegionBeginPosition(2000L);
        //this.setVisibleFeatureRegionEndPosition(2200L);
        //this.setVisibleBasePairRegionBeginPosition(2010L); // 601L); //
        //this.setVisibleBasePairRegionEndPosition(2071L); // 662L);
        // <- TEST
    }

    private static EnumSet<FeatureViewOptions> getDefaultOptions() {
         return EnumSet.of(
                    SHOW_GENE
                    ,SHOW_SOURCE
                    ,SHOW_FEATURE
                    ,SHOW_CONTIG
                    ,SHOW_ASSEMBLY
                    ,SHOW_SEQUENCE
                    ,SHOW_TRANSLATION
                    ,SHOW_FOCUS
                //    , GROUP_GENE
                    );
    }

    private static EnumSet<BasePairViewOptions> getViewOptions(
            EnumSet<FeatureViewOptions> options) {
        EnumSet<BasePairViewOptions> viewOptions = EnumSet.noneOf(BasePairViewOptions.class);
        if (options == null) {
            options = getDefaultOptions();
        }
        if (options != null && options.contains(SHOW_TRANSLATION)) {
            viewOptions.add(BasePairViewOptions.SHOW_TRANSLATION);
        }
        return viewOptions;
    }

    private class Gene {
        private Long beginPosition;
        private Long endPosition;
        private String geneName;
        private String locusTag;

        public Gene (Long beginPosition, Long endPosition,
                String geneName, String locusTag) {
            this.beginPosition = beginPosition;
            this.endPosition = endPosition;
            this.geneName = geneName;
            this.locusTag = locusTag;
        }

        public void adjustBeginPosition(Long position) {
            this.beginPosition = Math.min(beginPosition, position);
        }

        public void adjustEndPosition(Long position) {
            this.endPosition = Math.max(endPosition, position);
        }

        public Long getBeginPosition() {
            return beginPosition;
        }
        public Long getEndPosition() {
            return endPosition;
        }
        public String getGeneName() {
            return geneName;
        }
        public String getLocusTag() {
            return locusTag;
        }
    }

    private static final Color CONTIG_COLOR_EVEN = new Color(0x368EC9);
    private static final Color CONTIG_COLOR_ODD = new Color(0x02599C);
    private static final Color CONTIG_COLOR_GAP = new Color(0xD9D9D9);
    private static final Color ASSEMBLY_COLOR_EVEN = new Color(0x368EC9);
    private static final Color ASSEMBLY_COLOR_ODD = new Color(0x02599C);
    private static final Color CDS_COLOR = new Color(0xC93644);
    private static final Color GENE_COLOR = new Color(0xC97136);
    private static final Color SOURCE_COLOR = new Color(0xC9BB36);
    private static final Color FEATURE_COLOR = new Color(0x368EC9);
    private static final Color FOCUS_COLOR = Color.RED;
    private static final int CONTIG_HEIGHT = 20;
    private static final int FEATURE_REGION_ASSEMBLY_HEIGHT = 20;

    private Entry entry;
    private EnumSet<FeatureViewOptions> options;
    private HashMap<String, Vector<Gene>> locusTagGeneMap;
    private HashMap<String, Vector<Gene>> geneNameGeneMap;
    private HashMap<String, TilingGlyph> featureRegionTracks;
    private VerticalCompositeGlyph featureRegionTracksGlyph;
    private CompositeGlyph featureRegionTracksAndFocusGlyph;
    private TilingGlyph featureRegionGeneTrack;
    private TilingGlyph featureRegionSourceTrack;
    private TilingGlyph featureRegionAssemblyTrack;
    private TilingGlyph featureRegionContigSegmentTrack;
    private TilingGlyph featureRegionContigTextTrack;
    private Vector<FeatureGlyph> basePairRegionForwardFeatures;
    private Vector<FeatureGlyph> basePairRegionReverseFeatures;

    @Override
    protected void initGlyph() {
        //System.out.println("FeatureView.initGlyph()");
        //System.out.println("FeatureView.initGlyph ENUM SIZE:"+this.options.size());
        //System.out.println("FeatureView.initGlyph options enum->");
        //for (FeatureViewOptions o : this.options ) {
            //System.out.println("FeatureViewOptions.option: " + o.toString());
        //}
        //System.out.println("<- FeatureView.initGlyph options enum");

        featureRegionTracksGlyph = new VerticalCompositeGlyph(getCanvas());
        featureRegionTracksAndFocusGlyph = new CompositeGlyph(getCanvas());
        featureRegionTracksAndFocusGlyph.addGlyph(featureRegionTracksGlyph);
        basePairRegionForwardFeatures = new Vector<FeatureGlyph>();
        basePairRegionReverseFeatures = new Vector<FeatureGlyph>();
        addGlyph(new OverviewGlyph(getCanvas(),
                getSequenceLength(),
                getAccession(), getVersion()));
        addGlyph(new SeparatorGlyph(getCanvas()));
        addGlyph(new VerticalFillerGlyph(getCanvas(), 5));
        initFeatureRegionForwardRulerGlyphGlyph();
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        addGlyph(featureRegionTracksAndFocusGlyph);
        featureRegionTracks = new HashMap<String, TilingGlyph>();

        if (options.contains(SHOW_CONTIG)) {
            featureRegionContigSegmentTrack = initFeatureRegionTilingGlyph("contigSegmentTrack", "Contigs");
            featureRegionContigSegmentTrack.setContiguousGlyphs(true);
            featureRegionContigTextTrack = initFeatureRegionTilingGlyph("contigTextTrack", "");
        }
        if (options.contains(SHOW_ASSEMBLY)) {
            featureRegionAssemblyTrack = initFeatureRegionTilingGlyph("assemblyTrack", "Assembly");
            //assemblyTrack.setAllowRightAlign(false);
        }
        if (options.contains(SHOW_SOURCE)) {
            featureRegionSourceTrack = initFeatureRegionTilingGlyph("sourceTrack", "Source");;
        }
        if (options.contains(SHOW_GENE)) {
            featureRegionGeneTrack = initFeatureRegionTilingGlyph("geneTrack", "Genes");;
        }
        if (options.contains(SHOW_GENE)) {
            locusTagGeneMap = new HashMap<String, Vector<Gene>>();
            geneNameGeneMap = new HashMap<String, Vector<Gene>>();
        }
        for (Feature feature : entry.getFeatures()) {
            Long beginPosition = feature.getLocations().getMinPosition();
            Long endPosition = feature.getLocations().getMaxPosition();
            // DO not remove global complement. The FeatureGlyph is not
            // able to draw segmented features if the segment order is
            // reversed by removeGlobalComplement.
            // Don't do: feature.getLocations().removeGlobalComplement();
            String geneName = feature.getSingleQualifierValue(Qualifier.GENE_QUALIFIER_NAME);
            String locusTag = feature.getSingleQualifierValue(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
            if (options.contains(SHOW_SOURCE)) {
                initFeatureRegionSourceGlyph(feature, geneName, locusTag,
                        beginPosition, endPosition);
            }
            if (options.contains(SHOW_GENE)) {
                addGeneSegment(geneName, locusTag, beginPosition, endPosition);
            }
            if (options.contains(SHOW_FEATURE)) {
                  String trackName = "";
                  if (options.contains(GROUP_GENE) && (geneName != null || locusTag != null) &&
                          !feature.getName().equals("source")) {
                      trackName = getGeneName(geneName, locusTag);
                  }
                  else {
                      trackName = feature.getName();
                  }
                initFeatureGlyph(feature, geneName, locusTag,
                        beginPosition, endPosition, trackName);
            }
        }
        if (options.contains(SHOW_GENE)) {
            for (Vector<Gene> genes : locusTagGeneMap.values()) {
                for (Gene gene : genes) {
                    initFeatureRegionGeneGlyph(gene);
                }
            }
            for (Vector<Gene> genes : geneNameGeneMap.values()) {
                for (Gene gene : genes) {
                    initFeatureRegionGeneGlyph(gene);
                }
            }
        }
        if (options.contains(SHOW_CONTIG)) {
            Long contigPosition = 1L;

            int i = 0;
            for (Location location : entry.getSequence().getContigs()) {
                ++i;
                Color color;
                if (location instanceof Gap) {
                    color = CONTIG_COLOR_GAP;
                }
                else if (i % 2 == 0) {
                    color = CONTIG_COLOR_EVEN;
                }
                else {
                    color = CONTIG_COLOR_ODD;
                }
                contigPosition = initFeatureRegionContigGlyph(location, contigPosition, color);
            }
        }
        int i = 0;
        if (options.contains(SHOW_ASSEMBLY)) {
            for (Assembly assembly : entry.getAssemblies()) {
                ++i;
                Color color;
                if (i % 2 == 0) {
                    color = ASSEMBLY_COLOR_EVEN;
                }
                else {
                    color = ASSEMBLY_COLOR_ODD;
                }
                initFeatureRegionAssemblyGlyph(assembly, color);
            }
        }

        if (options.contains(FeatureViewOptions.SHOW_SEQUENCE)) {
            initFeatureRegionFocusRectGlyph();
            addGlyph(new VerticalSpacerGlyph(getCanvas(), 5));
            addGlyph(new SeparatorGlyph(getCanvas()));
            addGlyph(new VerticalSpacerGlyph(getCanvas(), 5));
            super.initGlyph();
        }
    }

    @Override
    protected void initBasePairRegionForwardFeatureGlyphs() {
        for (FeatureGlyph glyph : basePairRegionForwardFeatures) {
            addGlyph(glyph);
            addGlyph(new VerticalSpacerGlyph(getCanvas()));
        }
    }

    @Override
    protected void initBasePairRegionReverseFeatureGlyphs() {
        for (FeatureGlyph glyph : basePairRegionReverseFeatures) {
            addGlyph(glyph);
            addGlyph(new VerticalSpacerGlyph(getCanvas()));
        }
    }

    private void addGeneSegment(String geneName, String locusTag,
            Long beginPosition, Long endPosition) {
          Vector<Gene> genes = null;
          if (locusTag != null) {
              genes = locusTagGeneMap.get(locusTag);
              if (genes == null) {
                  genes = new Vector<Gene>();
                  locusTagGeneMap.put(locusTag, genes);
              }
          }
          else if (geneName != null) {
              genes = geneNameGeneMap.get(geneName);
              if (genes == null) {
                  genes = new Vector<Gene>();
                  geneNameGeneMap.put(geneName, genes);
              }
          }
          if (genes != null) {
              boolean addGene = true;
            for (Gene gene : genes) {
                if (beginPosition >= gene.getBeginPosition() &&
                    endPosition <= gene.getEndPosition()) {
                    // The new gene is contained within an old one.
//                	System.out.println("DEBUG: The new gene is contained within an old one: " + geneName + ", " + locusTag);
                    addGene = false;
                    break;
                }
                if (gene.getBeginPosition() >= beginPosition &&
                    gene.getEndPosition() <= endPosition ) {
                    // An old gene is contained within the new one.
                    // Adjust old gene to show the new one.
                    gene.adjustBeginPosition(beginPosition);
                    gene.adjustEndPosition(endPosition);
//                    System.out.println("DEBUG: An old gene is contained within the new one: " + geneName + ", " + locusTag);
                    addGene = false;
                    break;
                }
            }
            if (addGene) {
                // Add new gene.
//            	System.out.println("DEBUG: Add new gene: " + geneName + ", " + locusTag);
                Gene gene = new Gene(beginPosition, endPosition, geneName, locusTag);
                genes.add(gene);
            }
          }
    }

    private void initFeatureRegionForwardRulerGlyphGlyph() {
        ForwardRulerGlyph forwardRulerGlyph = new ForwardRulerGlyph(getCanvas());
        forwardRulerGlyph.setText(getCanvas().formatPosition(
                getCanvas().getVisibleFeatureRegionEndPosition() -
                getCanvas().getVisibleFeatureRegionBeginPosition() + 1));
        forwardRulerGlyph.setLabel("Features");
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
        initFeatureRegionScalebarGlyph();
    }

    private TilingGlyph initFeatureRegionTilingGlyph(String trackName, String trackLabel) {
        TilingGlyph track = featureRegionTracks.get(trackName);
          if (track == null) {
              track = new TilingGlyph(getCanvas(),
                      getCanvas().getMainPanelWidth());
              track.setLabel(trackLabel);
              featureRegionTracks.put(trackName, track);
              featureRegionTracksGlyph.addGlyph(track);
          }
          return track;
    }

    private void initFeatureRegionSourceGlyph(Feature feature, String geneName, String locusTag,
            Long beginPosition, Long endPosition) {
        if (!getCanvas().isVisibleFeatureRegionRange(beginPosition, endPosition)) {
            return;
        }
        if (!feature.getName().equals("source")) {
            return;
        }
        FeatureGlyph featureGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
        featureGlyph.setBgColor(SOURCE_COLOR);
        String bottomText = ((SourceFeature)feature).getScientificName();
        featureGlyph.setBottomText(bottomText);
        featureRegionSourceTrack.addGlyph(featureGlyph);
    }

    private void initFeatureGlyph(Feature feature, String geneName, String locusTag,
            Long beginPosition, Long endPosition, String trackName) {
        if (!getCanvas().isVisibleFeatureRegionRange(beginPosition, endPosition)) {
            return;
        }
        // Filter out gene and source features.
        if (feature.getName().equals("gene") ||
            feature.getName().equals("source")) {
            return;
        }
        boolean leftArrow = false;
        boolean rightArrow = false;
        List<Location> locations = feature.getLocations().getLocations();
        if (locations.size() > 0) {
            leftArrow =
               ((locations.get(0).isComplement() && !feature.getLocations().isComplement()) ||
               (!locations.get(0).isComplement() && feature.getLocations().isComplement()));
            rightArrow =
                (!locations.get(locations.size() - 1).isComplement() && !feature.getLocations().isComplement());
        }
        FeatureGlyph featureGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
        featureGlyph.setLocations(feature.getLocations().getLocations());
        featureGlyph.setShowLeftArrow(leftArrow);
        featureGlyph.setShowRightArrow(rightArrow);
        if (feature.getName() == "CDS") {
            featureGlyph.setBgColor(CDS_COLOR);
        }
        else {
            featureGlyph.setBgColor(FEATURE_COLOR);
        }
        String bottomText = getGeneName(geneName, locusTag);
        if (options.contains(GROUP_GENE)) {
            bottomText = feature.getName();
        }
          if (bottomText == null) {
              bottomText = feature.getName();
          }
        featureGlyph.setBottomText(bottomText);
        initFeatureRegionTilingGlyph(feature.getName(), feature.getName()).
            addGlyph(featureGlyph);
    }

    private void initFeatureRegionGeneGlyph(Gene gene) {
        Long beginPosition = gene.getBeginPosition();
        Long endPosition = gene.getEndPosition();
        if (!getCanvas().isVisibleFeatureRegionRange(beginPosition, endPosition)) {
            return;
        }
        FeatureGlyph featureGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
        featureGlyph.setBgColor(GENE_COLOR);
        String bottomText = getGeneName(gene.getGeneName(), gene.getLocusTag());
        featureGlyph.setBottomText(bottomText);
        featureRegionGeneTrack.addGlyph(featureGlyph);
    }

    private Long initFeatureRegionContigGlyph(Location location, Long contigPosition, Color color) {
        Long beginPosition = contigPosition;
        Long endPosition = contigPosition + location.getLength();
        if (!getCanvas().isVisibleFeatureRegionRange(beginPosition, endPosition)) {
            return endPosition;
        }
        String text = "";
        if (location instanceof RemoteLocation) {
            RemoteLocation remoteLocation =(RemoteLocation)location;
            text = remoteLocation.getAccession() + "." +
                    remoteLocation.getVersion();
            if(!location.isComplement()) {
                text += ">";
            }
            else {
                text = "<" + text;
            }
        }
        FeatureGlyph segmentGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
        segmentGlyph.setFeatureHeight(CONTIG_HEIGHT);
        segmentGlyph.setBgColor(color);
        segmentGlyph.setMiddleText(text);
        if (location instanceof RemoteLocation) {
            FeatureGlyph textGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
            textGlyph.setBottomText(text);
            textGlyph.setShowTextOnly(true);
            textGlyph.setShowPreferMiddleText(true);
            textGlyph.setAllowRightAlign(false);
            featureRegionContigTextTrack.addGlyph(textGlyph);
        }
        featureRegionContigSegmentTrack.addGlyph(segmentGlyph);
        return endPosition;
    }

    private void initFeatureRegionAssemblyGlyph(Assembly assembly, Color color) {
        Long beginPosition = assembly.getSecondarySpan().getBeginPosition();
        Long endPosition = assembly.getSecondarySpan().getEndPosition();
        if (!getCanvas().isVisibleFeatureRegionRange(beginPosition, endPosition)) {
            return;
        }
        FeatureGlyph assemblyGlyph = new FeatureGlyph(getCanvas(),
                beginPosition, endPosition);
        assemblyGlyph.setFeatureHeight(FEATURE_REGION_ASSEMBLY_HEIGHT);
        assemblyGlyph.setShowPreferMiddleText(true);
        assemblyGlyph.setBgColor(color);
        String text = "";
        text = assembly.getPrimarySpan().getAccession();
        if (assembly.getPrimarySpan().getVersion() != null) {
        	text += "." + assembly.getPrimarySpan().getVersion(); 
        }
        if(!assembly.getPrimarySpan().isComplement()) {
            text += ">";
        }
        else {
            text = "<" + text;
        }
        assemblyGlyph.setBottomText(text);
        featureRegionAssemblyTrack.addGlyph(assemblyGlyph);
    }

    private void initFeatureRegionFocusRectGlyph() {
        if (!options.contains(FeatureViewOptions.SHOW_FOCUS)) {
            return;
        }
        if (getCanvas().getVisibleBasePairRegionBeginPosition() >=
            getCanvas().getVisibleFeatureRegionEndPosition()) {
                return;
        }
        if (getCanvas().getVisibleBasePairRegionEndPosition() <=
            getCanvas().getVisibleFeatureRegionBeginPosition()) {
                return;
        }
        RectGlyph rectGlyph = new RectGlyph(getCanvas());
        rectGlyph.setColor(FOCUS_COLOR);
        rectGlyph.setFill(false);
        int x = getCanvas().convertFeatureRegionPosition(getCanvas().getVisibleBasePairRegionBeginPosition());
        rectGlyph.setPoint(new GlyphPoint(x, 0));
        rectGlyph.setWidth(getCanvas().convertFeatureRegionPosition(
                getCanvas().getVisibleBasePairRegionEndPosition()) - x);
        rectGlyph.setHeight(featureRegionTracksGlyph.getHeight());
        rectGlyph.setGlyphTranslate(new GlyphTranslate(0, -VerticalSpacerGlyph.DEFAULT_HEIGHT));
        //float[] dashPattern = { 1, 1, 1, 1 };
        //BasicStroke stroke = new BasicStroke(
        //        1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashPattern, 0);
        //rectGlyph.setStroke(stroke);
        featureRegionTracksAndFocusGlyph.addGlyph(rectGlyph);
    }

    private String getGeneName(String geneName, String locusTag) {
           if (locusTag != null && geneName != null) {
          return geneName + "|" + locusTag;
      }
      else if (locusTag != null) {
          return locusTag;
      }
        return geneName;
    }
}
