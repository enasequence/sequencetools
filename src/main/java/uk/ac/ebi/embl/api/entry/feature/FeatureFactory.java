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
package uk.ac.ebi.embl.api.entry.feature;

import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;

public class FeatureFactory {

  public Feature createFeature(String featureName) {
    if (Feature.CDS_FEATURE_NAME.equals(featureName)) {
      return createCdsFeature();
    } else if (Feature.SOURCE_FEATURE_NAME.equals(featureName)) {
      return createSourceFeature();
    } else if (Feature.MAP_PEPTIDE_FEATURE_NAME.equals(featureName)
        || Feature.SIG_PEPTIDE_FEATURE_NAME.equals(featureName)
        || Feature.TRANSIT_PEPTIDE_FEATURE_NAME.equals(featureName)) {
      return createPeptideFeature(featureName);
    } else {
      return new Feature(featureName);
    }
  }

  public Feature createFeature(String featureName, boolean join) {
    if (Feature.CDS_FEATURE_NAME.equals(featureName)) {
      return createCdsFeature();
    } else if (Feature.SOURCE_FEATURE_NAME.equals(featureName)) {
      return createSourceFeature();
    } else if (Feature.MAP_PEPTIDE_FEATURE_NAME.equals(featureName)
        || Feature.SIG_PEPTIDE_FEATURE_NAME.equals(featureName)
        || Feature.TRANSIT_PEPTIDE_FEATURE_NAME.equals(featureName)) {
      return createPeptideFeature(featureName);
    } else {
      return new Feature(featureName, join);
    }
  }

  public SourceFeature createSourceFeature() {
    return new SourceFeature();
  }

  public CdsFeature createCdsFeature() {
    return new CdsFeature();
  }

  public PeptideFeature createPeptideFeature(String featureName) {
    return new PeptideFeature(featureName, true);
  }

  public LocationFactory getLocationFactory() {
    return new LocationFactory();
  }

  public QualifierFactory getQualifierFactory() {
    return new QualifierFactory();
  }
}
