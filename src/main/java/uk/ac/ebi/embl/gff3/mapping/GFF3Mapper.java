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
package uk.ac.ebi.embl.gff3.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.gff3.GFF3Record;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;

public class GFF3Mapper {
  private Entry entry;
  private final EntryFactory entryFactory = new EntryFactory();
  private final FeatureFactory featureFactory = new FeatureFactory();
  private Feature feature;
  QualifierFactory qualifierFactory = new QualifierFactory();
  private Qualifier qualifier;
  private final LocationFactory locationFactory = new LocationFactory();
  private final List<Entry> entryList = new ArrayList<Entry>();
  String resourceBundle = "uk.ac.ebi.embl.gff3.mapping.gffMapper";

  public List<Entry> mapGFF3ToEntry(GFF3RecordSet records) throws IOException {

    ResourceBundle featureQualifiers = ResourceBundle.getBundle(resourceBundle);
    String tempSeqid = null;
    for (GFF3Record record : records.getRecords()) {
      Collection<Qualifier> qualifierList = new ArrayList<Qualifier>();
      boolean isFeature = false;
      String sequenceId = record.getSequenceID();
      String source = record.getSource();
      String featureType = record.getType();
      int start = record.getStart();
      int end = record.getEnd();
      double score = record.getScore();
      int strand = record.getStrand();
      int phase = record.getPhase();
      Map attributes = record.getAttributes();

      // GFF3 Qualifier Mapping

      Iterator attributeIterator = attributes.entrySet().iterator();
      while (attributeIterator.hasNext()) {
        Map.Entry attributePairs = (Map.Entry) attributeIterator.next();
        String attributeKey = attributePairs.getKey().toString();
        String attributeValue = attributePairs.getValue().toString();
        if (featureQualifiers.containsKey(attributeKey)) {
          qualifier =
              qualifierFactory.createQualifier(
                  featureQualifiers.getString(attributeKey), attributeValue);
          qualifierList.add(qualifier);

        } else {
          // System.out.println("invalid qualifier found:"
          // + attributeKey);
        }
      }

      if (qualifierList.size() == 0) {
        // System.out.println("no qualifiers matched");
      }

      // GFF3 start and end columns
      Location location = locationFactory.createLocalRange((long) start, (long) end);
      Order<Location> compoundJoin = new Order<Location>();
      compoundJoin.addLocation(location);

      // GFF3 Feature Mapping
      if (featureQualifiers.containsKey(featureType)) {
        feature = featureFactory.createFeature(featureQualifiers.getString(featureType));
        feature.setLocations(compoundJoin);
        feature.addQualifiers(qualifierList);
        isFeature = true;
      }

      if (isFeature) {

        if (tempSeqid == null || !tempSeqid.equals(sequenceId)) {
          tempSeqid = sequenceId;
          entry = entryFactory.createEntry();
          entry.addFeature(feature);
          entryList.add(entry);
        } else if (tempSeqid.equals(sequenceId)) {
          entry.addFeature(feature);
        }
      } else {
        // System.out.println("invalid feature:" + featureType);
      }
    }

    return entryList;
  }
}
