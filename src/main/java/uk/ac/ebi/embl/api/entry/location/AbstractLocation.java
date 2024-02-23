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
package uk.ac.ebi.embl.api.entry.location;

import java.io.Serializable;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public abstract class AbstractLocation implements HasOrigin, Serializable {

  private static final long serialVersionUID = -8498145871263031213L;

  private Origin origin;
  private boolean complement;
  private boolean simpleLocation;

  protected AbstractLocation() {}

  protected AbstractLocation(boolean complement) {
    this.complement = complement;
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public boolean isComplement() {
    return complement;
  }

  public boolean isSimpleLocation() {
    return simpleLocation;
  }

  public void setComplement(boolean complement) {
    this.complement = complement;
  }

  public void setSimpleLocation(boolean simpleLocation) {
    this.simpleLocation = simpleLocation;
  }

  public abstract long getLength();
}
