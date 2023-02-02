package uk.ac.ebi.embl.api.validation.check.entries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER})
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
public class NonUniqueSubmitterAccessionCheck extends EntriesValidationCheck {
    protected final static String SUBMITTER_ACCESSION_NOT_UNIQUE_MESSAGE_ID = "NonUniqueSubmitterAccessionCheck";

    @Override
    public ValidationResult check(ArrayList<Entry> entryList) {
        result = new ValidationResult();
        if (entryList == null) {
            return result;
        }
        Set<String> submitterAccessions = new HashSet<>();
        for (Entry entry : entryList) {
            String submitterAccession = entry.getSubmitterAccession();
            if (submitterAccession == null) {
                continue;
            }
            if (!submitterAccessions.add(submitterAccession)) {
                reportError(entry.getOrigin(), SUBMITTER_ACCESSION_NOT_UNIQUE_MESSAGE_ID, submitterAccession);
            }
        }

        return result;
    }
}
