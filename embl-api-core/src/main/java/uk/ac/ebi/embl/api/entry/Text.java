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
package uk.ac.ebi.embl.api.entry;

import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 09-Jan-2009
 * Time: 13:16:58
 * To change this template use File | Settings | File Templates.
 */
public class Text implements HasOrigin {

    private String text;
    private Origin origin;

    public Text(String text) {
        this.text = text;
    }

    public Text() {
    }

    public Text(String string, Origin origin) {
        this.text = string;
        this.origin = origin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public boolean equals(Object obj) {
        if (obj != null && this.text != null) {
            if (obj instanceof Text) {
                return this.text.equals(((Text) obj).getText());
            } else {
                return super.equals(obj);
            }
        }
        return false;
    }
}
