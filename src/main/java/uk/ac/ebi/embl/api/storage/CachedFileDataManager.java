/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.embl.api.storage.tsv.TSVReader;

public class CachedFileDataManager implements DataManager {

	private Map<String, DataSet> dataSetCache;
	private TSVReader reader = new TSVReader();
	
	public CachedFileDataManager() {
		this.dataSetCache = new HashMap<String, DataSet>();
	}
	
	/**
	 * Retrieves DataSet object (from cache or loads it) for provided file name. 
	 * 
	 * @param fileName a file name
     * @param devMode - if run in development mode the file will be read remotly and not from jar
	 * @return loaded DataSet object
	 */
    public DataSet getDataSet(String fileName, boolean devMode) {
        if (dataSetCache.containsKey(fileName)) {
            return dataSetCache.get(fileName);
        }
        DataSet dataSet = null;
        try {
            if (devMode) {
                dataSet = loadDataSetAsFile(fileName);
            } else {
                dataSet = loadDataSetAsStream(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        dataSetCache.put(fileName, dataSet);
        return dataSet;
    }

    /**
	 * Clears cache.
	 */
	public void clearCache()  {
		this.dataSetCache.clear();
	}
	
	private DataSet loadDataSetAsStream(String fileName) throws IOException {
		return reader.readDataSetAsStream(fileName);
	}

	private DataSet loadDataSetAsFile(String fileName) throws IOException {
		return reader.readDataSetAsFile(fileName);
	}
}

