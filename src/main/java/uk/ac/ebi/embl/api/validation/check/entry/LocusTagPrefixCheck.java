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
import java.util.*;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description(
    "Illegal /locus_tag value \"{0} \". locus_tag prefix \"{1}\" is not registered with the project")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class LocusTagPrefixCheck extends EntryValidationCheck {

  protected static final String MESSAGE_ID_INVALID_PREFIX = "LocusTagPrefixCheck1";

  public ValidationResult check(Entry entry) throws ValidationEngineException {
    result = new ValidationResult();
    HashSet<String> projectLocustagPrefixes = new HashSet<>();

    if (entry == null) {
      return result;
    }

    try {
      // Get the BioSample accession and allow it to be used as a locus tag.
      // The BioSample accession must be available in the flat file.
      for (XRef xref : entry.getXRefs()) {
        if ("BioSample".equals(xref.getDatabase())) {
          projectLocustagPrefixes.add(xref.getPrimaryAccession());
        }
      }

      // Get the registered locus tags for the project.
      if (getEntryDAOUtils() == null && getEraproDAOUtils() == null) {
        if (getEmblEntryValidationPlanProperty().getOptions().locusTagPrefixes.isPresent()) {
          projectLocustagPrefixes.addAll(
              getEmblEntryValidationPlanProperty().getOptions().locusTagPrefixes.get());
        }
      } else {
        // The BioProject accession must be available in the flat file.
        if (entry.getProjectAccessions() != null) {
          for (Text projectAccession : entry.getProjectAccessions()) {
            Set<String> locusTagPrefixes =
                getEraproDAOUtils() == null
                    ? getEntryDAOUtils().getProjectLocutagPrefix(projectAccession.getText())
                    : getEraproDAOUtils().getLocusTags(projectAccession.getText());
            if (!locusTagPrefixes.isEmpty()) {
              projectLocustagPrefixes.addAll(locusTagPrefixes);
            }
          }
        }
      }

      List<Qualifier> locusTagQualifiers =
          SequenceEntryUtils.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME, entry);
      if (locusTagQualifiers != null) {
        for (Qualifier qualifier : locusTagQualifiers) {
          String locusTagValue = qualifier.getValue();

          if (locusTagValue != null) {
            String locustagPrefix = locusTagValue.split("_")[0];
            if (!projectLocustagPrefixes.contains(locustagPrefix)) {
              reportError(
                  qualifier.getOrigin(), MESSAGE_ID_INVALID_PREFIX, locusTagValue, locustagPrefix);
            }
          }
        }
      }

      return result;
    } catch (SQLException e) {
      throw new ValidationEngineException(e);
    }
  }
}
