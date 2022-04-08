package uk.ac.ebi.embl.common;

import uk.ac.ebi.embl.api.entry.feature.Feature;

import java.util.Comparator;

public class ExonIntronSorter implements Comparator<Feature> {

    @Override
    public int compare(Feature f1, Feature f2) {

        Long minPosition = f1.getLocations().getMinPosition();
        Long maxPosition = f1.getLocations().getMaxPosition();

        Long otherMinPosition = f2.getLocations().getMinPosition();
        Long otherMaxPosition = f2.getLocations().getMaxPosition();

        // Order features with smaller minimum positions first.
        if (!minPosition.equals(otherMinPosition)) {
            return minPosition.compareTo(otherMinPosition);
        } else {
            // Order exons smaller maximum positions first.
            return maxPosition.compareTo(otherMaxPosition);    // Smaller maximum position first.
        }
    }
}
