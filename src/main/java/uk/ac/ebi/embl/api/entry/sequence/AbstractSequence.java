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

import java.io.Serializable;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalRange;

public abstract class AbstractSequence implements Serializable {

  private static final long serialVersionUID = -3810966793566146493L;

  public abstract long getLength();

  /**
   * Returns a subsequence string or null if the location is not valid.
   *
   * @param beginPosition the sequence begin position.
   * @param endPosition the sequence end position.
   * @return a subsequence string or null if the location is not valid.
   */
  public String getSequence(Long beginPosition, Long endPosition) {
    byte[] sequenceByte = getSequenceByte(beginPosition, endPosition);
    if (sequenceByte != null) return new String(sequenceByte);
    return null;
  }

  public abstract byte[] getSequenceByte();

  public byte[] getSequenceByte(Long beginPosition, Long endPosition) {
    byte[] sequence = getSequenceByte();
    if (beginPosition == null
        || endPosition == null
        || (beginPosition > endPosition)
        || beginPosition < 1
        || endPosition > sequence.length) {
      return null;
    }
    int length = (int) (endPosition.longValue() - beginPosition.longValue()) + 1;
    int offset = beginPosition.intValue() - 1;
    byte[] subSequence = new byte[length];
    System.arraycopy(getSequenceByte(), offset, subSequence, 0, length);
    return subSequence;
  }

  public byte[] getSequenceByte(LocalBase localBase) {
    return getSequenceByte(localBase.getBeginPosition(), localBase.getEndPosition());
  }

  public final byte[] getSequenceByte(LocalRange localRange) {
    return getSequenceByte(localRange.getBeginPosition(), localRange.getEndPosition());
  }

  public final byte[] getReverseComplementSequenceByte() {
    return (new ReverseComplementer()).reverseComplementByte(getSequenceByte());
  }

  public final byte[] getReverseComplementSequenceByte(Long beginPosition, Long endPosition) {
    return (new ReverseComplementer())
        .reverseComplementByte(getSequenceByte(beginPosition, endPosition));
  }

  public final byte[] getReverseComplementSequenceByte(LocalBase localBase) {
    return (new ReverseComplementer()).reverseComplementByte(getSequenceByte(localBase));
  }

  public final byte[] getReverseComplementSequenceByte(LocalRange localRange) {
    return (new ReverseComplementer()).reverseComplementByte(getSequenceByte(localRange));
  }
}
