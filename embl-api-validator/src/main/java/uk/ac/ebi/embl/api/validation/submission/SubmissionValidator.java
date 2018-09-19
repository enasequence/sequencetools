package uk.ac.ebi.embl.api.validation.submission;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class SubmissionValidator {
	
 SubmissionOptions options;
 public SubmissionValidator(SubmissionOptions options) {
	 this.options =options;
 }
 
 public boolean validate() throws ValidationEngineException
 {
	 SubmissionValidationPlan validationPlan = new SubmissionValidationPlan(options);
	 return validationPlan.execute().isValid();
 }
}
