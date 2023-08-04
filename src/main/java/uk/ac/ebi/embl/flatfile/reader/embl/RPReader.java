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
package uk.ac.ebi.embl.flatfile.reader.embl;

import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file RP lines. */
public class RPReader extends MultiLineBlockReader {

  public RPReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_NOSPACE);
  }

  @Override
  public String getTag() {
    return EmblTag.RP_TAG;
  }

  @Override
  protected void read(String block) {
    EmblReferenceLocationMatcher matcher = new EmblReferenceLocationMatcher(this);
    Reference reference = getCache().getReference();
    CompoundLocation<LocalRange> locations = reference.getLocations();
    if (locations.getOrigin() == null) {
      locations.setOrigin(getOrigin());
    }
    for (String value : FlatFileUtils.split(block, ",")) {
      if (!matcher.match(value)) {
        error("RP.1", getTag());
      } else {
        reference.getLocations().addLocation(matcher.getLocation());
      }
    }
  }
}
