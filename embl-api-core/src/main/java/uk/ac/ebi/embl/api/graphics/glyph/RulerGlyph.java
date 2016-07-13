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

import java.awt.Color;

public abstract class RulerGlyph extends CompositeGlyph {
    public RulerGlyph(Canvas canvas) {
        super(canvas);
        setColor(Color.black);
    }

    private String text;
    
    protected int arrowHeight = 6;
    protected int arrowWidth = 14;
    protected int indent = 25;
    protected int simplifiedColumnCount = 27;
    protected int moreSimplifiedColumnCount = 13;
 
    @Override
	public int getHeight() {
        TextGlyph textGlyph = new TextGlyph(getCanvas());
        textGlyph.setFont(Glyph.DEFAULT_FONT);
        int textHeight = textGlyph.getHeight();
        return arrowHeight / 2 + textHeight;
    }

    @Override
	public int getWidth() {
        return getCanvas().getVisibleColumnCount() * 
               getCanvas().getColumnWidth();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
