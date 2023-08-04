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

import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;

public class EntryFactory {

  public Entry createEntry() {
    return new Entry();
  }

  public Assembly createAssembly() {
    return new Assembly();
  }

  public Assembly createAssembly(
      String accession,
      Integer version,
      Long primaryBeginPosition,
      Long primaryEndPosition,
      boolean primaryComplement,
      Long secondaryBeginPosition,
      Long secondaryEndPosition) {

    LocationFactory locationFactory = new LocationFactory();
    RemoteRange primarySpan =
        locationFactory.createRemoteRange(
            accession, version, primaryBeginPosition, primaryEndPosition, primaryComplement);
    LocalRange secondarySpan =
        locationFactory.createLocalRange(secondaryBeginPosition, secondaryEndPosition);
    return new Assembly(primarySpan, secondarySpan);
  }

  public XRef createXRef() {
    return new XRef();
  }

  public XRef createXRef(String database, String primaryAccession) {
    return new XRef(database, primaryAccession);
  }

  public XRef createXRef(String database, String primaryAccession, String secondaryAccession) {
    return new XRef(database, primaryAccession, secondaryAccession);
  }
}
