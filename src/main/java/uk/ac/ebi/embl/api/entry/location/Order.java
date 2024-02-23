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

import org.apache.commons.lang.builder.EqualsBuilder;

public class Order<E extends Location> extends CompoundLocation<E> {

  private static final long serialVersionUID = -7109201074579953005L;

  public Order() {}

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Order) {
      final Order<Location> other = (Order<Location>) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.getLocations().toArray(), other.getLocations().toArray());
      builder.append(this.isComplement(), other.isComplement());
      return builder.isEquals();
    }
    return false;
  }
}
