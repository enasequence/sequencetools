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

import java.awt.image.BufferedImage;


import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.graphics.ImageComparer;

public class TranslationViewTest extends TestCase {

	private Entry entry;
	private CdsFeature cdsFeature;
	private SourceFeature sourceFeature;
	private EntryFactory entryFactory = new EntryFactory();
	private SequenceFactory sequenceFactory = new SequenceFactory();
	private FeatureFactory featureFactory = new FeatureFactory();
	private LocationFactory locationFactory = new LocationFactory();
    private ImageComparer imageComparer;
	
	@Before
	public void setUp() throws Exception {
		entry = entryFactory.createEntry();
		cdsFeature = featureFactory.createCdsFeature();
		sourceFeature = featureFactory.createSourceFeature();
		entry.addFeature(cdsFeature);
		entry.addFeature(sourceFeature);
        imageComparer = new ImageComparer();
	}

	private void compareImage(BufferedImage image, String fileName, String expectedChecksums[]) throws Exception {
        // Windows and Mac use slightly different fonts for rendering the image. Platform
        // specific checksums are needed for image comparison. Only one image is kept in
        // the resource directory for visual comparison.		
        assertTrue(imageComparer.compareImage(image, fileName, expectedChecksums));
	}


	@Test
	public void testCorrectTranslation1() throws Exception {
    	/*
		5'3' Frame 1: T G T Y V R D R A R Y R V
		5'3' Frame 2: P V R T Y V T A R D I A		
		5'3' Frame 3: R Y V R T Stop P R A I S R		
		3'5' Frame 1: Y A I S R A V T Y V R T G		
		3'5' Frame 2: T R Y R A R S R T Y V P		
		3'5' Frame 3: R D I A R G H V R T Y R
        */
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 39L));
        TranslationView view = new TranslationView(1L, 39L, 
        		View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestCorrectTranslation1.png", 
        		(new String[] {
        				"a9a2fdadcc496169e9b9a57b7f0992b0", // Mac 
        				"1cf1e7bf86292a173c674100a2a5b31c"  // Windows
            		}));       
    }

	@Test
	public void testCorrectTranslation2() throws Exception {
    	/*
		5'3' Frame 1: T G T Y V R D R A R Y R V
		5'3' Frame 2: P V R T Y V T A R D I A		
		5'3' Frame 3: R Y V R T Stop P R A I S R		
		3'5' Frame 1: Y A I S R A V T Y V R T G		
		3'5' Frame 2: T R Y R A R S R T Y V P		
		3'5' Frame 3: R D I A R G H V R T Y R
        */
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 39L));
        TranslationView view = new TranslationView(2L, 39L, 
        		View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestCorrectTranslation2.png", 
        		(new String[] {
            			"a9a2fdadcc496169e9b9a57b7f0992b0", // Mac 
            			"1cf1e7bf86292a173c674100a2a5b31c"  // Windows
            		}));    
    }
	
	@Test
	public void testCorrectTranslation3() throws Exception {
    	/*
		5'3' Frame 1: T G T Y V R D R A R Y R V
		5'3' Frame 2: P V R T Y V T A R D I A		
		5'3' Frame 3: R Y V R T Stop P R A I S R		
		3'5' Frame 1: Y A I S R A V T Y V R T G		
		3'5' Frame 2: T R Y R A R S R T Y V P		
		3'5' Frame 3: R D I A R G H V R T Y R
        */
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 39L));
		TranslationView view = new TranslationView(3L, 39L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
        	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
        	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestCorrectTranslation3.png", 
        		(new String[] {
            			"a9a2fdadcc496169e9b9a57b7f0992b0", // Mac 
            			"1cf1e7bf86292a173c674100a2a5b31c"  // Windows
            		}));
    }
		
	@Test
	public void testShortTranslation1() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 30L));
		TranslationView view = new TranslationView(1L, 30L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        assertEquals(new Long(1), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(30), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestShortTranslation1.png", 
        		(new String[] {
            			"56a2f39cecc28908f52c9356ca413707", // Mac 
            			"09b9464b8354311a97de6e70651dfe1e"  // Windows
            		}));
    }

	@Test
	public void testStartCodon1BeginPositionOne() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accttgacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(1);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");		
		TranslationView view = new TranslationView(1L, 117L,
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon1BeginPositionOne.png", 
        		(new String[] {
            			"208f2cd522bd1ccceeba4e48ee82d888", // Mac 
            			"05debfd3b858d85d70e14858471a07a6"  // Windows
            		}));
    }	
	
	@Test
	public void testStartCodon1BeginPositionTwo() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accttgacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(1);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");	
		TranslationView view = new TranslationView(2L, 117L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon1BeginPositionTwo.png", 
        		(new String[] {
            			"2e6cddc5f35edcb37f7dd3b380a0fa0d", // Mac 
            			"42f570bc2b0ab7cf266adb9901ccc2bf"  // Windows
            		}));
    }	

	@Test
	public void testStartCodon1BeginPositionThree() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accttgacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(1);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");				
		TranslationView view = new TranslationView(3L, 117L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,
				"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon1BeginPositionThree.png", 
        		(new String[] {
            			"1ed491eb9ca0a9cc5781e862de84e167", // Mac 
            			"f535a07797c73d7408000b7f6bf6c119"  // Windows
            		}));
    }	
	
	@Test
	public void testStartCodon2BeginPositionOne() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(2);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");					
		TranslationView view = new TranslationView(1L, 117L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon2BeginPositionOne.png", 
        		(new String[] {
            			"ef4f829a22a7f7e12a7eb700bd18dfd4", // Mac 
            			"4b9a6ec37ebea979f367b1d8b7a376cd"  // Windows
            		}));
    }	
	
	@Test
	public void testStartCodon2BeginPositionTwo() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(2);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");			
		TranslationView view = new TranslationView(2L, 117L,
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon2BeginPositionTwo.png", 
        		(new String[] {
            			"038cf6d8abd619083463cc77fd4c076d", // Mac 
            			"a17980ce35eb6c178e1692532e341d3d"  // Windows
            		}));
    }	

	@Test
	public void testStartCodon2BeginPositionThree() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(2);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");				
		TranslationView view = new TranslationView(3L, 117L,
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
				"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon2BeginPositionThree.png", 
        		(new String[] {
            			"1a39d07515e71520bc48aa5e78eefc55", // Mac 
            			"63717954197f114df9f64ed705bede16"  // Windows
            		}));
    }	
	
	@Test
	public void testStartCodon3BeginPositionOne() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(3);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");				
		TranslationView view = new TranslationView(1L, 117L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon3BeginPositionOne.png", 
        		(new String[] {
            			"9470e2d5e72cf0150806ec5801185457", // Mac 
            			"b239454d8132475ad48b79226717a4ff"  // Windows
            		}));
    }	
	
	@Test
	public void testStartCodon3BeginPositionTwo() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(3);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");			
		TranslationView view = new TranslationView(2L, 117L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon3BeginPositionTwo.png", 
        		(new String[] {
            			"d85a106cdbb01b4a87ed47c8c53de6fa", // Mac 
            			"0937cec023c88438a1923121065a4075"  // Windows
            		})); 
    }	

	@Test
	public void testStartCodon3BeginPositionThree() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgtaaccggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 117L));
		cdsFeature.setStartCodon(3);
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:1..3,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:7..9,aa:Trp)");
		cdsFeature.addQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, 
				"(pos:13..15,aa:TERM)");
		cdsFeature.addQualifier(Qualifier.CODON_QUALIFIER_NAME, 
				"(seq:\"acc\",aa:Trp)");				
		TranslationView view = new TranslationView(3L, 117L,
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,								
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUACONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestStartCodon3BeginPositionThree.png", 
        		(new String[] {
            			"27e497aeb36f4e059ff60f75b0a9c0c9", // Mac 
            			"fc373b712f4d5e2f21c19b18ce36482a"  // Windows
            		}));        
    }		
	
	@Test
	public void testShortTranslation2() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgcgta".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 24L));		
		TranslationView view = new TranslationView(1L, 24L,
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
				"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        assertEquals(new Long(1), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(24), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestShortTranslation2.png", 
        		(new String[] {
            			"708ab8769103251a75556efd139ec2eb", // Mac 
            			"f85537202cd4211edd0a73d88a9595de"  // Windows
            		}));     
    }

	@Test
	public void testShortTranslation3() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtac".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 12L));
		TranslationView view = new TranslationView(1L, 12L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        assertEquals(new Long(1), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(12), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestShortTranslation3.png",
        		(new String[] {
            			"243a43e714455d34ada109840129b340", // Mac 
            			"7edb7466f04b9135891c7d558bc96511"  // Windows
            		}));       
    }

	@Test
	public void testCompositeLocation1() throws Exception {
        entry.setSequence(sequenceFactory.createSequenceByte(
        	"accggtacgtacgtacgtgaccgcgcgcgatatcgc".getBytes()));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
		cdsFeature.getLocations().addLocation(
				locationFactory.createLocalRange(1L, 12L));
		LocalRange localRange2 = (new LocationFactory()).createLocalRange(1L, 24L);
		localRange2.setComplement(true);
		cdsFeature.getLocations().addLocation(localRange2);
		TranslationView view = new TranslationView(1L, 36L, 
				View.getSequence(entry, cdsFeature),  
        		entry.getSequence().getAccession(), entry.getSequence().getVersion(),
        		cdsFeature,				
            	"EXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATIONEXPECTEDTRANSLATION",
            	"CONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUALTRANSLATIONCONCEPTUA");		
        assertEquals(new Long(1), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(36), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "TranslationViewTestCompositeLocation1.png", 
        		(new String[] {
            			"2a0027be6358dcb5c2047e6a3a951206", // Mac 
            			"173be01d35212f18dff35ee37844eddb"  // Windows
            		}));       
    }
}
