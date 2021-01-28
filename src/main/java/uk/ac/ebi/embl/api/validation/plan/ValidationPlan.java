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

import uk.ac.ebi.embl.api.storage.CachedFileDataManager;
import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;

import java.lang.reflect.InvocationTargetException;

/**
 * This class is intended for implementation of validation execution plan. 
 * It provides basic execution method but the order of execution should be 
 * provided by a concrete implementation.
 * 
 * @author dlorenc
 *
 */
public abstract class ValidationPlan {

	protected ValidationPlanResult validationPlanResult;
	protected ValidationScope validationScope;
	private DataManager dataManager;
	private CheckFileManager fileManager;
    private boolean devMode = false;
    private FileType fileType = null;
    private boolean remote= false;
   protected EmblEntryValidationPlanProperty planProperty;
   protected EntryDAOUtils entryDAOUtils;
   protected EraproDAOUtils eraproDAOUtils;
    public ValidationPlan(EmblEntryValidationPlanProperty property)
	{
    	this(property.validationScope.get(),property.isDevMode.get());
    	this.planProperty=property;
    	this.planProperty.taxonHelper.set(new TaxonHelperImpl());
    	this.remote= property.isRemote.get();
    
	}
    /**
     *
     * @param validationScope - the validation scope
     * @param devMode - true if the validator is being run in development mode (remote tsv files for editing)
     */
    public ValidationPlan(ValidationScope validationScope,
                          boolean devMode) {//DELETE this constructor if there are references

		this.validationScope = validationScope;
		this.dataManager = new CachedFileDataManager();
		this.fileManager = new CheckFileManager();
        this.devMode = devMode;
		GlobalDataSets.init(dataManager, fileManager);
    }

    public void addMessageBundle(String bundleName){
        ValidationMessageManager.addBundle(bundleName);
    }

	/**
	 * 
	 * 
	 * @param target
	 * @return
	 * @throws ValidationEngineException
	 */
	public abstract ValidationPlanResult execute(Object target)
			throws ValidationEngineException;

	protected ValidationPlanResult execute(EmblEntryValidationCheck<?>[] checks, Object target)
			throws ValidationEngineException {
		ValidationPlanResult result = new ValidationPlanResult();
		for (EmblEntryValidationCheck<?> check : checks) {
			result.append(execute(check, target));
		}
		return result;
	}

	/**
	 * Executes a validation check. 
	 * 
	 * @param check a validation check to be executed
	 * @param target target object to be checked
	 * @return a validation result
	 * @throws ValidationEngineException
	 */
	@SuppressWarnings("unchecked")
	public ValidationPlanResult execute(ValidationCheck check, Object target) throws ValidationEngineException {
		
		
		if (check == null)
		{
			return validationPlanResult;
		}
		try
		{
		 check.setEmblEntryValidationPlanProperty(planProperty);
		 if(planProperty.enproConnection.get()!=null&&entryDAOUtils==null)
		 {
			 entryDAOUtils=new EntryDAOUtilsImpl(planProperty.enproConnection.get(),true);
		 }
		 check.setEntryDAOUtils(entryDAOUtils);
		 if(planProperty.eraproConnection.get()!=null&&eraproDAOUtils==null)
		 {
			 eraproDAOUtils = new EraproDAOUtilsImpl(planProperty.eraproConnection.get());
		 }
		 
		check.setEraproDAOUtils(eraproDAOUtils);
		}catch(Exception e)
		{
			throw new ValidationEngineException(e);
		}
		//long start= System.currentTimeMillis();
		Class<? extends ValidationCheck> checkClass = check.getClass();
		ExcludeScope excludeScopeAnnotation = checkClass.getAnnotation(ExcludeScope.class);
		RemoteExclude remoteExclude = checkClass.getAnnotation(RemoteExclude.class);
		Description descAnnotation = checkClass.getAnnotation(Description.class);
		GroupIncludeScope groupIncludeAnnotation = checkClass.getAnnotation(GroupIncludeScope.class);

		if(remoteExclude!=null&&remote)
		{
			return validationPlanResult;
		}
        if (excludeScopeAnnotation != null && isInValidationScope(excludeScopeAnnotation.validationScope())) {
			return validationPlanResult;
		}
        
        if(groupIncludeAnnotation!=null && !isInValidationScopeGroup(groupIncludeAnnotation.group()))
        {
        	return validationPlanResult;
        }


        // inject data sets
        /*if(null != checkDataSetAnnotation) {
			Stream.of(checkDataSetAnnotation.dataSetNames()).forEach( dsName -> GlobalDataSets.loadIfNotExist(dsName, dataManager, fileManager, devMode));
        }
*/
        validationPlanResult.append(check.check(target));

        if (excludeScopeAnnotation != null) {
            demoteSeverity(validationPlanResult, excludeScopeAnnotation.maxSeverity());
        }
        if(groupIncludeAnnotation!=null)
        {
        	demoteSeverity(validationPlanResult, groupIncludeAnnotation.maxSeverity());
        }

//        System.out.println(this.result.count());
        
        return validationPlanResult;
	}
	
	/**
	 * Demotes of severity to a specified level (maxSeverity) for all messages.
	 * 
	 * @param planResult a validation result
	 * @param maxSeverity a maximum severity
	 */
	protected void demoteSeverity(ValidationPlanResult planResult,
			Severity maxSeverity) {
		if (Severity.ERROR.equals(maxSeverity)) {
			return;
		}
		for (ValidationMessage<?> message : planResult.getMessages()) {
			switch (message.getSeverity()) {
			case ERROR:
				message.setSeverity(maxSeverity);
				break;
			case WARNING:
				message.setSeverity(maxSeverity);
				break;
			}
		}
	}

	protected boolean isInValidationScope(ValidationScope[] validationScopes) {
		if (validationScopes == null) {
			return false;
		}
		for (ValidationScope scope : validationScopes) {
			if (scope == null) {
				continue;
			}
			if (scope.equals(validationScope))
				return true;
		}
		return false;
	}
	
	protected boolean isInValidationScopeGroup(ValidationScope.Group[] validationScopeGroups) {
		if (validationScopeGroups == null) {
			return false;
		}
		for (ValidationScope.Group groupScope : validationScopeGroups) {
			if (groupScope == null) {
				continue;
			}
			if (groupScope.equals(validationScope.group()))
				return true;
		}
		return false;
	}
   
}
