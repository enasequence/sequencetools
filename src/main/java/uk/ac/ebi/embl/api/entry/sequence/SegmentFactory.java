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
package uk.ac.ebi.embl.api.entry.sequence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.service.SequenceRetrievalService;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;

public class SegmentFactory {

  private final SequenceRetrievalService service;

  public SegmentFactory() {
    this.service = SequenceToolsServices.sequenceRetrievalService();
  }

  public SegmentFactory(SequenceRetrievalService service) {
    this.service = service;
  }

  public Segment createSegment(Sequence sequence, LocalBase localBase) {
    if (localBase.isComplement()) {
      return new Segment(localBase, sequence.getReverseComplementSequenceByte(localBase));
    } else {
      return new Segment(localBase, sequence.getSequenceByte(localBase));
    }
  }

  public Segment createSegment(Sequence sequence, LocalRange localRange) {
    if (localRange.isComplement()) {
      return new Segment(localRange, sequence.getReverseComplementSequenceByte(localRange));
    } else {
      return new Segment(localRange, sequence.getSequenceByte(localRange));
    }
  }

  public Segment createSegment(Sequence sequence, CompoundLocation<Location> compoundLocation)
      throws IOException {
    if (compoundLocation == null) {
      return null;
    }

    if (compoundLocation.hasRemoteLocation() && service == null) {
      return null;
    }

    ByteBuffer segmentBuffer = ByteBuffer.wrap(new byte[(int) compoundLocation.getLength()]);

    for (Location location : compoundLocation.getLocations()) {
      Segment segment = null;
      if (location instanceof LocalBase) {
        segment = createSegment(sequence, (LocalBase) location);
      } else if (location instanceof LocalRange) {
        segment = createSegment(sequence, (LocalRange) location);
      } else if (location instanceof RemoteBase) {
        segment = createSegment((RemoteBase) location);
      } else if (location instanceof RemoteRange) {
        segment = createSegment((RemoteRange) location);
      }
      if (segment != null && segment.getSequenceByte() != null) {
        byte[] segByte = segment.getSequenceByte();
        segmentBuffer.put(segByte);
      }
    }

    byte[] segmentSeq =
        ByteBuffer.wrap(Arrays.copyOf(segmentBuffer.array(), segmentBuffer.position())).array();
    if (segmentSeq.length == 0) {
      segmentSeq = null;
    }
    if (compoundLocation.isComplement()) {
      return new Segment(
          compoundLocation, (new ReverseComplementer()).reverseComplementByte(segmentSeq));
    } else {
      return new Segment(compoundLocation, segmentSeq);
    }
  }

  public Segment createSegment(RemoteBase remoteBase) {
    if (remoteBase == null) {
      return null;
    }

    if (service == null) {
      return null;
    }

    byte[] subSequence;
    // Includes reverse complementation.
    subSequence = service.getSequence(remoteBase).array();
    return new Segment(remoteBase, subSequence);
  }

  public Segment createSegment(RemoteRange remoteRange) {
    if (remoteRange == null) {
      return null;
    }

    if (service == null) {
      return null;
    }

    byte[] subSequence;
    // Includes reverse complementation.
    subSequence = service.getSequence(remoteRange).array();
    return new Segment(remoteRange, subSequence);
  }
}
