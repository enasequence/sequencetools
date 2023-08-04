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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.ReaderOptions;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;

public class GenbankSequenceReaderTest extends GenbankReaderTest {

  public void testSkipSequence() throws IOException {
    initLineReader(
        "        1 gatcctccat atacaacggt atctccacct caggtttaga tctcaacaac ggaaccattg\n"
            + "       61 ccgacatgag acagttaggt atcgtcgaga gttacaagct aaaacgagca gtagtcagct\n"
            + "      121 ctgcatctga agccgctgaa gttctactaa gggtggataa catcatccgt gcaagaccaa\n"
            + "      181 gaaccgccaa tagacaacat atgtaacata tttaggatat acctcgaaaa taataaaccg\n");
    ReaderOptions rO = new ReaderOptions();
    rO.setIgnoreSequence(true);
    ValidationResult result = (new SequenceReader(lineReader.setReaderOptions(rO))).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(entry.getSequence().getSequenceByte());

    result = (new SequenceReader(lineReader.setReaderOptions(new ReaderOptions()))).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getSequence().getSequenceByte());
  }

  public void testRead_Sequence() throws IOException {
    initLineReader(
        "        1 gatcctccat atacaacggt atctccacct caggtttaga tctcaacaac ggaaccattg\n"
            + "       61 ccgacatgag acagttaggt atcgtcgaga gttacaagct aaaacgagca gtagtcagct\n"
            + "      121 ctgcatctga agccgctgaa gttctactaa gggtggataa catcatccgt gcaagaccaa\n"
            + "      181 gaaccgccaa tagacaacat atgtaacata tttaggatat acctcgaaaa taataaaccg\n");
    ValidationResult result = (new SequenceReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    String expected =
        "gatcctccatatacaacggtatctccacctcaggtttagatctcaacaacggaaccattg"
            + "ccgacatgagacagttaggtatcgtcgagagttacaagctaaaacgagcagtagtcagct"
            + "ctgcatctgaagccgctgaagttctactaagggtggataacatcatccgtgcaagaccaa"
            + "gaaccgccaatagacaacatatgtaacatatttaggatatacctcgaaaataataaaccg";
    assertEquals(expected.length(), entry.getSequence().getLength());
    assertEquals(expected, new String(entry.getSequence().getSequenceByte()));
  }

  public void testRead_Origin() throws IOException {
    initLineReader(
        "        1 gatcctccat atacaacggt atctccacct caggtttaga tctcaacaac ggaaccattg\n"
            + "       61 ccgacatgag acagttaggt atcgtcgaga gttacaagct aaaacgagca gtagtcagct\n"
            + "      121 ctgcatctga agccgctgaa gttctactaa gggtggataa catcatccgt gcaagaccaa\n"
            + "      181 gaaccgccaa tagacaacat atgtaacata tttaggatat acctcgaaaa taataaaccg\n");
    ValidationResult result = (new SequenceReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    FlatFileOrigin origin = (FlatFileOrigin) entry.getSequence().getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(4, origin.getLastLineNumber());
  }
}
