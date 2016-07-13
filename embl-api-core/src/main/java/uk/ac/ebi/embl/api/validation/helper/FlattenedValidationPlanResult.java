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

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;

public class FlattenedValidationPlanResult {

    String fileName;
    int entryCount = 0;
    int failedEntryCount = 0;
    int errorCount = 0;
    int warningInfoCount = 0;
    int fixCount = 0;

    public FlattenedValidationPlanResult(String fileName) {
        this.fileName = fileName;
    }

    public FlattenedValidationPlanResult(ValidationPlanResult planResult) {
        this.fileName = planResult.getTargetOrigin();
        incrementEntryCount();//one plan result is one entry
        int errorCount = planResult.getMessages(Severity.ERROR).size();
        addErrorCount(errorCount);
        if(errorCount > 0){
            incrementFailedEntryCount();
        }
        addWarningInfoCount(planResult.getMessages(Severity.WARNING).size());
        addWarningInfoCount(planResult.getMessages(Severity.INFO).size());
        addFixCount(planResult.getMessages(Severity.FIX).size());
    }

    public void append(ValidationPlanResult planResult) {
        this.fileName = planResult.getTargetOrigin();
        incrementEntryCount();//one plan result is one entry
        int errorCount = planResult.getMessages(Severity.ERROR).size();
        addErrorCount(errorCount);
        if(errorCount > 0){
            incrementFailedEntryCount();
        }
        addWarningInfoCount(planResult.getMessages(Severity.WARNING).size());
        addWarningInfoCount(planResult.getMessages(Severity.INFO).size());
        addFixCount(planResult.getMessages(Severity.FIX).size());
    }

    public String getFileName() {
        return fileName;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningInfoCount() {
        return warningInfoCount;
    }

    public int getFixCount() {
        return fixCount;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int getFailedEntryCount() {
        return failedEntryCount;
    }

    private void addErrorCount(int count){
        errorCount += count;
    }

    private void addWarningInfoCount(int count){
        warningInfoCount += count;
    }

    private void addFixCount(int count){
        fixCount += count;
    }

    private void incrementEntryCount(){
        entryCount += 1;
    }

    private void incrementFailedEntryCount(){
        failedEntryCount += 1;
    }

}
