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
package uk.ac.ebi.embl.gff3.writer;

import static uk.ac.ebi.embl.gff3.writer.GFF3Show.*;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationException;

/** Writes features in GFF3 format for AnnotationSketch. */
public class GFF3EntryWriter {

  private final Entry entry;
  private final Long windowBeginPosition;
  private final Long windowEndPosition;
  private EnumSet<GFF3Show> show;
  private HashMap<String, GFF3Gene> locusTagGeneMap;
  private HashMap<String, GFF3Gene> geneNameGeneMap;

  public GFF3EntryWriter(Entry entry, Long beginPosition, Long endPosition) {
    this(entry, beginPosition, endPosition, null);
  }

  public GFF3EntryWriter(
      Entry entry, Long beginPosition, Long endPosition, EnumSet<GFF3Show> show) {
    this.entry = entry;
    this.windowBeginPosition = beginPosition;
    this.windowEndPosition = endPosition;
    this.show = show;
    if (this.show == null) {
      this.show = EnumSet.of(SHOW_GENE, SHOW_SOURCE, SHOW_FEATURE);
    }
  }

  /**
   * Writes the GFF3 file.
   *
   * @param writer the output stream.
   * @throws IOException is there was an error in writing to the output stream.
   */
  public void write(Writer writer) throws IOException {
    GFF3Writer.writeVersionPragma(writer);
    GFF3Writer.writeRegionPragma(
        writer, entry.getPrimaryAccession(), windowBeginPosition, windowEndPosition);
    int ID = 0;
    if (show.contains(SHOW_GENE)) {
      locusTagGeneMap = new HashMap<String, GFF3Gene>();
      geneNameGeneMap = new HashMap<String, GFF3Gene>();
    }
    for (Feature feature : entry.getFeatures()) {
      Long minPosition = feature.getLocations().getMinPosition();
      Long maxPosition = feature.getLocations().getMaxPosition();
      // Remove global complement.
      feature.getLocations().removeGlobalComplement();
      String geneName = feature.getSingleQualifierValue(Qualifier.GENE_QUALIFIER_NAME);
      String locusTag = feature.getSingleQualifierValue(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
      if (show.contains(SHOW_GENE)) {
        addGeneSegment(geneName, locusTag, minPosition, maxPosition);
      }
      if (show.contains(SHOW_FEATURE)) {
        ++ID;
        writeFeature(writer, feature, geneName, locusTag, ID, minPosition, maxPosition);
        for (Location location : feature.getLocations().getLocations()) {
          int parentID = ID;
          ++ID;
          writeFeatureSegment(writer, feature, geneName, locusTag, location, parentID, ID);
        }
      }
    }
    if (show.contains(SHOW_GENE)) {
      for (GFF3Gene gene : locusTagGeneMap.values()) {
        ++ID;
        writeGene(writer, gene, ID);
      }
      for (GFF3Gene gene : geneNameGeneMap.values()) {
        ++ID;
        writeGene(writer, gene, ID);
      }
    }
    if (show.contains(SHOW_CONTIG)) {
      Long contigPosition = 1L;
      for (Location location : entry.getSequence().getContigs()) {
        ++ID;
        contigPosition = writeContig(writer, location, ID, contigPosition);
      }
    }
    if (show.contains(SHOW_ASSEMBLY)) {
      for (Assembly assembly : entry.getAssemblies()) {
        ++ID;
        writeAssembly(writer, assembly, ID);
      }
    }
  }

  private boolean filterSegment(Long beginPosition, Long endPosition) {
    return (beginPosition == null
        || endPosition == null
        || beginPosition > windowEndPosition
        || endPosition < windowBeginPosition);
  }

  private void addGeneSegment(
      String geneName, String locusTag, Long minPosition, Long maxPosition) {
    GFF3Gene gene = null;
    if (locusTag != null) {
      gene = locusTagGeneMap.get(locusTag);
      if (gene == null) {
        gene = new GFF3Gene(minPosition, maxPosition, geneName, locusTag);
        locusTagGeneMap.put(locusTag, gene);
      } else {
        gene.adjustBeginPosition(minPosition);
        gene.adjustEndPosition(maxPosition);
      }
    } else if (geneName != null) {
      gene = geneNameGeneMap.get(geneName);
      if (gene == null) {
        gene = new GFF3Gene(minPosition, maxPosition, geneName, locusTag);
        geneNameGeneMap.put(geneName, gene);
      } else {
        gene.adjustBeginPosition(minPosition);
        gene.adjustEndPosition(maxPosition);
      }
    }
  }

  private Long adjustPosition(Long pos) {
    // Positions must be contained in the windows for AnnotationSketch.
    if (pos <= windowBeginPosition) {
      return windowBeginPosition;
    }
    if (pos >= windowEndPosition) {
      return windowEndPosition;
    }
    return pos;
  }

  private void writeFeature(
      Writer writer,
      Feature feature,
      String geneName,
      String locusTag,
      int parentID,
      Long beginPosition,
      Long endPosition)
      throws IOException {
    if (filterSegment(beginPosition, endPosition)) {
      return;
    }
    // Filter out gene and source features.
    if (feature.getName().equals("gene")
        || (!show.contains(SHOW_SOURCE) && feature.getName().equals("source"))) {
      return;
    }
    // Complement
    // If the first segment is complemented then complement the parent.
    // This is required to make AnnotationSketch display 5' arrow correctly.
    Boolean isComplement = false;
    List<Location> locations = feature.getLocations().getLocations();
    if (locations.size() > 0) {
      isComplement = locations.get(0).isComplement();
    }
    if (feature.getName().equals("source")) {
      isComplement = null;
    }
    // Phase
    String phase = ".";
    if (feature instanceof CdsFeature) {
      CdsFeature cdsFeature = (CdsFeature) feature;
      Integer startCodon = null;
      try {
        startCodon = cdsFeature.getStartCodon();
      } catch (ValidationException ex) {
        startCodon = 0;
      }
      if (startCodon == null) {
        startCodon = 0;
      } else {
        --startCodon;
      }
      phase = startCodon.toString();
    }
    GFF3Writer.writeColumns(
        writer,
        entry.getPrimaryAccession(),
        feature.getName(),
        adjustPosition(beginPosition),
        adjustPosition(endPosition),
        isComplement,
        phase);
    // Attributes
    GFF3Writer.writeAttribute(writer, "ID", String.valueOf(parentID));
    writer.write(";");
    String name = getName(geneName, locusTag);
    if (feature.getName().equals("source")) {
      name = ((SourceFeature) feature).getScientificName();
    }
    GFF3Writer.writeAttribute(writer, "Name", name);
    writer.write(";");
    String trackName = "";
    if (show.contains(GROUP_GENE)
        && (geneName != null || locusTag != null)
        && !feature.getName().equals("source")) {
      trackName = getName(geneName, locusTag);
    } else {
      trackName = feature.getName();
    }
    GFF3Writer.writeAttribute(writer, "Track", trackName);
    writer.write("\n");
  }

  private void writeFeatureSegment(
      Writer writer,
      Feature feature,
      String geneName,
      String locusTag,
      Location location,
      int parentID,
      int ID)
      throws IOException {
    // Filter out segments that are outside the selected region.
    Long beginPosition = location.getBeginPosition();
    Long endPosition = location.getEndPosition();
    if (filterSegment(beginPosition, endPosition)) {
      return;
    }
    // Filter out gene and source features.
    if (feature.getName().equals("gene")
        || (!show.contains(SHOW_SOURCE) && feature.getName().equals("source"))) {
      return;
    }
    Boolean isComplement = location.isComplement();
    if (feature.getName().equals("source")) {
      isComplement = null;
    }
    GFF3Writer.writeColumns(
        writer,
        entry.getPrimaryAccession(),
        "segment_" + feature.getName(),
        adjustPosition(beginPosition),
        adjustPosition(endPosition),
        isComplement);
    // Attributes
    GFF3Writer.writeAttribute(writer, "ID", String.valueOf(ID));
    writer.write(";");
    GFF3Writer.writeAttribute(writer, "Parent", String.valueOf(parentID));
    writer.write("\n");
  }

  private void writeGene(Writer writer, GFF3Gene gene, int ID) throws IOException {
    // Filter out genes that are outside the selected region.
    Long beginPosition = gene.getBeginPosition();
    Long endPosition = gene.getEndPosition();
    if (filterSegment(beginPosition, endPosition)) {
      return;
    }
    GFF3Writer.writeColumns(
        writer,
        entry.getPrimaryAccession(),
        "gene",
        adjustPosition(beginPosition),
        adjustPosition(endPosition));
    // Attributes
    GFF3Writer.writeAttribute(writer, "ID", String.valueOf(ID));
    writer.write(";");
    GFF3Writer.writeAttribute(writer, "Name", getName(gene.getGeneName(), gene.getLocusTag()));
    writer.write(";");
    GFF3Writer.writeAttribute(writer, "Track", "gene");
    writer.write("\n");
  }

  private Long writeContig(Writer writer, Location location, int ID, Long contigPosition)
      throws IOException {
    Long beginPosition = contigPosition;
    Long endPosition = contigPosition + location.getLength();
    if (filterSegment(beginPosition, endPosition)) {
      return endPosition;
    }
    GFF3Writer.writeColumns(
        writer,
        entry.getPrimaryAccession(),
        "contig",
        adjustPosition(beginPosition),
        adjustPosition(endPosition),
        location.isComplement());
    // Attributes
    GFF3Writer.writeAttribute(writer, "ID", String.valueOf(ID));
    writer.write(";");
    if (location instanceof Gap) {
      GFF3Writer.writeAttribute(writer, "Name", "Gap");
    } else {
      GFF3Writer.writeAttribute(
          writer,
          "Name",
          ((RemoteLocation) location).getAccession()
              + "."
              + ((RemoteLocation) location).getVersion());
      GFF3Writer.writeAttribute(
          writer, "Range", location.getBeginPosition() + ".." + location.getEndPosition());
      writer.write(";");
    }
    writer.write(";");
    GFF3Writer.writeAttribute(writer, "Track", "contig");
    writer.write("\n");
    return endPosition;
  }

  private void writeAssembly(Writer writer, Assembly assembly, int ID) throws IOException {
    // Filter out assemblies that are outside the selected region.
    Long beginPosition = assembly.getSecondarySpan().getBeginPosition();
    Long endPosition = assembly.getSecondarySpan().getEndPosition();
    if (filterSegment(beginPosition, endPosition)) {
      return;
    }
    GFF3Writer.writeColumns(
        writer,
        entry.getPrimaryAccession(),
        "assembly",
        adjustPosition(beginPosition),
        adjustPosition(endPosition),
        assembly.getPrimarySpan().isComplement());
    // Attributes
    GFF3Writer.writeAttribute(writer, "ID", String.valueOf(ID));
    writer.write(";");
    GFF3Writer.writeAttribute(
        writer,
        "Name",
        assembly.getPrimarySpan().getAccession() + "." + assembly.getPrimarySpan().getVersion());
    writer.write(";");
    GFF3Writer.writeAttribute(
        writer,
        "Range",
        assembly.getPrimarySpan().getBeginPosition()
            + ".."
            + assembly.getPrimarySpan().getEndPosition());
    writer.write(";");
    GFF3Writer.writeAttribute(writer, "Track", "contig");
    writer.write("\n");
  }

  private String getName(String geneName, String locusTag) {
    if (locusTag != null && geneName != null) {
      return geneName + "|" + locusTag;
    } else if (locusTag != null) {
      return locusTag;
    }
    return geneName;
  }
}
