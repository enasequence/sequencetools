package uk.ac.ebi.embl.api.validation.check.file;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

public class AnnotationOnlyFlatfileValidationCheck extends FileValidationCheck 
{
	public AnnotationOnlyFlatfileValidationCheck(SubmissionOptions options) 
	{
		super(options);
	}

	@Override
	public boolean check(SubmissionFile file) throws ValidationEngineException 
	{
		
		return false;
	}

	@Override
	public boolean check() throws ValidationEngineException 
	{
		return false;
	}
	
}
