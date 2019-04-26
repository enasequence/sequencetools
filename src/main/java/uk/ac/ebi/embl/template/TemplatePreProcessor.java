package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.validation.ValidationResult;

public interface TemplatePreProcessor {
    ValidationResult process(TemplateVariables variablesMap);
}
