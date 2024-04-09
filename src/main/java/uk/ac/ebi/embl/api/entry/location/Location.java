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

public abstract class Location extends AbstractLocation implements Serializable {

  private static final long serialVersionUID = 1768891203971728277L;

  private String id;
  private Long beginPosition;
  private Long endPosition;

  private boolean fivePrimePartial;

  private boolean threePrimePartial;

  protected Location() {}

  protected Location(Long beginPosition, Long endPosition, boolean complement) {
    super(complement);
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
  }

  protected Location(Long beginPosition, Long endPosition) {
    this(beginPosition, endPosition, false);
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

  public long getLength() {
    if (endPosition == null || beginPosition == null) {
      return 0;
    }
    return Math.abs(endPosition - beginPosition) + 1;
  }

  public Long getBeginPosition() {
    return beginPosition;
  }

  public void setBeginPosition(Long beginPosition) {
    this.beginPosition = beginPosition;
  }

  public Long getEndPosition() {
    return endPosition;
  }

  public void setEndPosition(Long endPosition) {
    this.endPosition = endPosition;
  }

  public Integer getIntBeginPosition() {
    if (beginPosition == null) {
      return null;
    }
    return beginPosition.intValue();
  }

  public Integer getIntEndPosition() {
    if (endPosition == null) {
      return null;
    }
    return endPosition.intValue();
  }

  // 6361..6539,6363..6649
  public boolean overlaps(Location location) {
    if (location.getBeginPosition() >= getBeginPosition()
        && location.getBeginPosition() <= getEndPosition()) return true;
    return location.getBeginPosition() <= getBeginPosition()
        && location.getEndPosition() >= getBeginPosition();
  }

  public void setLocationPartiality() {
    if (isComplement() && (isFivePrimePartial() || isThreePrimePartial())) {
      // Swap 3 prime and 5 prime in case of complement
      boolean tempFivePrime = isFivePrimePartial();
      setFivePrimePartial(isThreePrimePartial());
      setThreePrimePartial(tempFivePrime);
    }
  }

  public boolean isFivePrimePartial() {
    return fivePrimePartial;
  }

  public void setFivePrimePartial(boolean fivePrimePartial) {
    this.fivePrimePartial = fivePrimePartial;
  }

  public boolean isThreePrimePartial() {
    return threePrimePartial;
  }

  public void setThreePrimePartial(boolean threePrimePartial) {
    this.threePrimePartial = threePrimePartial;
  }
}
