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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Vector;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Complementer;
import uk.ac.ebi.embl.api.entry.sequence.ReverseComplementer;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.graphics.glyph.BasePairViewLegendGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.Canvas;
import uk.ac.ebi.embl.api.graphics.glyph.ForwardRulerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.ReverseRulerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.ScaleBarGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.SequenceGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.TranslationGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalSpacerGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.ViewGlyph;
import uk.ac.ebi.embl.api.translation.Codon;
import uk.ac.ebi.embl.api.translation.SimpleTranslator;
import uk.ac.ebi.embl.api.translation.TranslationResult;
import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.ValidationException;

//|---------------|-----------------------------------|-----------------|
//|               |         OVERVIEW_REGION           |                 |
//|---------------|-----------------------------------|-----------------|
//|               |         FEATURE_REGION            |                 |
//|---------------|-----------------------------------|-----------------|
//|               |        BASE_PAIR_REGION           |                 |
//|---------------|-----------------------------------|-----------------|

public abstract class View extends ViewGlyph {
		
	public View(Long visibleFeatureRegionBeginPosition, Long visibleFeatureRegionEndPosition,
			Long visibleBasePairRegionBeginPosition, Long visibleBasePairRegionEndPosition,
			Long sequenceLength, Long availableSequenceBeginPosition, Long availableSequenceEndPosition,
			String sequence, Integer translationTable, int columnCount) {
		super(new Canvas(
				visibleFeatureRegionBeginPosition, 
				visibleFeatureRegionEndPosition,
				visibleBasePairRegionBeginPosition, 
				visibleBasePairRegionEndPosition,
				sequenceLength,
				columnCount));
		this.availableSequenceBeginPosition = availableSequenceBeginPosition;
		this.availableSequenceEndPosition = availableSequenceEndPosition;
		this.sequence = sequence;
		this.translationTable = translationTable;
    	int visibleColumnCount = getCanvas().getColumnCount();    	
    	if (new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue() < visibleColumnCount) {
    		while (visibleBasePairRegionEndPosition.compareTo(sequenceLength) < 0 &&
				new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue() < visibleColumnCount) {
    			++visibleBasePairRegionEndPosition;
    		}
    	}
    	if (new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue() < visibleColumnCount) {
    		while (visibleBasePairRegionBeginPosition > 1 &&
				new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue() < visibleColumnCount) {
    			--visibleBasePairRegionBeginPosition;
    		}
    	}
    	while (new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue() > visibleColumnCount) {
			--visibleBasePairRegionEndPosition;
    	}
    	visibleColumnCount = new Long(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1).intValue();
		getCanvas().setVisibleBasePairRegionBeginPosition(visibleBasePairRegionBeginPosition);    	
		getCanvas().setVisibleBasePairRegionEndPosition(visibleBasePairRegionEndPosition);
		getCanvas().setVisibleColumnCount(visibleColumnCount);		
	}
	
	private Long availableSequenceBeginPosition;
	private Long availableSequenceEndPosition;	
    private String sequence;
    private Integer translationTable;
    private BufferedImage bufferedImage;    
    	        
	public static String getSequence(Entry entry, Feature feature) throws SQLException, IOException {
		return new String((new SegmentFactory()).createSegment(
				entry.getSequence(), feature.getLocations()).getSequenceByte());		
	}
    
	public Long getVisibleFeatureRegionBeginPosition() {
		return getCanvas().getVisibleFeatureRegionBeginPosition();
	}

	public void setVisibleFeatureRegionBeginPosition(Long visibleBeginPosition) {
		getCanvas().setVisibleFeatureRegionBeginPosition(visibleBeginPosition);
	}	
	
	public Long getVisibleFeatureRegionEndPosition() {
		return getCanvas().getVisibleFeatureRegionEndPosition();
	}

	public void setVisibleFeatureRegionEndPosition(Long visibleEndPosition) {
		getCanvas().setVisibleFeatureRegionEndPosition(visibleEndPosition);
	}	

	public Long getVisibleBasePairRegionBeginPosition() {
		return getCanvas().getVisibleBasePairRegionBeginPosition();
	}

	public void setVisibleBasePairRegionBeginPosition(Long visibleBeginPosition) {
		getCanvas().setVisibleBasePairRegionBeginPosition(visibleBeginPosition);
	}	
	
	public Long getVisibleBasePairRegionEndPosition() {
		return getCanvas().getVisibleBasePairRegionEndPosition();
	}

	public void setVisibleBasePairRegionEndPosition(Long visibleEndPosition) {
		getCanvas().setVisibleBasePairRegionEndPosition(visibleEndPosition);
	}	
	
	public Long getSequenceLength() {
		return getCanvas().getSequenceLength();
	}
	
    public Long getAvailableSequenceBeginPosition() {
		return availableSequenceBeginPosition;
	}
    
	public Long getAvailableSequenceEndPosition() {
		return availableSequenceEndPosition;
	}
	
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Integer getTranslationTable() {
		return translationTable;
	}

	public void setTranslationTable(Integer translationTable) {
		this.translationTable = translationTable;
	}
	
	/** Returns true if the position is contained in the available sequence range.
     */		
	public boolean isAvailableSequencePosition(Long position) {
		return !(position.compareTo(availableSequenceEndPosition) > 0 || 
				 position.compareTo(availableSequenceBeginPosition) < 0);
	}		

    /** Returns true if the range is contained in the available sequence range.
     */			
	public boolean isAvailableSequenceRange(Long beginPosition, Long endPosition) {
		return !(beginPosition.compareTo(availableSequenceEndPosition) > 0 || 
				endPosition.compareTo(availableSequenceBeginPosition) < 0);	
	}    

	public BufferedImage drawBufferedImage() {
		if (bufferedImage != null) {
			return bufferedImage;
		}
		BufferedImage bufferedImage = new BufferedImage(
				getCanvas().getWidth(),	getHeight(), 
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		drawGlyph(g);
		bufferedImage.flush() ;
		g.dispose();
		return bufferedImage;
	}
		
	/** Returns a subsequence string and appends n's to the
	 * beginning and end of the sequence if the begin or
	 * end positions are not contained within the available
	 * sequence.
	 * 
	 * @param beginPosition the sequence begin position.
	 * @param endPosition the sequence end position.
	 * @return a subsequence string.
	 */	
	protected String getSequence(Long beginPosition, Long endPosition) {
		if (beginPosition == null || endPosition == null) {
			return null;
		}
		int pos = new Long(beginPosition - getAvailableSequenceBeginPosition()).intValue();
		int end = new Long(endPosition - getAvailableSequenceBeginPosition()).intValue();
		int length = sequence.length();
		StringBuilder builder = new StringBuilder();
		while (pos < 0 && pos <= end) {
			 builder.append("n");
			++pos;
		}
		while (pos < length && pos <= end) {
			builder.append(sequence.substring(pos, pos+1));
			++pos;
		}
		while (pos <= end) {
			 builder.append("n");
			++pos;
		}
		return builder.toString();
	}

	/** Returns a complemented subsequence string and appends n's 
	 * to the beginning and end of the sequence if the begin or
	 * end positions are not contained within the available sequence.
	 * 
	 * @param beginPosition the sequence begin position.
	 * @param endPosition the sequence end position.
	 * @return a complemented subsequence string.
	 */		
	protected String getComplementSequence(Long beginPosition, Long endPosition) {
		return (new Complementer()).complement(getSequence(beginPosition, endPosition));
	}

	protected String getReverseComplementSequence(Long beginPosition, Long endPosition) {
		return (new ReverseComplementer()).reverseComplement(getSequence(beginPosition, endPosition));
	}
		
	protected void initBasePairRegionForwardTranslationGlyph(int visibleStartCodon, String label) {
    	Long translationBeginPosition = null;
    	Long translationEndPosition = null;
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();
    	int firstColumnCount = 0;
    	int lastColumnCount = 0;
		int totalColumnCount = (int)(visibleBasePairEndPosition - visibleBasePairBeginPosition  + 1);    	
    	if (visibleStartCodon == 1) {
			translationBeginPosition = visibleBasePairBeginPosition;
			firstColumnCount = 3;
		}
		else if (visibleStartCodon == 2) {
			translationBeginPosition = visibleBasePairBeginPosition - 2;
			firstColumnCount = 1;
		}
		else if (visibleStartCodon == 3) {
			translationBeginPosition = visibleBasePairBeginPosition - 1;
			firstColumnCount = 2;
		}
		translationEndPosition = translationBeginPosition + 3;		
		for (int column = 0 ; ; translationEndPosition += 3) {
			if (column >= totalColumnCount - 3) {
				lastColumnCount = totalColumnCount - column;    	
				break;
			}
			if (column == 0) {
				column += firstColumnCount;				
			}
			else {
				column += 3;
			}
		}    	
        SimpleTranslator translator = new SimpleTranslator();
        try  {
    		translator.setTranslationTable(translationTable);
        }
        catch (ValidationException ex) {
        	return;
        }
		ExtendedResult<TranslationResult> extendedResult = translator.translate(
				getSequence(translationBeginPosition, translationEndPosition).getBytes());
		TranslationResult translationResult = extendedResult.getExtension();
    	Vector<Codon> codons = translationResult.getCodons();
        TranslationGlyph translationGlyph = new TranslationGlyph(getCanvas(),
        		 translationTable, codons, firstColumnCount, lastColumnCount);        		
    	translationGlyph.setLabel(label);
    	addGlyph(translationGlyph);                        
        addGlyph(new VerticalSpacerGlyph(getCanvas()));        
	}

    protected void initBasePairRegionReverseTranslationGlyph(int visibleStartCodon, String label) {
    	Long translationEndPosition = null;
    	Long translationBeginPosition = null;
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();
    	int firstColumnCount = 0;
    	int lastColumnCount = 0;
		int totalColumnCount = (int)(visibleBasePairEndPosition - visibleBasePairBeginPosition  + 1);
    	
    	// TODO: is the following really correct
		if (visibleStartCodon == 1) {
			translationEndPosition = visibleBasePairEndPosition;
			lastColumnCount = 3;
		}
		else if (visibleStartCodon == 2) {			
			translationEndPosition = visibleBasePairEndPosition + 2;
			lastColumnCount = 1;
		}
		else if (visibleStartCodon == 3) {
			translationEndPosition = visibleBasePairEndPosition + 1;
			lastColumnCount = 2;
		} 
		translationBeginPosition = translationEndPosition - 3;		
		for (int column = 0 ; ; translationBeginPosition -= 3) {
			if (column >= totalColumnCount - 3) {
				firstColumnCount = totalColumnCount - column;    	
				break;
			}
			if (column == 0) {
				column += lastColumnCount;				
			}
			else {
				column += 3;
			}
		}
        SimpleTranslator translator = new SimpleTranslator();
        try {
    		translator.setTranslationTable(translationTable);
        }
        catch (ValidationException ex) {
        	return;
        }
        
		ExtendedResult<TranslationResult> extendedResult = translator.translate(
				getReverseComplementSequence(translationBeginPosition, 
						translationEndPosition).getBytes());
		TranslationResult translationResult = extendedResult.getExtension();
    	Vector<Codon> codons = translationResult.getCodons();
    	Collections.reverse(codons);
        TranslationGlyph translationGlyph = new TranslationGlyph(getCanvas(),
        		 translationTable, codons, firstColumnCount, lastColumnCount);        		
    	translationGlyph.setLabel(label);
    	addGlyph(translationGlyph);                        
        addGlyph(new VerticalSpacerGlyph(getCanvas()));        
	}
        
    protected void initBasePairRegionScalebarGlyph() {  
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();    	
        ScaleBarGlyph scaleBarGlyph = new ScaleBarGlyph(getCanvas(), Region.BASE_PAIR_REGION);
        scaleBarGlyph.setLeftText(getCanvas().formatPosition(visibleBasePairBeginPosition));
        scaleBarGlyph.setRightText(getCanvas().formatPosition(visibleBasePairEndPosition));
        scaleBarGlyph.setLabel("");
        addGlyph(scaleBarGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }
        
    protected ScaleBarGlyph initFeatureRegionScalebarGlyph() {  
    	Long visibleFeatureBeginPosition = getVisibleFeatureRegionBeginPosition();
    	Long visibleFeatureEndPosition = getVisibleFeatureRegionEndPosition();    	
        ScaleBarGlyph scaleBarGlyph = new ScaleBarGlyph(getCanvas(), Region.FEATURE_REGION);
        scaleBarGlyph.setLeftText(getCanvas().formatPosition(visibleFeatureBeginPosition));
        scaleBarGlyph.setRightText(getCanvas().formatPosition(visibleFeatureEndPosition));
        scaleBarGlyph.setLabel("");
        addGlyph(scaleBarGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        return scaleBarGlyph;
    }
    
    protected void initBasePairRegionForwardRulerGlyph() {
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();    	
        ForwardRulerGlyph forwardRulerGlyph = new ForwardRulerGlyph(getCanvas());
        forwardRulerGlyph.setText(getCanvas().formatPosition(visibleBasePairEndPosition - visibleBasePairBeginPosition + 1));
        forwardRulerGlyph.setLabel("Sequence");
        forwardRulerGlyph.setBoldLabel(true);
        addGlyph(forwardRulerGlyph);
    }

    protected void initBasePairRegionForwardTranslationGlyphs() {
        initBasePairRegionForwardTranslationGlyph(3, "");
        initBasePairRegionForwardTranslationGlyph(2, "Amino acids");
        initBasePairRegionForwardTranslationGlyph(1, "");
    }

    protected void initBasePairRegionForwardSequenceGlyph() {
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();    	
        SequenceGlyph sequenceGlyph = new SequenceGlyph(getCanvas());
    	sequenceGlyph.setSequence(
    			getSequence(visibleBasePairBeginPosition, 
    					visibleBasePairEndPosition).toUpperCase());
        sequenceGlyph.setLabel("Forward");
        addGlyph(sequenceGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }

    protected void initBasePairRegionReverseSequenceGlyph() {
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();    	
    	SequenceGlyph sequenceGlyph = new SequenceGlyph(getCanvas());
    	sequenceGlyph.setSequence(
    			(new Complementer()).complement(getSequence(
    					visibleBasePairBeginPosition, 
    					visibleBasePairEndPosition)).toUpperCase());
        sequenceGlyph.setLabel("Reverse");
        addGlyph(sequenceGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }
    
    protected void initBasePairRegionReverseTranslationGlyphs() {  
        initBasePairRegionReverseTranslationGlyph(1, "");
        initBasePairRegionReverseTranslationGlyph(2, "Amino acids");
        initBasePairRegionReverseTranslationGlyph(3, "");
    }

    protected void initBasePairRegionReverseRulerGlyph() {
        addGlyph(new VerticalSpacerGlyph(getCanvas()));    	
       	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();    	    	
        ReverseRulerGlyph reverseRulerGlyph = new ReverseRulerGlyph(getCanvas());
        reverseRulerGlyph.setText(getCanvas().formatPosition(
        		visibleBasePairEndPosition - visibleBasePairBeginPosition + 1));
        addGlyph(reverseRulerGlyph);
    }

    protected void initBasePairRegionLegendGlyph() {
        addGlyph(new VerticalSpacerGlyph(getCanvas(), 10));
        BasePairViewLegendGlyph basePairViewLegendGlyph = new BasePairViewLegendGlyph(getCanvas());
        basePairViewLegendGlyph.setLabel("Legend");
        addGlyph(basePairViewLegendGlyph);
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }	
    
 
}
