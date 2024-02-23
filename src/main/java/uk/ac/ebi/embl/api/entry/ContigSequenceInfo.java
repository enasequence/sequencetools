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

public class ContigSequenceInfo {
  private String primaryAccession;
  private int sequenceVersion;
  private long sequenceChksum;
  private int sequenceLength;

  public String getPrimaryAccession() {
    return primaryAccession;
  }

  public void setPrimaryAccession(String primaryAccession) {
    this.primaryAccession = primaryAccession;
  }

  public int getSequenceVersion() {
    return sequenceVersion;
  }

  public void setSequenceVersion(int sequenceVersion) {
    this.sequenceVersion = sequenceVersion;
  }

  public long getSequenceChksum() {
    return sequenceChksum;
  }

  public void setSequenceChksum(long sequenceChksum) {
    this.sequenceChksum = sequenceChksum;
  }

  public int getSequenceLength() {
    return sequenceLength;
  }

  public void setSequenceLength(int sequenceLength) {
    this.sequenceLength = sequenceLength;
  }
}
