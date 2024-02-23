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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestCase;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public abstract class EmblReaderTest extends TestCase {

  protected Entry entry;
  protected LineReader lineReader;
  protected BufferedReader bufferedReader;

  protected void setUp() throws Exception {
    super.setUp();
    ValidationMessageManager.addBundle(FlatFileValidations.FLAT_FILE_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    Sequence sequence = (new SequenceFactory()).createSequence();
    entry.setSequence(sequence);
  }

  protected void setBufferedReader(String string) throws IOException {
    bufferedReader = new BufferedReader(new StringReader(string));
  }

  protected void initLineReader(String string) throws IOException {
    lineReader = new EmblLineReader(new BufferedReader(new StringReader(string)));
    lineReader.readLine();
  }
}
