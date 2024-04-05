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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.sql.SQLException;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;

@Description(
    "CO line/foreign entry \"{0}\" doesn't exist."
        + "CO line Contig entry location \"{0}\" is not within entry \"{1}\" sequence length"
        + "Invalid Contig location \"{0}\" in CO line"
        + "gaps with /linkage_evidence=\"paired-ends\" are not allowed at the beginning or end of CO Join")
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_CONTIG
    })
public class EntryContigsCheck extends EntryValidationCheck {

  private static final String CONTIG_EXISTS_MESSAGE_ID = "ContigEntryCheck-1";
  private static final String CONTIG_LOCATION_MESSAGE_ID = "ContigEntryCheck-2";
  private static final String CONTIG_LOCATION_MESSAGE_ID_1 = "ContigEntryCheck-3";
  private static final String GAP_CONTIG_MESSAGE_ID = "ContigEntryCheck-4";

  public ValidationResult check(Entry entry) throws ValidationEngineException {

    result = new ValidationResult();

    if (entry == null || entry.getSequence().getContigs().size() == 0) {
      return result;
    }

    if (entry.getSequence().getContigs().get(0) instanceof Gap
        || entry.getSequence().getContigs().get(entry.getSequence().getContigs().size() - 1)
            instanceof Gap) {
      List<Feature> assemblyGapFeatures =
          SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
      for (Feature assemblyGapFeature : assemblyGapFeatures) {
        if (assemblyGapFeature.getLocations().getMinPosition() == 1
            || assemblyGapFeature.getLocations().getMaxPosition()
                == entry.getSequence().getLength()) {
          if ("paired-ends"
              .equals(
                  SequenceEntryUtils.getQualifierValue(
                      Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, assemblyGapFeature))) {
            reportError(entry.getOrigin(), GAP_CONTIG_MESSAGE_ID);
          }
        }
      }
    }

    if (getEntryDAOUtils() == null) return result;

    for (Location location : entry.getSequence().getContigs()) {
      if (!(location instanceof RemoteLocation) && !(location instanceof Gap)) {
        StringBuilder locationString = new StringBuilder();
        FeatureLocationWriter.renderLocationForcePartiality(locationString, location, false, false);
        reportError(location.getOrigin(), CONTIG_LOCATION_MESSAGE_ID_1, locationString);
      }
      if (!getEmblEntryValidationPlanProperty().ncbiCon.get()) {

        if (location instanceof RemoteLocation) {
          RemoteLocation remoteLocation = (RemoteLocation) location;
          String accession =
              remoteLocation.getAccession()
                  + (remoteLocation.getVersion() == null ? "" : "." + remoteLocation.getVersion());
          try {
            if (!getEntryDAOUtils().isEntryExists(accession)) {
              reportError(location.getOrigin(), CONTIG_EXISTS_MESSAGE_ID, accession);
              continue;
            }

            // contig entry location check
            Long seqLength = getEntryDAOUtils().getSequenceLength(accession);

            if (location.getBeginPosition() < 0
                || location.getIntBeginPosition() > seqLength
                || location.getEndPosition() > seqLength
                || location.getEndPosition() < 0) {
              StringBuilder locationBlock = new StringBuilder();
              FeatureLocationWriter.renderLocationForcePartiality(
                  locationBlock, location, false, false);
              reportError(
                  location.getOrigin(), CONTIG_LOCATION_MESSAGE_ID, locationBlock, accession);
            }
          } catch (SQLException e) {
            throw new ValidationEngineException(e);
          }
        }
      }
    }

    return result;
  }
}
