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
package uk.ac.ebi.embl.api.entry.sequence;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.ebi.embl.api.entry.location.AbstractLocation;

public class Segment extends AbstractSequence implements Serializable {

  private static final long serialVersionUID = 8220089867381825543L;

  private final AbstractLocation location;
  private final byte[] sequenceByte;

  private final long length;

  protected Segment(AbstractLocation location, byte[] sequence) {
    this.location = location;
    this.sequenceByte = sequence;
    if (sequence != null) {
      this.length = sequenceByte.length;
    } else {
      this.length = 0L;
    }
  }

  public AbstractLocation getLocation() {
    return location;
  }

  @Override
  public long getLength() {
    return length;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.getLocation());
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Segment) {
      final Segment other = (Segment) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.getLocation(), other.getLocation());
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return this.getLocation().toString();
  }

  @Override
  public byte[] getSequenceByte() {
    // TODO Auto-generated method stub
    return sequenceByte;
  }
}
