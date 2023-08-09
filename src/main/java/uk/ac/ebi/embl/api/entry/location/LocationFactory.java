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

public class LocationFactory {

  public LocalRange createLocalRange(Long beginPosition, Long endPosition) {
    return new LocalRange(beginPosition, endPosition);
  }

  public LocalRange createLocalRange(Long beginPosition, Long endPosition, boolean complement) {
    return new LocalRange(beginPosition, endPosition, complement);
  }

  public Location createLocalRange(Location location) {
    return new LocalRange(
        location.getBeginPosition(), location.getEndPosition(), location.isComplement());
  }

  public LocalBase createLocalBase(Long position) {
    return new LocalBase(position);
  }

  public LocalBetween createLocalBetween(Long beginPosition, Long endPosition) {
    return new LocalBetween(beginPosition, endPosition);
  }

  public RemoteRange createRemoteRange(
      String accession, Integer version, Long beginPosition, Long endPosition) {
    return new RemoteRange(accession, version, beginPosition, endPosition);
  }

  public RemoteRange createRemoteRange(
      String accession, Integer version, Long beginPosition, Long endPosition, boolean complement) {
    return new RemoteRange(accession, version, beginPosition, endPosition, complement);
  }

  public RemoteBase createRemoteBase(String accession, Integer version, Long position) {
    return new RemoteBase(accession, version, position);
  }

  public RemoteBase createRemoteBase(
      String accession, Integer version, Long position, boolean complement) {
    return new RemoteBase(accession, version, position, complement);
  }

  public RemoteBetween createRemoteBetween(
      String accession, Integer version, Long beginPosition, Long endPosition) {
    return new RemoteBetween(accession, version, beginPosition, endPosition);
  }

  public RemoteBetween createRemoteBetween(
      String accession, Integer version, Long beginPosition, Long endPosition, boolean complement) {
    return new RemoteBetween(accession, version, beginPosition, endPosition, complement);
  }

  public Gap createUnknownGap(long length) {
    return new Gap(length, true);
  }

  public Gap createGap(long length) {
    return new Gap(length, false);
  }
}
