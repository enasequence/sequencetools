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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

public class SourceQualifierFix extends EntryValidationCheck {

  private static final String QUALIFIER_NAME_CHANGE = "QualifierNameChange";
  private static final String QUALIFIER_VALUE_CHANGE = "QualifierValueChange";
  private static final String QUALIFIER_DELETED = "QualifierDeleted";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null
        || entry.getPrimarySourceFeature() == null
        || entry.getPrimarySourceFeature().getQualifiers().size() == 0) {
      return result;
    }

    SourceFeature source = entry.getPrimarySourceFeature();
    String scientificName = source.getScientificName();

    if (scientificName != null && scientificName.toLowerCase().contains("salmonella")) {
      Qualifier seroTypeQual =
          entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SEROTYPE_QUALIFIER_NAME);
      if (seroTypeQual != null) {
        QualifierFactory qualFactory = new QualifierFactory();
        qualFactory.createQualifier(Qualifier.SEROVAR_QUALIFIER_NAME, seroTypeQual.getValue());
        source.addQualifier(
            qualFactory.createQualifier(Qualifier.SEROVAR_QUALIFIER_NAME, seroTypeQual.getValue()));
        source.removeQualifier(Qualifier.SEROTYPE_QUALIFIER_NAME);
        reportMessage(
            Severity.FIX,
            source.getOrigin(),
            QUALIFIER_NAME_CHANGE,
            Qualifier.SEROTYPE_QUALIFIER_NAME,
            Qualifier.SEROVAR_QUALIFIER_NAME);
      }
      Qualifier seroVarQual =
          entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SEROVAR_QUALIFIER_NAME);
      if (seroVarQual != null) {
        String oldVal = seroVarQual.getValue();
        if (oldVal.toLowerCase().contains("serotype")) {
          seroVarQual.setValue(oldVal.replaceAll("(?i)serotype", ""));
          reportMessage(
              Severity.FIX,
              source.getOrigin(),
              QUALIFIER_VALUE_CHANGE,
              Qualifier.SEROVAR_QUALIFIER_NAME,
              oldVal,
              seroVarQual.getValue());
        }
      }
    }

    Qualifier metagenomeSourceQual =
        source.getSingleQualifier(Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);

    if (metagenomeSourceQual != null) {
      String metegenomeSource = metagenomeSourceQual.getValue();
      List<Taxon> taxon =
          getEmblEntryValidationPlanProperty()
              .taxonClient
              .get()
              .getTaxonsByScientificName(metegenomeSource);

      if (metegenomeSource == null
          || !metegenomeSource.toLowerCase().contains("metagenome")
          || taxon == null
          || taxon.isEmpty()
          || taxon.get(0).getTaxId() == 408169L
          || !getEmblEntryValidationPlanProperty()
              .taxonClient
              .get()
              .isOrganismMetagenome(metegenomeSource)) {
        entry.getPrimarySourceFeature().removeQualifier(metagenomeSourceQual);
        reportMessage(
            Severity.FIX,
            source.getOrigin(),
            QUALIFIER_DELETED,
            Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME,
            metegenomeSource);
      }
    }

    return result;
  }
}
