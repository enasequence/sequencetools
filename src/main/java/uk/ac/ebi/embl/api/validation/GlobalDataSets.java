package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.storage.CachedFileDataManager;
import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class GlobalDataSets {

  private static final EnumMap<GlobalDataSetFile, DataSet> dataSets =
      new EnumMap<>(GlobalDataSetFile.class);
  private static final EnumMap<GlobalDataSetFile, DataSet> testDataSets =
      new EnumMap<>(GlobalDataSetFile.class);

  public static final HashMap<String, String> gapType = new HashMap<>();
  public static final HashMap<String, String> linkageEvidence = new HashMap<>();

  static {
    final DataManager dataManager = new CachedFileDataManager();
    final CheckFileManager fileManager = new CheckFileManager();
    for (GlobalDataSetFile file : GlobalDataSetFile.values()) {
      dataSets.put(file, dataManager.getDataSet(fileManager.filePath(file.getFileName(), false)));
    }

    gapType.put("within scaffold", "scaffold");
    gapType.put("between scaffolds", "contig");
    gapType.put("between scaffold", "contig");
    gapType.put("centromere", "centromere");
    gapType.put("short arm", "short_arm");
    gapType.put("heterochromatin", "heterochromatin");
    gapType.put("telomere", "telomere");
    gapType.put("repeat within scaffold", "repeat");
    gapType.put("unknown", "unknown");
    gapType.put("repeat between scaffolds", "repeat");
    gapType.put("contamination", "contamination");
    linkageEvidence.put("paired-ends", "paired-ends");
    linkageEvidence.put("align genus", "align_genus");
    linkageEvidence.put("align xgenus", "align_xgenus");
    linkageEvidence.put("align trnscpt", "align_trnscpt");
    linkageEvidence.put("within clone", "within_clone");
    linkageEvidence.put("clone contig", "clone_contig");
    linkageEvidence.put("map", "map");
    linkageEvidence.put("strobe", "strobe");
    linkageEvidence.put("unspecified", "unspecified");
    linkageEvidence.put("pcr", "pcr");
    linkageEvidence.put("proximity ligation", "proximity_ligation");
  }

  private GlobalDataSets() {}

  public static DataSet getDataSet(GlobalDataSetFile file) {
    if (file == null) {
      return null;
    }
    if (!testDataSets.isEmpty() && testDataSets.containsKey(file)) {
      return testDataSets.get(file);
    }
    return dataSets.get(file);
  }

  public static List<DataRow> getRows(GlobalDataSetFile file) {
    if (file == null) {
      return null;
    }
    return getDataSet(file).getRows();
  }

  /**
   * For unit testing that needs to modify the dataset to test certain scenarios.
   *
   * @param file
   * @param dataRows
   */
  public synchronized static void addTestDataSet(GlobalDataSetFile file, DataRow... dataRows) {
    DataSet dataSet = new DataSet();
    Stream.of(dataRows).forEach(dataSet::addRow);
    testDataSets.put(file, dataSet);
  }

  /**
   * For unit testing that needs to clear out changes made by other tests.
   */
  public synchronized static void resetTestDataSets() {
    testDataSets.clear();
  }
}
