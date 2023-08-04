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
package uk.ac.ebi.embl.api.gff3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 15-Sep-2010 Time: 11:39:46 To change this template
 * use File | Settings | File Templates.
 */
public class GFF3RecordSet {

  public static final String CDS_TYPE = "CDS";

  private List<GFF3Record> records = new ArrayList<GFF3Record>();

  public GFF3RecordSet() {}

  public List<GFF3Record> getRecords() {
    return records;
  }

  public void setRecords(List<GFF3Record> records) {
    this.records.addAll(records);
  }

  public void addRecord(GFF3Record record) {
    this.records.add(record);
  }
}
