package uk.ac.ebi.embl.api.validation.fixer.entry;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME;

public class SubmitterAccessionFixTest {

    public static Map<String, String> fixMap = new HashMap<String, String>() {{
        // submitterAccession, fixedSubmitterAccession
        put("test1", "test1");
        put("  test1  ", "test1");
        put("__test1__", "test1");
        put("te____st1", "te_st1");
        put("te    st1", "test1");
        put("test1;", "test1");
        put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
    }};

    private static Map<String, String> fixMapFastaFileReader = new HashMap<String, String>() {{
        // submitterAccession, fixedSubmitterAccession
        put("test1", "test1");
        put("  test1  ", "test1");
        put("__test1__", "test1");
        put("te____st1", "te_st1");
        // put("te    st1", "test1"); // does not parse internal spaces
        put("test1;", "test1");
        put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
    }};

    private static Map<String, String> fixMapEmblEntryReader = new HashMap<String, String>() {{
        // submitterAccession, fixedSubmitterAccession
        put("test1", "test1");
        put("  test1  ", "test1");
        put("__test1__", "test1");
        put("te____st1", "te_st1");
        // put("te    st1", "test1"); // does not parse internal spaces
        put("test1;", "test1");
        put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
    }};

    private static Map<String, String> fixMapAGPReader = new HashMap<String, String>() {{
        // submitterAccession, fixedSubmitterAccession
        put("test1", "test1");
        // put("  test1  ", "test1"); // does not parse leading or trailing spaces
        put("__test1__", "test1");
        put("te____st1", "te_st1");
        // put("te    st1", "test1"); // does not parse internal spaces
        put("test1;", "test1");
        put("\\/;'\"test\\/;'\"1\\/;'\"", "test_1");
    }};

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
            assertEquals(fixedSubmitterAccession, entry.getPrimarySourceFeature().getSingleQualifierValue(SUBMITTER_SEQID_QUALIFIER_NAME));
        }
    }

    @Test
    public void testFixEntry() {
        // Submitter accession in entry only
        fixMap.forEach((submitterName, fixedSubmitterName) ->
                assertFixEntry(fixedSubmitterName, submitterName, null));

        // Submitter accession in qualifier only
        fixMap.forEach((submitterName, fixedSubmitterName) ->
                assertFixEntry(fixedSubmitterName, null, submitterName));

        // Submitter accession entry preference
        fixMap.forEach((submitterName, fixedSubmitterName) ->
                assertFixEntry(fixedSubmitterName, submitterName, "ignore"));
    }

    @Test
    public void testFixString() {
        assertEquals(null, SubmitterAccessionFix.fix(""));
        fixMap.forEach((submitterName, fixedSubmitterName) ->
                assertEquals(fixedSubmitterName, SubmitterAccessionFix.fix(submitterName)));
    }

    @Test
    public void testFastaFileReader() {
        fixMapFastaFileReader.forEach((submitterName, fixedSubmitterName) -> {
            String str =
                    ">" + submitterName + "\n" +
                            "TCTCCGTGAATGTCTATCATTCCTACACAGGACCC\n";
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
    public void testEmblEntryReader() {
        fixMapEmblEntryReader.forEach((submitterName, fixedSubmitterName) -> {
            String str =
                    "ID   XXX; SV XXX; linear; genomic DNA; XXX; XXX; 60 BP.\n" +
                            "XX\n" +
                            "AC * " + submitterName + "\n" +
                            "XX\n" +
                            "FH   Key             Location/Qualifiers\n" +
                            "FH\n" +
                            "FT   source          1..1260\n" +
                            "FT                   /organism=\"Caenorhabditis briggsae\"\n" +
                            "FT                   /strain=\"AF16\"\n" +
                            "FT                   /mol_type=\"genomic DNA\"\n" +
                            "XX\n" +
                            "SQ   Sequence 1320 BP; 385 A; 250 C; 216 G; 469 T; 0 other;\n" +
                            "     tagtcaaaca gtaattgccc aatttgatgg atactgtgaa ttaaatcgat ccgaatttca        60\n" +
                            "//\n";
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
    public void testAGPFileReader() {
        fixMapAGPReader.forEach((submitterName, fixedSubmitterName) -> {
            String str =
                    "##agp-version	2.0\n" +
                            "# ORGANISM: Homo sapiens\n" +
                            "# TAX_ID: 9606\n" +
                            "# ASSEMBLY NAME: EG1\n" +
                            "# ASSEMBLY DATE: 09-November-2011\n" +
                            "# GENOME CENTER: NCBI\n" +
                            "# DESCRIPTION: Example AGP specifying the assembly of scaffolds from WGS contigs\n" +
                            submitterName + "	1	330	1	W	" + submitterName + "	1	330	+\n"+
                            submitterName + "	355	654	3	W	" + submitterName + "	1	300	+\n";
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

            assertEquals(fixedSubmitterName, entry.getSequence().getAgpRows().get(0).getComponent_id());
            assertEquals(fixedSubmitterName, entry.getSequence().getAgpRows().get(1).getComponent_id());
        });
    }
}