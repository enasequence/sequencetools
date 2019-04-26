package uk.ac.ebi.embl.api.validation.submission;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class SubmissionValidator {
	
 SubmissionOptions options;
 
 public SubmissionValidator(SubmissionOptions options) {
	 this.options =options;
 }
 
 public void validate() throws ValidationEngineException
 {
	 new SubmissionValidationPlan(options).execute();
 }
}
