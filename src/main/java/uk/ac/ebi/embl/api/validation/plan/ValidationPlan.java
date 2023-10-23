/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.plan;

import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

/**
 * This class is intended for implementation of validation execution plan. It provides basic
 * execution method but the order of execution should be provided by a concrete implementation.
 *
 * @author dlorenc
 */
public abstract class ValidationPlan {

  protected ValidationResult validationResult;
  protected ValidationScope validationScope;
  private boolean remote = false;
  protected EmblEntryValidationPlanProperty planProperty;
  protected EntryDAOUtils entryDAOUtils;
  protected EraproDAOUtils eraproDAOUtils;

  protected ValidationPlan(EmblEntryValidationPlanProperty property) {
    this(property.validationScope.get());
    this.planProperty = property;
    this.planProperty.taxonClient.set(new TaxonomyClient());
    this.remote = property.isRemote.get();
  }
  /**
   * @param validationScope - the validation scope
   */
  protected ValidationPlan(
      ValidationScope validationScope) { // DELETE this constructor if there are referencess
    this.validationScope = validationScope;
  }

  public void addMessageBundle(String bundleName) {
    ValidationMessageManager.addBundle(bundleName);
  }

  /**
   * @param target
   * @return
   * @throws ValidationEngineException
   */
  public abstract ValidationResult execute(Object target) throws ValidationEngineException;

  protected ValidationResult execute(EmblEntryValidationCheck<?>[] checks, Object target)
      throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
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
  public ValidationResult execute(ValidationCheck check, Object target)
      throws ValidationEngineException {

    if (check == null) {
      return validationResult;
    }
    try {
      check.setEmblEntryValidationPlanProperty(planProperty);
      if (planProperty.enproConnection.get() != null && entryDAOUtils == null) {
        entryDAOUtils = EntryDAOUtilsImpl.getEntryDAOUtilsImpl(planProperty.enproConnection.get());
      }
      check.setEntryDAOUtils(entryDAOUtils);
      if (planProperty.eraproConnection.get() != null && eraproDAOUtils == null) {
        eraproDAOUtils =
            new EraproDAOUtilsImpl(
                planProperty.eraproConnection.get(),
                planProperty.webinRestUri.get(),
                planProperty.webinUsername.get(),
                planProperty.webinPassword.get(),
                planProperty.webinAuthUri.get(),
                planProperty.biosamplesUri.get(),
                planProperty.biosamplesWebinUsername.get(),
                planProperty.biosamplesWebinPassword.get());
      }

      check.setEraproDAOUtils(eraproDAOUtils);
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }

    Class<? extends ValidationCheck> checkClass = check.getClass();
    ExcludeScope excludeScopeAnnotation = checkClass.getAnnotation(ExcludeScope.class);
    RemoteExclude remoteExclude = checkClass.getAnnotation(RemoteExclude.class);
    GroupIncludeScope groupIncludeAnnotation = checkClass.getAnnotation(GroupIncludeScope.class);

    if (remoteExclude != null && remote) {
      return validationResult;
    }
    if (excludeScopeAnnotation != null
        && isInValidationScope(excludeScopeAnnotation.validationScope())) {
      return validationResult;
    }

    if (groupIncludeAnnotation != null
        && !isInValidationScopeGroup(groupIncludeAnnotation.group())) {
      return validationResult;
    }

    // inject data sets
    /*if(null != checkDataSetAnnotation) {
    			Stream.of(checkDataSetAnnotation.dataSetNames()).forEach( dsName -> GlobalDataSets.loadIfNotExist(dsName, dataManager, fileManager, devMode));
            }
    */
    validationResult.append(check.check(target));

    if (excludeScopeAnnotation != null) {
      demoteSeverity(validationResult, excludeScopeAnnotation.maxSeverity());
    }
    if (groupIncludeAnnotation != null) {
      demoteSeverity(validationResult, groupIncludeAnnotation.maxSeverity());
    }

    //        System.out.println(this.result.count());

    return validationResult;
  }

  /**
   * Demotes of severity to a specified level (maxSeverity) for all messages.
   *
   * @param planResult a validation result
   * @param maxSeverity a maximum severity
   */
  protected void demoteSeverity(ValidationResult planResult, Severity maxSeverity) {
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
      if (scope.equals(validationScope)) return true;
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
      if (groupScope.equals(validationScope.group())) return true;
    }
    return false;
  }
}
