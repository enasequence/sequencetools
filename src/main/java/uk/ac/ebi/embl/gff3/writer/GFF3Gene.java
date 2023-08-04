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
package uk.ac.ebi.embl.gff3.writer;

public class GFF3Gene {

  private Long beginPosition;
  private Long endPosition;
  private String geneName;
  private String locusTag;

  public GFF3Gene(Long beginPosition, Long endPosition, String geneName, String locusTag) {
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
    this.geneName = geneName;
    this.locusTag = locusTag;
  }

  public void adjustBeginPosition(Long position) {
    this.beginPosition = Math.min(beginPosition, position);
  }

  public void adjustEndPosition(Long position) {
    this.endPosition = Math.max(endPosition, position);
  }

  public Long getBeginPosition() {
    return beginPosition;
  }

  public Long getEndPosition() {
    return endPosition;
  }

  public String getGeneName() {
    return geneName;
  }

  public String getLocusTag() {
    return locusTag;
  }
}
