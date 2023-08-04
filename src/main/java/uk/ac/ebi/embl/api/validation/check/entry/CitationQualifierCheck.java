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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description(
    "isolation_source is a country \"{0}\",change isolation_source qualifier to country qualifier "
        + "isolation_source is a lat_lon \"{0}\", change isolation_source qualifier to lat_lon qualifier ")
public class CitationQualifierCheck extends EntryValidationCheck {
  private static final Pattern CITATION_PATTERN = Pattern.compile("[^\\d]*(\\d+)[^\\d]*");
  private static final int CITATION_GROUP = 1;
  public static final String CITATION_QUALIFIER_MESSAGE_ID = "CitationQualifierCheck_1";

  public CitationQualifierCheck() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null || entry.getFeatures().size() == 0) {
      return result;
    }

    List<Qualifier> citationQualifiers =
        SequenceEntryUtils.getQualifiers(Qualifier.CITATION_QUALIFIER_NAME, entry);

    if (citationQualifiers.size() == 0) return result;
    List<Integer> referenceNumbers = new ArrayList<Integer>();
    for (Reference reference : entry.getReferences()) {
      referenceNumbers.add(reference.getReferenceNumber());
    }

    for (Qualifier citationQualifier : citationQualifiers) {
      Matcher matcher = CITATION_PATTERN.matcher(citationQualifier.getValue());

      if (matcher.find()) {
        String qualifierReferenceNumber = matcher.group(CITATION_GROUP);
        if (!referenceNumbers.contains(Integer.valueOf(qualifierReferenceNumber)))
          reportError(
              citationQualifier.getOrigin(),
              CITATION_QUALIFIER_MESSAGE_ID,
              citationQualifier.getValue());
      }
    }

    return result;
  }
}
