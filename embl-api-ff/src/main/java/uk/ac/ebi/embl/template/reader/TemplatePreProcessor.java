package uk.ac.ebi.embl.template.reader;

import uk.ac.ebi.embl.api.validation.ValidationResult;

public interface TemplatePreProcessor {
    ValidationResult process(TemplateVariables variablesMap);
}
