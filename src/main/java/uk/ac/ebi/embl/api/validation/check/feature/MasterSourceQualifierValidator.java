package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

import java.util.HashMap;
import java.util.Map;

public class MasterSourceQualifierValidator {
    private final Map<String,FeatureValidationCheck> validation = new HashMap<>();

    public MasterSourceQualifierValidator() {
        validation.put(Qualifier.COLLECTION_DATE_QUALIFIER_NAME, new CollectionDateQualifierCheck());
    }

    public boolean isValid(String qualifier, String value) {
        try {
            if (validation.get(qualifier) != null) {
                return validation.get(qualifier).isValid(value);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
