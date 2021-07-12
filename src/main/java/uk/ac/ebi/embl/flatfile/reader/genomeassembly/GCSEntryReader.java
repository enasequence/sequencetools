package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.File;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public abstract class GCSEntryReader implements FlatFileReader<Object> 
{
	ValidationResult validationResult =new ValidationResult();
	File file=null;

	public GCSEntryReader() {
        ValidationMessageManager.addBundle(FlatFileValidations.GENOMEASSEMBLY_FLAT_FILE_BUNDLE);

	}
	 protected void error(int lineNumber,String messageKey, Object... params) {
			validationResult.append(FlatFileValidations.message(lineNumber, Severity.ERROR, messageKey, params));
				
	    }

	    protected void warning(int lineNumber,String messageKey, Object... params) {
	    	validationResult.append(FlatFileValidations.message(lineNumber, Severity.WARNING, messageKey, params));
	    }

	protected void fix(int lineNumber,String messageKey, Object... params) {
		validationResult.append(FlatFileValidations.message(lineNumber, Severity.FIX, messageKey, params));
	}

}
