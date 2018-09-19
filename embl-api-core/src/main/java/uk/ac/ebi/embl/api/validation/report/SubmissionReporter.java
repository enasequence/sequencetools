package uk.ac.ebi.embl.api.validation.report;

import java.io.File;

import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public interface SubmissionReporter {
    void writeToFile(File reportFile, ValidationPlanResult validationPlanResult, String targetOrigin);
    void writeToFile(File reportFile, ValidationPlanResult validationPlanResult);
    void writeToFile(File reportFile, ValidationResult validationResult, String targetOrigin );
    void writeToFile(File reportFile, ValidationResult validationResult );
    void writeToFile(File reportFile, ValidationMessage validationMessage );
    void writeToFile(File reportFile, Severity severity, String message, Origin origin );
    void writeToFile(File reportFile, Severity severity, String message );
}
