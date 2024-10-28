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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
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

  private static final Pattern isolation_sourcePattern =
      Pattern.compile("^\\s*environment\\s*\\(material\\)\\s*$");
  private static final Pattern latLonPattern = Pattern.compile("\\d+(\\.\\d+)?");
  private static final Map<String, String> qualifierSynonyms = new HashMap<>();
  private static final Set<String> covid19RequiredQuals =
      Set.of(
          Qualifier.COLLECTION_DATE_QUALIFIER_NAME,
          Qualifier.GEO_LOCATION_QUALIFIER_NAME,
          Qualifier.LAT_LON_QUALIFIER_NAME,
          Qualifier.HOST_QUALIFIER_NAME,
          Qualifier.NOTE_QUALIFIER_NAME);
  private final MasterSourceQualifierValidator masterSourceQualifierValidator;

  public SourceFeatureUtils() {
    qualifierSynonyms.put("metagenomic source", Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);
    qualifierSynonyms.put("host scientific name", Qualifier.HOST_QUALIFIER_NAME);
    qualifierSynonyms.put("collection date", Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    qualifierSynonyms.put("gisaid accession id", Qualifier.NOTE_QUALIFIER_NAME);
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
      // return if not valid date
      return;
    }

    if (MASTERSOURCEQUALIFIERS.isValidQualifier(tag)) {

      if (!MASTERSOURCEQUALIFIERS.isNoValueQualifiers(tag)
          && MASTERSOURCEQUALIFIERS.isNullValue(value)) {
        return;
      }

      if (MASTERSOURCEQUALIFIERS.isNoValueQualifiers(tag)) {
        if (!"NO".equalsIgnoreCase(value)) {
          source.addQualifier(new QualifierFactory().createQualifier(tag));
        }
      } else {
        source.addQualifier(new QualifierFactory().createQualifier(tag, value));
      }
    } else if (isCovidTaxId(source.getTaxId()) && covid19RequiredQuals.contains(tag)) {
      source.addQualifier(new QualifierFactory().createQualifier(tag, value));
    }
  }

  public void addExtraSourceQualifiers(
      SourceFeature source, TaxonomyClient taxonomyClient, String uniqueName) {
    if (addUniqueName(source)
        && taxonomyClient.isProkaryotic(source.getScientificName())
        && source.getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size() == 0) {
      source.addQualifier(
          new QualifierFactory().createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, uniqueName));
    }
  }

  private boolean addUniqueName(SourceFeature source) {
    List<Qualifier> sourceQualifiers = source.getQualifiers();
    if (sourceQualifiers.contains(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME)
        || sourceQualifiers.contains(Qualifier.STRAIN_QUALIFIER_NAME)
        || sourceQualifiers.contains(Qualifier.ISOLATE_QUALIFIER_NAME)) {
      return false;
    }
    return true;
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
    String isolationSource = null;
    for (Attribute attribute : sample.getAttributes()) {
      String qualifier = attribute.getName().trim();
      String value = attribute.getValue().trim();

      switch (categorizeQualifiers(qualifier)) {
        case COUNTRY -> country = value;
        case REGION -> region = value;
        case LATITUDE -> latitude = value;
        case LONGITUDE -> longitude = value;
        case ISOLATION_SOURCE -> isolationSource = value;
        case OTHER -> addSourceQualifier(qualifier, value, sourceFeature);
      }
    }

    setLatLonQualifier(sourceFeature, latitude, longitude);
    setGeoLocationQualifier(sourceFeature, country, region);
    setIsolationSourceQualifier(sourceFeature, isolationSource);
    setSourceFeatureTaxon(sourceFeature, sample, taxonomyClient);
    addExtraSourceQualifiers(sourceFeature, taxonomyClient, sample.getName());
  }

  private SpecialQualifiers categorizeQualifiers(String qualifier) {
    if (qualifier.equals("geographic location (country and/or sea)")) {
      return SpecialQualifiers.COUNTRY;
    } else if (qualifier.equals("geographic location (region and locality)")) {
      return SpecialQualifiers.REGION;
    } else if (qualifier.contains("latitude")) {
      return SpecialQualifiers.LATITUDE;
    } else if (qualifier.contains("longitude")) {
      return SpecialQualifiers.LONGITUDE;
    } else if (isolation_sourcePattern.matcher(qualifier).matches()) {
      return SpecialQualifiers.ISOLATION_SOURCE;
    } else {
      return SpecialQualifiers.OTHER;
    }
  }

  public void setGeoLocationQualifier(SourceFeature sourceFeature, String country, String region) {
    String geoLocationValue;
    if (country != null && region != null) {
      geoLocationValue = country + ":" + region;
    } else {
      geoLocationValue = country != null ? country : region;
    }

    if (StringUtils.isNotEmpty(geoLocationValue)) {
      addSourceQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME, geoLocationValue, sourceFeature);
    }
  }

  public void setIsolationSourceQualifier(SourceFeature source, String isolationSource) {
    if (StringUtils.isNotEmpty(isolationSource)) {
      addSourceQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, isolationSource, source);
    }
  }

  public void setLatLonQualifier(SourceFeature sourceFeature, String latitude, String longitude) {
    if (latitude != null && longitude != null) {
      Matcher latitudeMatcher = latLonPattern.matcher(latitude);
      Matcher longitudeMatcher = latLonPattern.matcher(longitude);
      latitude = latitudeMatcher.find() ? latitudeMatcher.group() : latitude;
      longitude = longitudeMatcher.find() ? longitudeMatcher.group() : longitude;
      String latLonValue = "";
      if (latitude != null && longitude != null) {
        try {
          latLonValue += latitude + " " + (Double.parseDouble(latitude) < 0 ? "S" : "N") + " ";
          latLonValue += longitude + " " + (Double.parseDouble(longitude) < 0 ? "W" : "E");
          addSourceQualifier(Qualifier.LAT_LON_QUALIFIER_NAME, latLonValue, sourceFeature);
        } catch (NumberFormatException ex) {
          // ignore lat_lon qualifier if its values are not valid number.
        }
      }
    }
  }

  public void setSourceFeatureTaxon(
      SourceFeature sourceFeature, Sample sample, TaxonomyClient taxonomyClient) {
    Taxon taxon =
        taxonomyClient.getTaxonByTaxid(
            sample.getTaxId() == null ? null : sample.getTaxId().longValue());
    if (taxon != null) {
      sourceFeature.setTaxon(taxon);
    }
  }

  enum SpecialQualifiers {
    COUNTRY,
    REGION,
    LATITUDE,
    LONGITUDE,
    ISOLATION_SOURCE,
    OTHER
  }
}
