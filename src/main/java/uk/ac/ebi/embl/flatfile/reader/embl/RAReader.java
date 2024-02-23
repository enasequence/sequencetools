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
package uk.ac.ebi.embl.flatfile.reader.embl;

import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file RA lines. */
public class RAReader extends MultiLineBlockReader {

  public RAReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_SPACE);
  }

  @Override
  public String getTag() {
    return EmblTag.RA_TAG;
  }

  @Override
  protected void read(String block) {
    getCache().getReference().setAuthorExists(true);
    block = FlatFileUtils.remove(block, ';');
    for (String author : FlatFileUtils.split(block, ",")) {
      EmblPersonMatcher personMatcher = new EmblPersonMatcher(this);
      if (!personMatcher.match(author)) {
        getCache().getPublication().addAuthor(new ReferenceFactory().createPerson(author, null));
      } else {
        getCache().getPublication().addAuthor(personMatcher.getPerson());
      }
    }
  }
}
