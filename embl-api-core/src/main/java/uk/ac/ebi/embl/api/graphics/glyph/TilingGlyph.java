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
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import uk.ac.ebi.embl.api.graphics.glyph.TextAlignGlyph.Align;

public class TilingGlyph extends AbstractCompositeGlyph {
    public TilingGlyph(Canvas canvas, int width) {
    	super(canvas);    	
    	this.width = width;
    }
    
    private static final int MAX_ROWS = 10;
    
    private int height;
    private int width;
    private TextGlyph textGlyph;    
    private boolean sortGlyphs = true;    
    private boolean contiguousGlyphs = false;    
    private boolean addGlyphsToEnd = false;
    private int leftGlyphMargin = 2;

    private Vector<HidableGlyph> glyphs = 
    	new Vector<HidableGlyph>();            
    
	public boolean isSortGlyphs() {
		return sortGlyphs;
	}

	public void setSortGlyphs(boolean sortGlyphs) {
		this.sortGlyphs = sortGlyphs;
	}
	
	public boolean isContiguousGlyphs() {
		return contiguousGlyphs;
	}

	public void setContiguousGlyphs(boolean contiguousGlyphs) {
		this.contiguousGlyphs = contiguousGlyphs;
	}

	public boolean isAddGlyphsToEnd() {
		return addGlyphsToEnd;
	}

	public void setAddGlyphsToEnd(boolean addGlyphsToLastRow) {
		this.addGlyphsToEnd = addGlyphsToLastRow;
	}
	
	public int getLeftGlyphMargin() {
		return leftGlyphMargin;
	}

	public void setLeftGlyphMargin(int leftGlyphMargin) {
		this.leftGlyphMargin = leftGlyphMargin;
	}

	@Override
	protected void initGlyph() {
    	if (sortGlyphs) {
        	class GlyphComparator implements Comparator<HidableGlyph>{
        		public int compare(HidableGlyph a, HidableGlyph b){
        			return (new Integer(a.getGlyph().getGlyphTranslate().getX()).
        					compareTo(b.getGlyph().getGlyphTranslate().getX()));
        		}
    		}
    		Collections.sort(glyphs, new GlyphComparator());
    	}
        int rowX[] = new int[MAX_ROWS];
        int rowHeight[] = new int[MAX_ROWS];
        int cumulativeRowHeight[] = new int[MAX_ROWS];        
    	for (int row = 0 ; row < MAX_ROWS ; ++row) {
    		rowX[row] = 0;
    		rowHeight[row] = 0;
            cumulativeRowHeight[row] = 0;
    	}
    	int lastRow = 0;
    	int glyphHeight = 0;
    	for(HidableGlyph hidableGlyph : glyphs) {
    		Glyph glyph = hidableGlyph.getGlyph();
    		glyphHeight = Math.max(glyphHeight, glyph.getHeight());
    	}
    	int hiddenGlyphs = 0;
    	for(HidableGlyph hidableGlyph : glyphs) {
    		Glyph glyph = hidableGlyph.getGlyph();
    		hidableGlyph.setShowGlyph(false);
        	int glyphX = (glyph.getGlyphTranslate() != null) ? glyph.getGlyphTranslate().getX() : 0;
			boolean showGlyph = false;
			int row = (addGlyphsToEnd) ? lastRow : 0;
        	for ( ; row < MAX_ROWS ; ++row) {
    			int leftMargin = ((rowX[row] > 0) ? leftGlyphMargin : 0);
    			if (contiguousGlyphs || (rowX[row] + leftMargin <= glyphX)) {
        			boolean alignTextRight = false;
        			if (contiguousGlyphs) {
        				// Tile glyphs contiguously on a single row. Let the glyphs
        				// overlap.        				
        				showGlyph = true;
        			}    			
					else {    					
	    				// Feature must not overlap with previous features on the row.    				
	        			if (rowX[row] == 0) {
	            			// First feature on the row.        				
	        				if (glyph instanceof TextAlignGlyph) {        					
		        				if (((TextAlignGlyph)glyph).isAllowRightAlign() && glyphX >= glyph.getWidth()) {
		            				// Right align text if it fits on the left hand side of the first feature.	        					
		        					alignTextRight = true;
		        				}
	        				}
	        				showGlyph = true;
	        			}        			
	        			else {
	        				// Other features on the row.        				
	    					if (glyphX + glyph.getWidth() <= getCanvas().getMainPanelWidth() + Canvas.RIGHT_MARGIN) {
	            				// Left aligned text fits on the row.
	    						showGlyph = true;	
	    					}
	    					else if (glyph instanceof TextAlignGlyph) {
		    					if (((TextAlignGlyph)glyph).isAllowRightAlign() && (rowX[row] + leftMargin <= glyphX - glyph.getWidth())) {
		        					// Right aligned text fits on the row.
		    						alignTextRight = true;
		    						showGlyph = true;
		    					}
	    					}
	        			}
					}
        			if (showGlyph) {
        				if (glyph instanceof TextAlignGlyph) {
        					if (alignTextRight) {
        						((TextAlignGlyph)glyph).setAlign(Align.RIGHT);
        					}
        					else {
        						((TextAlignGlyph)glyph).setAlign(Align.LEFT);
        					}
        				}
    					rowX[row] = glyphX + glyph.getWidth();
    					rowHeight[row] = Math.max(rowHeight[row], glyph.getHeight());
    					// Initialise y position to be the row number.
        				int y = row;
        				glyph.getGlyphTranslate().setY(y);
        				hidableGlyph.setShowGlyph(true);
        				lastRow = Math.max(lastRow, row);        				
        				break;
        			}
        		}
        	}
        	if (!showGlyph) {
        		++hiddenGlyphs;
        	}
        }
        height = 0;
    	for (int row = 0 ; row < MAX_ROWS ; ++row) {
    		if (rowX[row] == 0) {
    			break;
    		}
    		height += rowHeight[row];
			height += VerticalSpacerGlyph.DEFAULT_HEIGHT;
    		cumulativeRowHeight[row] = height;
    	}
    	if (hiddenGlyphs > 0) {
			textGlyph = new TextGlyph(getCanvas());
			textGlyph.setText(new Integer(hiddenGlyphs).toString() + " features not displayed");
			height += VerticalSpacerGlyph.DEFAULT_HEIGHT;
			height += textGlyph.getHeight();
    	}
    	// Set y position.
    	for(HidableGlyph hidableGlyph : glyphs) {
    		Glyph glyph = hidableGlyph.getGlyph();
    		if (hidableGlyph.isShowGlyph()) {
				int row = glyph.getGlyphTranslate().getY();
				if (row > 0) {
					glyph.getGlyphTranslate().setY(cumulativeRowHeight[row-1]);
				}    			
    		}
    	}
    }

    @Override
	protected void drawTranslatedGlyph(Graphics2D g) {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}    	
    	if (glyphs.size() > 0 && getLabel() != null) {
    		drawLabel(g, getLabel());
    	}

    	for(HidableGlyph hidableGlyph : glyphs) {
    		Glyph glyph = hidableGlyph.getGlyph();
    		if (!hidableGlyph.isShowGlyph()) {
    			continue;
    		}
			glyph.drawGlyph(g);
		}
		if (textGlyph != null) {
			g.translate(0, height - 
					VerticalSpacerGlyph.DEFAULT_HEIGHT - textGlyph.getHeight());
			(new VerticalSpacerGlyph(getCanvas())).drawGlyph(g); 			
			textGlyph.drawGlyph(g);
			g.translate(0, - height + 
					VerticalSpacerGlyph.DEFAULT_HEIGHT + textGlyph.getHeight());
		}
   }   
    
    @Override
	public int getWidth() {
    	return width;
    }      
    
    @Override
	public int getHeight() {
    	if (!isInit) {
    		initGlyph();
    		isInit = true;
    	}
        if (glyphs.size() > 0 && getLabel() != null) {
        	return Math.max(height,  getLabelHeight());
        }    	
    	return height;
    }
    
    @Override
    public void addGlyph(Glyph glyph) {
    	if (glyph.getGlyphTranslate() == null) {
    		glyph.setGlyphTranslate(new GlyphTranslate(0,0));
    	}
        glyphs.add(new HidableGlyph(glyph));
    }
}
