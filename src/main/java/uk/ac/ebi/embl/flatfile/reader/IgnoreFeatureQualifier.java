package uk.ac.ebi.embl.flatfile.reader;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

import java.util.Arrays;
import java.util.List;

public class IgnoreFeatureQualifier {

    private static final List<IgnoreFeatureQualifier> IGNORE_QUALIFIERS = Arrays.asList(
            new IgnoreFeatureQualifier(Feature.REPEAT_REGION, Qualifier.LOCUS_TAG_QUALIFIER_NAME, false),
            new IgnoreFeatureQualifier(Feature.SOURCE_FEATURE_NAME, Qualifier.COUNTRY_QUALIFIER_NAME,true),
            new IgnoreFeatureQualifier(Feature.SOURCE_FEATURE_NAME, Qualifier.COLLECTION_DATE_QUALIFIER_NAME,true),
            new IgnoreFeatureQualifier(Feature.SOURCE_FEATURE_NAME, Qualifier.LAT_LON_QUALIFIER_NAME,true)
    );
    public static final List<String> MISSING_VALUE_TERMS = Arrays.asList(
            "missing: control sample",
            "missing: data agreement established pre-2023",
            "missing: endangered species",
            "missing: human-identifiable",
            "missing: lab stock",
            "missing: sample group",
            "missing: synthetic construct",
            "missing: third party data",
            "not applicable",
            "not collected",
            "not provided",
            "restricted access");

    private final String featureName;
    private final String qualifierName;
    private final boolean ignoreMissingValueTerms;

    IgnoreFeatureQualifier(String featureName, String qualifierName, boolean ignoreMissingValueTerms){
        this.featureName = featureName;
        this.qualifierName = qualifierName;
        this.ignoreMissingValueTerms = ignoreMissingValueTerms;
    }

    /** Returns true if the qualifier should be ignored. Ignoring a qualifier during parsing
     * avoids first creating the qualifier and having to later remove it.
     * @return true if the qualifier should be ignored.
     */
    public static boolean isIgnore(String featureName, String qualifierName, String qualifierValue) {
        for (IgnoreFeatureQualifier ignoreQualifier : IGNORE_QUALIFIERS) {
            if (featureName.equals(ignoreQualifier.featureName) &&
                    qualifierName.equals(ignoreQualifier.qualifierName)) {
                // Qualifier is a candidate for being ignored.
                if (ignoreQualifier.ignoreMissingValueTerms) {
                    // Ignore qualifier if the qualifier value matches one of missing value terms.
                    return ignoreQualifier.MISSING_VALUE_TERMS.contains(qualifierValue);
                } else {
                    // Ignore qualifier regardless of its value.
                    return true;
                }
            }
        }
        return false;
    }
}