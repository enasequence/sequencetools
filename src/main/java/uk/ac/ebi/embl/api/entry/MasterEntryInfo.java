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
package uk.ac.ebi.embl.api.entry;

import java.sql.Date;

public class MasterEntryInfo {
  private String studyID;
  private String analysisId;
  private int statusId;
  private int taxId;
  private Date hold_date;

  public Date getHold_date() {
    return hold_date;
  }

  public void setHold_date(Date hold_date) {
    this.hold_date = hold_date;
  }

  public String getStudyID() {
    return studyID;
  }

  public void setStudyID(String studyID) {
    this.studyID = studyID;
  }

  public String getAnalysisId() {
    return analysisId;
  }

  public void setAnalysisId(String analysisId) {
    this.analysisId = analysisId;
  }

  public int getStatusId() {
    return statusId;
  }

  public void setStatusId(int statusId) {
    this.statusId = statusId;
  }

  public int getTaxId() {
    return taxId;
  }

  public void setTaxId(int taxId) {
    this.taxId = taxId;
  }
}
