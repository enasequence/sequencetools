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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.*;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.fasta.reader.FastaFileReader;
import uk.ac.ebi.embl.fasta.reader.FastaLineReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

public class SubmitterAccessionFixTest {

  public static Map<String, String> fixMap =
      new HashMap<String, String>() {
        {
          // submitterAccession, fixedSubmitterAccession
          put("test1", "test1");
          put("  test1  ", "test1");
          put("__test1__", "test1");
          put("te____st1", "te_st1");
          put("te    st1", "test1");
          put("test1;", "test1");
          put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
          put("||test|test1||", "test_test1");
        }
      };

  private static final Map<String, String> fixMapFastaFileReader =
      new HashMap<String, String>() {
        {
          // submitterAccession, fixedSubmitterAccession
          put("test1", "test1");
          put("  test1  ", "test1");
          put("__test1__", "test1");
          put("te____st1", "te_st1");
          // put("te    st1", "test1"); // does not accept internal spaces
          put("test1;", "test1");
          put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
        }
      };

  private static final Map<String, String> fixMapEmblEntryReader =
      new HashMap<String, String>() {
        {
          // submitterAccession, fixedSubmitterAccession
          put("test1", "test1");
          put("  test1  ", "test1");
          put("__test1__", "test1");
          put("te____st1", "te_st1");
          // put("te    st1", "test1"); // does not accept internal spaces
          // put("test1;", "test1"); // does not accept semicolon
          put("\\/'\"test\\/'\"1\\/'\"", "test_1"); // does not accept semicolon
        }
      };

  private static final Map<String, String> fixMapAGPReader =
      new HashMap<String, String>() {
        {
          // submitterAccession, fixedSubmitterAccession
          put("test1", "test1");
          // put("  test1  ", "test1"); // does not accept leading or trailing spaces
          put("__test1__", "test1");
          put("te____st1", "te_st1");
          // put("te    st1", "test1"); // does not accept internal spaces
          put("test1;", "test1");
          put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
        }
      };

  private void assertFixEntry(
      String fixedSubmitterAccession,
      String submitterAccessionInEntry,
      String submitterAccessionInQualifier) {
    Entry entry = new Entry();
    entry.setSubmitterAccession(submitterAccessionInEntry);
    if (submitterAccessionInQualifier != null) {
      FeatureFactory factory = new FeatureFactory();
      SourceFeature feature = factory.createSourceFeature();
      feature.addQualifier(SUBMITTER_SEQID_QUALIFIER_NAME, submitterAccessionInQualifier);
      entry.addFeature(feature);
    }
    SubmitterAccessionFix.fix(entry);
    assertEquals(fixedSubmitterAccession, entry.getSubmitterAccession());
    if (submitterAccessionInQualifier != null) {
      assertEquals(
          fixedSubmitterAccession,
          entry.getPrimarySourceFeature().getSingleQualifierValue(SUBMITTER_SEQID_QUALIFIER_NAME));
    }
  }

  @Test
  public void testFixEntry() {
    // Submitter accession in entry only
    fixMap.forEach(
        (submitterName, fixedSubmitterName) ->
            assertFixEntry(fixedSubmitterName, submitterName, null));

    // Submitter accession in qualifier only
    fixMap.forEach(
        (submitterName, fixedSubmitterName) ->
            assertFixEntry(fixedSubmitterName, null, submitterName));

    // Submitter accession entry preference
    fixMap.forEach(
        (submitterName, fixedSubmitterName) ->
            assertFixEntry(fixedSubmitterName, submitterName, "ignore"));
  }

  @Test
  public void testFixString() {
    assertNull(SubmitterAccessionFix.fix(""));
    fixMap.forEach(
        (submitterName, fixedSubmitterName) ->
            assertEquals(fixedSubmitterName, SubmitterAccessionFix.fix(submitterName)));
  }

  @Test
  public void testFastaFileReader() {
    fixMapFastaFileReader.forEach(
        (submitterName, fixedSubmitterName) -> {
          String str = ">" + submitterName + "\n" + "TCTCCGTGAATGTCTATCATTCCTACACAGGACCC\n";
          BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
          FastaFileReader reader = new FastaFileReader(new FastaLineReader(bufferedReader));
          try {
            reader.read();
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
          Entry entry = reader.getEntry();
          assertEquals(fixedSubmitterName, entry.getSubmitterAccession());
        });
  }

  @Test
  public void testEmblEntryReaderACStarLine() {
    fixMapEmblEntryReader.forEach(
        (submitterName, fixedSubmitterName) -> {
          String str =
              "ID   XXX; SV XXX; linear; genomic DNA; XXX; XXX; 60 BP.\n"
                  + "XX\n"
                  + "AC * "
                  + submitterName
                  + "\n"
                  + "XX\n"
                  + "FH   Key             Location/Qualifiers\n"
                  + "FH\n"
                  + "FT   source          1..1260\n"
                  + "FT                   /organism=\"Caenorhabditis briggsae\"\n"
                  + "FT                   /strain=\"AF16\"\n"
                  + "FT                   /mol_type=\"genomic DNA\"\n"
                  + "XX\n"
                  + "SQ   Sequence 1320 BP; 385 A; 250 C; 216 G; 469 T; 0 other;\n"
                  + "     tagtcaaaca gtaattgccc aatttgatgg atactgtgaa ttaaatcgat ccgaatttca        60\n"
                  + "//\n";
          BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
          EmblEntryReader reader = new EmblEntryReader(bufferedReader);
          try {
            reader.read();
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
          Entry entry = reader.getEntry();
          assertEquals(fixedSubmitterName, entry.getSubmitterAccession());
        });
  }

  @Test
  public void testEmblEntryReaderIDLine() {
    fixMapEmblEntryReader.forEach(
        (submitterName, fixedSubmitterName) -> {
          String str =
              "ID   "
                  + submitterName
                  + "; SV XXX; linear; genomic DNA; XXX; XXX; 60 BP.\n"
                  + "XX\n"
                  + "FH   Key             Location/Qualifiers\n"
                  + "FH\n"
                  + "FT   source          1..1260\n"
                  + "FT                   /organism=\"Caenorhabditis briggsae\"\n"
                  + "FT                   /strain=\"AF16\"\n"
                  + "FT                   /mol_type=\"genomic DNA\"\n"
                  + "XX\n"
                  + "SQ   Sequence 1320 BP; 385 A; 250 C; 216 G; 469 T; 0 other;\n"
                  + "     tagtcaaaca gtaattgccc aatttgatgg atactgtgaa ttaaatcgat ccgaatttca        60\n"
                  + "//\n";
          BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
          EmblEntryReader reader =
              new EmblEntryReader(
                  bufferedReader, EmblEntryReader.Format.ASSEMBLY_FILE_FORMAT, null);
          try {
            reader.read();
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
          Entry entry = reader.getEntry();
          System.out.println(
              "XXX:original "
                  + submitterName
                  + ", fixed "
                  + fixedSubmitterName
                  + ",actual submitter name: "
                  + entry.getSubmitterAccession());
          assertEquals(fixedSubmitterName, entry.getSubmitterAccession());
        });
  }

  @Test
  public void testAGPFileReader() {
    fixMapAGPReader.forEach(
        (submitterName, fixedSubmitterName) -> {
          String str =
              "##agp-version	2.0\n"
                  + "# ORGANISM: Homo sapiens\n"
                  + "# TAX_ID: 9606\n"
                  + "# ASSEMBLY NAME: EG1\n"
                  + "# ASSEMBLY DATE: 09-November-2011\n"
                  + "# GENOME CENTER: NCBI\n"
                  + "# DESCRIPTION: Example AGP specifying the assembly of scaffolds from WGS contigs\n"
                  + submitterName
                  + "	1	330	1	W	"
                  + submitterName
                  + "	1	330	+\n"
                  + submitterName
                  + "	355	654	3	W	"
                  + submitterName
                  + "	1	300	+\n";
          BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
          AGPFileReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
          try {
            ValidationResult result = reader.read();
            assertTrue(result.isValid());
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
          Entry entry = reader.getEntry();
          assertEquals(fixedSubmitterName, entry.getSubmitterAccession());

          assertEquals(fixedSubmitterName, entry.getSequence().getAgpRows().get(0).getObject());
          assertEquals(fixedSubmitterName, entry.getSequence().getAgpRows().get(1).getObject());

          assertEquals(
              fixedSubmitterName, entry.getSequence().getAgpRows().get(0).getComponent_id());
          assertEquals(
              fixedSubmitterName, entry.getSequence().getAgpRows().get(1).getComponent_id());
        });
  }
}
