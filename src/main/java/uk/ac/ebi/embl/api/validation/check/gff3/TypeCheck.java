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
package uk.ac.ebi.embl.api.validation.check.gff3;

import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.AminoAcidFactory;
import uk.ac.ebi.embl.api.gff3.GFF3Record;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Invalid amino acid {0} in translation.")
public class TypeCheck extends GFF3ValidationCheck {

    //todo add the sofa terms to a tsv and populate
    private final static String MESSAGE_ID = "TypeCheck";

    public ValidationResult check(GFF3RecordSet gff3RecordSet) {
        result = new ValidationResult();

        if(gff3RecordSet == null){
            return result;
        }        

        for (GFF3Record gff3Record : gff3RecordSet.getRecords()) {
            if (gff3Record.getType() != null) {
                if (gff3Record.getType().equals("david")) {
                    reportError(gff3Record.getOrigin(), MESSAGE_ID);
                }
            }
        }

        return result;
    }
}
