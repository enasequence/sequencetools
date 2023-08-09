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
package uk.ac.ebi.embl.api.validation.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.check.feature.MasterSourceQualifierValidator;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl.MASTERSOURCEQUALIFIERS;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class SourceFeatureUtils {

  private Qualifier isolationSourceQualifier = null;
  private final String isolation_source_regex = "^\\s*environment\\s*\\(material\\)\\s*$";
  private final Pattern isolation_sourcePattern = Pattern.compile(isolation_source_regex);
  private boolean addUniqueName = true;
  private final Map<String, String> qualifierSynonyms = new HashMap<>();
  private final Set<String> covid19RequiredQuals = new HashSet<>();
  private final MasterSourceQualifierValidator masterSourceQualifierValidator;

  public SourceFeatureUtils() {
    qualifierSynonyms.put("metagenomic source", Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);
    qualifierSynonyms.put("host scientific name", Qualifier.HOST_QUALIFIER_NAME);
    qualifierSynonyms.put("collection date", Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    qualifierSynonyms.put("gisaid accession id", Qualifier.NOTE_QUALIFIER_NAME);
    covid19RequiredQuals.add(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    covid19RequiredQuals.add(Qualifier.COUNTRY_QUALIFIER_NAME);
    covid19RequiredQuals.add(Qualifier.LAT_LON_QUALIFIER_NAME);
    covid19RequiredQuals.add(Qualifier.HOST_QUALIFIER_NAME);
    covid19RequiredQuals.add(Qualifier.NOTE_QUALIFIER_NAME);

    isolationSourceQualifier = null;
    masterSourceQualifierValidator = new MasterSourceQualifierValidator();
  }

  public boolean isCovidTaxId(Long taxID) {
    return taxID != null && taxID == 2697049L;
  }

  public void addSourceQualifier(String tag, String value, SourceFeature source) {

    if (tag == null) return;
    tag = tag.toLowerCase();

    if (qualifierSynonyms.containsKey(tag)) {
      tag = qualifierSynonyms.get(tag);
    }

    if (tag.equals(Qualifier.COLLECTION_DATE_QUALIFIER_NAME)
        && !masterSourceQualifierValidator.isValid(
            Qualifier.COLLECTION_DATE_QUALIFIER_NAME, value)) {
      // we have to do similar check for other qualifier as well.
      return;
    }

    if (isolation_sourcePattern.matcher(tag).matches()) {
      tag = Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME;
      if (value != null && !value.isEmpty())
        isolationSourceQualifier = (new QualifierFactory()).createQualifier(tag, value);
    } else {
      if (MASTERSOURCEQUALIFIERS.isValid(tag)) {

        if (!MASTERSOURCEQUALIFIERS.isNoValue(tag) && MASTERSOURCEQUALIFIERS.isNullValue(value))
          return;

        if (Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME.equals(tag)
            || Qualifier.STRAIN_QUALIFIER_NAME.equals(tag)
            || Qualifier.ISOLATE_QUALIFIER_NAME.equals(tag)) {
          addUniqueName = false;
        }

        if (MASTERSOURCEQUALIFIERS.isNoValue(tag)) {
          if (!"NO".equalsIgnoreCase(value))
            source.addQualifier(new QualifierFactory().createQualifier(tag));
        } else source.addQualifier(new QualifierFactory().createQualifier(tag, value));
      } else if (isCovidTaxId(source.getTaxId()) && covid19RequiredQuals.contains(tag)) {
        source.addQualifier(new QualifierFactory().createQualifier(tag, value));
      }
    }
  }

  public void addExtraSourceQualifiers(
      SourceFeature source, TaxonomyClient taxonomyClient, String uniqueName) {
    if (addUniqueName
        && taxonomyClient.isProkaryotic(source.getScientificName())
        && source.getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size() == 0) {
      source.addQualifier(
          new QualifierFactory().createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, uniqueName));
    }

    if (source.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size() == 0
        && isolationSourceQualifier != null) source.addQualifier(isolationSourceQualifier);
  }

  public SourceFeature constructSourceFeature(Sample sample, TaxonomyClient taxonomyClient) {
    FeatureFactory featureFactory = new FeatureFactory();
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.setTaxId(sample.getTaxId() == null ? null : sample.getTaxId().longValue());
    sourceFeature.setScientificName(sample.getOrganism());
    sourceFeature.setMasterLocation();
    addQualifiers(sourceFeature, sample, taxonomyClient);
    return sourceFeature;
  }

  public SourceFeature updateSourceFeature(
      SourceFeature sourceFeature, Sample sample, TaxonomyClient taxonomyClient) {
    sourceFeature.setTaxId(sample.getTaxId() == null ? null : sample.getTaxId().longValue());
    sourceFeature.setScientificName(sample.getOrganism());
    addQualifiers(sourceFeature, sample, taxonomyClient);
    return sourceFeature;
  }

  public void addQualifiers(
      SourceFeature sourceFeature, Sample sample, TaxonomyClient taxonomyClient) {
    String latitude = null;
    String longitude = null;
    String country = null;
    String region = null;
    for (Attribute attribute : sample.getAttributes()) {
      String tag = attribute.getName();
      String value = attribute.getValue();
      if (isCovidTaxId(sourceFeature.getTaxId()) && tag != null) {
        // Master source qualifiers values created from multiple sample fields are constructed here.
        if (tag.toLowerCase().contains("latitude")) {
          latitude = value;
        } else if (tag.toLowerCase().contains("longitude")) {
          longitude = value;
        } else if (tag.trim().equalsIgnoreCase("geographic location (country and/or sea)")) {
          country = value;
        } else if (tag.trim().equalsIgnoreCase("geographic location (region and locality)")) {
          region = value;
        } else {
          addSourceQualifier(tag, value, sourceFeature);
        }
      } else {
        addSourceQualifier(tag, value, sourceFeature);
      }
    }

    if (latitude != null && longitude != null) {
      String latValue = latitude;
      String lonValue = longitude;
      try {
        double lat = Double.parseDouble(latitude);
        latValue += " " + (lat < 0 ? "S" : "N");
      } catch (NumberFormatException ex) {
        // ignore
      }
      try {
        double lon = Double.parseDouble(longitude);
        lonValue += " " + (lon < 0 ? "W" : "E");
      } catch (NumberFormatException ex) {
        // ignore
      }
      String latLonValue = latValue + " " + lonValue;
      addSourceQualifier(Qualifier.LAT_LON_QUALIFIER_NAME, latLonValue, sourceFeature);
    }
    if (country != null || region != null) {
      addSourceQualifier(
          Qualifier.COUNTRY_QUALIFIER_NAME,
          country == null ? region : region == null ? country : country + ":" + region,
          sourceFeature);
    }

    Taxon taxon =
        taxonomyClient.getTaxonByTaxid(
            sample.getTaxId() == null ? null : sample.getTaxId().longValue());
    if (taxon != null) {
      sourceFeature.setTaxon(taxon);
    }
    addExtraSourceQualifiers(sourceFeature, taxonomyClient, sample.getName());
  }
}
