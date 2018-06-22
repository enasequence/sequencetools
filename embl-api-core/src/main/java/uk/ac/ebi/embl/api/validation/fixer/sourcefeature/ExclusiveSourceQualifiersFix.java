package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.Collection;

@Description("Qualifiers {0} and {1} cannot exist together, qualifier {1} has been removed from source feature.")
public class ExclusiveSourceQualifiersFix extends EntryValidationCheck {


    private final static String FIX_QUALIFIER_REMOVED = "SourceQualifierRemovalFix";

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        if (entry == null) {
            return result;
        }

        for(DataRow dataRow : GlobalDataSets.getDataSet(FileName.SOURCE_EXCLUSIVE_QUALIFIERS).getRows()) {

            String qualifierName1 = dataRow.getString(0);
            String qualifierName2 = dataRow.getString(1);

            Collection<Feature> sources = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
            if (sources == null || sources.isEmpty()) {
                continue;
            }

            for (Feature source : sources) {
                if("environmental_sample".equals(qualifierName1)
                        && SequenceEntryUtils.isQualifierAvailable(qualifierName1, source)
                        && SequenceEntryUtils.isQualifierAvailable(qualifierName2, source)) {
                    source.removeQualifier(qualifierName2);
                    reportMessage(Severity.FIX, entry.getPrimarySourceFeature().getOrigin(), FIX_QUALIFIER_REMOVED, qualifierName1, qualifierName2);
                }
            }
        }
        return result;
    }
}
