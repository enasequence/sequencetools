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
package uk.ac.ebi.embl.template;

public class PolySample {
  String sequenceAcc;
  String submittedId;
  String SampleId;
  long frequency;

  public PolySample(String sequenceAcc, String accession, String sampleId, long frequency) {
    this.sequenceAcc = sequenceAcc;
    this.submittedId = accession;
    this.SampleId = sampleId;
    this.frequency = frequency;
  }

  public PolySample(String accession, String sampleId, long frequency) {
    this.sequenceAcc = null;
    this.submittedId = accession;
    this.SampleId = sampleId;
    this.frequency = frequency;
  }

  public String getSequenceAcc() {
    return sequenceAcc;
  }

  public void setSequenceAcc(String sequenceAcc) {
    this.sequenceAcc = sequenceAcc;
  }

  public String getSubmittedId() {
    return submittedId;
  }

  public void setSubmittedId(String submittedId) {
    this.submittedId = submittedId;
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
