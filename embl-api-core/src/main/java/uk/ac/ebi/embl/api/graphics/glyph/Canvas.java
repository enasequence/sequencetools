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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

// <- LEFT_MARGIN -><--- COLUMN_WIDTH * COLUMN_COUNT --><- RIGHT_MARGIN ->
// |---------------|-----------------------------------|-----------------|
// |  labelPanel   |             mainPanel             |                 |
// |               | | | | | | | | | | | | | | | | | | |                 |
// |               | | | | | | | | | | | | | | | | | | |                 |
// |---------------|-----------------------------------|-----------------|

public class Canvas {

	public Canvas(
			Long visibleFeatureRegionBeginPosition, 
			Long visibleFeatureRegionEndPosition,
			Long visibleBasePairRegionBeginPosition, 
			Long visibleBasePairRegionEndPosition,
			Long sequenceLength) {
	this(visibleFeatureRegionBeginPosition, 
		visibleFeatureRegionEndPosition,
		visibleBasePairRegionBeginPosition, 
		visibleBasePairRegionEndPosition,
		sequenceLength,
		DEFAULT_COLUMN_COUNT);
	}
	
	public Canvas(
			Long visibleFeatureRegionBeginPosition, 
			Long visibleFeatureRegionEndPosition,
			Long visibleBasePairRegionBeginPosition, 
			Long visibleBasePairRegionEndPosition,
			Long sequenceLength,
			int columnCount) {
		this.visibleFeatureRegionBeginPosition = visibleFeatureRegionBeginPosition;
		this.visibleFeatureRegionEndPosition = visibleFeatureRegionEndPosition;		
		this.visibleBasePairRegionBeginPosition = visibleBasePairRegionBeginPosition;
		this.visibleBasePairRegionEndPosition = visibleBasePairRegionEndPosition;
		this.sequenceLength = sequenceLength;
		this.columnCount = columnCount;
		this.visibleColumnCount = columnCount;
	    BufferedImage tempImage = new BufferedImage(
    		1, 1, BufferedImage.TYPE_INT_RGB);
	    this.g = tempImage.createGraphics();
	}	
	
	public enum View {
		FEATURE_VIEW,
		BASEPAIR_VIEW
	}
	
    private Graphics2D g;
	public int LEFT_MARGIN = 80;
    public static final int RIGHT_MARGIN = 80;
    public static final int COLUMN_WIDTH = 10;
    public static final int DEFAULT_COLUMN_COUNT = 62;
    private int columnCount;    
    private int visibleColumnCount;
    private int columnWidth = COLUMN_WIDTH;
    
    private Long visibleFeatureRegionBeginPosition;    
    private Long visibleFeatureRegionEndPosition;
    private Long visibleBasePairRegionBeginPosition;
    private Long visibleBasePairRegionEndPosition;
    private Long sequenceLength;
    
        	
	public Graphics2D getTempGraphics2D() {
		return g;
	}	

    public int getColumnCount() {
    	return columnCount;
    }
    
    public int getVisibleColumnCount() {
		return visibleColumnCount;
	}

	public void setVisibleColumnCount(int visibleColumnCount) {
		this.visibleColumnCount = visibleColumnCount;
	}

    public int getColumnWidth() {
    	return columnWidth;
    }

    public int getWidth() {
    	return getLabelPanelWidth() + getMainPanelWidth() + Canvas.RIGHT_MARGIN;
    } 

    public int getMainPanelWidth() {
    	return columnCount * columnWidth;
    }

    public int getLabelPanelWidth() {
    	return LEFT_MARGIN;
    }
    
	public Long getSequenceLength() {
		return sequenceLength;
	}

	public void setSequenceLength(Long sequenceLength) {
		this.sequenceLength = sequenceLength;
	}

	public Long getVisibleFeatureRegionBeginPosition() {
		return visibleFeatureRegionBeginPosition;
	}

	public void setVisibleFeatureRegionBeginPosition(Long visibleFeatureRegionBeginPosition) {
		this.visibleFeatureRegionBeginPosition = visibleFeatureRegionBeginPosition;
	}

	public Long getVisibleFeatureRegionEndPosition() {
		return visibleFeatureRegionEndPosition;
	}

	public void setVisibleFeatureRegionEndPosition(Long visibleFeatureRegionEndPosition) {
		this.visibleFeatureRegionEndPosition = visibleFeatureRegionEndPosition;
	}

	public Long getVisibleBasePairRegionBeginPosition() {
		return visibleBasePairRegionBeginPosition;
	}

	public void setVisibleBasePairRegionBeginPosition(Long visibleBasePairRegionBeginPosition) {
		this.visibleBasePairRegionBeginPosition = visibleBasePairRegionBeginPosition;
	}

	public Long getVisibleBasePairRegionEndPosition() {
		return visibleBasePairRegionEndPosition;
	}

	public void setVisibleBasePairRegionEndPosition(Long visibleBasePairEndPosition) {
		this.visibleBasePairRegionEndPosition = visibleBasePairEndPosition;
	}

    /** Return true if the position is within the visible feature range.
     */
	public boolean isVisibleFeatureRegionPosition(Long position) {
		return !(position.compareTo(visibleFeatureRegionEndPosition) > 0 || 
				position.compareTo(visibleFeatureRegionBeginPosition) < 0);
	}		

    /** Return true if the position is within the visible base pair range.
     */	
	public boolean isVisibleBasePairRegionPosition(Long position) {
		return !(position.compareTo(visibleBasePairRegionEndPosition) > 0 || 
				position.compareTo(visibleBasePairRegionBeginPosition) < 0);
	}		
		
    /** Return true if the range is within the visible feature range.
     */					
	public boolean isVisibleFeatureRegionRange(Long beginPosition, Long endPosition) {
		return !(beginPosition.compareTo(visibleFeatureRegionEndPosition) > 0 || 
				endPosition.compareTo(visibleFeatureRegionBeginPosition) < 0);		
	}		

    /** Return true if the range is within the visible base pair range.
     */				
	public boolean isVisibleBasePairRegionRange(Long beginPosition, Long endPosition) {
		return !(beginPosition.compareTo(visibleBasePairRegionEndPosition) > 0 || 
				endPosition.compareTo(visibleBasePairRegionBeginPosition) < 0);		
	}		
			
    /** Adjust the position to fit in the in the visible feature range.
     */			
	public Long toVisibleFeatureRegionPosition(Long position) {
		if (position.compareTo(visibleFeatureRegionBeginPosition) < 0) {
			return visibleFeatureRegionBeginPosition;
		}
		if (position.compareTo(visibleFeatureRegionEndPosition) > 0) {
			return visibleFeatureRegionEndPosition;
		}
		return position;
	}

    /** Adjust the position to fit in the in the visible base pair.
     */			
	public Long toVisibleBasePairRegionPosition(Long position) {
		if (position.compareTo(visibleBasePairRegionBeginPosition) < 0) {
			return visibleBasePairRegionBeginPosition;
		}
		if (position.compareTo(visibleBasePairRegionEndPosition) > 0) {
			return visibleBasePairRegionEndPosition;
		}
		return position;
	}

	/** Converts the position into an x coordinate position
	 * in the overview region. 
	 * @param position the base position.
	 * @return the x coordinate position.
	 */
	public int convertOverviewRegionPosition(Long position) {
		return Math.round(((float)(position)) /
    	((float)(sequenceLength)) * getMainPanelWidth());		
	}	  
	
	/** Converts the position into an x coordinate position
	 * in the feature region. 
	 * @param position the base position.
	 * @return the x coordinate position.
	 */
	public int convertFeatureRegionPosition(Long position) {
		// Tested with Excel
		position = toVisibleFeatureRegionPosition(position);
		return Math.round(((float)(position - visibleFeatureRegionBeginPosition)) /
    	((float)(visibleFeatureRegionEndPosition - visibleFeatureRegionBeginPosition + 1)) * getMainPanelWidth());		
	}	

	/** Converts the base pair base position into an x coordinate position
	 * in the base pair region. 
	 * @param position the base position.
	 * @return the x coordinate position.
	 */
	public int convertBasePairRegionPosition(Long position) {
		// Tested with Excel
		position = toVisibleBasePairRegionPosition(position);
		return Math.round(((float)(position - visibleBasePairRegionBeginPosition)) /
    	((float)(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1)) * getMainPanelWidth());		
	}

    /** Returns the range width in X coordinates in the feature region.
     */
	public int getFeatureRegionRangeWidth(Long beginPosition, Long endPosition) {
		// Tested with Excel
		beginPosition = toVisibleFeatureRegionPosition(beginPosition);
		endPosition = toVisibleFeatureRegionPosition(endPosition);
		return Math.max(1,Math.round(
				(((float)(endPosition - visibleFeatureRegionBeginPosition + 1)) /
				((float)(visibleFeatureRegionEndPosition - visibleFeatureRegionBeginPosition + 1)) * getMainPanelWidth()) -		
		    	(((float)(beginPosition - visibleFeatureRegionBeginPosition)) /
		    	((float)(visibleFeatureRegionEndPosition - visibleFeatureRegionBeginPosition + 1)) * getMainPanelWidth())));		
	}

    /** Returns the range width in X coordinates in the base pair region.
     */
	public int getBasePairRegionRangeWidth(Long beginPosition, Long endPosition) {
		// Tested with Excel
		beginPosition = toVisibleBasePairRegionPosition(beginPosition);
		endPosition = toVisibleBasePairRegionPosition(endPosition);
		return Math.round(
				(((float)(endPosition - visibleBasePairRegionBeginPosition + 1)) /
				((float)(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1)) * getMainPanelWidth()) -		
		    	(((float)(beginPosition - visibleBasePairRegionBeginPosition)) /
		    	((float)(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1)) * getMainPanelWidth()));		
	}

    /** Returns the location distance in X coordinates in the feature region.
     */
	public int getFeatureRegionLocationDistance(Long beginPosition, Long endPosition) {
		// Tested with Excel
		beginPosition = toVisibleFeatureRegionPosition(beginPosition);
		endPosition = toVisibleFeatureRegionPosition(endPosition);
		return Math.round(
				(((float)(endPosition - visibleFeatureRegionBeginPosition)) /
				((float)(visibleFeatureRegionEndPosition - visibleFeatureRegionBeginPosition + 1)) * getMainPanelWidth()) -		
		    	(((float)(beginPosition - visibleFeatureRegionBeginPosition)) /
		    	((float)(visibleFeatureRegionEndPosition - visibleFeatureRegionBeginPosition + 1)) * getMainPanelWidth()));		
	}

	/** Returns the location distance in X coordinates in the base pair region.
     */
	public int getBasePairRegionLocationDistance(Long beginPosition, Long endPosition) {
		// Tested with Excel
		beginPosition = toVisibleBasePairRegionPosition(beginPosition);
		endPosition = toVisibleBasePairRegionPosition(endPosition);
		return Math.round(
				(((float)(endPosition - visibleBasePairRegionBeginPosition)) /
				((float)(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1)) * getMainPanelWidth()) -		
		    	(((float)(beginPosition - visibleBasePairRegionBeginPosition)) /
		    	((float)(visibleBasePairRegionEndPosition - visibleBasePairRegionBeginPosition + 1)) * getMainPanelWidth()));		
	}
	
	
    public String formatPosition(long position) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");        
        return decimalFormat.format(position) + " bp";
    }	
}
