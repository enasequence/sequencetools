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
package uk.ac.ebi.embl.template;

public enum TemplateTokenType {
  TAXON_FIELD,
  DATE_FIELD,
  TEXT_FIELD,
  TEXT_CHOICE_FIELD,
  INTEGER_FIELD,
  TEXT_AREA_FIELD,
  BOOLEAN_FIELD,
  start_location,
  end_location
}
