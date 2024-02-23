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
package uk.ac.ebi.embl.api.entry.sequence;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.helper.ByteBufferUtils;

public class Sequence extends AbstractSequence implements HasOrigin, Serializable {

  private static final long serialVersionUID = -7213946602036350730L;

  public enum Topology {
    LINEAR,
    CIRCULAR
  }

  public static final String MRNA_MOLTYPE = "mRNA";
  public static final String RRNA_MOLTYPE = "rRNA";
  public static final String GENOMIC_DNA_MOLTYPE = "genomic DNA";

  private Origin origin;
  private SequenceAccession sequenceAccession = new SequenceAccession(null, null);
  private String GIAccession;
  private ByteBuffer sequence;

  private long length = 0;
  private long contigLength = 0;
  private long agpLength = 0;
  private String moleculeType;
  private Topology topology;
  protected List<Location> contigs;
  protected List<AgpRow> agpRows;

  protected Sequence() {
    this.sequence = null;
    this.contigs = new ArrayList<Location>();
    this.agpRows = new ArrayList<AgpRow>();
    this.contigLength = 0;
    this.agpLength = 0;
    this.sequenceAccession = new SequenceAccession(null, null);
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public String getAccession() {
    return sequenceAccession.getAccession();
  }

  public void setAccession(String accession) {
    sequenceAccession.setAccession(accession);
  }

  public String getAccessionwithVersion() {
    return getAccession() + "." + getVersion();
  }

  public String getGIAccession() {
    return GIAccession;
  }

  public void setGIAccession(String GIaccession) {
    GIAccession = GIaccession;
  }

  public Integer getVersion() {
    return sequenceAccession.getVersion();
  }

  public void setVersion(Integer version) {
    sequenceAccession.setVersion(version);
  }

  public String getMoleculeType() {
    return moleculeType;
  }

  public void setMoleculeType(String moleculeType) {
    this.moleculeType = moleculeType;
  }

  public Topology getTopology() {
    return topology;
  }

  public void setTopology(Topology topology) {
    this.topology = topology;
  }

  @Deprecated
  public ByteBuffer getSequenceBuffer() {

    return sequence;
  }

  /**
   * Overridden so we can create appropriate sized buffer before making string.
   *
   * @see uk.ac.ebi.embl.api.entry.sequence.AbstractSequence#getSequence(java.lang.Long,
   *     java.lang.Long)
   */
  @Deprecated
  @Override
  public String getSequence(Long beginPosition, Long endPosition) {

    if (beginPosition == null
        || endPosition == null
        || (beginPosition > endPosition)
        || beginPosition < 1
        || endPosition > getLength()) {
      return null;
    }

    int length = (int) (endPosition.longValue() - beginPosition.longValue()) + 1;
    int offset = beginPosition.intValue() - 1;
    String subSequence = null;
    try {
      subSequence = ByteBufferUtils.string(getSequenceBuffer(), offset, length);
    } catch (Exception e) {
      e.printStackTrace();
      return subSequence;
    }
    return subSequence;
    /*// note begin:1 end:4 has length 4


    byte[] subsequence = new byte[length];


    synchronized (sequence) {
    	sequence.position(offset);
    	sequence.get(subsequence, 0, length);
    }

    String string = new String(subsequence);

    return string;*/
  }

  /**
   * The sequence parameter may be bigger than the sequence it contains.
   *
   * @param sequence
   */
  public void setSequence(ByteBuffer sequence) {
    this.sequence = sequence;
  }

  @Override
  public long getLength() {
    if (sequence != null) return sequence.array().length;
    else if (getContigs().size() != 0) {
      if (this.contigLength == 0) {
        for (Location contig : getContigs()) {
          this.contigLength += contig.getLength();
        }
      }
      return this.contigLength;
    } else if (getAgpRows().size() != 0) {
      if (agpLength == 0) {
        for (AgpRow agpRow : getAgpRows()) {
          if (agpRow.isValid()) {
            if (!agpRow.isGap()) {
              agpLength += agpRow.getComponent_end() - agpRow.getComponent_beg() + 1;
            } else {
              agpLength += agpRow.getGap_length();
            }
          }
        }
      }
      return this.agpLength;
    } else return 0;
  }

  @Deprecated
  public void setLength(long length) {
    this.length = length;
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.sequenceAccession);
    builder.append(this.length);
    builder.append(this.moleculeType);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Sequence) {
      final Sequence other = (Sequence) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.sequenceAccession, other.sequenceAccession);
      builder.append(this.sequence, other.sequence);
      builder.append(this.length, other.length);
      builder.append(this.moleculeType, other.moleculeType);
      builder.append(this.topology, other.topology);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("sequenceAccession", sequenceAccession);
    // builder.append("sequence", sequence);
    builder.append("length", length);
    builder.append("moleculeType", moleculeType);
    builder.append("topology", topology);
    return builder.toString();
  }

  @Override
  public byte[] getSequenceByte() {
    if (sequence != null) return sequence.array();
    return null;
  }

  @Override
  public byte[] getSequenceByte(Long beginPosition, Long endPosition) {
    if (beginPosition == null
        || endPosition == null
        || (beginPosition > endPosition)
        || beginPosition < 1
        || endPosition > getLength()) {
      return null;
    }

    int length = (int) (endPosition.longValue() - beginPosition.longValue()) + 1;
    int offset = beginPosition.intValue() - 1;
    byte[] subSequence = new byte[length];
    System.arraycopy(getSequenceByte(), offset, subSequence, 0, length);
    return subSequence;
  }

  public List<Location> getContigs() {
    return this.contigs;
  }

  public boolean addContig(Location location) {
    if (location == null) {
      return false;
    }
    return this.contigs.add(location);
  }

  public boolean addContigs(Collection<Location> locations) {
    if (locations == null) {
      return false;
    }
    this.contigLength = 0;
    boolean changed = false;
    for (Location location : locations) {
      changed |= this.contigs.add(location);
      this.contigLength += location.getLength();
    }
    return changed;
  }

  public boolean removeContig(Location location) {
    return this.contigs.remove(location);
  }

  public List<AgpRow> getAgpRows() {
    return this.agpRows;
  }

  public boolean addAgpRow(AgpRow agpRow) {
    if (agpRow == null) {
      return false;
    }
    return this.agpRows.add(agpRow);
  }

  public boolean addAgpRows(Collection<AgpRow> agpRows) {
    if (agpRows == null) {
      return false;
    }
    boolean changed = false;
    for (AgpRow agpRow : agpRows) {
      changed |= this.agpRows.add(agpRow);
    }
    return changed;
  }

  public boolean removeAgpRow(AgpRow agpRow) {
    return this.agpRows.remove(agpRow);
  }

  public List<AgpRow> getSortedAGPRows() {
    Collections.sort(
        agpRows,
        (object1, object2) -> object1.getPart_number() < object2.getPart_number() ? -1 : 1);
    return agpRows;
  }
}
