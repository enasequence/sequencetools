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
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.graphics.ImageComparer;

public class FeatureSummaryViewTest extends TestCase {

    private ImageComparer imageComparer;

	@Before
	public void setUp() throws Exception {
        imageComparer = new ImageComparer();
	}

	private void compareImage(BufferedImage image, String fileName, String expectedChecksums[]) throws Exception {
        // Windows and Mac use slightly different fonts for rendering the image. Platform
        // specific checksums are needed for image comparison. Only one image is kept in
        // the resource directory for visual comparison.
        assertTrue(imageComparer.compareImage(image, fileName, expectedChecksums));
	}
	
	@Test
	public void test() throws Exception {
		/*
	  	EntryFactory entryFactory = new EntryFactory();
		Entry entry = entryFactory.createEntry();
		entry.setPrimaryAccession("ACCESSIONA");
	    entry.setSequence(
	        	(new SequenceFactory()).createSequence(""));		
		entry.getSequence().setLength(300L);
		entry.getSequence().setVersion(1);
		*/
		/*
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONB", 1, 1L, 50L, false, 100L, 150L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONC", 2, 25L, 75L, false, 125L, 175L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIOND", 3, 76L, 125L, false, 176L, 225L));
		*/
		
		/*
		FeatureFactory featureFactory = new FeatureFactory();
		LocationFactory locationFactory = featureFactory.getLocationFactory();

		for (long i = 1; i < 300; ++i ) {	
			Feature feature = featureFactory.createFeature("feature1L1S-TEST");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+1));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}

		for (long i = 1; i < 295; i+=5 ) {	
			Feature feature = featureFactory.createFeature("feature1L5S");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+1));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 280; i+=10 ) {	
			Feature feature = featureFactory.createFeature("feature1L10S");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+1));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}

		for (long i = 1; i < 295; i+=5 ) {	
			Feature feature = featureFactory.createFeature("CDS1L15S");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+1));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}

		
		for (long i = 1; i < 200; i+=5 ) {	
			Feature feature = featureFactory.createFeature("Gene50L5S");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10S");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sb");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sc");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sd");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Se");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		
        
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sf");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		
        
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sg");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		
 
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sh");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Si");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sj");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sk");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sl");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sm");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10Sn");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}
		
		for (long i = 1; i < 200; i+=10 ) {	
			Feature feature = featureFactory.createFeature("Gene50L10So");
			
			Join<Location> join1 = new Join<Location>();
			join1.addLocation(locationFactory.createLocalRange(i, i+50));
			
			feature.setLocations(join1);
			entry.addFeature(feature);
		}		

		Feature feature = featureFactory.createFeature("XXX");
		
		Join<Location> join1 = new Join<Location>();
		join1.addLocation(locationFactory.createLocalRange(1L,300L));		
		feature.setLocations(join1);
		entry.addFeature(feature);		
	
		
        FeatureSummaryView view = new FeatureSummaryView(entry);
        
        
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "FeatureSummary.png", 
     		(new String[] {
     			"7cb85bf707ec995bef8797ce6c5f096b", // Mac
     			""  // Windows
     		}));    
        
        */
	}
}


