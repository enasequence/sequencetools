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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

/**
 * Adds gene qualifiers to features sharing the same locus_tag qualifier where there is a 1 to 1
 * mapping with gene qualifiers - ignores where multiple genes are associated (to be picked up by
 * other checks)
 */
@Description("Added gene \\\"{0}\\\" to feature sharing \\\"{2}\\\" qualifier \\\"{1}\\")
public class GeneAssociationFix extends EntryValidationCheck {

  protected static final String MESSAGE_ID = "GeneAssociationFix";
  protected static final String PSEUDOGENE_QUOTE_REMOVAL_FIX = "PseudogeneQuoteRemovalFix";

  /**
   * Adds gene qualifiers to features sharing the same locus_tag qualifier where there is a 1 to 1
   * mapping with gene qualifiers - ignores where multiple genes are associated (to be picked up by
   * other checks)
   *
   * @param entry an entry to be checked (expected type is Entry)
   * @return a validation result
   */
  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    // collect all locus_tag features
    Collection<Feature> locusFeatures =
            SequenceEntryUtils.getFeaturesContainingQualifier(
                    Qualifier.LOCUS_TAG_QUALIFIER_NAME, entry);
    // collect all pseudogene features
    Collection<Feature> pseudogeneFeatures =
            SequenceEntryUtils.getFeaturesContainingQualifier(
                    Qualifier.PSEUDOGENE_QUALIFIER_NAME, entry);

    // remove single quotes if pseudogene surrounded with
    if (!pseudogeneFeatures.isEmpty()) {
      for (Feature pseudogeneFeat : pseudogeneFeatures) {
        Qualifier qual = pseudogeneFeat.getSingleQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME);
        String qualVal = qual.getValue();
        if (qualVal != null) {
          if (qualVal.startsWith("'")) qualVal = qualVal.substring(1);
          if (qualVal.endsWith("'")) qualVal = qualVal.substring(0, qualVal.length() - 1);
          if (!qualVal.equals(qual.getValue())) {
            reportMessage(
                    Severity.FIX,
                    pseudogeneFeat.getOrigin(),
                    PSEUDOGENE_QUOTE_REMOVAL_FIX,
                    qual.getValue(),
                    qualVal);
            qual.setValue(qualVal);
          }
        }
      }
    }

    if (locusFeatures.isEmpty() && pseudogeneFeatures.isEmpty()) {
      return result;
    }

    /** which locus_tag qualifier is associated with which gene qualifier - should be 1 to 1 */
    HashMap<String, String> locusTag2Gene = new HashMap<String, String>();

    /**
     * firstly, build up a list of all genes associated with only 1 locus_tag - ignore those
     * associated with more than 1 locus_tag as we are just looking for clean, 1 to 1 relationships.
     */
    for (Feature locusFeature : locusFeatures) {

      /** we know this contains a locus qualifier cos that's how we built the list */
      List<Qualifier> locusQualifiers =
              locusFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
      String locusName = locusQualifiers.get(0).getValue();
      if (locusQualifiers.size() > 1) {
        continue; // just leave it - other checks for this - should be only 1
      }

      int geneCount =
              SequenceEntryUtils.getFeatureQualifierCount(Qualifier.GENE_QUALIFIER_NAME, locusFeature);

      if (geneCount > 1) {
        continue; // just leave it - other checks for this
      } else if (geneCount == 1) {
        String currentGeneName =
                locusFeature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME).get(0).getValue();

        if (locusTag2Gene.containsKey(locusName)) {
          /**
           * if the gene already associated with this locus_tag has a different value, bail out for
           * this locus_tag. other checks will point out that there needs to be a 1 to 1
           * correspondence.
           */
          if (!locusTag2Gene.get(locusName).equals(currentGeneName)) {
            locusTag2Gene.remove(
                    locusName); // remove existing mapping as this is now not clearly the intended
            // mapping
            continue;
          }
        } else {
          // this gene is now reserved by this locus_tag qualifier
          locusTag2Gene.put(locusName, currentGeneName);
        }
      }
    }


    QualifierFactory qualifierFactory = new QualifierFactory();
    /**
     * then add the gene to all features that do not have any and share the same locus_tag qualifier
     * as another feature that does have a gene associated
     */
    for (Feature locusFeature : locusFeatures) {
      String locusTag =
              locusFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();

      int geneCount =
              SequenceEntryUtils.getFeatureQualifierCount(Qualifier.GENE_QUALIFIER_NAME, locusFeature);

      if (geneCount == 0) {
        if (locusTag2Gene.containsKey(locusTag)) {
          String geneName = locusTag2Gene.get(locusTag);
          Qualifier geneQualifier =
                  qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, geneName);
          locusFeature.addQualifier(geneQualifier);
          reportMessage(
                  Severity.FIX,
                  locusFeature.getOrigin(),
                  MESSAGE_ID,
                  geneName,
                  locusTag,
                  "\\locus_tag");
        }
      }
    }

    return result;
  }
}
