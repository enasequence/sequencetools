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
package uk.ac.ebi.embl.api.validation.helper;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class UtilsTest {

  @Test
  public void getContigDescription() {
    Assert.assertEquals(
        "masterDescription, contig: submitterAccession",
        Utils.getContigDescription("masterDescription", "submitterAccession"));
  }

  @Test
  public void getScaffoldDescription() {
    Assert.assertEquals(
        "masterDescription, scaffold: submitterAccession",
        Utils.getScaffoldDescription("masterDescription", "submitterAccession"));
  }

  @Test
  public void getChromosomeDescription() {
    SourceFeature feature = new FeatureFactory().createSourceFeature();
    Assert.assertEquals(
        "masterDescription, submitterAccession",
        Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

    feature.addQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, "segment value");
    Assert.assertEquals(
        "masterDescription, segment: segment value",
        Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

    feature.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "organelle value");
    Assert.assertEquals(
        "masterDescription, organelle: organelle value",
        Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

    feature.addQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, "chromosome value");
    Assert.assertEquals(
        "masterDescription, chromosome: chromosome value",
        Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));

    feature.addQualifier(Qualifier.PLASMID_QUALIFIER_NAME, "plasmid value");
    Assert.assertEquals(
        "masterDescription, plasmid: plasmid value",
        Utils.getChromosomeDescription(feature, "masterDescription", "submitterAccession"));
  }
}
