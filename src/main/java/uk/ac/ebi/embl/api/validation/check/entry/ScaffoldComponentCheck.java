package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.HashSet;
import java.util.Set;

@Description("Scaffold \"{0}\" has only \"{1}\" component, minimum two components expected.")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_CHROMOSOME, ValidationScope.ASSEMBLY_CONTIG, ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class ScaffoldComponentCheck extends EntryValidationCheck {


        private final static String SCAFFOLD_WITH_ONE_COMPONENT = "ScaffoldWithOneComponentCheck";

        public ValidationResult check(Entry entry) {

            result = new ValidationResult();

            if (entry == null || getEmblEntryValidationPlanProperty().ignore_errors.get() ||
                    entry.getSequence().getAgpRows() == null || entry.getSequence().getAgpRows().isEmpty()) {
                return result;
            }

            if (ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get())) {
                Set<String> components = new HashSet<>();
                for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
                    if (!agpRow.getComponent_type_id().equals("N") && !agpRow.getComponent_type_id().equals("U")) {
                        components.add(agpRow.getComponent_id().toUpperCase());
                    }
                }
                if (components.size() < 2) {
                    reportError(entry.getOrigin(), SCAFFOLD_WITH_ONE_COMPONENT, entry.getSubmitterAccession(), components.size());
                }
            }
            return result;
        }

}
