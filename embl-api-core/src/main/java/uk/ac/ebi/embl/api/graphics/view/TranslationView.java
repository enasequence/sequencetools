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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.location.LocalLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.CodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.CodonStartQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.graphics.glyph.BasePairViewContigGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.Canvas;
import uk.ac.ebi.embl.api.graphics.glyph.HorizontalCompositeGlyph;
import uk.ac.ebi.embl.api.graphics.glyph.TranslationGlyphLegacy;
import uk.ac.ebi.embl.api.graphics.glyph.VerticalSpacerGlyph;

import uk.ac.ebi.embl.api.translation.Codon;
import uk.ac.ebi.embl.api.translation.TranslationTable;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class TranslationView extends View {
	
	public TranslationView(Long visibleBasePairRegionBeginPosition, Long visibleBasePairRegionEndPosition, 
			String sequence, String accession, Integer version,
			CdsFeature feature,	String expectedTranslation, String translation) {
		super(visibleBasePairRegionBeginPosition, visibleBasePairRegionEndPosition, 
				visibleBasePairRegionBeginPosition, visibleBasePairRegionEndPosition, 
				new Long(sequence.length()),  // sequence length
				1L,                           // availableBeginPosition
				new Long(sequence.length()),  // availableEndPosition
				sequence, getTranslationTable(feature), Canvas.DEFAULT_COLUMN_COUNT); 
		this.accession = accession;
		this.version = version;
		this.feature = feature;
		this.expectedTranslation = expectedTranslation;
		this.translation = translation;
		this.startCodon = getStartCodon(feature);
		this.exceptionalTranslation = getExceptionalTranslation(sequence, feature); 
	}

	private static Integer getTranslationTable(CdsFeature feature) {
    	Integer translationTable = null;
    	try {
    		translationTable = feature.getTranslationTable();
    	}
    	catch (ValidationException ex) {
    	}
        if (translationTable == null) {
        	translationTable = TranslationTable.DEFAULT_TRANSLATION_TABLE;
        }        
        return translationTable;
	}
	
	private String accession;
	private Integer version;	
	private CdsFeature feature;
    private String expectedTranslation;
    private String translation;
	private Integer startCodon;
    private String exceptionalTranslation;
        
	private Integer getStartCodon(CdsFeature feature) {
		Integer startCodon = null;
		try {
			startCodon = feature.getStartCodon();
		} catch (ValidationException ex) {}
		if (startCodon == null) {
			return CodonStartQualifier.DEFAULT_CODON_START;
		}
		return startCodon;
	}

	private String getExceptionalTranslation(String sequence, CdsFeature feature) {	
		StringBuilder builder = new StringBuilder();
		boolean isExceptions = false;
		Long maxTranslationLength = (getSequenceLength() - 1) / 3 + 1;
		for (Long i = 0L ; i < maxTranslationLength ; ++i) builder.append(" ");
		// codon exceptions	
		List<CodonQualifier> codonQualifiers = new ArrayList<CodonQualifier>();
			feature.getComplexQualifiers(Qualifier.CODON_QUALIFIER_NAME,
				codonQualifiers);
		for (CodonQualifier qualifier : codonQualifiers ) {
			try { 
				for(int i = startCodon ; i < sequence.length() - 3 ; i += 3) {
					if (sequence.substring(i - 1, i + 2).equals(qualifier.getCodon())) {
						builder.setCharAt((i + 2) / 3 - 1, qualifier.getAminoAcid().getLetter());
					}
					isExceptions = true;
				}
			} catch (ValidationException ex) {
			}
		}		
		// translation exceptions
		List<TranslExceptQualifier> translExceptQualifiers = 
			new ArrayList<TranslExceptQualifier>();	
		feature.getComplexQualifiers(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME,
				translExceptQualifiers);		
		for (TranslExceptQualifier qualifier : translExceptQualifiers ) {
			try {
				int translationExceptionBeginPosition =
					feature.getLocations().getRelativeIntPosition(
						qualifier.getLocation().getBeginPosition());
				int translationExceptionStartCodon = translationExceptionBeginPosition % 3;
	            if (translationExceptionStartCodon == 0) {
	                translationExceptionStartCodon = 3;
	            }
	            if (translationExceptionStartCodon == startCodon) {
	            	// Translation exception is in the correct frame.
	            	builder.setCharAt((translationExceptionBeginPosition + 2) / 3 - 1,
						qualifier.getAminoAcid().getLetter());
	            	isExceptions = true;
	            }
			} catch (ValidationException ex) {}			
		}		
		if (isExceptions) {
			return builder.toString();
		}
		return null;
	}

    @Override
	protected void initGlyph() {
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    	if (expectedTranslation != null) {
    		initTranslationGlyph(expectedTranslation, "Expected");
    	}    	
    	if (translation != null) {
    		initTranslationGlyph(translation, "Translation");
    	}

		if (exceptionalTranslation != null) {
			initTranslationGlyph(exceptionalTranslation, "Exceptions");
		}		
        initBasePairRegionForwardRulerGlyph();       
        initBasePairRegionScalebarGlyph();        
        initBasePairRegionForwardTranslationGlyphs();
        initBasePairRegionForwardSequenceGlyph();      
        initContigGlyphs();        
        initBasePairRegionReverseSequenceGlyph();      
        initBasePairRegionReverseTranslationGlyphs();       
        initBasePairRegionScalebarGlyph();
        initBasePairRegionReverseRulerGlyph();
        initBasePairRegionLegendGlyph();
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }

    private void initTranslationGlyph(String translation, String label) {
    	Integer visibleStartCodon = null;
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
		if (startCodon == 1) {
			if ((visibleBasePairBeginPosition + 2) % 3 == 0) visibleStartCodon = 1;				
			else if ((visibleBasePairBeginPosition + 1) % 3 == 0) visibleStartCodon = 3;				
			else visibleStartCodon = 2;
			translation = translation.substring(
					((visibleBasePairBeginPosition.intValue() + 2) / 3) - 1);
		}
		else if (startCodon == 2) {
			if ((visibleBasePairBeginPosition + 2) % 3 == 0) visibleStartCodon = 2;
			else if ((visibleBasePairBeginPosition + 1) % 3 == 0) visibleStartCodon = 1;				
			else visibleStartCodon = 3;
			translation = translation.substring(
					Math.max(0, ((visibleBasePairBeginPosition.intValue() + 1) / 3) - 1));
		}
		else if (startCodon == 3) {
			if ((visibleBasePairBeginPosition + 2) % 3 == 0) visibleStartCodon = 3;				
			else if ((visibleBasePairBeginPosition + 1) % 3 == 0) visibleStartCodon = 2;				
			else visibleStartCodon = 1;
			translation = translation.substring(
					Math.max(0, (visibleBasePairBeginPosition.intValue() / 3) - 1));
		}
    	Vector<Codon> codons = new  Vector<Codon>();
    	if (visibleBasePairBeginPosition - startCodon < 0) {
    		Codon codon = new Codon();
    		// NO call to setAminoAcid -> no display of amino acid.
    		// No call to setCodon -> no display of alternative start codon.
    		codons.add(codon);
    	}    	
    	for (int i = 0 ; i < translation.length() ; ++i) {
    		Codon codon = new Codon();
    		// No call to setCodon -> no display of alternative start codon.
    		codon.setAminoAcid(translation.charAt(i));
    		codons.add(codon);
    	}    	    	
        TranslationGlyphLegacy translationGlyph = new TranslationGlyphLegacy(getCanvas());
        translationGlyph.setTranslationTable(getTranslationTable());
        translationGlyph.setCodons(codons);
    	translationGlyph.setVisibleStartCodon(visibleStartCodon);
    	translationGlyph.setLabel(label);
    	addGlyph(translationGlyph);                    
        addGlyph(new VerticalSpacerGlyph(getCanvas()));  	
    }
    
    protected void initContigGlyphs() {
    	Long visibleBasePairBeginPosition = getVisibleBasePairRegionBeginPosition();
    	Long visibleBasePairEndPosition = getVisibleBasePairRegionEndPosition();
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
        HorizontalCompositeGlyph horizontalCompositeGlyph = new HorizontalCompositeGlyph(getCanvas());
        addGlyph(horizontalCompositeGlyph);
        for (Location location : feature.getLocations().getLocations()) {
	        int contigBeginPosition = (int)Math.max(visibleBasePairBeginPosition, feature.getLocations().
    			getRelativeBeginPosition(location));
	        int contigEndPosition = (int)Math.min(visibleBasePairEndPosition, feature.getLocations().
	        	getRelativeEndPosition(location));
	        if (contigBeginPosition <= visibleBasePairEndPosition &&
	        	contigEndPosition >= visibleBasePairBeginPosition) {
		        BasePairViewContigGlyph contigGlyph = new BasePairViewContigGlyph(getCanvas());
	        	contigGlyph.setComplement(location.isComplement());
		        if (location instanceof LocalLocation) {
		        	contigGlyph.setText(accession + "." + version);
		        }
		        else {
		        	RemoteLocation remoteLocation = (RemoteLocation)location;
		        	contigGlyph.setText(remoteLocation.getAccession() + 
		        			"." + remoteLocation.getVersion());
		        }
		        contigGlyph.setColumnCount(contigEndPosition - contigBeginPosition + 1);
		        horizontalCompositeGlyph.addGlyph(contigGlyph);
		        horizontalCompositeGlyph.setLabel("");
	        }
        }
        addGlyph(new VerticalSpacerGlyph(getCanvas()));    	
        addGlyph(new VerticalSpacerGlyph(getCanvas()));
    }    
}
