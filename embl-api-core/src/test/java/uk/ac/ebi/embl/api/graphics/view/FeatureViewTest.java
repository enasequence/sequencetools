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
import java.util.EnumSet;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.graphics.ImageComparer;
import uk.ac.ebi.embl.api.graphics.glyph.Canvas;
import uk.ac.ebi.embl.api.translation.TranslationTable;

public class FeatureViewTest extends TestCase {

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
	public void testAsAssemblyLinesWithSequenceVersion() throws Exception {
	  	EntryFactory entryFactory = new EntryFactory();
		Entry entry = entryFactory.createEntry();
		entry.setPrimaryAccession("ACCESSIONA");
	    entry.setSequence(
	        	(new SequenceFactory()).createSequenceByte("agagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagann".getBytes()));		
		//entry.getSequence().setLength(225L);
		entry.getSequence().setVersion(1);
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONB", 1, 1L, 50L, false, 100L, 150L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONC", 2, 25L, 75L, false, 125L, 175L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIOND", 3, 76L, 125L, false, 176L, 225L));
        FeatureView view = new FeatureView(entry, 1L, 225L, 1L, 225L,
        		1L, 225L, TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT,       
        		EnumSet.of(
        			FeatureViewOptions.SHOW_ASSEMBLY,
        			FeatureViewOptions.SHOW_FOCUS
        		));
        
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "FeatureViewTestAsAssemblyLinesWithSequenceVersion.png", 
     		(new String[] {
     			"8ca82834b814a28e0c0c6bd1df1945df", // Mac
     			"1f5612181e19e4073aea94512ba7001c"  // Windows
     		}));       
	}
		
	@Test
	public void testAsAssemblyLinesWithoutSequenceVersion() throws Exception {
	  	EntryFactory entryFactory = new EntryFactory();
		Entry entry = entryFactory.createEntry();
		entry.setPrimaryAccession("ACCESSIONA");
	    entry.setSequence(
	        	(new SequenceFactory()).createSequenceByte("agagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagannnnnnnaagagagagagagann".getBytes()));		
		//entry.getSequence().setLength(225L);
		entry.getSequence().setVersion(1);
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONB", null, 1L, 50L, false, 100L, 150L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIONC", null, 25L, 75L, false, 125L, 175L));
		entry.addAssembly(entryFactory.createAssembly(
				"ACCESSIOND", null, 76L, 125L, false, 176L, 225L));
        FeatureView view = new FeatureView(entry, 1L, 225L, 1L, 225L,
        		1L, 225L, TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT,       
        		EnumSet.of(
        			FeatureViewOptions.SHOW_ASSEMBLY,
        			FeatureViewOptions.SHOW_FOCUS
        		));
        
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "FeatureViewTestAsAssemblyLinesWithoutSequenceVersion.png", 
     		(new String[] {
     			"28e7aa4eb574be8a453c24bbc1cf1020", // Mac
     			"8537b986f7dd84403201de07a241a983"  // Windows
     		}));       
	}	
}
