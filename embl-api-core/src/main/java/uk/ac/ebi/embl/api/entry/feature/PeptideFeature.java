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
package uk.ac.ebi.embl.api.entry.feature;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 04-Oct-2010
 * Time: 16:33:35
 * To change this template use File | Settings | File Templates.
 */
public class PeptideFeature extends CdsFeature {

    /**
     * Used to distinguish peptide features from CDS features, despite the fact they have the same functionality. The
     * Translator class will deal with CdsFeatures and Peptide features differently.
     * @param featureName
     * @param join
     */
    public PeptideFeature(String featureName, boolean join) {
        super(featureName, join);
    }
}
