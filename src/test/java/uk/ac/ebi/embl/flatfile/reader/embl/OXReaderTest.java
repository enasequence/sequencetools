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

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class OXReaderTest extends EmblReaderTest {

  public void testReadWithCommonName() throws IOException {
    initLineReader("OX   NCBI_TaxID=1423;\n");
    OXReader reader = (new OXReader(lineReader));
    reader.getCache().setScientificName("Trifolium repens");
    ValidationResult result = reader.read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(Long.valueOf(1423), reader.getCache().getTaxId("Trifolium repens"));
  }
}
