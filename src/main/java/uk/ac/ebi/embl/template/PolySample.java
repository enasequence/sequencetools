/*
 * Copyright 2019-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.template;

public class PolySample {
  String assignedAcc;
  String submittedAccession;
  String SampleId;
  long frequency;

  public PolySample(String assignedAcc, String accession, String sampleId, long frequency) {
    this.assignedAcc = assignedAcc;
    this.submittedAccession = accession;
    this.SampleId = sampleId;
    this.frequency = frequency;
  }

  public PolySample(String accession, String sampleId, long frequency) {
    this.assignedAcc = null;
    this.submittedAccession = accession;
    this.SampleId = sampleId;
    this.frequency = frequency;
  }


  public String getAssignedAcc() {
    return assignedAcc;
  }

  public void setAssignedAcc(String assignedAcc) {
    this.assignedAcc = assignedAcc;
  }

  public String getSubmittedAccession() {
    return submittedAccession;
  }

  public void setSubmittedAccession(String submittedAccession) {
    this.submittedAccession = submittedAccession;
  }

  public String getSampleId() {
    return SampleId;
  }

  public void setSampleId(String sampleId) {
    SampleId = sampleId;
  }

  public long getFrequency() {
    return frequency;
  }

  public void setFrequency(long frequency) {
    this.frequency = frequency;
  }
}
