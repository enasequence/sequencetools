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
package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ChromosomeListFileReaderTest {
  @Test
  public void testRead_validFile() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("valid_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertTrue(parseResult.isValid());
    assertEquals(2, reader.getentries().size());

    Map<String, Long> clc = reader.getChromosomeLocationCount();
    assertEquals(1, clc.size());
    long v = clc.get("mitochondrion");
    assertEquals(1L, v);
  }

  @Test
  public void testRead_EmptyFile() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("empty_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertFalse(parseResult.isValid());
    assertEquals(1, parseResult.getMessages("EmptyFileCheck").size());
  }

  @Test
  public void testRead_invalidFileFormat() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("invalid_fileformat_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertFalse(parseResult.isValid());
    assertEquals(1, parseResult.getMessages("FileFormatCheck").size());
  }

  @Test
  public void testRead_duplilcateChromosomeNames() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("duplicate_name_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertFalse(parseResult.isValid());
    assertEquals(1, parseResult.getMessages("ChromosomeListChromosomeNameDuplicationCheck").size());
  }

  @Test
  public void testChromosomeNameFix() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("chr_name_fix_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertTrue(parseResult.isValid());
    assertEquals("test1", reader.chromosomeEntries.get(1).getChromosomeName());
  }

  @Test
  public void testRead_invalidnoofColumns() throws IOException {
    String fileName = null;
    URL url =
        ChromosomeListFileReaderTest.class
            .getClassLoader()
            .getResource("invalid_no_of_columns_chromosome_list.txt");
    if (url != null) fileName = url.getPath().replaceAll("%20", " ");
    ChromosomeListFileReader reader = new ChromosomeListFileReader(new File(fileName));
    ValidationResult parseResult = reader.read();
    assertFalse(parseResult.isValid());
    assertEquals(1, parseResult.getMessages("InvalidNoOfFields").size());
  }
}
