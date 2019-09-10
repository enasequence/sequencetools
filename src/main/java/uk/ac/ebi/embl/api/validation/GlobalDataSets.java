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

    public static final HashMap<String, String> gapType= new HashMap<String, String>();
    public static final HashMap<String, String> linkageEvidence= new HashMap<String, String>();
    static
    {

        gapType.put("within scaffold","scaffold");
        gapType.put("between scaffolds","contig");
        gapType.put("between scaffold","contig");
        gapType.put("centromere","centromere");
        gapType.put("short arm","short_arm");
        gapType.put("heterochromatin","heterochromatin");
        gapType.put("telomere","telomere");
        gapType.put("repeat within scaffold","repeat");
        gapType.put("unknown","unknown");
        gapType.put("repeat between scaffolds","repeat");
        gapType.put("contamination","contamination");
        linkageEvidence.put("paired-ends","paired-ends");
        linkageEvidence.put("align genus","align_genus");
        linkageEvidence.put("align xgenus","align_xgenus");
        linkageEvidence.put("align trnscpt","align_trnscpt");
        linkageEvidence.put("within clone","within_clone");
        linkageEvidence.put("clone contig","clone_contig");
        linkageEvidence.put("map","map");
        linkageEvidence.put("strobe","strobe");
        linkageEvidence.put("unspecified","unspecified");
        linkageEvidence.put("pcr","pcr");
        linkageEvidence.put("proximity ligation", "proximity_ligation");
    }

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
                dataSet = dataManager.getDataSet(fileManager.filePath(dataSetName, false));
                add(dataSetName, dataSet);
            }
        }
        return dataSet;
    }

    public static List<DataRow> getRows(String dataSetName) {
        DataSet ds = GlobalDataSets.getDataSet(dataSetName);
        return null == ds? null: ds.getRows();
    }


}
