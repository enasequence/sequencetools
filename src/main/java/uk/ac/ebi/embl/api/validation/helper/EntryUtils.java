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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationScope;

public class EntryUtils {

  public static final HashMap<String, String> gapType = new HashMap<>();
  public static final HashMap<String, String> linkageEvidence = new HashMap<>();

  static {
    gapType.put("unknown", "unknown");
    gapType.put("repeatnoLinkage", "repeat between scaffolds");
    gapType.put("scaffold", "within scaffold");
    gapType.put("contig", "between scaffolds");
    gapType.put("centromere", "centromere");
    gapType.put("short_arm", "short arm");
    gapType.put("heterochromatin", "heterochromatin");
    gapType.put("telomere", "telomere");
    gapType.put("repeatwithLinkage", "repeat within scaffold");
    gapType.put("contamination", "contamination");
    linkageEvidence.put("pcr", "pcr");
    linkageEvidence.put("na", "unspecified");
    linkageEvidence.put("paired-ends", "paired-ends");
    linkageEvidence.put("align_genus", "align genus");
    linkageEvidence.put("align_xgenus", "align xgenus");
    linkageEvidence.put("align_trnscpt", "align trnscpt");
    linkageEvidence.put("within_clone", "within clone");
    linkageEvidence.put("clone_contig", "clone contig");
    linkageEvidence.put("map", "map");
    linkageEvidence.put("strobe", "strobe");
    linkageEvidence.put("unspecified", "unspecified");
    linkageEvidence.put("proximity_ligation", "proximity ligation");
  }

  public enum Topology {
    LINEAR("L"),
    CIRCULAR("C");

    String topology;

    private Topology(String topology) {
      this.topology = topology;
    }

    public String getTopology() {
      return topology;
    }
  }

  public static boolean isProject(String id) {
    return id.startsWith("PRJ");
  }

  public static boolean isAnalysis_id(String id) {
    return id.startsWith("ERZ");
  }

  public static String getIdType(String id) {
    return isProject(id) ? "study_id" : "analysis_id";
  }

  public static boolean hasLetter(String subject) {
    boolean found = false;
    Pattern p = Pattern.compile(".*\\w+.*");
    Matcher m = p.matcher(subject);
    if (m.find()) {
      found = true;
    }
    return found;
  }

  public static String getObjectNameFromDescription(String desc) {
    String objectName = null;
    if (desc.indexOf("\\t+") != -1) {
      System.err.println(
          "ERROR: The description line is a tab delimited line. It must be a white space delimited.");
      return objectName;
    }
    String[] words = desc.trim().split("\\s+");
    if (!hasLetter(words[words.length - 1])) {

      objectName = words[words.length - 2] + " " + words[words.length - 1];
    } else {

      objectName = words[words.length - 1];
    }
    return objectName;
  }

  public static boolean isValidEntry_name(String entry_name) {
    if (entry_name.split(" ").length > 1) {
      return false;
    }
    return true;
  }

  public static String concat(String delimiter, String... params) {
    StringBuffer concatString = new StringBuffer();
    int i = 0;
    if (params.length != 0) {

      for (String param : params) {
        i++;
        if (param != null) {
          concatString.append(param);
          if (i != (params.length)) concatString.append(delimiter);
        }
      }
    }
    return concatString.toString();
  }

  public static String convertNonAsciiStringtoAsciiString(String non_asciiString)
      throws UnsupportedEncodingException {
    if (StringUtils.isAsciiPrintable(non_asciiString)
        || non_asciiString == null
        || non_asciiString.isEmpty()) return non_asciiString;
    String encodedString =
        Normalizer.normalize(
            new String(non_asciiString.getBytes(), Charset.forName("UTF-8")), Normalizer.Form.NFKD);
    String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
    String asciiString = new String(encodedString.replaceAll(regex, "").getBytes("ascii"), "ascii");
    return asciiString;
  }

  public static Entry convertAGPtofeatureNContigs(Entry entry) {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    LocationFactory locationFactory = new LocationFactory();
    ArrayList<Location> components = new ArrayList<Location>();

    for (AgpRow agpRow : entry.getSequence().getSortedAGPRows()) {
      Long object_begin = agpRow.getObject_beg();
      Long object_end = agpRow.getObject_end();
      Long component_begin = agpRow.getComponent_beg();
      Long component_end = agpRow.getComponent_end();
      String orientation = agpRow.getOrientation();
      Long gap_length = agpRow.getGap_length();
      String gap_type = agpRow.getGap_type();
      List<String> linkage_evidences = agpRow.getLinkageevidence();
      String component_acc = agpRow.getComponent_acc();
      String componentId = agpRow.getComponent_id();

      if (agpRow.isGap()) {
        Feature assembly_gapFeature =
            featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        Order<Location> locations = new Order<Location>();
        LocalRange location =
            locationFactory.createLocalRange((long) object_begin, (long) object_end);
        locations.addLocation(location);
        locations.setSimpleLocation(true);
        assembly_gapFeature.setLocations(locations);
        Qualifier gap_typeQualifier =
            qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
        if ("repeat".equals(gap_type)) {
          if (linkage_evidences == null || linkage_evidences.isEmpty()) {
            gap_type = "repeatnoLinkage";
          } else {
            gap_type = "repeatwithLinkage";
          }
        }
        if (linkage_evidences != null && agpRow.hasLinkage()) {
          for (String linkage_evidence : linkage_evidences) {
            Qualifier linkage_evidenceQualifier =
                qualifierFactory.createQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
            String linkage_evidenceQualifierValue = linkageEvidence.get(linkage_evidence);
            linkage_evidenceQualifier.setValue(linkage_evidenceQualifierValue);
            assembly_gapFeature.addQualifier(linkage_evidenceQualifier);
          }
        }
        String gapTypeValue = gapType.get(gap_type);
        gap_typeQualifier.setValue(gapTypeValue);
        assembly_gapFeature.addQualifier(gap_typeQualifier);
        Qualifier estimated_lengthQualifier =
            qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
        if ("U".equals(agpRow.getComponent_type_id())) {
          estimated_lengthQualifier.setValue("unknown");
          components.add(locationFactory.createUnknownGap(agpRow.getGap_length()));
        } else {
          estimated_lengthQualifier.setValue(new String(new Long(gap_length).toString()));
          components.add(locationFactory.createGap(agpRow.getGap_length()));
        }
        assembly_gapFeature.addQualifier(estimated_lengthQualifier);
        entry.addFeature(assembly_gapFeature);
      } else {
        if (component_acc == null & componentId == null) continue;
        if (component_acc != null) {
          String[] accessionWithVersion = component_acc.split("\\.");
          String accession = accessionWithVersion[0];
          Integer version = Integer.valueOf(accessionWithVersion[1]);
          Location remoteLocation =
              locationFactory.createRemoteRange(
                  accession, version, (long) component_begin, (long) component_end);
          if (orientation == "-" || orientation == "minus") remoteLocation.setComplement(true);
          components.add(remoteLocation);
        } else {

          Location remoteLocation =
              locationFactory.createRemoteRange(
                  componentId, 0, (long) component_begin, (long) component_end);
          if (orientation == "-" || orientation == "minus") remoteLocation.setComplement(true);
          components.add(remoteLocation);
        }
      }

      // reportMessage(Severity.FIX, entry.getOrigin(), ACCESSION_FIX_ID,
      // agpRow.getComponent_acc(),componentID,agpRow.getObject());
    }

    if (entry.getSequence() == null) {
      SequenceFactory sequenceFactory = new SequenceFactory();
      entry.setSequence(sequenceFactory.createSequence());
    }
    entry.getSequence().addContigs(components);
    return entry;
  }

  public static void setKeyWords(Entry masterEntry) {
    masterEntry.addKeyword(new Text("Third Party Data"));
    masterEntry.addKeyword(new Text("TPA"));
    masterEntry.addKeyword(new Text("TPA:assembly"));
  }

  public static boolean isGenome(ValidationScope scope) {
    return scope == ValidationScope.ASSEMBLY_MASTER
        || scope == ValidationScope.ASSEMBLY_CONTIG
        || scope == ValidationScope.ASSEMBLY_SCAFFOLD
        || scope == ValidationScope.ASSEMBLY_CHROMOSOME;
  }

  public static boolean isEnvironmentalSample(Entry entry) {
    return entry
            .getPrimarySourceFeature()
            .getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME)
        != null;
  }

  public static boolean isBinomialRequired(Entry entry, ValidationScope scope) {
    // If the scope is GENOME and sample is NOT environmental.
    return EntryUtils.isGenome(scope) && !EntryUtils.isEnvironmentalSample(entry);
  }
}
