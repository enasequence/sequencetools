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

import java.util.EnumSet;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.graphics.glyph.BasePairViewContigGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalSpacerGlyph;
import uk.ac.ebi.embl.api.translation.TranslationTable;

public class BasePairView extends View {

	public BasePairView(
			Long visibleFeatureRegionBeginPosition, Long visibleFeatureRegionEndPosition,
			Long visibleBasePairRegionBeginPosition, Long visibleBasePairRegionEndPosition,			 
			Long sequenceLength, 
			Long availableSequenceBeginPosition, Long availableSequenceEndPosition, 
			String sequence,
			Integer translationTable, String accession, Integer version, 
			int columnCount,
			EnumSet<BasePairViewOptions> options) {
		super(visibleFeatureRegionBeginPosition, visibleFeatureRegionEndPosition, 
				visibleBasePairRegionBeginPosition, visibleBasePairRegionEndPosition,
				sequenceLength, availableSequenceBeginPosition, availableSequenceEndPosition,
				sequence, translationTable, columnCount);
		this.accession = accession;
		this.version = version;
		options = (options != null) ? options :	
			EnumSet.of(BasePairViewOptions.SHOW_TRANSLATION);
		this.options = options;
	}
		
	public BasePairView(Entry entry, 
			Long visibleFeatureBeginPosition, Long visibleFeatureEndPosition,
			Long visibleBasePairBeginPosition, Long visibleBasePairEndPosition, 
			Integer translationTable, int columnCount, EnumSet<BasePairViewOptions> options) {
		super(visibleFeatureBeginPosition, visibleFeatureEndPosition,
				visibleBasePairBeginPosition, visibleBasePairEndPosition,
				entry.getSequence().getLength(),
				1L, // availableSequenceBeginPosition 
				entry.getSequence().getLength(), // availableSequenceEndPosition
				new String(entry.getSequence().getSequenceByte()), 
				translationTable,
				columnCount);
		this.accession = entry.getSequence().getAccession();
		this.version = entry.getSequence().getVersion();
		options = (options != null) ? options :	
			EnumSet.of(BasePairViewOptions.SHOW_TRANSLATION);		
		this.options = options;
		if (translationTable == null) {
			setTranslationTable(TranslationTable.DEFAULT_TRANSLATION_TABLE);
		}
	}
	
	private String accession;
	private Integer version;
    private EnumSet<BasePairViewOptions> options;
		
	@Override
	protected void initGlyph() {
		addGlyph(new VerticalSpacerGlyph(getCanvas()));
        initBasePairRegionForwardRulerGlyph();		
        initBasePairRegionScalebarGlyph();
        initBasePairRegionForwardFeatureGlyphs();
        if (options.contains(BasePairViewOptions.SHOW_TRANSLATION)) {
        	initBasePairRegionForwardTranslationGlyphs();
        }        	
        initBasePairRegionForwardSequenceGlyph();
        initBasePairRegionContigGlyph();
        initBasePairRegionReverseSequenceGlyph();
        if (options.contains(BasePairViewOptions.SHOW_TRANSLATION)) {
        	initBasePairRegionReverseTranslationGlyphs();
        }
    	addGlyph(new VerticalSpacerGlyph(getCanvas()));
    	initBasePairRegionReverseFeatureGlyphs();
        initBasePairRegionScalebarGlyph();
        initBasePairRegionReverseRulerGlyph();
        if (options.contains(BasePairViewOptions.SHOW_TRANSLATION)) {
        	initBasePairRegionLegendGlyph();
        }
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }

		
	protected void initBasePairRegionForwardFeatureGlyphs() {		
	}

	protected void initBasePairRegionReverseFeatureGlyphs() {		
	}

    private void initBasePairRegionContigGlyph() {
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        BasePairViewContigGlyph contigGlyph = new BasePairViewContigGlyph(getCanvas());
        contigGlyph.setText(accession + "." + version);
        contigGlyph.setColumnCount(getCanvas().getColumnCount());
        contigGlyph.setLabel("");
        addGlyph(contigGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));    	
        addGlyph(new VerticalSpacerGlyph(getCanvas()));           	
    }

	public String getAccession() {
		return accession;
	}

	public Integer getVersion() {
		return version;
	}    
}


