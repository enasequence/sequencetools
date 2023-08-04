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
package uk.ac.ebi.embl.api.entry.location;

import java.io.Serializable;

// TODO: add constraints for between location's positions (n^n+1 & p^1)
public abstract class Between extends Location implements Serializable {

  private static final long serialVersionUID = 2510429722841357323L;

  protected Between(Long beginPosition, Long endPosition) {
    super(beginPosition, endPosition);
  }

  protected Between(Long beginPosition, Long endPosition, boolean complement) {
    super(beginPosition, endPosition, complement);
  }

  @Override
  public long getLength() {
    return 0;
  }
}
