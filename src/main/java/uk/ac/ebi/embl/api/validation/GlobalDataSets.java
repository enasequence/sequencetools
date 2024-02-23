/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import uk.ac.ebi.embl.api.storage.CachedFileDataManager;
import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;

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
  public static synchronized void addTestDataSet(GlobalDataSetFile file, DataRow... dataRows) {
    DataSet dataSet = new DataSet();
    Stream.of(dataRows).forEach(dataSet::addRow);
    testDataSets.put(file, dataSet);
  }

  /** For unit testing that needs to clear out changes made by other tests. */
  public static synchronized void resetTestDataSets() {
    testDataSets.clear();
  }
}
