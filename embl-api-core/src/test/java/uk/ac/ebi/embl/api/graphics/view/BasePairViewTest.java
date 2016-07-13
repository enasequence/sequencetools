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

public class BasePairViewTest extends TestCase {

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
	public void testFormatPosition() {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
			"aaaa".getBytes()
        ));
        BasePairView view = new BasePairView(entry, 1L, 1L, 1L, 1L,
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, null);
        view.getCanvas().formatPosition(1L);
		assertEquals("1 bp", view.getCanvas().formatPosition(1L));
		assertEquals("999 bp", view.getCanvas().formatPosition(999L));
		assertEquals("1,000 bp", view.getCanvas().formatPosition(1000L));
		assertEquals("999,999 bp", view.getCanvas().formatPosition(999999L));
		assertEquals("1,000,000 bp", view.getCanvas().formatPosition(1000000L));
	}
	
	@Test
	public void testGetSequence() {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
			"abcd".getBytes()
        ));
        BasePairView view = new BasePairView(entry, 1L, 1L, 1L, 1L,
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, null);
		assertEquals("abcd", view.getSequence(1L, 4L));
		assertEquals("nabc", view.getSequence(0L, 3L));
		assertEquals("nnabc", view.getSequence(-1L, 3L));
		assertEquals("abcdn", view.getSequence(1L, 5L));
		assertEquals("abcdnn", view.getSequence(1L, 6L));
	}	

	@Test
	public void testStartCodon1ExpandRight() throws Exception {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
			("acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg").getBytes()		
        	));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
        BasePairView view = new BasePairView(entry, 1L, 1L, 1L, 1L,
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, null);
        assertEquals(new Long(1), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(62), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "BasePairViewTestStartCodon1ExpandRight.png", 
     		(new String[] {
     			"1c2de9693aff5486a1bcbf64cea515de", // Mac 
     			"23b3b556c50fa16ad0f31e5bf4d68f9f"  // Windows
     		}));
    }

	@Test
	public void testStartCodon2ExpandRight() throws Exception {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
			("acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg").getBytes()		
        	));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
        BasePairView view = new BasePairView(entry, 2L, 2L, 2L, 2L,
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, null);
        assertEquals(new Long(2), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(63), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "BasePairViewTestStartCodon2ExpandRight.png", 
     		(new String[] {
     			"e8633c9a20a7e1821cf51c8b73f2ca48", // Mac 
     			"e5ba53771a7b1a4ce6a49c583f13c87a"  // Windows
     		}));
    }

	@Test
	public void testStartCodon3ExpandRight() throws Exception {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
			("acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg").getBytes()		
        	));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
        BasePairView view = new BasePairView(entry, 3L, 3L, 3L, 3L,
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, null);
        assertEquals(new Long(3), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(64), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();   
        compareImage(image, "BasePairViewTestStartCodon3ExpandRight.png", 
     		(new String[] {
     			"c4c4c1767d2ba394f4f764c2fdc91dd2", // Mac 
     			"7d957ad8b95bfd947ff98db96afa2fca"  // Windows
     		}));
        
    }
	
	@Test
	public void testNoTranslation() throws Exception {
        Entry entry = (new EntryFactory()).createEntry();
        entry.setSequence(
        	(new SequenceFactory()).createSequenceByte(
        			("acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg" +
        			"acgtaaaacccggttaaccggtcacaagtgcatcgatcg").getBytes()
        	));
        entry.getSequence().setAccession("A0001");
        entry.getSequence().setVersion(1);
        BasePairView view = new BasePairView(entry, 273L, 273L, 273L, 273L, 
        		TranslationTable.DEFAULT_TRANSLATION_TABLE, Canvas.DEFAULT_COLUMN_COUNT, 
        		EnumSet.noneOf(BasePairViewOptions.class));
        				// No AbstractBasePairViewOptions.SHOW_TRANSLATION
        assertEquals(new Long(212), view.getVisibleBasePairRegionBeginPosition());
        assertEquals(new Long(273), view.getVisibleBasePairRegionEndPosition());
        BufferedImage image = view.drawBufferedImage();
        compareImage(image, "BasePairViewTestNoTranslation.png", 
        		(new String[] {
        			"d4c04959bed84076b57df71f3405c179", // Mac 
        			"57c758c90f08aac730ef2b5b9d3f3375"  // Windows
        		}));
    }
}
