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
package uk.ac.ebi.embl.flatfile.writer.degenerator;

import static uk.ac.ebi.embl.api.entry.feature.Feature.CDS_FEATURE_NAME;
import static uk.ac.ebi.embl.api.entry.feature.Feature.GENE_FEATURE_NAME;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.*;
import static uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology.CIRCULAR;
import static uk.ac.ebi.embl.api.validation.SequenceEntryUtils.*;

import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 01-Nov-2010 Time: 14:31:34 To change this template
 * use File | Settings | File Templates.
 */
public class DEData {

  private final Entry entry;

  private boolean isTPA;
  private final List<Feature> cdsFeatures;
  private final List<Feature> geneFeatures;
  private String mol_type;
  private String chromosome;
  private String segment;
  private final boolean circular;
  private boolean hasSequenceLengthrRna;
  private Feature sequenceLengthrRna;

  public DEData(Entry entry) {

    this.entry = entry;

    for (Text text : entry.getKeywords()) {
      if (text.getText().equals("Third Party Annotation")) {
        this.isTPA = true;
        break;
      }
    }

    this.cdsFeatures = getFeatures(CDS_FEATURE_NAME, entry);
    this.geneFeatures = getFeatures(GENE_FEATURE_NAME, entry);

    Feature primarySource = entry.getPrimarySourceFeature();
    if (primarySource != null) {
      this.chromosome = getQualifierValue(Qualifier.CHROMOSOME_QUALIFIER_NAME, primarySource);
      this.segment = getQualifierValue(Qualifier.SEGMENT_QUALIFIER_NAME, primarySource);
    }

    if (entry.getSequence() != null) {
      this.mol_type = entry.getSequence().getMoleculeType();
    }

    this.circular =
        entry.getSequence() != null
            && entry.getSequence().getTopology() != null
            && entry.getSequence().getTopology().equals(CIRCULAR);

    for (Feature rrna_feature : getFeatures(Feature.rRNA_FEATURE_NAME, entry)) {
      if (isFeatureSequenceLength(rrna_feature)) {
        this.hasSequenceLengthrRna = true;
        this.sequenceLengthrRna = rrna_feature;
      }
    }
  }

  public boolean isTPA() {
    return isTPA;
  }

  public static String getGeneString(Feature feature) {
    return getQualifierValue(GENE_QUALIFIER_NAME, feature);
  }

  public static String getProductString(Feature feature) {
    return getQualifierValue(PRODUCT_QUALIFIER_NAME, feature);
  }

  public List<Feature> getCdsFeatures() {
    return cdsFeatures;
  }

  public int getCdsFeatureCount() {
    return cdsFeatures.size();
  }

  public List<Feature> getGeneFeatures() {
    return geneFeatures;
  }

  public int getGeneFeatureCount() {
    return geneFeatures.size();
  }

  public String getMol_type() {
    return mol_type;
  }

  public boolean isMolType(String molType) {
    return mol_type != null && mol_type.equals(molType);
  }

  public boolean hasChromosome() {
    return chromosome != null;
  }

  public String getChromosome() {
    return chromosome;
  }

  public boolean hasSegment() {
    return segment != null;
  }

  public String getSegment() {
    return segment;
  }

  public boolean isSingleCdsOrGeneFeature() {
    return (cdsFeatures.size() == 1) || (cdsFeatures.isEmpty() && geneFeatures.size() == 1);
  }

  public Feature getSingleCdsOrGeneFeature() {

    if (cdsFeatures.size() == 1) {
      return cdsFeatures.get(0);
    } else if (cdsFeatures.isEmpty() && geneFeatures.size() == 1) {
      return geneFeatures.get(0);
    }

    return null;
  }

  public boolean isMultiCdsOrGeneFeature() {
    return (cdsFeatures.size() > 1) || (cdsFeatures.isEmpty() && geneFeatures.size() > 1);
  }

  public List<Feature> getMultiCdsOrGeneFeatures() {

    if (cdsFeatures.size() > 1) {
      return cdsFeatures;
    } else if (geneFeatures.size() > 1) {
      return geneFeatures;
    }

    return null;
  }

  static String getUniquifierString(List<String> qualifiers, SourceFeature primarySource) {
    String returnString = "";
    for (String qualifier : qualifiers) {
      if (isQualifierAvailable(qualifier, primarySource)) {
        String value = getQualifier(qualifier, primarySource).getValue();
        returnString = returnString.concat(", ").concat(qualifier).concat(" ").concat(value);
      }
    }
    return returnString;
  }

  public boolean isFeatureSequenceLength(Feature feature) {
    Sequence sequence = entry.getSequence();

    if (sequence != null) {
      long seqLength = sequence.getLength();
      CompoundLocation<Location> compoundLocation = feature.getLocations();
      if (compoundLocation.getLocations().size() == 1) {
        Location location = compoundLocation.getLocations().get(0);
        return location.getBeginPosition() == 1 && location.getEndPosition() == seqLength;
      }
    }
    return false;
  }

  public boolean hasSequenceLengthrRna() {
    return hasSequenceLengthrRna;
  }

  public Feature getSequenceLengthrRna() {
    return sequenceLengthrRna;
  }

  public boolean isCircular() {
    return circular;
  }

  public static boolean isFeaturePartial(Feature feature) {
    return feature.getLocations() != null
        && (feature.getLocations().isFivePrime() || feature.getLocations().isThreePrime());
  }
}
