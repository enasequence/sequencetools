/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static uk.ac.ebi.embl.api.validation.helper.location.LocationToStringCoverter.renderCompoundLocation;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.RepositoryException;

public class CdsTranslatorTest {

    private Entry entry;
    private CdsFeature cdsFeature;
    private SourceFeature sourceFeature;
    private EntryFactory entryFactory = new EntryFactory();
    private SequenceFactory sequenceFactory = new SequenceFactory();
    private FeatureFactory featureFactory = new FeatureFactory();
    private LocationFactory locationFactory = new LocationFactory();
    private CdsTranslator cdsTranslator;
    private CdsTranslator fixingCdsTranslator;

    private boolean write = false;

    @Before
    public void setUp() throws Exception {
        entry = entryFactory.createEntry();
        cdsFeature = featureFactory.createCdsFeature();
        sourceFeature = featureFactory.createSourceFeature();
        entry.addFeature(cdsFeature);
        entry.addFeature(sourceFeature);
        EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
        property.taxonHelper.set(createMock(TaxonHelper.class));
        cdsTranslator = new CdsTranslator(property);
        EmblEntryValidationPlanProperty fixingProperty=new EmblEntryValidationPlanProperty();
        fixingProperty.taxonHelper.set(createMock(TaxonHelper.class));
        fixingProperty.isFixMode.set(true);
        fixingProperty.isFixCds.set(true);
        fixingCdsTranslator = new CdsTranslator(fixingProperty);
    }

    private void writeTranslation(TranslationResult translationResult,
                                  String expectedTranslation) throws IOException {
        TranslationResultWriter translationResultWriter = new TranslationResultWriter(
                translationResult, expectedTranslation);
        Writer writer = new OutputStreamWriter(System.out);
        translationResultWriter.write(writer);
        writer.flush();
        System.out.print("\n");
    }

    private boolean testValidTranslation (String expectedTranslation) {
        return testValidTranslation (cdsTranslator, expectedTranslation, null);
    }

    private boolean testValidTranslation (String expectedTranslation, String expectedMessageKey) {
        return testValidTranslation (cdsTranslator, expectedTranslation, expectedMessageKey);
    }

    private boolean testValidTranslationFixMode(String expectedTranslation, String expectedMessage) {
        return testValidTranslation (fixingCdsTranslator, expectedTranslation, expectedMessage);
    }

    @SuppressWarnings("unchecked")
    private boolean testValidTranslation(CdsTranslator translator, String expectedTranslation, String expectedMessageKey) {
        boolean isSuccess = true;

        try {
            ValidationResult validationResult = translator.translate(cdsFeature, entry);
            TranslationResult translationResult = null;
            if (validationResult instanceof ExtendedResult) {
                ExtendedResult<TranslationResult> extendedResult =
                        (ExtendedResult<TranslationResult>) validationResult;
                translationResult = extendedResult.getExtension();
            }
            if (validationResult.count() > 0 ) {

                System.out.print("--------------------\n");
                for (ValidationMessage message : validationResult.getMessages()) {
                    System.out.print("MESSAGE: "+ message.getMessage() + "\n");
                }
                if (expectedMessageKey != null) {
                    if (!(validationResult.count(expectedMessageKey, Severity.WARNING) >= 1 ||
                          validationResult.count(expectedMessageKey, Severity.ERROR) >= 1 ||
                           validationResult.count(expectedMessageKey, Severity.FIX) >= 1 )) {
                        isSuccess = false;
                    }
                }

                if (write) {
                    writeTranslation(translationResult, expectedTranslation);
                }
                if (!validationResult.isValid()) {
                    return false;
                }
            }

            String conceptualTranslation = cdsFeature.getTranslation();

            assertTrue(( expectedTranslation == null && cdsFeature.isPseudo()) ||
                       ( expectedTranslation != null && !cdsFeature.isPseudo()));

            if (conceptualTranslation == null) {
                if (expectedTranslation != null) {
                    if (write) {
                        System.out.print("FAILED TRANSLATION\n");
                        System.out.print("Conceptual translation is null\n");
                    }
                    isSuccess = false;
                }
            }
            else if(!conceptualTranslation.equals(expectedTranslation)) {
                if (write) {
                    System.out.print("FAILED TRANSLATION\n");
                    System.out.print("------------------\n");
                    writeTranslation(translationResult, expectedTranslation);
                }
                isSuccess = false;
            }
            else {
                isSuccess = true;
                if (write) {
                    System.out.print("SUCCESFULL TRANSLATION\n");
                    System.out.print("++++++++++++++++++++++\n");
                    writeTranslation(translationResult, expectedTranslation);
                }
            }
        }
        catch (IOException ex) {
            return false;
        } catch (RepositoryException e) {
            return false;
        }
        return isSuccess;
    }

    @SuppressWarnings("unchecked")
    public boolean testInvalidTranslation(String expectedMessageKey) {
        try {
            ValidationResult validationResult = cdsTranslator.translate(cdsFeature, entry);
            TranslationResult translationResult = null;
            if (validationResult instanceof ExtendedResult) {
                ExtendedResult<TranslationResult> extendedResult =
                        (ExtendedResult<TranslationResult>) validationResult;
                translationResult = extendedResult.getExtension();
            }
            if (validationResult.count(Severity.ERROR) == 0 &&validationResult.count(Severity.WARNING)==0) {
                if (write) {
                    System.out.print("NO ERROR\n");
                    System.out.print("----------------------\n");
                    if (translationResult != null) {
                        writeTranslation(translationResult, null);
                    }
                }
                return false;
            }
            if (validationResult.count(expectedMessageKey, Severity.ERROR) >= 1||validationResult.count(expectedMessageKey, Severity.WARNING) >= 1) {
                if (write) {
                    System.out.print("EXPECTED ERROR\n");
                    System.out.print("++++++++++++++++++\n");
                    for (ValidationMessage message : validationResult.getMessages()) {
                        System.out.print("MESSAGE: "+ message.getMessage() + "\n");
                    }
                    if (translationResult != null) {
                        writeTranslation(translationResult, null);
                    }
                }
                return true;
            }
            else {
                if (write) {
                    System.out.print("WRONG ERROR\n");
                    System.out.print("---------------\n");
                    for (ValidationMessage message : validationResult.getMessages()) {
                        System.out.print("MESSAGE: "+ message.getMessage() + "\n");
                    }
                    if (translationResult != null) {
                        writeTranslation(translationResult, null);
                    }
                }
                return false;
            }
        }
        catch (IOException ex) {
            return false;
        } catch (RepositoryException ex) {
            return false;
        }
    }

    @Test
    public void testTranslate1() {
        setSequenceAndOrganismForJcPolyomavirus();
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(266L, 481L));
        assertTrue(testValidTranslation(
                "MVLRQLSRKASVKVSKTWSGTKKRAQRILIFLLEFLLDFCTGEDSVDGKKRQKHSGLTEQ" +
                        "KYSALPEPKAT"
        ));
    }

    @Test
    public void testTranslate2() {
        setSequenceAndOrganismForJcPolyomavirus();
        sourceFeature.setScientificName("JC polyomavirus");
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(515L, 1549L));
        assertTrue(testValidTranslation(
                "MGAAVALLGDLVATVSEAAAATGFSVAEIAAGEAAATIEVEIASL" +
                        "ATVEGITSTSEAIAAIGLTPETYAVITGAPGAVAGFAALVQTVTGGSAIAQLGYRFFAD" +
                        "WDHKVSTVGLFQQPAMALQLFNPEDYYDILFPGVNAFVNNIHYLDPRHWGPSLFSTISQ" +
                        "AFWNLVRDDLPSLTSQEIQRRTQKLFVESLARFLEETTWAIVNSPVNLYNYISDYYSRL" +
                        "SPVRPSMVRQVAQREGTYISFGHSYTQSIDDADSIQEVTQRLDLKTPNVQSGEFIEKSL" +
                        "APGGANQRSAPQWMLPLLLGLYGTVTPALEAYEDGPNKKKRRKEGPRASSKTSYKRRSR" +
                        "SARS"
        ));
    }

    @Test
    public void testTranslate3() {
        setSequenceAndOrganismForJcPolyomavirus();
        sourceFeature.setScientificName("JC polyomavirus");
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(872L, 1549L));
        assertTrue(testValidTranslation(
                "MALQLFNPEDYYDILFPGVNAFVNNIHYLDPRHWGPSLFSTISQA" +
                        "FWNLVRDDLPSLTSQEIQRRTQKLFVESLARFLEETTWAIVNSPVNLYNYISDYYSRLS" +
                        "PVRPSMVRQVAQREGTYISFGHSYTQSIDDADSIQEVTQRLDLKTPNVQSGEFIEKSLA" +
                        "PGGANQRSAPQWMLPLLLGLYGTVTPALEAYEDGPNKKKRRKEGPRASSKTSYKRRSRS" +
                        "ARS"
        ));
    }

    @Test
    public void testTranslate4() {
        setSequenceAndOrganismForJcPolyomavirus();
        sourceFeature.setScientificName("JC polyomavirus");
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(2592L, 4415L));
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(4760L, 5002L));
        cdsFeature.getLocations().setComplement(true);
        assertTrue(testValidTranslation(
                "MDKVLNREESMELMDLLGLDRSAWGNIPVMRKAYLKKCKELHPDK" +
                        "GGDEDKMKRMNFLYKKMEQGVKVAHQPDFGTWNSSEVPTYGTDEWESWWNTFNEKWDED" +
                        "LFCHEEMFASDDENTGSQHSTPPKKKKKVEDPKDFPVDLHAFLSQAVFSNRTVASFAVY" +
                        "TTKEKAQILYKKLMEKYSVTFISRHGFGGHNILFFLTPHRHRVSAINNYCQKLCTFSFL" +
                        "ICKGVNKEYLFYSALCRQPYAVVEESIQGGLKEHDFNPEEPEETKQVSWKLVTQYALDI" +
                        "KCEDVFLLMGMYLDFQENPLQCKKCEKKDQPNHFNHHEKHYYNAQIFADSKNQKSICQQ" +
                        "AVDTVAAKQRVDSIHMTREEMLVERFNFLLDKMDLIFGAHGNAVLEQYMAGVAWIHCLL" +
                        "PQMDTVIYEFLKCIVLNIPKKRYWLFKGPIDSGKTTLAAALLDLCGGKSLNVNMPLERL" +
                        "NFELGVGIDQFMVVFEDVKGTGAESRDLPSGHGISNLDCLRDYLDGSVKVNLERKHQNK" +
                        "RTQVFPPGIVTMNEYSVPRTLQARFVRQIDFRPKAYLRKSLSCSEYLLEKRILQSGMTL" +
                        "LLLLIWFRPVADFAAAIHERIVQWKERLDLEISMYTFSTMKANVGMGRPILDFPREEDS" +
                        "EAEDSGHGSSTESQSQCSSQVSEASGADTQEHCTYHICKGFQCFKKPKTPPPK"
        ));
    }


    @Test
    public void testTranslate5() {
        setSequenceAndOrganismForJcPolyomavirus();
        sourceFeature.setScientificName("JC polyomavirus");
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(4484L, 5002L, true));
        assertTrue(testValidTranslation(
                "MDKVLNREESMELMDLLGLDRSAWGNIPVMRKAYLKKCKELHPDK" +
                        "GGDEDKMKRMNFLYKKMEQGVKVAHQPDFGTWNSSEVGCDFPPNSDTLYCKEWPNCATN" +
                        "PSVHCPCLMCMLKLRHRNRKFLRSSPLVWIDCYCFDCFRQWFGCDLTQEALHCWEKVLG" +
                        "DTPYRDLKL"
        ));
    }

    @Test
    public void testTranslate6() {
        setSequenceAndOrganismForJcPolyomavirus();
        sourceFeature.setScientificName("JC polyomavirus");
        cdsFeature.setStartCodon(1);
        cdsFeature.setException("Not a read exception");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setTranslation(
                "MDKVLNREESMELMDLLGLDRSAWGNIPVMRKAYLKKCKELHPDK" +
                        "GGDEDKMKRMNFLYKKMEQGVKVAHQPDFGTWNSSEVGCDFPPNSDTLYCKEWPNCATN" +
                        "PSVHCPCLMCMLKLRHRNRKFLRSSPLVWIDCYCFDCFRQWFGCDLTQEALHCWEKVLG" +
                        "DTPYRDLKL");
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(4484L, 5002L, true));
        assertTrue(testValidTranslation(
                cdsTranslator, "MDKVLNREESMELMDLLGLDRSAWGNIPVMRKAYLKKCKELHPDK" +
                        "GGDEDKMKRMNFLYKKMEQGVKVAHQPDFGTWNSSEVGCDFPPNSDTLYCKEWPNCATN" +
                        "PSVHCPCLMCMLKLRHRNRKFLRSSPLVWIDCYCFDCFRQWFGCDLTQEALHCWEKVLG" +
                        "DTPYRDLKL", "CDSTranslator-3"
        ));
    }

    @Test
    public void testTranslateStartCodon1() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("acgcgggttcacatcttcgaacggtcaaaattgggttcaattccttggagggacttacaa" +
                        "tgtcaccgatacacaagtgttcaatcttgcatatggaggagcgacgatcgattctgcgct" +
                        "cgttgcaccatatatgccgacagtgcaatcggtcgtaactcaggtctctctgtttgaaca" +
                        "attcctcgggtctaagcctgctggcgcatcatggaaaagcgacaacagtctcttcgcatt" +
                        "ttggataggcatcaatgacgttgggaactcatttgcgtggaacaatgtatcgcagagcgc" +
                        "atttcacacaacgctcatgaaacgtcttttcggtcaggttgaagagctctaccagtctgg" +
                        "cgctcgctcgttcctgttcctcactgttcctcccaccaaccgagccccactccttgtcgt" +
                        "gcaaggccccacggcaaccgcgcgaattgcatcgtccatcgccgactacaataaccaatt" +
                        "acggtctttcgtgacaaaatttaagcagcagcacaaggatcttgaccaggtcattgtctt" +
                        "cgacacgcagcccatcttcaacaccctgttgaacaacgctagaacgttcggatacgtcaa" +
                        "cacaacggggttttgtgaggcctatcaaaacggt").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(2);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L,634L));
        cdsFeature.getLocations().setLeftPartial(true);
        cdsFeature.getLocations().setRightPartial(true);
        assertTrue(testValidTranslation(
                "RGFTSSNGQNWVQFLGGTYNVTDTQVFNLAYGGATIDSALVAPYM" +
                        "PTVQSVVTQVSLFEQFLGSKPAGASWKSDNSLFAFWIGINDVGNSFAWNNVSQSAFHTT" +
                        "LMKRLFGQVEELYQSGARSFLFLTVPPTNRAPLLVVQGPTATARIASSIADYNNQLRSF" +
                        "VTKFKQQHKDLDQVIVFDTQPIFNTLLNNARTFGYVNTTGFCEAYQNG"
        ));
    }

    @Test
    public void testTranslateStartCodon1_2() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("gcacctcttgtttaaatggtcaaatgctcgagcacagagggatgggtttggcttgatggg" +
                        "aaggttggcgttcaaggggcagctactgacgtaagatgtgcccagtgacccccaggycat" +
                        "cttagcaagtcatcatattgtgaataacctatttaaaaaataaagatcataatgccagtg" +
                        "gagggatgatcaacagattgragrgcgcctagatgayggatagcatgaacatcgtgagtc" +
                        "crtgatcgtcttacggaaattctaacacatttctctctcccaggc").getBytes()
        ));
        sourceFeature.setScientificName("Balaenoptera bonaerensis");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(284L,285L));
        cdsFeature.getLocations().setLeftPartial(true);
        cdsFeature.getLocations().setRightPartial(true);
        assertTrue(testValidTranslation(
                "A"
        ));
    }

    @Test
    public void testTranslateStartCodon2() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("acaaattcgacggcttccgcttcgacggtgtcactagcatgatgtatctgcaccacggca" +
                        "ttggcacgggattctctgggggctatcatgaatatttcgggccaggcgtcgacgaggagg" +
                        "ccgtcgtctatctcatgctggctaacgatgccatgcactctctcttcccctcgattatca" +
                        "ccatagccgaagatgtgtcgggcatgccgctgctctgtatccccgtctcgaagggcggcg" +
                        "tcgggttcgactaccgcctctcgatggccgtgccggacatgtggatcaagctgctgaagc" +
                        "acaagtccgacggcgagtgggagatgggcgacatcgtgcacacgctcataaacaggcgcc" +
                        "acctcgagaagagcgtcgcatacgcggagagccacgaccaggcgctcgtgggcgacaaga" +
                        "ccctggcgttctggctgatggacaaggagatgt").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(3);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L,453L));
        cdsFeature.getLocations().setLeftPartial(true);
        cdsFeature.getLocations().setRightPartial(true);
        assertTrue(testValidTranslation(
                "KFDGFRFDGVTSMMYLHHGIGTGFSGGYHEYFGPGVDEEAVVYLM" +
                        "LANDAMHSLFPSIITIAEDVSGMPLLCIPVSKGGVGFDYRLSMAVPDMWIKLLKHKSDG" +
                        "EWEMGDIVHTLINRRHLEKSVAYAESHDQALVGDKTLAFWLMDKEM"
        ));
    }

    @Test
    public void testTranslateStartCodon3() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("acgcgggtgctctccgcaggcgcgatcatgacgccccacatcttacttctgtctggtgtc" +
                        "ggggatggttcgttgctctcctcactcggtattgaaacgatcgtggacctgcctgatgta" +
                        "ggccagaacttgcaggatcatcctctggtgtcgtcgtcatacaccgtcaactcaacgaac" +
                        "accctcgacaatctgacagcaaacgcgacgttgcttgcagaacagctacagcaatgggag" +
                        "agcacccgcaccggcgagctggttatcggaccgagcaaccaagtgggctggttgagacta" +
                        "cccggcaattcgtcgatcttcgaatctgcggtagatccgagcgctggccctacttccgcc" +
                        "cattaccaactattcttttcggatagcttcatatccttttcagaacctccgccgcctggc" +
                        "ggccatttccttaccgtattcaccaatctcatctctccctcgacacgcggcaatgtcacg" +
                        "ttggcctcgaaagatccctttgagtatcctgtcatacaaccgaacttctttagt").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L,534L));
        cdsFeature.getLocations().setLeftPartial(true);
        cdsFeature.getLocations().setRightPartial(true);
        assertTrue(testValidTranslation(
                "TRVLSAGAIMTPHILLLSGVGDGSLLSSLGIETIVDLPDVGQNLQ" +
                        "DHPLVSSSYTVNSTNTLDNLTANATLLAEQLQQWESTRTGELVIGPSNQVGWLRLPGNS" +
                        "SIFESAVDPSAGPTSAHYQLFFSDSFISFSEPPPPGGHFLTVFTNLISPSTRGNVTLAS" +
                        "KDPFEYPVIQPNFFS"
        ));
    }

    @Test
    public void testTranslateTranslationException1() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("gagacggacggtggccagggatcaggcagcggctcaggcgaccctgagtgtgcccccacc" +
                        "ccgccatggcccggctgctgcaggcgtcctgcctgctttccctgcttctggccggcttcg" +
                        "tcccgcagagccggggacaagagaagtcgaagatggactgccatggtggcataagtggca" +
                        "ccatttacgagtacggagccctcaccattgatggggaggagtacatccccttcaagcagt" +
                        "acgctggcaaatacgtcctctttgtcaacgtggccagctactgaggcctgacgggccagt" +
                        "acattgaactgaatgcactacaggaggagcttgcaccattcggtctcgtcattctgggct" +
                        "ttccctgcaaccaatttggaaaacaggaaccaggagagaactcagagatccttcccaccc" +
                        "tcaagtatgtccgaccaggtggaggctttgtccctaatttccagctctttgagaaagggg" +
                        "atgtcaatggagagaaagagcagaaattctacacttttctaaagaactcctgtcctccca" +
                        "cctcggagctcctgggtacatctgaccgcctcttctgggaacccatgaaggttcacgaca" +
                        "tccgctggaactttgagaagttcctggtggggccagatggtatacctatcatgcgctggc" +
                        "accaccggaccacggtcagcaacgtcaagatggacatcctgtcctacatgaggcggcagg" +
                        "cagccctgggggtcaagaggaagtaactgaaggccatcttatcccatgtccaccatgtag" +
                        "gggagagactttgttcaggaaggaatccgtttctccaaccacactatctacccaccacag" +
                        "acccctttcctatcactcaaggccccagcctggcacaaatggatgcatacagttctgtgt" +
                        "actgccaggcatgtgggtgtgggtgcatgtgggtgtttacacacatgcctacaggtatgt" +
                        "gtgactgtgtgtgtgtgcatgggtgtacagccacatgtctacctatgtgtctttctggga" +
                        "atgtgtaccatctgtgtgcctgcagctgtgtagtgctggagagtaacaaccctttctctc" +
                        "cagttctccattccaatgataatagttcacttacacctaaacccaaaggaaaaaccagct" +
                        "ctaggtccagttgttctgctctaaccgatacctccaccttggggccagcatctcccactg" +
                        "cctccaaatattagtaactacgactgacatccccagaagtttctgggtctaccacactgc" +
                        "ccaaccccccactcctacttcatgaagggccctcccaaggctacatccccaccccaccgt" +
                        "tctccctgagagaggtcaacctccctgagatcagccaaggcaggtacgtaccccatgtac" +
                        "ctgcaaggtatcagcaagggccacgtaccccatgtcaggggtggcgtcttcatgagggag" +
                        "gggcccaaagcccttgtgggcggacctcccctgagcctgtctgaggggccagcccttagt" +
                        "gcattcaggctaaggcccctgggcagggatgccacccctgctccttcggaggacgtgccc" +
                        "tctcccctcactggtccactggcatgagactcacccgtttgcccagtaaaagcctttctg" +
                        "cagcagctgaaaaaaaaaaaaaaaaaaaaa").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.addQualifier("transl_except", "(pos:282..284,aa:Sec)");
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(66L,746L));
        assertTrue(testValidTranslation(
                "MARLLQASCLLSLLLAGFVPQSRGQEKSKMDCHGGISGTIYEYGA" +
                        "LTIDGEEYIPFKQYAGKYVLFVNVASYUGLTGQYIELNALQEELAPFGLVILGFPCNQF" +
                        "GKQEPGENSEILPTLKYVRPGGGFVPNFQLFEKGDVNGEKEQKFYTFLKNSCPPTSELL" +
                        "GTSDRLFWEPMKVHDIRWNFEKFLVGPDGIPIMRWHHRTTVSNVKMDILSYMRRQAALG" +
                        "VKRK"
        ));
    }

    @Test
    public void testTranslateTranslationException2() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("ggccattatggccgggacctcagttttcttcagtccggcatttgcagcagagcgaaaggt" +
                        "ggtcgagtcctgaaggagggcctgatgtcttcatcattctcaaattcttgtaagctctgc" +
                        "gtcgggtgaaaccagacaaagccgcgagcccagggatgggagcacgcgggggacggcctg" +
                        "ccggcggggacgacagctttgcgcctgggtgcagcagcgtgcgtctcggggaagggaaga" +
                        "tattttaaggcgtgtctgagcagacggggaggcttttccaaacccaggcagcttcgtggc" +
                        "gtgtgcggtttcgacccggtcacacaaagcgtcagcatgtgaggacggtcgggccctgga" +
                        "aggaacgctctcggaactggccgcggaaaccgatctgcccgttgtgtttgtgaaacagag" +
                        "aaagataggcggccatggtccaaccttgaaggcttatcaggagggcagacttcaaaagct" +
                        "actaaaaatgaacggccctgaagatcttcccgagtcctatgactatgaccttatcatcat" +
                        "tggaggtggctcaggaggcctggcagctgctaaggaggcagcccaatatggcaagaaggt" +
                        "gatggtcctggactttgtcactcccacccctcttggaactagatggggtctcgaaggaac" +
                        "atgtgtgaatgtgggttgcatacctaaaaaactgatgcatcaagcagctttgttaggaca" +
                        "agccctacaagactctcgaaactatggatggaaagtcgaggagacagttaagcatgactg" +
                        "ggacagaatgatagaagctgtacagaatcacattggctctttgaattggggctaccgagt" +
                        "agctctgcgggagaaaaaagttgtctatgagaatgcttacgggcaatttattggtcctca" +
                        "caggattaaggcaacaaataataaaggcaaagaaaaaatttattcagcagagaggtttct" +
                        "cattgccactggtgaaagaccacgttacttgggcatccctggtgacaaagaatactgcat" +
                        "cagcagtgatgatcttttctccttgccttattgcccgggtaagaccctgattgttggagc" +
                        "atcctatgttgctttggagtgtgctggatttcttgccggtattggtttagacgtcactgt" +
                        "tatggttaggtccattcttcttagaggatttgaccaggacatggccaacaaaatcggtga" +
                        "acacatggaagaacatggcatcaagtttataagacagtttgtaccaattaaagttggaca" +
                        "aattgaagcggggacaccaggccgactcagagtagtagctcagtccaccaatagtgagga" +
                        "aatcattgaaggagaatataatacggtgttgctggcaataggaagagatgcttgcacaag" +
                        "aaaaattggcttagaaaccgtaggggtgaagataaatgaaaagactggaaaaatacctgt" +
                        "cacagatgaagaacagaccaatgtgccttacatctatgccattggcgatatattggagga" +
                        "taaggtggagctcaccccagttgcaatccaggcaggaagattgctggctcagaggctcta" +
                        "tgcaggttccactgtcaagtgtgattatgaaaatgttccaaccactgtatttactccttt" +
                        "ggaatatggtgcttgtggcctttctgaggagaaagctgtggagaagtttggggaagaaaa" +
                        "tattgaagtttaccatagttacttttggccattggaacggacgattccatcaagagataa" +
                        "caacaaatgttacgcaaaaataatctgtaatactaaagacaatgaacgtgttgtgggctt" +
                        "tcacgtactgggtccaaatgctggagaagttacacaaggctttgcagctgcactcaaatg" +
                        "tggactgaccaaaaagcagctggacagcacaattggaatccaccctgtctgtgcagaggt" +
                        "attcacaacattgtctgtgaccaagcgctctggggcaagcatcctccaggctggctgctg" +
                        "aggttaagccccagtgtggatgcttttgccaagactccaaaccactgactcgtttccgtg").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.addQualifier("transl_except", "(pos:1979..1981,aa:Sec)");
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(488L,1987L));
        assertTrue(testValidTranslation(
                "MNGPEDLPESYDYDLIIIGGGSGGLAAAKEAAQYGKKVMVLDFVT" +
                        "PTPLGTRWGLEGTCVNVGCIPKKLMHQAALLGQALQDSRNYGWKVEETVKHDWDRMIEA" +
                        "VQNHIGSLNWGYRVALREKKVVYENAYGQFIGPHRIKATNNKGKEKIYSAERFLIATGE" +
                        "RPRYLGIPGDKEYCISSDDLFSLPYCPGKTLIVGASYVALECAGFLAGIGLDVTVMVRS" +
                        "ILLRGFDQDMANKIGEHMEEHGIKFIRQFVPIKVGQIEAGTPGRLRVVAQSTNSEEIIE" +
                        "GEYNTVLLAIGRDACTRKIGLETVGVKINEKTGKIPVTDEEQTNVPYIYAIGDILEDKV" +
                        "ELTPVAIQAGRLLAQRLYAGSTVKCDYENVPTTVFTPLEYGACGLSEEKAVEKFGEENI" +
                        "EVYHSYFWPLERTIPSRDNNKCYAKIICNTKDNERVVGFHVLGPNAGEVTQGFAAALKC" +
                        "GLTKKQLDSTIGIHPVCAEVFTTLSVTKRSGASILQAGCUG"
        ));
    }

    @Test
    public void testTranslateTranslationException3() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setTranslationTable(1);
        cdsFeature.setStartCodon(1);
        cdsFeature.addQualifier("transl_except", "(pos:1979..1989,aa:Sec)");//outside the sequence range
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(488L,1987L));
        assertTrue(testInvalidTranslation("CDSTranslator-7"));

    }

    @Test
    public void testCodonException1() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("ggtggagctgccagagtaaagcaaagagaaaggaagcaggcccgttggaaggggttgtga" +
                        "caaccccagcaatgtggagaagcctggggcttgccctggctctctgtctcctcccattgg" +
                        "gaggaacagagagccaggaccaaagctccttatgtaagcaacccccagcctggagcataa" +
                        "gagatcaaggtccaatgctaaactccaatggttcagtgactgtggttgctcttcttcaag" +
                        "ccagctgatacctgtgcatactgcaggcatctaaattagaagacctgcgagtaaaactga" +
                        "agaaagaaggatattctaatatttctcatattgttgttaatcatcaaggaatctcttctc" +
                        "gattaaaatacacacaccttaagaataaggtttcagagcatattcctgtttatcagcaag" +
                        "aagaaaaccaaacagatgtctggactcttttaaatggaagcaaagatgacttcctcatat" +
                        "atgatagatgtggccgtcttgtatatcatcttggtttgcctttttccttcctaactttcc" +
                        "catatgtggaagaagccgttaagattgcttactgtgaaaagaaatgtggatactgctctc" +
                        "tcacgactctcaaagatgaagacttttgtaaaagtgtatctttggctactgtggataaaa" +
                        "cagttgaagctccatcgccccattaccatcatgagcatcatcacaatcacagacatcagc" +
                        "accttggcagcagtgagctttcagagaatcagcaaccaggagcaccagatgctcctactc" +
                        "atcctgctcctccaggccttcatcaccaccataagcacaagggtcaacataggcagggtc" +
                        "acccagagaaccgagatatgccaggaagtgaagatatacaagatttacaaaagaagctct" +
                        "gtcgaaagagatgtataaatcaattactctgtaaattgcccaaagattcagagttggctc" +
                        "ctaggagctgttgctgccattgtcgacatctgatatttgaaaaaacagggtctgcaatca" +
                        "cctgacagtgtaaagaaaacctcccatctttatgtagctgacagggacttcgggcagagg" +
                        "agaacataactgaatcttgtcagtgacgtttgcctccacctgcctgacaaataagtcagc" +
                        "agcttatacccacagaagccagtaccagttgacgctgaaagaatcaggcaaaaaagtgag" +
                        "aatgaccttcaaactaaatatttaaaataggacatactcccaaatttagtctagacacaa" +
                        "tttcatttccagcatttttataaactaccaaattagtgaaccaaaaatagaaattagatt" +
                        "tgtgcaaacatggagaaatctactgagttggcttccagattttaaatttcatgtcataga" +
                        "aatattgactcaaaccatattttttatgatggggcaactgaaaggtgattgcagcttttg" +
                        "gttaatatgtctttttttttctttttccagtgttctatttgctttaatgagaatagaaac" +
                        "gtaaactatgacctaggggtttctgttggatagttagcaatttagaatggaggaagaaca" +
                        "acaaagacatgctttccatttttttctttacttatctctcaaaacaacattactttgtct" +
                        "tttcagtcttctacttttaactaataaaagaagtggattttgtattttaagatccagaaa" +
                        "tacttaacaagtgaatattttgctaaaaaagcatatataactattttaaatatccattta" +
                        "tcttttgtatatctaagactcatcctgatttttactatcacacatgaataaagcctttgt" +
                        "ctctttctttctataatgttgtatcacactcttctaaaacttgagtggctgtcttaaaag" +
                        "atataaggggaaagataatattgtctgtctctgtattgcttagtaagtatttccatagtc" +
                        "aatgatggtttaataggtaaaccaaaccctataaacctgacctcctttatggttaatact" +
                        "attaaggaagaatgcagtacacaattggatacagtatggatttgtccaaata").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.addQualifier("codon","(seq:\"tga\",aa:Sec)");
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(72L,1217L));
        assertTrue(testValidTranslation(
                "MWRSLGLALALCLLPLGGTESQDQSSLCKQPPAWSIRDQGPMLNS" +
                        "NGSVTVVALLQASUYLCILQASKLEDLRVKLKKEGYSNISHIVVNHQGISSRLKYTHLK" +
                        "NKVSEHIPVYQQEENQTDVWTLLNGSKDDFLIYDRCGRLVYHLGLPFSFLTFPYVEEAV" +
                        "KIAYCEKKCGYCSLTTLKDEDFCKSVSLATVDKTVEAPSPHYHHEHHHNHRHQHLGSSE" +
                        "LSENQQPGAPDAPTHPAPPGLHHHHKHKGQHRQGHPENRDMPGSEDIQDLQKKLCRKRC" +
                        "INQLLCKLPKDSELAPRSCCCHCRHLIFEKTGSAITUQCKENLPSLCSUQGLRAEENIT" +
                        "ESCQURLPPPAUQISQQLIPTEASTSURUKNQAKKUEUPSN"
        ));
    }

    @Test
    public void testCodonException2() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("cagcgggaagcgcgctgcggtcccggtggcgccatgtccttctgcagcttcttcgggggc" +
                        "gaggttttccagaatcactttgagcccggcgtttacgtgtgtgccaagtgtggctatgag" +
                        "ctgttctccagccgctcgaagtacgcacactcgtctccatggccggcgttcaccgagacc" +
                        "attcatgccgacagcgtggccaagcgtccggagcacaatcgagccgaagccttgaaagtg" +
                        "tcctgtggcaagtgtggcaacgggttgggccacgagttcctgaacgacggccccaagcca" +
                        "gggcagtcccgattctgaatattcagcagctcgctgaagtttgtccctaaaggcaaagaa" +
                        "acttctgcctcccagggtcactaggcaggcagcccacacccaccccagacggccaccaca").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.addQualifier("codon","(seq:\"tga\",aa:Sec)");
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(34L,384L));
        assertTrue(testValidTranslation(
                "MSFCSFFGGEVFQNHFEPGVYVCAKCGYELFSSRSKYAHSSPWPA" +
                        "FTETIHADSVAKRPEHNRAEALKVSCGKCGNGLGHEFLNDGPKPGQSRFUIFSSSLKFV" +
                        "PKGKETSASQGH"
        ));
    }

    @Test
    public void testException1() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setTranslationTable(11);
        cdsFeature.setStartCodon(1);
        cdsFeature.setException("RNA editing");
        cdsFeature.setTranslation(
                "MSQSDARRTRKGREETLGKWIDGRRRKEELERELRKVTKNLKRLE" +
                        "EDNPWLGNIRGIIRKDKDGEGAPPAKRARTDQMEVDSGPRKRKHPGGFTEQERRDHRKK" +
                        "KALENKKRQLSSGGKSLSMEEEEELRRLTEEDERRERRVAGPRVGDVNPLEGPPRGAPG" +
                        "GGFIPTMEGVPESPFSRRGDGLDTRGTQGFPWVGPGSPSPRFPLLECTPQ");
        LocalRange localRange = locationFactory.createLocalRange(961L, 1602L);
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testValidTranslation(
                "MSQSDARRTRKGREETLGKWIDGRRRKEELERELRKVTKNLKRLE" +
                        "EDNPWLGNIRGIIRKDKDGEGAPPAKRARTDQMEVDSGPRKRKHPGGFTEQERRDHRKK" +
                        "KALENKKRQLSSGGKSLSMEEEEELRRLTEEDERRERRVAGPRVGDVNPLEGPPRGAPG" +
                        "GGFIPTMEGVPESPFSRRGDGLDTRGTQGFPWVGPGSPSPRFPLLECTPQ"
        ));
    }

    @Test
    public void testException2() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setException("RNAediting");
        cdsFeature.setTranslation(
                "MSRSDKKEKRGGREDILEQWVSTRGRIGELERDLRKAKKKIKKLE" +
                        "DDNPWLGNIKGIIGKKDRDGEGAPPAKRARTDRMEVDSGPGKGPLRGRFTEQERRDHRR" +
                        "RKALENKKKQLSGGGKNLSKEEEEELRRLTGEDEERKRRTAGPPVGGVNPLEGAARGAP" +
                        "GGGFVPSMQGVPESIFTRTGEGLDVRGDRGFPWDILFPSDPPFSPQSCRPQ"
        );
        LocalRange localRange=locationFactory.createLocalRange(948L,1592L);
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testValidTranslation(
                "MSRSDKKEKRGGREDILEQWVSTRGRIGELERDLRKAKKKIKKLE" +
                        "DDNPWLGNIKGIIGKKDRDGEGAPPAKRARTDRMEVDSGPGKGPLRGRFTEQERRDHRR" +
                        "RKALENKKKQLSGGGKNLSKEEEEELRRLTGEDEERKRRTAGPPVGGVNPLEGAARGAP" +
                        "GGGFVPSMQGVPESIFTRTGEGLDVRGDRGFPWDILFPSDPPFSPQSCRPQ"
        ));
    }

    @Test
    public void testInvalidTranslationException1() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setException("RNAediting");
        LocalRange localRange=locationFactory.createLocalRange(948L,1592L);
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testInvalidTranslation("CDSTranslator-1"));
    }

    @Test
    public void testInvalidTranslationAminoAcid1() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setTranslation(
                "SRSD1KEKRGGREDILEQWVSTRGRIGELERDLRKAKKKIKKLE" +
                        "DDNPWLGNIKGIIGKKDRDGEGAPPAKRARTDRMEVDSGPGKGPLRGRFTEQERRDHRR" +
                        "RKALENKKKQLSGGGKNLSKEEEEELRRLTGEDEERKRRTAGPPVGGVNPLEGAARGAP" +
                        "GGGFVPSMQGVPESIFTRTGEGLDVRGDRGFPWDILFPSDPPFSPQSCRPQ"
        );
        LocalRange localRange=locationFactory.createLocalRange(948L,1592L);
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testInvalidTranslation("CdsFeatureAminoAcidCheck"));
    }

    @Test
    public void testInvalidTranslationAminoAcid2() {
        setSequenceAndOrganismForUnclassified();
        cdsFeature.setTranslation(
                "SRSD*KEKRGGREDILEQWVSTRGRIGELERDLRKAKKKIKKLE" +
                        "DDNPWLGNIKGIIGKKDRDGEGAPPAKRARTDRMEVDSGPGKGPLRGRFTEQERRDHRR" +
                        "RKALENKKKQLSGGGKNLSKEEEEELRRLTGEDEERKRRTAGPPVGGVNPLEGAARGAP" +
                        "GGGFVPSMQGVPESIFTRTGEGLDVRGDRGFPWDILFPSDPPFSPQSCRPQ"
        );
        LocalRange localRange=locationFactory.createLocalRange(948L,1592L);
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testInvalidTranslation("CdsFeatureAminoAcidCheck"));
    }

    @Test
    public void testInvalidTranslationSequenceLength() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("gtgagcctcgtgccgaccgaagggcgcgatgggggagggggatcctccgagaggggatac"+
                        "ccactaaagagagggagataccctgagggagactgctcccaagaagttccaagagaatct"+
                        "caagaatgaggaggatccccaggacgctggagaaatctaggaatgggaaagaggagggcg").getBytes()
        ));

        sourceFeature.setScientificName("unclassified");
        cdsFeature.setTranslation(
                "SRSDKEKRGGREDILEQWVSTRGRIGELERDLRKAKKKIKKLE" +
                        "DDNPWLGNIKGIIGKKDRDGEGAPPAKRARTDRMEVDSGPGKGPLRGRFTEQERRDHRR" +
                        "RKALENKKKQLSGGGKNLSKEEEEELRRLTGEDEERKRRTAGPPVGGVNPLEGAARGAP" +
                        "GGGFVPSMQGVPESIFTRTGEGLDVRGDRGFPWDILFPSDPPFSPQSCRPQ"
        );
        LocalRange localRange=locationFactory.createLocalRange(-1L,1592L);//bad ranges
        localRange.setComplement(true);
        cdsFeature.getLocations().addLocation(localRange);
        assertTrue(testInvalidTranslation("CDSTranslator-4"));
    }

    @Test
    public void testInvalidTranslationSequenceNoSequence() {
        assertTrue(testInvalidTranslation("CDSTranslator-5"));
    }


    @Test
    public void testFixNoStartCodonMake5Partial() { // ProteinTranslatorTest.cxx: ProteinTranslatorFixCds.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("ttttag").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 6L, false));
        String translation = "F";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-18"));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNoStartCodonMake5Partial"));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertEquals("<1..6", renderCompoundLocation(cdsFeature.getLocations()));
    }


    @Test
    public void testFixNoStartCodonMake5PartialGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("ctaaaa").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().setComplement(true);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 6L, false));
        String translation = "F";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-18"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNoStartCodonMake5Partial"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertEquals("complement(1..>6)", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixValidStopCodonRemove3Partial() { // ProteinTranslatorTest.cxx: ProteinTranslatorErrorStopCodonPartial.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("atgtag").getBytes())); // M*
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 6L, false));
        cdsFeature.getLocations().setRightPartial(true);
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-14"));
        assertEquals("1..>6", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixValidStopCodonRemove3Partial"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertEquals("1..6", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixValidStopCodonRemove3PartialGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte("ctacat".getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().setComplement(true);
        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 6L, false));
        cdsFeature.getLocations().setLeftPartial(true);
        String translation = "M";
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-14"));
        assertEquals("complement(<1..6)", renderCompoundLocation(cdsFeature.getLocations()));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixValidStopCodonRemove3Partial"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertEquals("complement(1..6)", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixNoStopCodonMake3Partial() { // ProteinTranslatorTest.cxx: ProteinTranslatorErrorMissingStopCodon.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("atg").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 3L, false));
        write = true;
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-15"));
        assertEquals("1..3", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNoStopCodonMake3Partial"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertEquals("1..>3", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixNoStopCodonMake3PartialGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("cat").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().setComplement(true);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 3L, false));
        write = true;
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-15"));
        assertEquals("complement(1..3)", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNoStopCodonMake3Partial"));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertEquals("complement(<1..3)", renderCompoundLocation(cdsFeature.getLocations()));
    }


    @Test
    public void testErrorMoreThanOneStopCodon() { // ProteinTranslatorTest.cxx: ProteinTranslatorComplementError.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("ctactacatgattaggatccgcatg").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 9L, true));
        write = true;
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-13"));
        assertEquals("complement(1..9)", renderCompoundLocation(cdsFeature.getLocations()));

    }

    @Test
    public void testErrorShortSeq() { // ProteinTranslatorTest.cxx: ProteinTranslatorErrorShortSequence.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("atggatgaggattaggatccgcatg").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 2L, false));
        write = true;
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-10"));
        assertEquals("1..2", renderCompoundLocation(cdsFeature.getLocations()));

    }

    @Test
    public void testFixMultipleOfThree() { // ProteinTranslatorTest.cxx: ProteinTranslatorError5.in
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("atggatga").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(5L, 8L, false));
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-11"));
        assertEquals("5..8", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNonMultipleOfThreeMake3And5Partial"));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertEquals("<5..>8", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixMultipleOfThreeGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("tcatccat").getBytes()));
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().setComplement(true);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(5L, 8L, false));
        String translation = "M";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-11"));
        assertEquals("complement(5..8)", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixNonMultipleOfThreeMake3And5Partial"));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertEquals("complement(<5..>8)", renderCompoundLocation(cdsFeature.getLocations()));
    }


    @Test
    public void testFixInternalStopCodonMakePseudo() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("atgtagaaatag").getBytes())); // M*K*
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 12L, false));
        String translation = null;
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-17"));
        assertEquals("1..12", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(cdsFeature.getQualifiers(Qualifier.PSEUDO_QUALIFIER_NAME).isEmpty());
        assertTrue(testValidTranslationFixMode(translation, "fixInternalStopCodonMakePseudo"));
        assertTrue(cdsFeature.getQualifiers(Qualifier.PSEUDO_QUALIFIER_NAME).size() == 1);
        assertNull(cdsFeature.getTranslation());
        assertEquals("1..12", renderCompoundLocation(cdsFeature.getLocations()));
    }


    @Test
    public void testFixInternalStopCodonMakePseudoGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("ctatttctacat").getBytes())); // M*K*
        cdsFeature.setStartCodon(1);
        cdsFeature.setTranslationTable(11);
        cdsFeature.getLocations().setComplement(true);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 12L, false));
        write = true;
        String translation = null;
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-17"));
        assertEquals("complement(1..12)", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(cdsFeature.getQualifiers(Qualifier.PSEUDO_QUALIFIER_NAME).isEmpty());
        assertTrue(testValidTranslationFixMode(translation, "fixInternalStopCodonMakePseudo"));
        assertTrue(cdsFeature.getQualifiers(Qualifier.PSEUDO_QUALIFIER_NAME).size() == 1);
        assertNull(cdsFeature.getTranslation());
        assertEquals("complement(1..12)", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixCodonStartNotOneMake5Partial() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("aatgagtaaatag").getBytes())); // MSK*
        cdsFeature.setStartCodon(2);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 13L, false));
        write = true;
        String translation = "MSK";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-3"));
        assertEquals("1..13", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixCodonStartNotOneMake5Partial"));
        assertTrue(cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertEquals("<1..13", renderCompoundLocation(cdsFeature.getLocations()));
    }

    @Test
    public void testFixCodonStartNotOneMake5PartialGlobalComplement() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(("ctatttactcatt").getBytes())); // MSK*
        cdsFeature.getLocations().setComplement(true);
        cdsFeature.setStartCodon(2);
        cdsFeature.setTranslationTable(11);

        cdsFeature.getLocations().addLocation(locationFactory.createLocalRange(1L, 13L, false));
        write = true;
        String translation = "MSK";
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(!testValidTranslation(translation, "Translator-3"));
        assertEquals("complement(1..13)", renderCompoundLocation(cdsFeature.getLocations()));

        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(!cdsFeature.getLocations().isRightPartial());
        assertTrue(testValidTranslationFixMode(translation, "fixCodonStartNotOneMake5Partial"));
        assertTrue(!cdsFeature.getLocations().isLeftPartial());
        assertTrue(cdsFeature.getLocations().isRightPartial());
        assertEquals("complement(1..>13)", renderCompoundLocation(cdsFeature.getLocations()));
    }
    private void setSequenceAndOrganismForJcPolyomavirus() {
        sourceFeature.setScientificName("JC polyomavirus");
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("gcctcggcctcctgtatatataaaaaaaagggaaggtagggaggagctggctaaaactgg" +
                        "atggctgccagccaagcatgagctcatacctagggagccaaccagctggcagccagaggg" +
                        "agccctggctgcatgccactggcagttatagtgaaacccctcccatagtccttaatcaca" +
                        "agtaaacaaagcacaaggggaagtggaaagcagccaggagcatgttttgcgagccagagc" +
                        "tgttttggcttgtcaccagctggccatggttcttcgccagctgtcacgtaaggcttctgt" +
                        "gaaagttagtaaaacctggagtggaacaaaaaaaagagctcaaaggattttaattttttt" +
                        "gttagaatttttgctggatttttgcacaggtgaagacagtgtagacgggaaaaaaagaca" +
                        "gaaacacagtggtttgactgagcagaaatacagtgctttgcctgaaccaaaagctacata" +
                        "ggtaagtaatgtttttttttgtgttttcaggttcatgggtgccgcagttgcacttttggg" +
                        "ggacctagttgctactgtttctgaggctgctgctgccacaggattttcagtagctgaaat" +
                        "tgctgctggagaggctgctgctactatagaagttgaaattgcatcccttgctactgtaga" +
                        "ggggattacaagtacctctgaggctatagctgcaataggccttactcctgaaacatatgc" +
                        "tgtaattactggagctccgggggctgtagctgggtttgctgcattggttcaaactgtaac" +
                        "tggtggtagtgctattgctcagttgggatatagattttttgctgactgggatcataaagt" +
                        "ttcaacagttgggctttttcagcagccagctatggctttacaattatttaatccagaaga" +
                        "ctactatgatattttatttcctggagtgaatgcctttgttaacaatattcactatttaga" +
                        "tcctagacattggggcccttctttgttctccacaatctcccaggctttttggaatcttgt" +
                        "tagagatgatttgccatctttaacatctcaggaaattcagagaagaacccaaaaactatt" +
                        "tgttgaaagtttagcaaggtttttggaagaaactacttgggcaatagttaattcaccagt" +
                        "taacttatataattatatttcagactattattctagattgtctccagttaggccctctat" +
                        "ggtaaggcaggttgcccaaagggagggaacctatatttcctttggccactcatataccca" +
                        "aagtatagatgatgcagacagcattcaagaagttacccaaaggctagatttaaaaacccc" +
                        "aaatgtgcaatctggtgaatttatagagaaaagtcttgcaccaggaggtgcaaatcaaag" +
                        "atctgctcctcaatggatgttgcctttacttttagggttgtacgggactgtaacacctgc" +
                        "tcttgaagcatatgaagatggccccaacaaaaagaaaaggagaaaggaaggaccccgtgc" +
                        "aagttccaaaacttcttataagaggaggagtagaagtgctagaagttaaaactggggttg" +
                        "actcaattacagaggtagaatgctttttaactccagaaatgggtgacccagatgagcatc" +
                        "ttaggggttttagtaagtcaatttctatatcagatacatttgaaagtgactccccaaata" +
                        "aggacatgcttccttgttacagtgtggccagaattccactgcccaatctaaatgaggatc" +
                        "tgacctgtggaaatatactaatgtgggaggctgttaccttaaaaactgaggttatagggg" +
                        "tgacaactttgatgaatgtgcactctaatggtcaagcaactcatgacaatggtgcaggaa" +
                        "agccagtgcagggcaccagctttcattttttttctgttgggggggaggctttagaattac" +
                        "agggggtggtttttaattacagaacaaagtacccagatggaacaatttttccaaagaatg" +
                        "caacagtgcaatctcaagtaatgaacacagagcacaaggcgtacctagataagaacaagg" +
                        "catatcctgttgaatgttgggttcctgatcccaccagaaatgaaaacacaagatattttg" +
                        "ggacactaacaggaggagaaaatgttcctccagttcttcatataacaaacactgccacaa" +
                        "cagtgctgcttgatgaatttggtgttgggccactttgcaaaggtgacaacttgtatttgt" +
                        "cagctgttgatgtttgtggcatgtttactaacagatctggttcccagcagtggagaggac" +
                        "tgtccagatattttaaggttcagctaagaaaaaggagggttaaaaacccctacccaattt" +
                        "ctttccttcttactgatttaattaacagaaggacccctagagttgatgggcagcctatgt" +
                        "atggcatggatgctcaggtagaggaggttagagtttttgaggggacagaggaacttccag" +
                        "gggacccagacatgatgagatatgttgacagatatggacagttgcagacaaagatgctgt" +
                        "aatcaaaggcctttattgtaatatgcagtacattttaataaagtataaccagctttactt" +
                        "tactgttgcagttattttgggggaggggtttttggttttttgaaacattgaaagccttta" +
                        "cagatgtgataagtgcagtgttcctgtgtgtctgcaccagaggcttctgagacctgggaa" +
                        "gagcattgtgattgagattcagtgcttgatccatgtccagagtcttctgcttcagaatct" +
                        "tcctctctaggaaagtcaagaatgggtctccccataccaacattagctttcatagtagaa" +
                        "aatgtatacatgcttatttctaaatccagcctttctttccactgcacaatcctctcatga" +
                        "atggcagctgcaaagtcagcaactggcctaaaccagattaaaagcaaaagcaaagtcatg" +
                        "ccactttgcaaaatccttttttctagtaaatattcagagcagcttagtgattttcttagg" +
                        "taggccttaggtctaaaatctatttgccttacaaatctggcttgtaaagttctaggcact" +
                        "gaatattcattcatggttacaattccaggtggaaacacctgtgttcttttgttttggtgt" +
                        "tttctctctaaattaactttcacacttccatctaagtaatctcttaggcaatcaaggttg" +
                        "cttatgccatgccctgaaggtaaatcccttgactctgcaccagtgccttttacatcctca" +
                        "aatacaaccataaactgatctatacccactcctaactcaaagtttaatctttctaatggc" +
                        "atgttaacatttaatgactttcccccacagagatcaagtaaagctgcagctaaagtagtt" +
                        "ttgccactgtctattggccccttgaatagccagtacctttttttgggaatgtttaataca" +
                        "atgcattttaagaactcataaatgacagtgtccatttgaggcagcaaacaatgaatccag" +
                        "gccaccccagccatatattgctctaaaacagcattgccatgtgccccaaaaattaagtcc" +
                        "attttatcaagcaaaaaattaaacctttcaactaacatttcttctctggtcatatggata" +
                        "ctgtcaaccctttgtttggctgctacggtatcaacagcctgctggcaaatgcttttttga" +
                        "tttttgctatctgcaaaaatttgggcattataatagtgcttttcatgatggttaaagtga" +
                        "tttggctgatcctttttttcacattttttgcattgcagtgggttttcctgaaagtctaag" +
                        "tacatgcccataagcaagaaaacatcctcacacttgatgtccaaagcatactgtgtaact" +
                        "aatttccatgaaacctgcttagtttcttctggttcttctgggttaaagtcatgctcctta" +
                        "aggcccccctgaatactttcttccactactgcatatggctgtctacacaaggcactgtaa" +
                        "aacaagtattccttattcacacctttacaaattaaaaaactaaaggtacatagtttttga" +
                        "cagtagttattaattgctgaaactctgtgtctatgtggtgttaaaaaaaacaaaatatta" +
                        "tgacccccaaaaccatgtctacttataaaggttacagagtatttttccataagtttctta" +
                        "tataaaatttgagctttttctttagtggtatacacagcaaaagaagcaacagttctatta" +
                        "ctaaacacagcttgactgaggaatgcatgcagatctacaggaaagtctttagggtcttct" +
                        "accttttttttcttcttaggtggggtagagtgctgggatcctgtgttttcatcatcactg" +
                        "gcaaacatttcttcatggcaaaacaggtcttcatcccacttctcattaaatgtattccac" +
                        "caggattcccattcatctgttccataggttggcacctaagaacaaaaaattaagtttatt" +
                        "gtaaaaaacaaaatgccctgcaaaacaaaaattgtggtttaccttaaagctttagatccc" +
                        "tgtagggggtgtctccaagaaccttctcccagcaatgaagagcttcttgggttaagtcac" +
                        "acccaaaccattgtctgaagcaatcaaagcaatagcaatctatccacacaagtgggctgc" +
                        "ttcttaaaaattttctgtttctatgccttaattttagcatgcacattaaacagggacaat" +
                        "gcactgaaggattagtggcacaattaggccattccttgcaataaagggtatcagaattag" +
                        "gaggaaaatcacaaccaacctctgaactattccatgtaccaaaatcaggctgatgagcaa" +
                        "cttttacaccttgttccatttttttatataaaaaattcattctcttcattttgtcttcgt" +
                        "ccccacctttatcagggtgaagttctttgcattttttcagataagcttttctcatgacag" +
                        "gaatgtttccccatgcagatctatcaaggcctaataaatccatgagctccatggattcct" +
                        "ccctattcagcactttgtccatttttgctttttgtagcaaaaaattaaagcaaaaaaggg" +
                        "aaaaacaagggaatttccctggcctcctaaaaagcctccacgcccttactacttctgagt" +
                        "aagcttggaggcggaggcg").getBytes()
        ));
    }

    private void setSequenceAndOrganismForUnclassified() {
        entry.setSequence(sequenceFactory.createSequenceByte(
                ("gtgagcctcgtgccgaccgaagggcgcgatgggggagggggatcctccgagaggggatac"+
                        "ccactaaagagagggagataccctgagggagactgctcccaagaagttccaagagaatct"+
                        "caagaatgaggaggatccccaggacgctggagaaatctaggaatgggaaagaggagggcg"+
                        "gaaaaaatggagcggcctcccgattcgaacggcccgacgccagaactggagagcactccg"+
                        "ggccgtacggtcgggaaccctccagagggaagaagccacacggagtagaacggaaaaatc"+
                        "acctccagaggaccccttcagcgaacagaaggctctctcacgcggcaggagtaagaccat"+
                        "agcgtaggaagagatgctaggagttgggggagaccgaagcgaggagaaaagtaaagagag"+
                        "caacggggctagccagtgggtgttccgccccccaagaggcacgagtgaggcttatcccgg"+
                        "ggaactcggcgaatcgtccccacatagcagagccccggaccctcttccaaagagaccgga"+
                        "gggggtggcttggagcgtgggggagccgtgggtccgtgggatgctcctcccgattccgtc"+
                        "caatccccccccccgagaggtcccccaggaatggcgggaccccactcggcagggtccgcg"+
                        "taccatcctttcttacctgatggccggcatggtcccagcctcctcgctggcgccggctgg"+
                        "gcaacattccgaggggaccgtcccctcggtaatggcgaatgggacccagaactctctctg"+
                        "attccaagtgagaatcgagagaaaactggctctcccttagccatccgagtggacgttgtc"+
                        "ctccttcggatgcccaggtcggaccgcgaggaggtggagatgccatgccgacccgaagag"+
                        "gaaagaaggacgcgagacgcaaacctgtgagtggacacccgctttattcactggggacga"+
                        "caactctggggagagaagggaggatcggatgggaagagtatatcctacgggaatccccgg"+
                        "tctcccctcacgtccagcccctccccggtccgagtgaagatggactccgggaccccttgc"+
                        "atgctggggacgaagccgcccccgggcgctcccctcgctgctccctcgagggggttcaca"+
                        "cccccaactggcgggccggctgttcttcttttcctttcctcgtcttctccggtcaacctc"+
                        "ctaagttcctcttcttcttccttgctgaggttcttccctcccccggagagctgcttcttc"+
                        "ttgttctcgagggccttccttcttcggtgatcccgcctctcctgktcggtgaatcttccc"+
                        "ctgagaggtccctttccgggtccggagtctacctccatcctgtccgtccgggccctcttc"+
                        "gccgggggagccccttctccgtccctatccttctttccaattattcctttgatgtttccc"+
                        "agccagggattatcatcctcgagcttcttgatcttcttcttggccttccggagatctctc"+
                        "tcgagttctcctattcttcctcttgtggatacccactgctcgagaatgtcctcccgtcct"+
                        "cctcgcttctccttcttgtcggaccggctcatctcggcaagaggcggacggtcctcagtg"+
                        "ctcttactcttttctgtaaagaggagactgctggactcgtcgccccagtccgag").getBytes()
        ));
        sourceFeature.setScientificName("unclassified");
    }
}