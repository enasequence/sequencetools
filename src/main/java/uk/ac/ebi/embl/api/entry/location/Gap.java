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
package uk.ac.ebi.embl.api.entry.location;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Gap extends Location implements Serializable {

  private static final long serialVersionUID = 1894018497980252594L;

  private static final String SEQUENCE_ELEMENT = "N";
  public static final long DEFAULT_UNKNOWN_LENGTH = 100;

  private long length;
  private boolean unknownLength;

  protected Gap(long length, boolean unknownLength) {

    setGapSecurely(length, unknownLength);
  }

  public long getLength() {
    return length;
  }

  private void setGapSecurely(long length, boolean unknownLength) {
    if (unknownLength && length <= 0) {
      this.length = DEFAULT_UNKNOWN_LENGTH;
    } else this.length = length;

    this.unknownLength = unknownLength;
  }

  public String getSequence() {
    return StringUtils.repeat(SEQUENCE_ELEMENT, (int) length);
  }

  public byte[] getSequenceByte() {
    return StringUtils.repeat(SEQUENCE_ELEMENT.toLowerCase(), (int) length).getBytes();
  }

  public boolean isUnknownLength() {
    return unknownLength;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.length);
    builder.append(this.unknownLength);
    builder.append(this.isComplement());
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Gap) {
      final Gap other = (Gap) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.length, other.length);
      builder.append(this.unknownLength, other.unknownLength);
      builder.append(this.isComplement(), other.isComplement());
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("length", length);
    builder.append("unknownLength", unknownLength);
    return builder.toString();
  }
}
