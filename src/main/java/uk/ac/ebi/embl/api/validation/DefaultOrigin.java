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
package uk.ac.ebi.embl.api.validation;

public final class DefaultOrigin implements Origin {
  private static final long serialVersionUID = 1L;
  private final String origin_text;

  public DefaultOrigin(String origin_text) {
    this.origin_text = origin_text;
  }

  @Override
  public String getOriginText() {
    return origin_text;
  }

  @Override
  public String toString() {
    return getOriginText();
  }
}
