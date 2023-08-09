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
package uk.ac.ebi.embl.api.entry.location;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.sequence.SequenceAccession;

public class RemoteRange extends Range implements RemoteLocation, Serializable {

  private static final long serialVersionUID = 727472442874683537L;

  private final SequenceAccession sequenceAccession;

  protected RemoteRange(
      String accession, Integer version, Long beginPosition, Long endPosition, boolean complement) {
    super(beginPosition, endPosition, complement);
    this.sequenceAccession = new SequenceAccession(accession, version);
  }

  protected RemoteRange(String accession, Integer version, Long beginPosition, Long endPosition) {
    super(beginPosition, endPosition);
    this.sequenceAccession = new SequenceAccession(accession, version);
  }

  public String getAccession() {
    return sequenceAccession.getAccession();
  }

  public Integer getVersion() {
    return sequenceAccession.getVersion();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof RemoteRange) {
      final RemoteRange other = (RemoteRange) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.getBeginPosition(), other.getBeginPosition());
      builder.append(this.getEndPosition(), other.getEndPosition());
      builder.append(this.isComplement(), other.isComplement());
      builder.append(this.getAccession(), other.getAccession());
      builder.append(this.getVersion(), other.getVersion());
      return builder.isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.getBeginPosition());
    builder.append(this.getEndPosition());
    builder.append(this.isComplement());
    builder.append(this.getAccession());
    builder.append(this.getVersion());
    return builder.toHashCode();
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("beginPosition", getBeginPosition());
    builder.append("endPosition", getEndPosition());
    builder.append("complement", isComplement());
    builder.append("accession", getAccession());
    builder.append("version", getVersion());
    return builder.toString();
  }

  @Override
  public void setAccession(String accession) {
    sequenceAccession.setAccession(accession);
  }

  @Override
  public void setVersion(int version) {
    sequenceAccession.setVersion(version);
  }
}
