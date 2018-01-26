package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalDataSets {

    private static Map<String, DataSet> dataSets = new HashMap<>();

    private GlobalDataSets(){}

    public static boolean isPopulated(String dataSetName) {
        return dataSets.containsKey(dataSetName);
    }

    public static boolean isNotPopulated(String dataSetName) {
        return !isPopulated(dataSetName);
    }

    public static void add(String dataSetName, DataSet dataSet){
        dataSets.put(dataSetName, dataSet);
    }

    public static void loadIfNotExist(String dataSetName, DataManager dataManager, CheckFileManager fileManager, boolean devMode ){
        if (null != dataSetName && isNotPopulated(dataSetName)) {
            add(dataSetName, dataManager.getDataSet(fileManager.filePath(dataSetName, devMode), devMode));
        }
    }

    public static void clear() {
       dataSets = new HashMap<>();
    }

    /**
     * @param dataSetName
     * @return DataSet with the dataSetName(tsv file name) if exists, otherwise null
     */
    public static DataSet getDataSet(String dataSetName) {
        return dataSets.get(dataSetName);
    }

    public static List<DataRow> getRows(String dataSetName) {
        DataSet ds = GlobalDataSets.getDataSet(dataSetName);
        return null == ds? null: ds.getRows();
    }


}
