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
    private static boolean devMode;

    private GlobalDataSets(){
    }

    public static void init(DataManager dm, CheckFileManager fm, boolean dev) {
        dataManager = dm;
        fileManager = fm;
        devMode = dev;
    }

    public static boolean isPopulated(String dataSetName) {
        return dataSets.containsKey(dataSetName);
    }

    public static void add(String dataSetName, DataSet dataSet){
        dataSets.put(dataSetName, dataSet);
    }

    public static void clear() {
       dataSets = new HashMap<>();
    }

    public static synchronized DataSet getDataSet(String dataSetName) {
        DataSet dataSet = null;
        if (null != dataSetName) {
            if (isPopulated(dataSetName)) {
                dataSet = dataSets.get(dataSetName);
            } else {
                try {
                    dataSet = dataManager.getDataSet(fileManager.filePath(dataSetName, devMode), devMode);
                    add(dataSetName, dataSet);
                } catch(Exception e) {
                    System.out.println("Could not load dataset "+dataSetName+" Exception got: "+e.getMessage()+"\tDev Mode:"+devMode);
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
