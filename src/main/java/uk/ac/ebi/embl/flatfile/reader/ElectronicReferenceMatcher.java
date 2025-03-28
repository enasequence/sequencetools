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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.reference.ElectronicReference;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class ElectronicReferenceMatcher extends FlatFileMatcher {

  public ElectronicReferenceMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN = Pattern.compile("^\\s*\\(\\s*er\\s*\\)\\s*(.+)?$");

  private static final int GROUP_TEXT = 1;

  public ElectronicReference getElectronicReference(Publication publication) {
    ElectronicReference electronicReference = null;
    if (publication != null) {
      electronicReference = (new ReferenceFactory()).createElectronicReference(publication);
      electronicReference.setOrigin(publication.getOrigin());
    } else {
      electronicReference = (new ReferenceFactory()).createElectronicReference();
    }
    electronicReference.setText(getString(GROUP_TEXT));
    return electronicReference;
  }
}
