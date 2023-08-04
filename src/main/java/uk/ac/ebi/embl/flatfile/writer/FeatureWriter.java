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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Vector;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

/** Flat file writer for the features and qualifiers on the FT lines. */
public class FeatureWriter extends FlatFileWriter {

  protected Feature feature;
  private final boolean isReducedFlatfile;
  private final boolean sortQualifiers;
  private final String featureHeader;
  private final String qualifierHeader;

  public FeatureWriter(
      Entry entry,
      Feature feature,
      boolean sortQualifiers,
      WrapType wrapType,
      String featureHeader,
      String qualifierHeader) {
    super(entry, wrapType);
    this.feature = feature;
    this.sortQualifiers = sortQualifiers;
    this.featureHeader = featureHeader;
    this.qualifierHeader = qualifierHeader;
    this.isReducedFlatfile = false;
  }

  public FeatureWriter(
      Entry entry,
      Feature feature,
      boolean sortQualifiers,
      WrapType wrapType,
      String featureHeader,
      String qualifierHeader,
      boolean isReducedFlatfile) {
    super(entry, wrapType);
    this.feature = feature;
    this.sortQualifiers = sortQualifiers;
    this.featureHeader = featureHeader;
    this.qualifierHeader = qualifierHeader;
    this.isReducedFlatfile = isReducedFlatfile;
  }

  // TODO: return value?
  public boolean write(Writer writer) throws IOException {
    writeFeatureLocation(writer);
    writeFeatureQualifiers(writer);
    return true;
  }

  /**
   * overridden in HTMLFeatureWriter
   *
   * @param writer
   * @throws IOException
   */
  protected void writeFeatureLocation(Writer writer) throws IOException {
    new FeatureLocationWriter(entry, feature, wrapType, featureHeader, qualifierHeader)
        .write(writer);
  }

  /**
   * Adds organism, /mol_type and /db_ref="taxon:" feature qualifiers into the source feature. If
   * these qualifiers already exist they are removed.
   */
  public Vector<Qualifier> getFeatureQualifiers(Entry entry, Feature feature) {
    Vector<Qualifier> qualifiers = new Vector<Qualifier>();
    if (!isReducedFlatfile && feature instanceof SourceFeature) {
      String scientificName = ((SourceFeature) feature).getScientificName();
      if (!FlatFileUtils.isBlankString(scientificName)) {
        Qualifier qualifier = (new QualifierFactory()).createQualifier("organism", scientificName);
        qualifiers.add(qualifier);
      }
      String moleculeType = entry.getSequence().getMoleculeType();
      if (!FlatFileUtils.isBlankString(moleculeType)) {
        Qualifier qualifier = (new QualifierFactory()).createQualifier("mol_type", moleculeType);
        qualifiers.add(qualifier);
      }
      Long taxId = ((SourceFeature) feature).getTaxId();
      if (taxId != null && taxId > -1 /* do not show negative taxIds */) {
        Qualifier qualifier =
            (new QualifierFactory()).createQualifier("db_xref", "taxon:" + taxId.toString());
        qualifiers.add(qualifier);
      }
    }

    for (Qualifier qualifier : feature.getQualifiers()) {
      String name = qualifier.getName();
      String value = qualifier.getValue();
      if (name == null) {
        continue;
      }
      if (name.equals(Qualifier.ORGANISM_QUALIFIER_NAME)) {
        continue; // Ignore /organism qualifiers.
      }
      if (name.equals(Qualifier.MOL_TYPE_QUALIFIER_NAME)) {
        continue; // Ignore /mol_type qualifiers.
      }
      if (name.equals(Qualifier.DB_XREF_QUALIFIER_NAME)
          && (isReducedFlatfile || (value != null && value.startsWith("taxon:")))) {
        continue; // Ignore /db_xref="taxon:" qualifiers.
      }
      if (name.equals(Qualifier.SUB_SPECIES)) {
        continue; // Ignore /sub_species qualifiers.
      }
      qualifiers.add(qualifier);
    }
    return qualifiers;
  }

  protected void writeFeatureQualifiers(Writer writer) throws IOException {
    Vector<Qualifier> qualifiers = getFeatureQualifiers(entry, feature);
    for (XRef xRef : feature.getXRefs()) {
      if (xRef == null) {
        continue;
      }
      Qualifier qualifier = (new QualifierFactory()).createQualifier("db_xref");
      StringBuilder value = new StringBuilder();
      if (!FlatFileUtils.isBlankString(xRef.getDatabase())) {
        value.append(xRef.getDatabase());
      }
      if (!FlatFileUtils.isBlankString(xRef.getPrimaryAccession())) {
        value.append(":");
        value.append(xRef.getPrimaryAccession());
      }
      qualifier.setValue(value.toString());
      qualifiers.add(qualifier);
    }
    if (sortQualifiers) {
      Collections.sort(qualifiers);
    }
    for (Qualifier qualifier : qualifiers) {
      new QualifierWriter(entry, qualifier, wrapType, qualifierHeader).write(writer);
    }
  }
}
