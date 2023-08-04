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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class Linkage_evidenceFixTest {

  private Feature feature;
  private Qualifier linkageEvidenceQual;
  private Linkage_evidenceFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    feature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    linkageEvidenceQual =
        qualifierFactory.createQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
    check = new Linkage_evidenceFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_noLinkage_evidence() {
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_Linkage_evidence_withUnderscore() {
    linkageEvidenceQual.setValue("align_genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
    assertEquals(0, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
  }

  @Test
  public void testCheck_Linkage_evidence_withInvalid() {
    linkageEvidenceQual.setValue("alignnus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
    assertEquals(0, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
  }

  @Test
  public void testCheck_Linkage_evidencevalid() {
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
  }

  @Test
  public void testCheckLinkageEvidenceRemoval() {
    QualifierFactory qualifierFactory = new QualifierFactory();
    Qualifier gaptypQual = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gaptypQual.setValue("between scaffolds");
    feature.addQualifier(gaptypQual);
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
    assertEquals(0, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
  }

  @Test
  public void testCheckLinkageEvidenceWithValidGapType() {
    QualifierFactory qualifierFactory = new QualifierFactory();
    Qualifier gaptypQual = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gaptypQual.setValue("within scaffold");
    feature.addQualifier(gaptypQual);
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
  }

  @Test
  public void testCheckLinkageEvidenceWithGapTypeContamination() {
    QualifierFactory qualifierFactory = new QualifierFactory();
    Qualifier gaptypQual = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gaptypQual.setValue("contamination");
    feature.addQualifier(gaptypQual);
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("Linkage_evidenceFix_2", Severity.FIX));
    assertEquals(
        "unspecified",
        feature.getSingleQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME).getValue());
  }

  @Test
  public void testCheckLinkageEvidenceWithValidGapType1() {
    QualifierFactory qualifierFactory = new QualifierFactory();
    Qualifier gaptypQual = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gaptypQual.setValue("repeat within scaffold");
    feature.addQualifier(gaptypQual);
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
  }

  @Test
  public void testCheckNonGapFeature() {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    Feature feature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    Qualifier gaptypQual = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gaptypQual.setValue("between scaffolds");
    feature.addQualifier(gaptypQual);
    linkageEvidenceQual.setValue("align genus");
    feature.addQualifier(linkageEvidenceQual);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("LinkageEvidenceRemovalFix", Severity.FIX));
  }
}
