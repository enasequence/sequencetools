/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataSet {

  private final List<DataRow> dataRows;

  public DataSet() {
    this.dataRows = new ArrayList<DataRow>();
  }

  /**
   * Adds a DataRow object.
   *
   * @param dataRow DataRow object to be added
   */
  public void addRow(DataRow dataRow) {
    this.dataRows.add(dataRow);
  }

  /**
   * Gets a list of data rows.
   *
   * @return a list of data rows
   */
  public List<DataRow> getRows() {
    return Collections.unmodifiableList(dataRows);
  }

  /**
   * Checks whether data set contains a row which values of specified column equals to provided
   * value.
   *
   * @param columnIndex a number of column
   * @param value a value to be compared
   * @return true if data set contains a row with provided value in specified column
   */
  public boolean contains(int columnIndex, Object value) {
    return findRow(columnIndex, value) != null;
  }

  public DataRow findRow(int columnIndex, Object value) {
    for (DataRow dataRow : dataRows) {
      Object columnValue = dataRow.getColumn(columnIndex);
      if (value.equals(columnValue)) {
        return dataRow;
      }
    }
    return null;
  }
  /**
   * checks whether dataset contains the value given (ignoring the case)
   *
   * @param columnIndex
   * @param value
   * @return
   */
  public DataRow findRowIgnoreCase(int columnIndex, String value) {
    for (DataRow dataRow : dataRows) {
      String columnValue = (String) dataRow.getColumn(columnIndex);
      if (value.equalsIgnoreCase(columnValue)) {
        return dataRow;
      }
    }
    return null;
  }
}
