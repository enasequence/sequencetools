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
package uk.ac.ebi.embl.api.entry.genomeassembly;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;

public class ChromosomeEntry extends GCSEntry {

  private String chromosomeName;
  private String chromosomeType;
  private String chromosomeLocation;
  private String objectName;
  private String accession;
  private Sequence.Topology topology;

  private final List<Qualifier> chromosomeQualifeirs = new ArrayList<>();

  public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getChromosomeName() {
    return chromosomeName;
  }

  public void setChromosomeName(String chromosomeName) {
    this.chromosomeName = chromosomeName;
  }

  public String getChromosomeType() {
    return chromosomeType;
  }

  public void setChromosomeType(String chromosomeType) {
    this.chromosomeType = chromosomeType;
  }

  public String getChromosomeLocation() {
    return chromosomeLocation;
  }

  public void setChromosomeLocation(String chromosomeLocation) {
    this.chromosomeLocation = chromosomeLocation;
  }

  public Sequence.Topology getTopology() {
    return topology;
  }

  public void setTopology(Sequence.Topology topology) {
    this.topology = topology;
  }

  public boolean equals(ChromosomeEntry entry) {
    return (entry.getObjectName() != null
            && this.getObjectName() != null
            && this.getObjectName().equals(entry.getObjectName()))
        && (entry.getChromosomeName() != null
            && this.getChromosomeName() != null
            && this.getChromosomeName().equals(entry.getChromosomeName()))
        && (entry.getChromosomeLocation() != null
            && this.getChromosomeLocation() != null
            && this.getChromosomeLocation().equals(entry.getChromosomeLocation()))
        && (entry.getChromosomeType() != null
            && this.getChromosomeType() != null
            && this.getChromosomeType().equals(entry.getChromosomeType()));
  }

  public List<Qualifier> setAndGetQualifiers(boolean virus) {
    // This method has been already called somewhere and calling it twice will add duplicate
    // qualifiers as chromosomeQualifeirs is List.
    // This is the only place we add data into chromosomeQualifeirs, so it is safe to return here
    // Check and changechromosomeQualifeirs to Set if required.
    if (!chromosomeQualifeirs.isEmpty()) {
      return chromosomeQualifeirs;
    }
    if (chromosomeLocation != null
        && !chromosomeLocation.isEmpty()
        && !virus
        && !chromosomeLocation.equalsIgnoreCase("Phage")) {
      String organelleValue = SequenceEntryUtils.getOrganelleValue(chromosomeLocation);
      if (organelleValue != null) {
        chromosomeQualifeirs.add(
            new QualifierFactory()
                .createQualifier(
                    Qualifier.ORGANELLE_QUALIFIER_NAME,
                    SequenceEntryUtils.getOrganelleValue(chromosomeLocation)));
      }
    } else if (chromosomeName != null && !chromosomeName.isEmpty()) {
      if (Qualifier.PLASMID_QUALIFIER_NAME.equals(chromosomeType)) {
        chromosomeQualifeirs.add(
            new QualifierFactory()
                .createQualifier(Qualifier.PLASMID_QUALIFIER_NAME, chromosomeName));
      } else if (Qualifier.CHROMOSOME_QUALIFIER_NAME.equals(chromosomeType)) {
        chromosomeQualifeirs.add(
            new QualifierFactory()
                .createQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, chromosomeName));
      } else if ("segmented".equals(chromosomeType) || "multipartite".equals(chromosomeType)) {
        chromosomeQualifeirs.add(
            new QualifierFactory()
                .createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, chromosomeName));
      }
    }
    if ("monopartite".equals(chromosomeType)) {
      chromosomeQualifeirs.add(
          new QualifierFactory().createQualifier(Qualifier.NOTE_QUALIFIER_NAME, chromosomeType));
    }
    return chromosomeQualifeirs;
  }

  @Override
  public String toString() {
    return "ChromosomeEntry{"
        + "chromosomeName='"
        + chromosomeName
        + '\''
        + ", chromosomeType='"
        + chromosomeType
        + '\''
        + ", chromosomeLocation='"
        + chromosomeLocation
        + '\''
        + ", objectName='"
        + objectName
        + '\''
        + ", accession='"
        + accession
        + '\''
        + ", topology="
        + topology
        + ", chromosomeQualifeirs="
        + chromosomeQualifeirs
        + '}';
  }
}
