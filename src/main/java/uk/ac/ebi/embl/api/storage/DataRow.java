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
package uk.ac.ebi.embl.api.storage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class DataRow {

  public static final String ARRAY_SEPARATOR = ",";

  private final Object[] values;

  /** used in the tsv files to represent comma characters - are replaced upon parsing */
  private static final String COMMA_TOKEN = "{COM}";

  public DataRow() {
    this.values = new Object[0];
  }

  public DataRow(Object... values) {
    this.values = values;
  }

  public DataRow(String... values) {
    this.values = values;
  }

  public boolean hasColumn(int columnIndex) {
    return values.length >= (columnIndex + 1);
  }

  public Object getColumn(int columnIndex) {
    return values[columnIndex];
  }

  public String getString(int columnIndex) {
    return (String) getColumn(columnIndex);
  }

  public String getStringDefault(int columnIndex, String defValue) {
    if (columnIndex < values.length) {
      return (String) getColumn(columnIndex);
    } else {
      return defValue;
    }
  }

  public String[] getStringArray(int columnIndex) {
    return getStringArray(columnIndex, ARRAY_SEPARATOR);
  }

  public String[] getStringArray(int columnIndex, String separator) {
    String value = getString(columnIndex);
    String[] result = StringUtils.split(value, ARRAY_SEPARATOR);
    for (int i = 0; i < result.length; i++) {
      result[i] = result[i].replace(COMMA_TOKEN, ",");
    }
    return result;
  }

  @Override
  public String toString() {
    return ArrayUtils.toString(values);
  }

  public int getLength() {
    return values.length;
  }
}
