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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

@Description("Invalid Master entry \"{0}\", not exists in database.")
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class MasterEntryExistsCheck extends EntryValidationCheck {

    private final static String MASTER_EXISTS_MESSAGE_ID = "MasterEntryExistsCheck_1";
    private final static String MASTER_BIOSAMPLE_EXISTS_MESSAGE_ID = "MasterEntryExistsCheck_2";

    public ValidationResult check(Entry entry) throws ValidationEngineException {

        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        if (getEmblEntryValidationPlanProperty().analysis_id.get() == null) {
            return result;
        }

        try {
            EntryDAOUtils entryDAOUtils = getEntryDAOUtils();
            if (entryDAOUtils != null) {
                if(!entryDAOUtils.isEntryExists(getEmblEntryValidationPlanProperty().analysis_id.get())) {
                    reportError(entry.getOrigin(), MASTER_EXISTS_MESSAGE_ID, getEmblEntryValidationPlanProperty().analysis_id.get());
                }
            }

        } catch (SQLException e) {
            throw new ValidationEngineException(e);
        }

        if (entry.getBiosampleId() == null) {
            reportError(entry.getOrigin(), MASTER_BIOSAMPLE_EXISTS_MESSAGE_ID, getEmblEntryValidationPlanProperty().analysis_id.get());
        }

        return result;
    }

}
