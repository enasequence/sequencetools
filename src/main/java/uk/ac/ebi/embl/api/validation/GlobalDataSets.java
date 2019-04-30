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
    private static DataManager dataManager;
    private static CheckFileManager fileManager;

    private GlobalDataSets(){
    }

    public static void init(DataManager dm, CheckFileManager fm) {
        dataManager = dm;
        fileManager = fm;
    }

    public static boolean isPopulated(String dataSetName) {
        return dataSets.containsKey(dataSetName);
    }

    public static void add(String dataSetName, DataSet dataSet){
        dataSets.put(dataSetName, dataSet);
    }

    public static synchronized void clear() {
       dataSets.clear();
        dataManager = null;
    }

    public static synchronized DataSet getDataSet(String dataSetName) {
        DataSet dataSet = null;
        if (null != dataSetName) {
            if (isPopulated(dataSetName)) {
                dataSet = dataSets.get(dataSetName);
            } else {
                try {
                    dataSet = dataManager.getDataSet(fileManager.filePath(dataSetName, false));
                    add(dataSetName, dataSet);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not load dataset "+dataSetName+" Exception got: "+e.getMessage());
                }
            }
        }
        return dataSet;
    }

    public static List<DataRow> getRows(String dataSetName) {
        DataSet ds = GlobalDataSets.getDataSet(dataSetName);
        return null == ds? null: ds.getRows();
    }


}
