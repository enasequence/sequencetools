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
package uk.ac.ebi.embl.api.validation.helper;

import uk.ac.ebi.embl.api.validation.ValidationMessage;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 31-Mar-2010
 * Time: 14:40:02
 * To change this template use File | Settings | File Templates.
 */
public class ValidationMessageComparator implements Comparator<ValidationMessage> {

    public int compare(ValidationMessage message1, ValidationMessage message2) {

        Integer severity1 = message1.getSeverity().getIntVal();
        Integer severity2 = message2.getSeverity().getIntVal();

        return severity1.compareTo(severity2);
    }
}
