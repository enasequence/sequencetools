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
package uk.ac.ebi.embl.api.validation.plan;

import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.check.gff3.TypeCheck;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 17-Sep-2010
 * Time: 13:12:14
 * To change this template use File | Settings | File Templates.
 */
public class GFF3ValidationPlan extends ValidationPlan {

    private TypeCheck typeCheck = new TypeCheck();

    public GFF3ValidationPlan(EmblEntryValidationPlanProperty planProperty) {
        super(planProperty);
    }

    @Override
    public ValidationPlanResult execute(Object target) throws ValidationEngineException {

        validationPlanResult = new ValidationPlanResult();

        if(target instanceof GFF3RecordSet){
            GFF3RecordSet gff3RecordSet = (GFF3RecordSet) target;
            execute(gff3RecordSet);
        }

        return validationPlanResult;
    }

    public void execute(GFF3RecordSet recordSet) throws ValidationEngineException {
        execute(typeCheck, recordSet);
    }

	
}
