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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class CompoundLocation<E extends Location> extends AbstractLocation {

  private static final long serialVersionUID = -4629919604475171150L;

  private List<E> locations;

  private boolean leftPartial;
  private boolean rightPartial;

  protected CompoundLocation() {
    this.locations = new ArrayList<E>();
  }

  protected CompoundLocation(boolean leftPartial, boolean rightPartial) {
    this();
    this.leftPartial = leftPartial;
    this.rightPartial = rightPartial;
  }

  /**
   * Returns the list of locations.
   *
   * @return the list of locations.
   */
  public List<E> getLocations() {
    return Collections.unmodifiableList(this.locations);
  }

  public boolean addLocation(E location) {
    return this.locations.add(location);
  }

  public boolean addLocations(Collection<E> locations) {
    if (locations == null) {
      return false;
    }
    return this.locations.addAll(locations);
  }

  public boolean removeLocation(E location) {
    return this.locations.remove(location);
  }

  public boolean isLeftPartial() {
    return leftPartial;
  }

  public void setLeftPartial(boolean leftPartial) {
    this.leftPartial = leftPartial;
  }

  public boolean isRightPartial() {
    return rightPartial;
  }

  public void setRightPartial(boolean rightPartial) {
    this.rightPartial = rightPartial;
  }

  @Override
  public long getLength() {
    long length = 0;
    for (Location location : locations) {
      if (location instanceof Base) {
        length += 1;
        continue;
      }
      length += location.getLength();
    }
    return length;
  }

  /**
   * Returns the sequence position relative to the compound location.
   *
   * @param position the local sequence position.
   * @return the relative position.
   */
  public Long getRelativePosition(Long position) {
    long relativePosition = 0L;
    for (Location location : locations) {
      if (location instanceof RemoteLocation) {
        relativePosition += location.getLength();
      } else {
        if (position < location.getBeginPosition() || position > location.getEndPosition()) {
          relativePosition += location.getLength();
        } else {
          if (location.isComplement()) {
            relativePosition += (location.getEndPosition() - position + 1);
          } else {
            relativePosition += (position - location.getBeginPosition() + 1);
          }
          if (isComplement()) {
            relativePosition = getLength() - relativePosition + 1;
          }
          return relativePosition;
        }
      }
    }
    return null;
  }

  public Integer getRelativeIntPosition(Long position) {
    Long relativeLocation = getRelativePosition(position);
    if (relativeLocation != null) {
      return relativeLocation.intValue();
    }
    return null;
  }

  public Long getRelativeBeginPosition(Location location) {
    long relativePosition = 0L;
    for (Location thisLocation : locations) {
      if (!thisLocation.equals(location)) {
        relativePosition += thisLocation.getLength();
      } else {
        return relativePosition + 1;
      }
    }
    return null;
  }

  public Long getRelativeEndPosition(Location location) {
    long relativePosition = 0L;
    for (Location thisLocation : locations) {
      if (!thisLocation.equals(location)) {
        relativePosition += thisLocation.getLength();
      } else {
        return relativePosition + thisLocation.getLength();
      }
    }
    return null;
  }

  public Long getMinPosition() {
    long minPosition = Long.MAX_VALUE;
    for (Location location : getLocations()) {
      if (!(location instanceof RemoteLocation)) {
        minPosition = Math.min(minPosition, location.getBeginPosition());
      }
    }
    if (minPosition == Long.MAX_VALUE) {
      return null;
    }
    return minPosition;
  }

  public Long getMaxPosition() {
    long maxPosition = Long.MIN_VALUE;
    for (Location location : getLocations()) {
      if (!(location instanceof RemoteLocation)) {
        maxPosition = Math.max(maxPosition, location.getEndPosition());
      }
    }
    if (maxPosition == Long.MIN_VALUE) {
      return null;
    }
    return maxPosition;
  }

  public void removeGlobalComplement() {
    if (!isComplement()) {
      return;
    }
    this.setComplement(false);
    java.util.Collections.reverse(locations);
    for (Location location : getLocations()) {
      location.setComplement(!location.isComplement());
    }
  }

  public void setGlobalComplement() {
    if (isComplement()) {
      return;
    }
    this.setComplement(true);
    java.util.Collections.reverse(locations);
    for (Location location : getLocations()) {
      location.setComplement(!location.isComplement());
    }
  }

  public boolean hasRemoteLocation() {
    for (Location location : getLocations()) {
      if (location instanceof RemoteLocation) return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    if (locations.size() > 0) {
      builder.append(this.locations.get(0).hashCode());
    } else {
      builder.append(0);
    }
    return builder.toHashCode();
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("locations", locations);
    return builder.toString();
  }

  public CompoundLocation<Location> getSortedLocations() {

    Collections.sort(
        this.locations,
        (location1, location2) ->
            (location1.getBeginPosition() < location2.getBeginPosition())
                ? -1
                : (location1.getBeginPosition() == location2.getBeginPosition()
                        && location1.getEndPosition() < location2.getEndPosition())
                    ? -1
                    : 1);
    return (CompoundLocation<Location>) this;
  }

  public boolean hasOverlappingLocation() {
    for (Location location1 : getLocations()) {
      for (Location location2 : getLocations()) {
        if (location1.overlaps(location2) && !location1.equals(location2)) {
          return true;
        }
      }
    }
    return false;
  }
}
