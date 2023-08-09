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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.sql.SQLException;
import java.util.Collection;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    })
@Description(
    "Sequence of >100kb should probably have a project id."
        + "Circular genomic entry should probably have a project id."
        + "Entry must not have multiple project ids."
        + "\"{0}\" qualifier is not allowed in entries not having project id"
        + "Invalid projectId : \"{0}\"")
public class EntryProjectIdCheck extends EntryValidationCheck {

  private static final String TOPOLOGY_MESSAGE = "EntryProjectIdCheck2";
  private static final String WGS_PROJECTID_NOTFOUND_ID = "EntryProjectIdCheck3";
  private static final String KEYWORD_PROJECTID_NOTFOUND_ID = "EntryProjectIdCheck4";
  private static final String KEYWORD_MISSED_ID = "EntryProjectIdCheck5";
  private static final String MULTIPLE_PROJECT_MESSAGE_ID = "EntryProjectIdCheck6";
  private static final String PROJECT_LOCUS_TAG_MESSAGE = "EntryProjectIdCheck7";
  private static final String CON_PROJECTID_NOTFOUND_ID = "EntryProjectIdCheck8";
  private static final String INVALID_PROJECT_MESSAGE = "EntryProjectIdCheck9";
  private static final String PROJECTID_KEYWORD = "complete genome";

  public void setPopulated() {
    super.setPopulated();
  }

  public ValidationResult check(Entry entry) throws ValidationEngineException {
    KWCheck keywordLineCheck = new KWCheck();

    result = new ValidationResult();
    boolean virus = false;

    if (entry == null) {
      return result;
    }

    String dataclass = entry.getDataClass();
    if (entry.getProjectAccessions().isEmpty()) {

      Sequence sequence = entry.getSequence();
      if (sequence == null || entry.getSequence().getLength() == 0) {
        return result; // dont make a fuss - other checks for this
      }
      /** check the topology */
      if (entry.getSequence().getTopology() == Sequence.Topology.CIRCULAR) {
        Collection<Feature> sourceFeatures =
            SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
        if (!sourceFeatures.isEmpty()) {
          Feature source = sourceFeatures.iterator().next();
          Qualifier organismQualifier =
              source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
          // System.out.println("match found"+taxonomyClient.isChildOf(organismQualifier.getValue(),
          // "viruses"));
          if (organismQualifier != null) {
            if (getEmblEntryValidationPlanProperty()
                .taxonClient
                .get()
                .isOrganismValid(organismQualifier.getValue())) {
              if (getEmblEntryValidationPlanProperty()
                  .taxonClient
                  .get()
                  .isChildOf(organismQualifier.getValue(), "Viruses")) {

                virus = true;
              }
            } else {
              virus = true;
            }
          }
          if (SequenceEntryUtils.getFeatureQualifierCount("plasmid", source) == 0
              && SequenceEntryUtils.getFeatureQualifierCount("organelle", source) == 0
              && !virus) {
            reportError(entry.getOrigin(), TOPOLOGY_MESSAGE);
          }
        }
      }

      /** check the WGS Dataclass */
      if (dataclass != null && dataclass.equals(Entry.WGS_DATACLASS)) {
        reportError(entry.getOrigin(), WGS_PROJECTID_NOTFOUND_ID);
      }
      /** check the CON Dataclass */
      if (dataclass != null && dataclass.equals(Entry.CON_DATACLASS)) {
        reportError(entry.getOrigin(), CON_PROJECTID_NOTFOUND_ID);
      }
      /** check the "complete genome" keyword in DE and KW lines */
      if (keywordLineCheck.hasKeyword(entry, PROJECTID_KEYWORD)
          || keywordLineCheck.hasDEKeyword(entry, PROJECTID_KEYWORD)) {
        reportError(entry.getOrigin(), KEYWORD_PROJECTID_NOTFOUND_ID);
      }

      if (SequenceEntryUtils.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME, entry).size() > 0) {
        reportError(
            entry.getOrigin(), PROJECT_LOCUS_TAG_MESSAGE, Qualifier.LOCUS_TAG_QUALIFIER_NAME);
      }

    } else {
      // check the project is registered: Move this code to separate class
      for (Text project : entry.getProjectAccessions()) {

        try {
          if (getEraproDAOUtils() != null
              && !getEraproDAOUtils().isProjectValid(project.getText())) {
            reportError(entry.getOrigin(), INVALID_PROJECT_MESSAGE, project.getText());
          } else if (getEntryDAOUtils() != null
              && !getEntryDAOUtils().isProjectValid(project.getText())) {
            reportError(entry.getOrigin(), INVALID_PROJECT_MESSAGE, project.getText());
          }
        } catch (SQLException e) {
          throw new ValidationEngineException(e.getMessage(), e);
        }
      }
      if (entry.getProjectAccessions().size() > 1) {
        reportError(entry.getOrigin(), MULTIPLE_PROJECT_MESSAGE_ID);
      }
    }
    /** check the missing keyword in KW line which exists in DE Line */
    if (getEmblEntryValidationPlanProperty().validationScope.get() != null) {
      if (keywordLineCheck.hasDEKeyword(entry, PROJECTID_KEYWORD)
          && !keywordLineCheck.hasKeyword(entry, PROJECTID_KEYWORD)
          && getEmblEntryValidationPlanProperty()
              .validationScope
              .get()
              .equals(ValidationScope.EMBL)) {
        reportError(entry.getOrigin(), KEYWORD_MISSED_ID);
      }
    }

    return result;
  }
}
