package uk.ac.ebi.embl.api.validation.check.entry;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@ExcludeScope(validationScope = {ValidationScope.ARRAYEXPRESS, ValidationScope.ASSEMBLY_CHROMOSOME, ValidationScope.ASSEMBLY_CONTIG,
ValidationScope.ASSEMBLY_MASTER, ValidationScope.ASSEMBLY_SCAFFOLD, ValidationScope.ASSEMBLY_TRANSCRIPTOME, ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class DescriptionCheck extends EntryValidationCheck {

    private final static String INVALID_DE_LINE = "templateInvalidDescription";

    @Override
    public ValidationResult check(Entry entry) {

        if (entry == null) {
            return result;
        }

        if( entry.getDescription() == null || StringUtils.isBlank(entry.getDescription().getText())
                || entry.getDescription().getText().length() < 10) {
            reportError(entry.getOrigin(), INVALID_DE_LINE);
        }


        return result;
    }
}
