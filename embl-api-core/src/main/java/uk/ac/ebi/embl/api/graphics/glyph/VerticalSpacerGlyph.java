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

public class VerticalSpacerGlyph extends SimpleGlyph {
    public VerticalSpacerGlyph(Canvas canvas) {
    	super(canvas);
    }

    public VerticalSpacerGlyph(Canvas canvas, int height) {
    	super(canvas);
    	this.height = height;
    }
    
    public static final int DEFAULT_HEIGHT = 2;
    private int height = DEFAULT_HEIGHT;

    @Override
    protected void drawSimpleGlyph(Graphics2D g) {
    }
    
    @Override
	public int getHeight() {
    	return height;
    }

    @Override
	public int getWidth() {
        return 0;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
