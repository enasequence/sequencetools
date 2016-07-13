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

import static org.junit.Assert.*;

import org.junit.Test;

public class CanvasTest {

	@Test
	public void testGetFeatureRegionRangeWidth() {
		Long visibleFeatureRegionBeginPosition = 1L;
		Long visibleFeatureRegionEndPosition = 620L;
		Long visibleBasePairRegionBeginPosition = 1L; 
		Long visibleBasePairRegionEndPosition = 62L;
		Long sequenceLength = 100L; 		
		Canvas canvas = new Canvas(
				visibleFeatureRegionBeginPosition, 
				visibleFeatureRegionEndPosition,
				visibleBasePairRegionBeginPosition, 
				visibleBasePairRegionEndPosition,
				sequenceLength);
		assertEquals(Canvas.COLUMN_WIDTH/10, canvas.getFeatureRegionRangeWidth(1L, 1L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getFeatureRegionRangeWidth(1L, 20L));
		assertEquals(Canvas.COLUMN_WIDTH/10, canvas.getFeatureRegionRangeWidth(5L, 5L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getFeatureRegionRangeWidth(5L, 24L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getFeatureRegionRangeWidth(15L, 24L));
	}

	@Test
	public void testGetBasePairRegionRangeWidth() {
		Long visibleFeatureRegionBeginPosition = 1L;
		Long visibleFeatureRegionEndPosition = 1000L;
		Long visibleBasePairRegionBeginPosition = 1L; 
		Long visibleBasePairRegionEndPosition = 62L;
		Long sequenceLength = 100L; 		
		Canvas canvas = new Canvas(
				visibleFeatureRegionBeginPosition, 
				visibleFeatureRegionEndPosition,
				visibleBasePairRegionBeginPosition, 
				visibleBasePairRegionEndPosition,
				sequenceLength);
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(1L, 1L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(1L, 2L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(5L, 5L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(5L, 6L));
		assertEquals(6*Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(15L, 20L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(61L, 62L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionRangeWidth(62L, 62L));

	}

	@Test
	public void testGetFeatureRegionLocationDistance() {
		Long visibleFeatureRegionBeginPosition = 1L;
		Long visibleFeatureRegionEndPosition = 620L;
		Long visibleBasePairRegionBeginPosition = 1L; 
		Long visibleBasePairRegionEndPosition = 62L;
		Long sequenceLength = 100L; 		
		Canvas canvas = new Canvas(
				visibleFeatureRegionBeginPosition, 
				visibleFeatureRegionEndPosition,
				visibleBasePairRegionBeginPosition, 
				visibleBasePairRegionEndPosition,
				sequenceLength);
		assertEquals(0, canvas.getFeatureRegionLocationDistance(1L, 1L));
		assertEquals(Canvas.COLUMN_WIDTH/10, canvas.getFeatureRegionLocationDistance(1L, 2L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getFeatureRegionLocationDistance(1L, 21L));
		assertEquals(0, canvas.getFeatureRegionLocationDistance(5L, 5L));
		assertEquals(2*Canvas.COLUMN_WIDTH, canvas.getFeatureRegionLocationDistance(5L, 25L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getFeatureRegionLocationDistance(15L, 25L));
	}	
	
	
	@Test
	public void testGetBasePairRegionLocationDistance() {
		Long visibleFeatureRegionBeginPosition = 1L;
		Long visibleFeatureRegionEndPosition = 1000L;
		Long visibleBasePairRegionBeginPosition = 1L; 
		Long visibleBasePairRegionEndPosition = 62L;
		Long sequenceLength = 100L; 		
		Canvas canvas = new Canvas(
				visibleFeatureRegionBeginPosition, 
				visibleFeatureRegionEndPosition,
				visibleBasePairRegionBeginPosition, 
				visibleBasePairRegionEndPosition,
				sequenceLength);
		assertEquals(0, canvas.getBasePairRegionLocationDistance(1L, 1L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionLocationDistance(1L, 2L));
		assertEquals(0, canvas.getBasePairRegionLocationDistance(5L, 5L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionLocationDistance(5L, 6L));
		assertEquals(5*Canvas.COLUMN_WIDTH, canvas.getBasePairRegionLocationDistance(15L, 20L));
		assertEquals(Canvas.COLUMN_WIDTH, canvas.getBasePairRegionLocationDistance(61L, 62L));
		assertEquals(0, canvas.getBasePairRegionLocationDistance(62L, 62L));

	}	

}
