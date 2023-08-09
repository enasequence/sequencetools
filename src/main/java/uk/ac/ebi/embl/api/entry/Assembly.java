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
package uk.ac.ebi.embl.api.entry;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public class Assembly implements HasOrigin, Serializable {

  private static final long serialVersionUID = 9137163580451901145L;

  private String id;
  private Origin origin;
  private RemoteRange primarySpan;
  private LocalRange secondarySpan;

  protected Assembly() {}

  protected Assembly(RemoteRange primarySpan, LocalRange secondarySpan) {
    this.primarySpan = primarySpan;
    this.secondarySpan = secondarySpan;
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public String getId() {
    return id;
  }

  public void setId(Object id) {
    if (id != null) {
      this.id = id.toString();
    } else {
      this.id = null;
    }
  }

  public RemoteRange getPrimarySpan() {
    return primarySpan;
  }

  public void setPrimarySpan(RemoteRange remoteRange) {
    this.primarySpan = remoteRange;
  }

  public LocalRange getSecondarySpan() {
    return secondarySpan;
  }

  public void setSecondarySpan(LocalRange localRange) {
    this.secondarySpan = localRange;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.primarySpan);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Assembly) {
      final Assembly other = (Assembly) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.primarySpan, other.primarySpan);
      builder.append(this.secondarySpan, other.secondarySpan);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("primarySpan", primarySpan);
    builder.append("secondarySpan", secondarySpan);
    return builder.toString();
  }
}
