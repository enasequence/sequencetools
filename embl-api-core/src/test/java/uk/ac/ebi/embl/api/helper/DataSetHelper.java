package uk.ac.ebi.embl.api.helper;

import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;

import java.util.stream.Stream;

public class DataSetHelper {

    public static void createAndAdd(String dataSetName, DataRow... dataRows) {
        DataSet ds = new DataSet();
        Stream.of(dataRows).forEach(ds::addRow);
        GlobalDataSets.add(dataSetName, ds);
    }

    public static void clear() {
        GlobalDataSets.clear();
    }
}
