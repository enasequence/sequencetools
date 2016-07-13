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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;

public class TranslatorTest {

	private class TranslatorTester extends Translator{
	  
	    private boolean write = false;
	    
	    private void writeTranslation(TranslationResult translationResult, 
	    		String expectedTranslation) throws IOException {
            TranslationResultWriter translationResultWriter = new TranslationResultWriter(
            		translationResult, expectedTranslation);
            Writer writer = new OutputStreamWriter(System.out);
            translationResultWriter.write(writer);
            writer.flush();	    
            System.out.print("\n");
	    }
	    
	    public boolean testValidTranslation (String sequence, String expectedTranslation) {
	    	try {
	            ExtendedResult<TranslationResult> extendedResult = translate(sequence.getBytes());
	            TranslationResult translationResult = extendedResult.getExtension();
	            if (extendedResult.count(Severity.ERROR) > 0) {
	            	if (write) {
	            		System.out.print("UNEXPECTED ERROR\n");
	            		System.out.print("--------------------\n");
	            		for (ValidationMessage<Origin> message : 
	            			extendedResult.getMessages()) {
	            			System.out.print("MESSAGE: "+ message.getMessage() + "\n");
	            		}
	            		writeTranslation(translationResult, expectedTranslation);
	            	}
		            return false;
	            	
	            }
	            String conceptualTranslation = translationResult.getConceptualTranslation();
	            if(!conceptualTranslation.equals(expectedTranslation)) {
	            	if (write) {
	            		System.out.print("FAILED TRANSLATION\n");
	            		System.out.print("------------------\n");
	            		writeTranslation(translationResult, expectedTranslation);
	            	}
		            return false;
	            }
	            else {
	            	if (write) {
	            		System.out.print("SUCCESFULL TRANSLATION\n");               
	            		System.out.print("++++++++++++++++++++++\n");
	            		writeTranslation(translationResult, expectedTranslation);
	            	}
                    return true;
	            }
	    	}
	    	catch (IOException ex) {
	    		return false;
	    	}            
	    }

	    public boolean testInvalidTranslation (String sequence, 
	        String expectedMessageKey) {
	    	try {
	            ExtendedResult<TranslationResult> extendedResult = 
	            	translate(sequence.getBytes());
	            TranslationResult translationResult = 
	            	extendedResult.getExtension();
	            if (extendedResult.count(Severity.ERROR) == 0) {
	            	if (write) {	            	
	            		System.out.print("NO ERROR\n");
	            		System.out.print("----------------------\n");
	            		writeTranslation(translationResult, null);
	            	}
		            return false;	            
		        }
	            if (extendedResult.count(expectedMessageKey, Severity.ERROR) >= 1) {
	            	if (write) {	            	
	            		System.out.print("EXPECTED ERROR\n");
	            		System.out.print("++++++++++++++++++\n");
	            		for (ValidationMessage<Origin> message : 
	            			extendedResult.getMessages()) {
	            			System.out.print("MESSAGE: "+ message.getMessage() + "\n");
	            		}
	            		writeTranslation(translationResult, null);
	            	}
	                return true;
	            }
	            else {
	            	if (write) {	            		            	
	            		System.out.print("WRONG ERROR\n");
	            		System.out.print("---------------\n");
	            		for (ValidationMessage<Origin> message : 
	            			extendedResult.getMessages()) {
	            			System.out.print("MESSAGE: "+ message.getMessage() + "\n");
	            		}
	            		writeTranslation(translationResult, null);
	            	}
		            return false;
	            }
	    	}
	    	catch (IOException ex) {
	    		return false;
	    	}
	    }
	}
	
	@Before
	public void setUp() throws Exception {
	}
	
	   
    @Test 
    public void testValidTranslationNonRightPartialOneTrailingBase1() throws ValidationException {
       TranslatorTester test = new TranslatorTester();
       test.setTranslationTable(11);
       test.setCodonStart(2);
       test.setLeftPartial(true);
       test.setFixRightPartialCodon(true);
       assertTrue(test.testValidTranslation(
           "gcgcgccgctgtagccgctgatcgtggaattattggtggttatggtctaggtgccccctacggtttagctggtggttacggtttggaagttccttacggcttggctggatacgctgactaccgctaccccgctggtgcatgcggtatcgatgcttacggtggtattggtgaaggtaacgttgctgtcgctggtgagctgcccgtagctggtaccactgctgtcgctggtcaagtacctatcatgggcgctgtgaaattcggtggtgatgtctgcgctgctggttccgtatccatcgctggcaagtgcgcttgcggctgcggtgattacggttacggctacggattaggtgctccctacctgtactaaa",
           "RAAVAADRGIIGGYGLGAPYGLAGGYGLEVPYGLAGYADYRYPAGACGIDAYGGIGEGNVAVAGELPVAGTTAVAGQVPIMGAVKFGGDVCAAGSVSIAGKCACGCGDYGYGYGLGAPYLY"
       ));
    }

    @Test 
    public void testValidTranslationRightPartialOneTrailingBase1() throws ValidationException {
		TranslatorTester test = new TranslatorTester();
		test.setTranslationTable(11);
		test.setCodonStart(1);
		test.setRightPartial(true);
		assertTrue(test.testValidTranslation(
		    "atggctgacggcctcgacttcacaggcatgctgcgttcggccgggctccggatcactcgtccccggctggcggtactcaatgcggtgaaagagcatccgcacgccgagacggaccatgtcatccgggccgtgcgcgttcaattgcccgacgtctcccatcagacggtgtacgacgngctcaacgcgttgacggcggccggcttggtgcgccgcatccagcccaccggttcggtagcccgctacgagacccgtgtcaacgacaaccaccaccacgtcgtgtgcaggtcgtgtggtgcgatcgccgacgtcgattgcgcggtgggcgatgcaccgtgtctgaccgccgcagacgacaacggtttcgacatcgatgaagccgaggtcatctattgggccagtgccctgactgctcgcgatctccgagttcttgacgacagccccgtcgctcaccagatgaaagggaaagcaatgcccccgaataccc",
		    "MADGLDFTGMLRSAGLRITRPRLAVLNAVKEHPHAETDHVIRAVRVQLPDVSHQTVYDXLNALTAAGLVRRIQPTGSVARYETRVNDNHHHVVCRSCGAIADVDCAVGDAPCLTAADDNGFDIDEAEVIYWASALTARDLRVLDDSPVAHQMKGKAMPPNT"
		));        
    }

    @Test 
    public void testValidTranslationRightPartialTwoTrailingBases1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "atggctgaagccgaaacccatcctcctatcggtgaatc",
            "MAEAETHPPIGES"
        ));        
    }

    @Test 
    public void testValidTranslationShorterThanThree1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "at",
            "M"
        ));        
    }

    @Test 
    public void testValidTranslationShorterThanThree2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setNonTranslating(true);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "a",
            ""
        ));
    }
    
    @Test 
    public void testValidTranslationShorterThanThree3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setNonTranslating(true);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "at",
            ""
        ));
    }    

    @Test 
    public void testValidTranslationTranslationException1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        test.setRightPartial(true);
        test.addTranslationException(1, 3, 'M');
        assertTrue(test.testValidTranslation(
            "nnn",
            "M"
        ));
    }

    @Test 
    public void testValidTranslationTranslationException2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.addTranslationException(4, 6, 'R');
        assertTrue(test.testValidTranslation(
            "atggagcgctttttgagaaaatacaatatcagtggggattacgcaaatgccacgagaacttttttggctatttcacctcaatggacttgcagccacttaaaaaggaattgtctatttaatggaatgtgcgtcaagcagcattttgagagagcgatgatcgcggcaactgatgcggaggagccggcgaaagcatacaaattggttgaattggcaaaggaagcaatgtatgatcgggaaacagtctggcttcaatgtttcaaaagcttttcccaaccatacgaggaagatgtcgaagggaagatgaagcgatgcggagcgcaattgcttgaggattaccgcaaaagtgggatgatggatgaagccgtgaaacaatctgcgctggttaattcagaaagaattagattggatgattcactttccgcaatgccttacatctacgtgccaatcaataatggtcaaattgttaatccgacatttatatcaagatatcgccaaattgcatattatttttacaacccagatgcagctgatgattggattgatccaaatctctttggtattcgcggacagcacaatcagattaaacgtgaggttgagagacaaattaacacatgcccttacactggatacagaggtagagtgtttcaagtaatgtttttgccgattcagctgatcaatttcttgagaatggatgattttgcgaagcattttaacaggtacgcctcgatggcgatacaacaatatctgagagttggttatgctgaagagatcagatatgtacaacagctcttcggaaaggtcccaacaggtgaatttccattacaccagatgatgctgatgagacgcgatctcccaacgcgcgatcgcagtattgtggaggcgcgggtgaggagatcaggtgatgagaactggcaaagctggctactacctatgatcatcattcgtgaggggttggatcatcaggatcggtgggaatggtttattgattacatggataggaaacatacatgtcaactttgctacttgaaacattcaaaacagatcccagcctgtagtgtgattgatgtacgtgcatctgaattaactgggtgctcgccgttcaaaatggtgaagatcgaagagcatgtaggaaatgattcagtgtttaaaacgaaattagttcgcgatgagcaaattggcaggattggagatcattattatacaacaaattgttacactggagcggaggcgttgattacaaccgcgattcatattcatcgctggattagggggtctggcatctggaacgatgaaggatggcaggagggtattttcatgcttggacgcgtgctgctgagatgggaattgacaaaggcgcaacgcagcgctttgctcaggctattctgttttgtatgttacggatatgcaccacgcgcagacggaacgataccggactggaataatcttggaaactttttggatatcattttgaaggggccagaacttagtgaagatgaggatgaaagagcttatgctacaatgtttgaaatggttcgatgcattatcactctatgctatgcagaaaaggttcacttcgctgggttcgctgcgcctgcgtgtgaaggcggggaagtaattaatcttgctgcgcgcatgtctcagatgtggatggagtattag",
            "MRRFLRKYNISGDYANATRTFLAISPQWTCSHLKRNCLFNGMCVKQHFERAMIAATDAEEPAKAYKLVELAKEAMYDRETVWLQCFKSFSQPYEEDVEGKMKRCGAQLLEDYRKSGMMDEAVKQSALVNSERIRLDDSLSAMPYIYVPINNGQIVNPTFISRYRQIAYYFYNPDAADDWIDPNLFGIRGQHNQIKREVERQINTCPYTGYRGRVFQVMFLPIQLINFLRMDDFAKHFNRYASMAIQQYLRVGYAEEIRYVQQLFGKVPTGEFPLHQMMLMRRDLPTRDRSIVEARVRRSGDENWQSWLLPMIIIREGLDHQDRWEWFIDYMDRKHTCQLCYLKHSKQIPACSVIDVRASELTGCSPFKMVKIEEHVGNDSVFKTKLVRDEQIGRIGDHYYTTNCYTGAEALITTAIHIHRWIRGSGIWNDEGWQEGIFMLGRVLLRWELTKAQRSALLRLFCFVCYGYAPRADGTIPDWNNLGNFLDIILKGPELSEDEDERAYATMFEMVRCIITLCYAEKVHFAGFAAPACEGGEVINLAARMSQMWMEY"
        ));
    }

    @Test 
    public void testValidTranslationTranslationException3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(5);
        test.setCodonStart(1);
        test.addTranslationException(787, 788, '*');
        String sequence = new String (
            "gtgtacataactcgatggttattttctaccaaccacaaagacatcggaacactttatttt" + 
            "atttttggcgcctgagcagctatagttggcacagcaataagcttattgattcgtgcagaa" + 
            "ctctctcaaccaggatccttactaggtgatgatcacctttataatgtaattgttactgcc" + 
            "catgctttcgtcataattttctttatagtaatacctgtaataatcggaggcttcgggaat" + 
            "tgactagttccaataataattggcgctcctgatatagcttttccacgtataaacaatata" + 
            "agtttttggatattacccccatctttttcattattacttgcttcatctgctgttgaagca" + 
            "ggagtagggacaggatgaacagtctacccccctttatcaggcaacatcgcgcatgcaggg" + 
            "gcatcagttgatttagccattttttctcttcatttagccggaatttcttcaattttaggt" + 
            "gcaattaatttcattacaaccattcataatatgcgggcaagcatcgaatgaaatcgtgtt" + 
            "cccctattcgtatgatctatttgagtaaccgcttatctacttcttctttccctaccagta" + 
            "ctagcgggagcaattactatacttttaacagaccgaaatattaatactacattctttgac" + 
            "ccctccggaggaggagacccaattttgtacgaacacttgttttgattctttggtcaccca" + 
            "gaggtatatattctcattctccctgggtttggaataatttctcacatcattattcattat" + 
            "gcaggtaaattacgttccttcggttatttaggaataacttgagccatatttactatcgga" + 
            "ttactcggattttgagtatgagcacatcatatattcacagttggtatagacgttgacaca" + 
            "cgaagttatttcaccgctgcaacaatagtgattgctgtacccacaggaattaaagttttt" + 
            "agctgattagctacactagcagggtcaaaacaactaaaatgagagacccctttgctttga" + 
            "gcattaggattcattttcctatttaccattggaggactaactggtattgtattagccaac" + 
            "tcatctttagatattgttcttcacgatacctattatgtagttgcacattttcactacgtt" + 
            "ttgtctatgggagcagtattctctattttaggaggcttaacttactgatttccactattc" + 
            "acaggatttacattacaagaatcttgaacgaaaattcacttcttcgttatgttcgtaggg" + 
            "gttaatttaacatttttcccacagcattttttaggcttagcaggtctaccccgacgatac" + 
            "tccgattacccagatgcttatacagtctgaaatgttgtatcatccctaggatctatcatc" + 
            "tctttaggctcagtaatcttcttcgtatttattatttgagaagccttctctgctcaacga" + 
            "aaagctgtaccagctagtcatacttctcattcagctgaatgactaataggatgtcctcca" + 
            "ttattccatactcacgaagaacttccatttatctcaaaaaaggtttaaaatagtaaaaat" + 
            "aaagtaaatattatactataattccttaccataatgaccggatatcaaactcatccatgg" + 
            "cacttagttgagcccagcccttgaccattcgtaggagggtcagcagcatttactctcaca" + 
            "gttggaataattatatgattccactataattcaacacttcttttaacattgggtttaatt" + 
            "ctagttgttgttacaataattcaatgatgacgagacgttgttcgagaagcaacatttcaa" + 
            "ggctgtcatacatcttatgtaattgccggattacgacgaggaatagttttattcatttta" + 
            "tcagaagtattttttttcctagccttcttttgagcctttttccacagcagcttggctccc" + 
            "acagtagaactgggtgtaacatggcctccagttggaattcatcctttaaatgcttttgct" + 
            "gtaccgcttctaaatactgccgtacttttaagctctggagttacagtaacgtgagcccat" + 
            "catgccttaatagacggcaatcgaacagaatcaattcaagcgttagcattcacagtaata" + 
            "ctaggtctttacttttcaggcctgcaagcatgagaatattatgaagcaccttttaccatt" + 
            "gcagatggaatttacggatctactttcttcgtagctacaggattccacggacttcacgta" + 
            "attattggctctactttcttactagtatgtttaattcgccaaattctctaccactatacc" + 
            "tcctctcaccattttggcttcgaggcagcagcctgatattgacacttcgtagatgtagta" + 
            "tgactatttctctacgtatgtatttactgatgaggatcttaatgttaactctaacacaca" + 
            "ttgttggtatcgcttcaattttagtctctctactcttactaatcagactaaccttaccaa" + 
            "acttacaccccgacaatgaaaaattgtcagcctatgaatgtggctttgacccactcggta" + 
            "atgctcgactccccttctcattacggttttttctagtagctattttatttctcctctttg" + 
            "acctcgaaattgcccttattctaccctaccccttaagacttgcccaaggaacatccgtat" + 
            "tcttcaacttctgaatagttacactcctaatcattgtcttaactttaggtttaatatatg" + 
            "aatgattaaaagggggcctcgaatgatcagaataaaatttatctgagatatagtgtaaga" + 
            "aacacaatagattttgagtctactaatgccagtgcaactcttgctttctcattaacggaa" + 
            "acaaaaggtaatctaaacctctgtccatcggtttcaagccgatcacattctctctgccat" + 
            "atttccagcaatatacatcagaaatattaggttatattaaacttacagcctgtcagattg" + 
            "tagatctcataattatgagatatttcgatggcaacgcctgcacaattaggactcacagat" + 
            "gcagcatcacccgtaatagaagaaataatttattttcacgatcacgttatactagttctc" + 
            "atcttaattacttgcttaattttctatagtatattaatccttattaactctacatatatt" + 
            "taccggtttttaacagacggacacgtaattgaaacagtatgaactgttattcctgcaatt" + 
            "attttagttgtagtagcattaccctctttaaaattactttacttaacagacgaattggac" + 
            "acacctcaattaaccatcaagactgtaggtcaccagtggtactgaagctatgaatatacc" + 
            "gattattatgatattgaatttgattcctatatattacctacaactgatatttctgatggt" + 
            "gctgctcgactgttagaggtagacaatcgagtagtcttaccagttgacacatcaatccgc" + 
            "atactagttaccgccgctgatgtacttcactcttgaacaatccctgcattaggcttaaaa" + 
            "atagatgccgtcccaggacgactcaaccaattagctctacaatgcagccgtgttggtact" + 
            "ttttatggtcaatgctctgaaatttgtggtgccaatcatagttttataccaattgttatt" + 
            "gaagcagtccctgtagaagtatttgaaacttgatgtgacacaatactagatgaagaatca" + 
            "ttaaatagcttaaatataaagcattaaccttttaagttaatgatgagataactctttaat" + 
            "gtgtgccacaattaaatccaatcccctgagtctttttcatagggctagcatgaagtacct" + 
            "tccttctctttggcttatacaaaatttccaaaacttatagccccattatagatagagaca" + 
            "atcaactaacacaaactcaagaaatacaaacaaatgaatattttataccatgataagcag" + 
            "tttatttaaccaatttgactcgccctgatttttagcttgcccactggcccttttagctct" + 
            "cttagtaccttgaaaactttttatttatgaacataatagctgaccaggcacacgcaatgc" + 
            "ttctctcttagactgaacatttcaaagacttgccagacaagtttgacaacctataagcat" + 
            "ctgacggggtgggcgatgagttgtactctttacaagcttgatattaatattaataaccct" + 
            "aaatctaatcggattatttccctatacctttactcccacaacccaactctcaataaattt" + 
            "aggtttagcaattcctttatgattaggaaccattatctatggctttcgcaatcaccctac" + 
            "aattgccctagcccacttgtgccctgaaggcgcacccgcccctttaattcctatattaat" + 
            "tcttgtcgaaaccctaagaattttcatacgccccctggcactcggcctacgacttactgc" + 
            "aaacctaaccgccgggcatctattgatgcaccttatttcttcagccgtgctaagcctaag" + 
            "agcaatttctaatattttaggtggaacaattctggtcttattggttttacttacactact" + 
            "tgaaatagcagtagcattaattcaaggttacgtcttcgccattttagtgactctctactt" + 
            "agatgaaaatctttaaacagatagtttaaacaaaatatttggtttcggcccataagatat" + 
            "ccactccgattctgttttatgataaccataacactaattttttgcattgctttattaggt" + 
            "ttaggcttagcacaaacacacttattatctgctctgctctgcttagaaataataatacta" + 
            "gccctctattttggcctaggccaaacggctttactaggccctgattatcccttaataatt" + 
            "gcccttattctcttaactttcagcgcctgtgaagccagttcaggtttagcactcttagta" + 
            "ttaatttcacgtagtcacggaagtgacttactggcatctttcaacctatcgtaatgtata" + 
            "atattatcttagcttatgtaggtttagcacttagcgtattaatatgtagtaataaaattc" + 
            "tctgacgaattagtcaattcagatctctagtattaatctttccagtagtatccctcacaa" + 
            "ttatagaatactcttctacttctgtaaacagtgcccttagagccgactttatagcagtcg" + 
            "gcctagctagcttaagtgcttggctgctcccattaatactcatagccagccaacaacatg" + 
            "taaatgcagaaccaataatttatcaacgcgtatttattttttcccaagcagtattaacgg" + 
            "gggccttaattttagcctttttaagcagtgacttattgctattttatattgcattcgaaa" + 
            "ttactttattacctacattaatactaattacccgctgaggagcccaaaaggaacgttatc" + 
            "aagcagggacatattttatattttatactctggtaggttcacttcctttacttatctgtc" + 
            "taattggtcaataccaacatagcgggaccctttgtctagacattttttatcttaatccac" + 
            "ctgaatttaattacaccattaatttttgatgacttggctgtattattgcctttctcgtaa" + 
            "aactccctttatatggaagtcatttatgacttcccaaagcacacgtagaagcccctatcg" + 
            "ctgggtctatggtactcgctggggtcttactaaaacttgggggttatggaataatgcgtg" + 
            "tcagccacctttgaggccctataaacaccctttcaagtgaattcattctagggtttgggc" + 
            "tttgaggaataatcagagtaggcattatttgtctccgtcaaacagacttaaaatccctta" + 
            "tcgcatactcttctgtaggccatatagcacttgtcgcagctggtatcctaagtggctcca" + 
            "catggggttacatgggggcattaattttaatgattgcacatggaattgtatcttcatgct" + 
            "tgttctgcttagctaactgctgatatgaacgaggacacacacgaaatttaatcggctcac" + 
            "gaggagcattaataatatttcccttactaactaccgcttgacttacaagtagactaataa" + 
            "acctagcactccctccctcaattaacctattcggagaattaatagcaatagtagctactt" + 
            "acacatgaagtacattttccttaagtcttatagttatctctacagtcttagccgcaggct" + 
            "actccctatacttatttggcgcaacacaatgaggctatacagtaaaaactctccataatc" + 
            "tgcaactattgactagacgtgaataccttttaataattctacacttaacaccagcaattt" + 
            "atttaatcccaatggcacaatgactaatttaatatatctgcgttaacctagtttaataag" + 
            "aatattaagttgtggtcttaaaggtgagtacttaactcggttaatcggtgttacgaggct" + 
            "taggttatgttgctaccataactgatcagagttcaattctctgataataccatatatgca" + 
            "gacactacttaactttataattttattaaccgcaataatagttatccgcgccacttcccc" + 
            "ttatttcggcgccctagcgatagcaatactatcccttctaatttcaatcacaatatttca" + 
            "actaaacctcattttccctgctataattttaatacttatttatcttggaggtatacttgt" + 
            "cgtatttgtttatagtacagcttattcagcagatttaatgcccttaccagtaaatataac" + 
            "cctcacactattcattgctatcagaggaacaggcttcctagccattttttccctctcccc" + 
            "catggaaaacttttgcgaagctaactcatgaatatctttcacaataagttacaattattt" + 
            "actgtttgatttatacgaacggggttatatagcttttagactagcaatccttgtactaac" + 
            "tatcctattattttccatcctagaaattatctcccaccggcaagtagcgattaaatgatt" + 
            "ttatttgtcccactaatcagcggatccttagaatggaaatctaaaataatttttatacta" + 
            "gacaaatagtaacacgtcactacaaccataatgacaaataatatcaacagcattcttgca" + 
            "agatacatcttaattaaagatgtttgtgcctgactaacataattagataataacccagaa" + 
            "ttcaccccaagaccctgcggcatggccaattctgtccacccccggtccaatctctgagtc" + 
            "tgaaatttttctccgagatgaagtcaaactcatggaattactttatgcataataaaagga" + 
            "taatacccaacttgagatataaatgaaagatatgctgttcctacattatcctccttatga" + 
            "agcaattttattacatctcaagctaacacggctcccacaaaagtaaccccaatagccatt" + 
            "aatttcaatgtataaggcaatgacaaaataacattatatttaggggtcataaaagcaaca" + 
            "aataataccccacttaagactcttccataagctaaccgttgcaaaggagccactaaaaac" + 
            "ttataggcttcgtttagggattgcaaaggtagtgtacgtgcataacccgctgttgtataa" + 
            "taaagcagtcgaagtctgtaagccgcagtaaaactagttgctaataataaaagacttaca" + 
            "gcccaactattaacattgcccatattaatagcctcaatgataggatccttggagaagaag" + 
            "cccgccaaaaaaggagtaccaattagagccgctcttccgataaataaacacgttctcgtc" + 
            "acagggctagccttatacaatccccccattttccgtacatcttgctcattttgtaaccca" + 
            "tgaatataaccccctgaacacataaacaacatagctttaaaaaaggcatgcatacaaatg" + 
            "tgaagaaaagctaattgaggtacccccacacctacagctgttaccatcagacctaactga" + 
            "cttgctgtagaaaacgcaaccactttttttatatcattttgcgcaagagcacatacagca" + 
            "gaaaacaacgttgttattctccctaaaacaaagacccccacttgtactccttcatgttct" + 
            "agaattaaaggacttatacgaataagtaagaacacccccgcaacaactatagtgctagaa" + 
            "tgaagtaaagatgacaccggtgtgggcccctctattgccgcaggaagtcaaggatgtaaa" + 
            "cctaactgcgcagatttacctatagccgctaatactgccccggacaaaaaaatagtactt" + 
            "acattccccattacataaatagaagtgaaagttcattcttgattattaagaaaacatcaa" + 
            "aggagtataccaattaaacctatatccccaatacgattgtaaaaaatagcctgtagcgca" + 
            "gcagtattagcatcactccgagcatatcaccaactaattagtaaataagacattacacct" + 
            "accccctctcatcctacaagtaattgaaataacgtttctgcactaaccaaaactaatata" + 
            "gcaattaaaaatattcccaaatatttacaaaacagctctacccgaggatccgtagacata" + 
            "taatataaagaaaacactaaaatatttcacgttacatataaacccacaacgaaaaaacag" + 
            "cacgtatacacatcataacgataactaaaagttaggctatatccgcctaattttagccac" + 
            "tcccactttaaaaataatagctctctctcatcagcaactaaaaagactcccaataaaaaa" + 
            "aaattaacataccacccccacttaattgctcctgcaataaattctttacgcttacctaac" + 
            "agaaccacagctaacaaaattaagcataataaagagattcttccacataattgtaacacc" + 
            "attcaattgaagcactacttcattttttggttcctaaaaccaatgtaataaatatactaa" + 
            "ttgaataaactaatttttgcttgacaccaagaatttaagcatgaaaagcttatgttataa" + 
            "attaactacaaaaattatgaccggacctatacgaaaacaccacccccttttaaaagtcgt" + 
            "taaccactctcttattgatctccctgttcctgccaatatttctatttgatgaaatttcgg" + 
            "ttccttactcggcctctgcttagtaattcaaatcataaccggactattcttagctataca" + 
            "ttacacagcagacgtcaatttagcattttcttctgtagctcatatctgtcgagacgtaaa" + 
            "ttacggctgactcctgcgaaacttacatgcaaacggaggttcttttatatttatctgctt" + 
            "atacctccatcttggtcgaggaatgtattacgggtcatatttctttcttgaaacgtgaaa" + 
            "tattggcgtcattctactaatcataaccatggccacagcttttctaggatatgttctacc" + 
            "gtgaggacaaatatccttttgaggtgcaactgtaattactaatctcttttctgccatccc" + 
            "ctatttaggccctagtttagttgagtggttatgaggaggattttccgtagataacgctac" + 
            "tcttacccgattttttgcgtttcatttttttctacctttcttaatctcaggactagctat" + 
            "tgtacatttaatatttctccaccagacaggagctaacaacccaacaggactagtagggga" + 
            "tgtagataaagtaccgtttcacgcttacttctcgtacaaagatgtagtgggattcgttat" + 
            "tcttctagctggactaattataattgccttattttcaccgaaccttttaacagatccaga" + 
            "aaattatatcccagctaacccactagtcacgcccgttcacatccaaccagaatgatactt" + 
            "tctgtttgcttatgcaattctccgatctgttccaaataaattagggggagtcgtggccct" + 
            "tgcaatatccgttattatctttttcttcttaccgatacttcacctcagtaaccaaactag" + 
            "ccacaacttccgcccattatcccaaatcttattttgatttatagttattaacgctttatt" + 
            "acttacctgactcggcggtcaaccagtagaatatccctacattatacttggacaggttac" + 
            "ctctttcctttacttcttcacccttctatttataggccccgccattggtttattagagaa" + 
            "taagttattattccgataagttttagtagcttaacttaaagcgccggtcttgtaagccga" + 
            "agatagtataaatattctaagactcagaaaaggtaaattaccatcttaaactcccaaagt" + 
            "ttatgttttaattttaaactatcttctgcagatttgattctggtttcacttataagattg" + 
            "cccttgatgatacatgctaaccgtagatactccagtgagaatacatagttgaacagaatg" + 
            "tttaatatattgttcacataaagagcattagatctattatatgctccgctagcctcgcgc" + 
            "cactccacaacggtattagcagtactcagtattgaatttagatgcgaaagcatgccccag" + 
            "ctaacggcatcaatacatggcaaatgctgtgccagccgccgcgattacacctcaaaatgt" + 
            "aaacttataaatcacacacccgtatctgccccctactcaatgattactacttatgacttg" + 
            "cggtcgtatgcactcagaaagaagcaatcaaagaagtagctacgcgagaagaaggcaaaa" + 
            "accaggattagagaccctgctattcttcaatagttaacattaggttaatattaataatgt" + 
            "ttgaaaaccaaagattttggcagtgactaaggcctattcagaggaatttgttttatgaaa" + 
            "tcgatactccgcgaaaatcttactccacccttgtatcacagcttatataccgccgtcgtc" + 
            "agctgatattgagaaataataagcatcagcaaaaagactaatttcttcacgtcaggtcag" + 
            "ggtgtagcctatgggggagatagaatgaattacacttaattgcaattaccgaattctaac" + 
            "atgtaacggttagctaaagctggatttgatagtaattttatattatatatgataaattga" + 
            "ttatagctcttagttatgcacacaccgcccgtcattttcctaaaaccataaaggaaaaaa" + 
            "gtcgtaacaaggtagggcatctggaaagatcccctggatatggtacatataagttactta" + 
            "gactattaccttgaaaaggtaacagcgggctatataccctattaccacgaattagagctt" + 
            "ggcataagcatctcttttacacagagaagatatttgggaatcagattaattcgacttaac" + 
            "gctaatcactcaagcattatttacaacaataaaactttcacttatgtagctgagagcaaa" + 
            "tagtaataaatgagtactgaaaaggaaacaaataaataaattgaaacagcaattaaaaat" + 
            "ttacaggttaattcttattccttttgcataatgacttaattagtgcccttttagtaagga" + 
            "ccacctactagctgccccgaaagtaaacgatctattttaggccaagtattttgactccac" + 
            "taatttatgttgcaatataattaggagagcttagaatagagatgacatgttaaccgcgtt" + 
            "tacttctagctggttgcttgggaaatgaatatgagttcagtcggaacaaacagtttcgct" + 
            "aagacaagccggaatgtgctggcctgttaaaatataaacaaatatacctaaaattaagtt" + 
            "ttatcttgcccaccgtaggcttaaaagcagccatcgggtaagaatagcgtcaaagcttaa" + 
            "attttactgctattacaatttttagctcggtaccaagcaatctttaaataagatatcttc" + 
            "tgttaagacgcgtagaactataacacctaatacaagtgtgctagatattatgcttaaaca" + 
            "attactattcaagtatattacataagtctgcctaacataagcgtattataaaggttgtga" + 
            "cataaaagaaaggaactcggcaaatattaagctcgcctgtttaccaaaaacatcgctttt" + 
            "agctaactattaaaagtctgatctgcccggtgtaaaatcaacggccgcggtattttgact" + 
            "gtgcaaaggtagcataatcacttgtcctttaaatgggggaatgtatgaatgattagacga" + 
            "ggtttcagctgtctttctcttaaaaatcgagactccactgtacgtgaaaatgcgaacaaa" + 
            "taattaaaggacgagaagaccctgttgagctttcaagtataattaaaagtacatgtcaat" + 
            "ttataacaaataaaactaatactcataataaactttttggctggggtggcaaacaaagaa" + 
            "attaagctttgtatattattgacagcataagaccatataggtctactactatttattaca" + 
            "gatccgctataagcgatttaaaaactaagttaccacagggataacagcgtaattcttttt" + 
            "aagagcccaaattgacaaaagagtttgcgacctcgatgttggatcaagacccctggtggt" + 
            "gcagccgctaccccgggtttgcctgttcggcgattaaaatcttacgtgatctgagttcag" + 
            "accgtcgtaagacaggttagattctatccttaagttgatgtattcagtacgaaaggacca" + 
            "actacatctgattttaagtttatgttaaatcataactaaaatagcaaaaagtattgcgtg" + 
            "aggcctaagccctttagttataggtgcaacccctatttttagtgatgtgatgcattaatg" + 
            "tgatccacttatttctatacttcgtgcctgttcttctagcagtagcctttcttacactca" + 
            "ccgaacggaaggtcattggttatattcaactacgtaaaggccccaatgtagtggggccct" + 
            "atgggcttcttcagcccattgccgacggagtaaaactctttattaaagaaccaattaaac" + 
            "cctcttcatcagtcccccaattatttttcctggcgccttttctggcactaaccttagccc" + 
            "ttctcttatgggcccctataccaatagcagaatcatttatcgaattaaactttggagttc" + 
            "tatttgtattagcaatttctagcttatccgtctattcattaatagcctctggttgagctt" + 
            "ccaattcaaagtatgcactgctcggagcgctccgagcagtggcgcaaatagtatcttacg" + 
            "aagttagtctaggacttattattttaagcctgatttgcttagtaggcaacttcaatttaa" + 
            "gtcaattctccattgcacaagaagaaaccgttttattacttagttgttgaccactagcta" + 
            "ttatatgatttatttctactgtagcagagacaaatcgatcacctttcgacctcacagaag" + 
            "gagaatctgagctagtatctggttttaacgtagagtattcgggtggaccctttgcactat" + 
            "ttttcttagcagaatatgctaacattctatttataaatatactatcagtagtacttttct" + 
            "tatctttctatttcactctagccgatatgtcattaaaagcaagtttattggttgccttat" + 
            "atctctgatttcgggcctcttacccccgattccggtatgaccaattaatacatctagcat" + 
            "gaaagagctttttgcctataagcctgggcctgcttattcttaacttcagtatccccataa" + 
            "ttttttcaggactaggccccggaagttgaaaacgtagtttatacaaaaccttgctttgat" + 
            "agggcaaagaaacaacatatgttgtcatttttacgatagagtaagctaattaggctattg" + 
            "ggcccataccccaacagtgttggtaaatccatcctctaccatgagaaatatagacgtcgc" + 
            "atcaacttttacgagtatatacatctcatatgagtccttatatttcccctttatttagat" + 
            "taacaatactcttcagcgtcctgctcattctctgctccactcactgagcgttcatatgac" + 
            "taggcctagaagtaggtacgctcgcatttgtcccattacttacttgatggcatacagccc" + 
            "cggaagttgaagcaacagttaaatactttatcactcaagcaacagcagcagctatgtttt" + 
            "tcctcggaggcctcatcttaataagcagtgagtactcaagcggtattgctcattgattag" + 
            "gaagactaggagaggttattatccttctttccgtattaataaaattaggacttgcaccac" + 
            "tccactactgggtagtagatgtagttcagggcttaaattatctccccggaatagtattac" + 
            "taacttgacaaaaactcccagggctaattgtactcacacaactcttgtctaaactaaaca" + 
            "gctccattctacttattctcgcaccaacagccgcccttattggaggtctaggggggctag" + 
            "gacaaacccaagtacggaaactgttggcattctcctcaatcgcccatctaggttgactaa" + 
            "cggttggaattgttattaactcatgattaggaataatatactttatattatatatattga" + 
            "tctccctcccaattttccttttattgcacgtttcagggggagtacatctcaatcaattac" + 
            "ggagcactctgggggccaatcctattttttctttttcagtgggagcgggcttcttatcgc" + 
            "ttgcaggtctcccaccttttctgggtttcttttcgaaatgattaatcttaactcattccg" + 
            "tcgcgcaacttttagtaattacatcagcggttttaatcctgggcgcattaattagcgcct" + 
            "tctattatttacgcatcagttatctctgcctagtagttttagcaccccaacagattatag" + 
            "taataactaattgacgcaatatgtctaaaattaacttaattagtattattttattgtcaa" + 
            "acataataggtttattactagtcgggggtttaagcactttaactaagtaagtagtctcta" + 
            "catctttgaattaacagtccaacgctttaaatattaagctattacttataaggagttagg" + 
            "ttaataatagacctttagccttcaaagctgaaagcggaatgtttattccagtccttaaaa" + 
            "taaaatctgagataatcacattgtttgattgcaaatcaaatagtttatttaacttaagat" + 
            "tttattacaggctttagaaagtataactttctttctccgggttgcaaccagagcatttat" + 
            "aagcctagtaaaagaaggattagcctccatgagcggagctacaatccgctgctttagatc" + 
            "agccattttactactagcctatta");        
        assertTrue(test.testValidTranslation(
            sequence.substring(1594 - 1, 2381),
         "MTGYQTHPWHLVEPSPWPFVGGSAAFTLTVGMIMWFHYNSTLLLTLGLILVVVTMIQWWRDVVREATFQGCHTSYVIAGLRRGMVLFILSEVFFFLAFFWAFFHSSLAPTVELGVTWPPVGIHPLNAFAVPLLNTAVLLSSGVTVTWAHHALMDGNRTESIQALAFTVMLGLYFSGLQAWEYYEAPFTIADGIYGSTFFVATGFHGLHVIIGSTFLLVCLIRQILYHYTSSHHFGFEAAAWYWHFVDVVWLFLYVCIYWWGS"
        ));
}
    @Test 
    public void testValidTranslationTranslationException4() throws ValidationException {
         TranslatorTester test = new TranslatorTester();
         test.setTranslationTable(2);
         test.setCodonStart(2);
         test.setLeftPartial(true);
         test.addTranslationException(53, null, '*');
         assertTrue(test.testValidTranslation(        
         //   12345678901234567890123456789012345678901234567890123
             "cgtagatatcgtctgactattcctctacgtttcaatctactgatgaggttcct",
             "VDIVWLFLYVSIYWWGS"
         ));
    }

    @Test 
    public void testValidTranslationTranslationException5() throws ValidationException {
         TranslatorTester test = new TranslatorTester();
         test.setTranslationTable(2);
         test.setCodonStart(2);
         test.setLeftPartial(true);
         test.addTranslationException(53, null, '*');
         assertTrue(test.testValidTranslation(        
         //   12345678901234567890123456789012345678901234567890123
             "cgtagatattgtctgactattcctttacgtctcaatctactgatgaggttgct",
             "VDIVWLFLYVSIYWWGC"
         ));
    }
    
    @Test 
    public void testValidTranslationPseudo1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(2);
        test.setNonTranslating(true);
        assertTrue(test.testValidTranslation(
            "ctttgactttaactccatgcatatggagccgtgggatgg",
            ""
        ));
    }

    @Test 
    public void testValidTranslation1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atgaaagcgatcttaatcccatttttatctcttctgattccgttaaccccgcaatctgcattcgctcagagtgagccggagctgaagctggaaagtgtggtgattgtcagtgcccatggtgtgcgtgctccaaccaaggccacgcaactgatgcaggatgtcaccccagacgcatggccaacctggccggtaaaactgggttggctgacaccgcgnggtggtgagctaatcgcctatctcggacattaccaacgccagcgtctggtagccgacggattgctggcgaaaaagggctgcccgcagtctggtcaggtcgcgattattgctgatgtcgacgagcgtacccgtaaaacaggcgaagccttcgccgccgggctggcacctgactgtgcaataaccgtacatacccaggcagatacgtccagtcccgatccgttatttaatcctctaaaaactggcgtttgccaactggataacgcgaacgtgactgacgcgatcctcagcagggcaggagggtcaattgctgactttaccgggcatcggcaaacggcgtttcgcgaactggaacgggtgcttaattttccgcaatcaaacttgtgccttaaacgtgagaaacaggacgaaagctgttcattaacgcaggcattaccatcggaactcaaggtgagcgccgacaatgtctcattaaccggtgcggtaagcctcgcatcaatgctgacggagatatttctcctgcaacaagcacagggaatgccggagccggggtggggaaggatcaccgattcacaccagtggaacaccttgctaagtttgcataacgcgcaattttatttgctacaacgcacgccagaggttgcccgcagccgcgccaccccgttattagatttgatcaagacagcgttgacgccccatccaccgcaaaaacaggcgtatggtgtgacattacccacttcagtgctgtttatcgccggacacgatactaatctggcaaatctcggcggcgcactggagctcaactggacgcttcccggtcagccggataacacgccgccaggtggtgaactggtgtttgaacgctggcgtcggctaagcgataacagccagtggattcaggtttcgctggtcttccagactttacagcagatgcgtgataaaacgccgctgtcattaaatacgccgcccggagaggtgaaactgaccctggcaggatgtgaagagcgaaatgcgcagggcatgtgttcgttggcaggttttacgcaaatcgtgaatgaagcacgcataccggcgtgcagtttgtaa",
            "MKAILIPFLSLLIPLTPQSAFAQSEPELKLESVVIVSAHGVRAPTKATQLMQDVTPDAWPTWPVKLGWLTPRGGELIAYLGHYQRQRLVADGLLAKKGCPQSGQVAIIADVDERTRKTGEAFAAGLAPDCAITVHTQADTSSPDPLFNPLKTGVCQLDNANVTDAILSRAGGSIADFTGHRQTAFRELERVLNFPQSNLCLKREKQDESCSLTQALPSELKVSADNVSLTGAVSLASMLTEIFLLQQAQGMPEPGWGRITDSHQWNTLLSLHNAQFYLLQRTPEVARSRATPLLDLIKTALTPHPPQKQAYGVTLPTSVLFIAGHDTNLANLGGALELNWTLPGQPDNTPPGGELVFERWRRLSDNSQWIQVSLVFQTLQQMRDKTPLSLNTPPGEVKLTLAGCEERNAQGMCSLAGFTQIVNEARIPACSL"
        ));
    }
        
    @Test 
    public void testValidTranslation2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atgaaagcgatcttaatcccatttttatctcttctgattccgttaaccccgcaatctgcattcgctcagagtgagccggagctgaagctggaaagtgtggtgattgtcagtcgtaatggtgtgcgtgctccaaccaaggccacgcaactgatgcaggatgtcaccccagacgcatggccaacctggccggtaaaactgggttggctgacaccgcgnggtggtgagctaatcgcctatctcggacattaccaacgccagcgtctggtagccgacggattgctggcgaaaaagggctgcccgcagtctggtcaggtcgcgattattgctgatgtcgacgagcgtacccgtaaaacaggcgaagccttcgccgccgggctggcacctgactgtgcaataaccgtacatacccaggcagatacgtccagtcccgatccgttatttaatcctctaaaaactggcgtttgccaactggataacgcgaacgtgactgacgcgatcctcagcagggcaggagggtcaattgctgactttaccgggcatcggcaaacggcgtttcgcgaactggaacgggtgcttaattttccgcaatcaaacttgtgccttaaacgtgagaaacaggacgaaagctgttcattaacgcaggcattaccatcggaactcaaggtgagcgccgacaatgtctcattaaccggtgcggtaagcctcgcatcaatgctgacggagatatttctcctgcaacaagcacagggaatgccggagccggggtggggaaggatcaccgattcacaccagtggaacaccttgctaagtttgcataacgcgcaattttatttgctacaacgcacgccagaggttgcccgcagccgcgccaccccgttattagatttgatcaagacagcgttgacgccccatccaccgcaaaaacaggcgtatggtgtgacattacccacttcagtgctgtttatcgccggacacgatactaatctggcaaatctcggcggcgcactggagctcaactggacgcttcccggtcagccggataacacgccgccaggtggtgaactggtgtttgaacgctggcgtcggctaagcgataacagccagtggattcaggtttcgctggtcttccagactttacagcagatgcgtgataaaacgccgctgtcattaaatacgccgcccggagaggtgaaactgaccctggcaggatgtgaagagcgaaatgcgcagggcatgtgttcgttggcaggttttacgcaaatcgtgaatgaagcacgcataccggcgtgcagtttgtaa",
            "MKAILIPFLSLLIPLTPQSAFAQSEPELKLESVVIVSRNGVRAPTKATQLMQDVTPDAWPTWPVKLGWLTPRGGELIAYLGHYQRQRLVADGLLAKKGCPQSGQVAIIADVDERTRKTGEAFAAGLAPDCAITVHTQADTSSPDPLFNPLKTGVCQLDNANVTDAILSRAGGSIADFTGHRQTAFRELERVLNFPQSNLCLKREKQDESCSLTQALPSELKVSADNVSLTGAVSLASMLTEIFLLQQAQGMPEPGWGRITDSHQWNTLLSLHNAQFYLLQRTPEVARSRATPLLDLIKTALTPHPPQKQAYGVTLPTSVLFIAGHDTNLANLGGALELNWTLPGQPDNTPPGGELVFERWRRLSDNSQWIQVSLVFQTLQQMRDKTPLSLNTPPGEVKLTLAGCEERNAQGMCSLAGFTQIVNEARIPACSL"
        ));
    }

    @Test 
    public void testValidTranslation3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atggagcgctttttgagaaaatacaatatcagtggggattacgcaaatgccacgagaacttttttggctatttcacctcaatggacttgcagccacttaaaaaggaattgtctatttaatggaatgtgcgtcaagcagcattttgagagagcgatgatcgcggcaactgatgcggaggagccggcgaaagcatacaaattggttgaattggcaaaggaagcaatgtatgatcgggaaacagtctggcttcaatgtttcaaaagcttttcccaaccatacgaggaagatgtcgaagggaagatgaagcgatgcggagcgcaattgcttgaggattaccgcaaaagtgggatgatggatgaagccgtgaaacaatctgcgctggttaattcagaaagaattagattggatgattcactttccgcaatgccttacatctacgtgccaatcaataatggtcaaattgttaatccgacatttatatcaagatatcgccaaattgcatattatttttacaacccagatgcagctgatgattggattgatccaaatctctttggtattcgcggacagcacaatcagattaaacgtgaggttgagagacaaattaacacatgcccttacactggatacagaggtagagtgtttcaagtaatgtttttgccgattcagctgatcaatttcttgagaatggatgattttgcgaagcattttaacaggtacgcctcgatggcgatacaacaatatctgagagttggttatgctgaagagatcagatatgtacaacagctcttcggaaaggtcccaacaggtgaatttccattacaccagatgatgctgatgagacgcgatctcccaacgcgcgatcgcagtattgtggaggcgcgggtgaggagatcaggtgatgagaactggcaaagctggctactacctatgatcatcattcgtgaggggttggatcatcaggatcggtgggaatggtttattgattacatggataggaaacatacatgtcaactttgctacttgaaacattcaaaacagatcccagcctgtagtgtgattgatgtacgtgcatctgaattaactgggtgctcgccgttcaaaatggtgaagatcgaagagcatgtaggaaatgattcagtgtttaaaacgaaattagttcgcgatgagcaaattggcaggattggagatcattattatacaacaaattgttacactggagcggaggcgttgattacaaccgcgattcatattcatcgctggattagggggtctggcatctggaacgatgaaggatggcaggagggtattttcatgcttggacgcgtgctgctgagatgggaattgacaaaggcgcaacgcagcgctttgctcaggctattctgttttgtatgttacggatatgcaccacgcgcagacggaacgataccggactggaataatcttggaaactttttggatatcattttgaaggggccagaacttagtgaagatgaggatgaaagagcttatgctacaatgtttgaaatggttcgatgcattatcactctatgctatgcagaaaaggttcacttcgctgggttcgctgcgcctgcgtgtgaaggcggggaagtaattaatcttgctgcgcgcatgtctcagatgtggatggagtattag",
            "MERFLRKYNISGDYANATRTFLAISPQWTCSHLKRNCLFNGMCVKQHFERAMIAATDAEEPAKAYKLVELAKEAMYDRETVWLQCFKSFSQPYEEDVEGKMKRCGAQLLEDYRKSGMMDEAVKQSALVNSERIRLDDSLSAMPYIYVPINNGQIVNPTFISRYRQIAYYFYNPDAADDWIDPNLFGIRGQHNQIKREVERQINTCPYTGYRGRVFQVMFLPIQLINFLRMDDFAKHFNRYASMAIQQYLRVGYAEEIRYVQQLFGKVPTGEFPLHQMMLMRRDLPTRDRSIVEARVRRSGDENWQSWLLPMIIIREGLDHQDRWEWFIDYMDRKHTCQLCYLKHSKQIPACSVIDVRASELTGCSPFKMVKIEEHVGNDSVFKTKLVRDEQIGRIGDHYYTTNCYTGAEALITTAIHIHRWIRGSGIWNDEGWQEGIFMLGRVLLRWELTKAQRSALLRLFCFVCYGYAPRADGTIPDWNNLGNFLDIILKGPELSEDEDERAYATMFEMVRCIITLCYAEKVHFAGFAAPACEGGEVINLAARMSQMWMEY"
        ));
    }

    @Test 
    public void testValidTranslation4() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atggcgctagtgcccgtgggtatggcgccgcgacaaatgagagttaatcgctgcattttcgcgtccatcgtgtcgttcgacgcgtgcataacatacaaatcgccgtgttcgcccgacgcgtatcatgacgatggatggtttatttgcaacaaccacctcataaaacgttttaaaatgtcaaaaatggttttgcccattttcgacgaagacgacaatcaattcaaaatgacgatcgctaggcatttagttggaaataaagaaagaggtatcaagcgaattttaattccaagcgcaaccaattaccaagacgtgtttaatctaaacagtatgatgcaagccgaacagctaatctttcatttgatatataacaacgaaaacgcagttaacactatatgcgacaatctaaaatataccgaaggtttcacaagcaacacgcaacgcgttatacacagcgtttacgcaactacaaaaagcattctggacaccacaaacccgaacacgttttgttcgcgggtgtcgcgagacgaattgcgtttctttgacgtgaccaacgcccgagcgcttcgaggcggtgctggcgatcaattatttaacaattacagtggatttttgcaaaatttgattcgacgcgcagtagcgcccgagtacttgcaaatcgacacggaggaattgaggtttagaaattgcgccacgtgtataattgacgaaacgggtctggtcgcgtctgtgcccgacggccccgagttgtacaacccgataagaagcagtgacattatgagaagtcaacccaatcgtttgcaaattagaaacgttttgaaatttgaaggcgacacacgtgagctggacagaacgcttagcggatacgaagaatacccgacgtacgttccgctgtttttgggataccaaataatcaattcagaaaacaactttttgcgcaacgactttataccaagagcaaatcctaacgctactctgggcggcggcgcagtggcaggtcctgcgcctggtgttgcaggcgaagcaggtggaggaatagccgtctaa",
            "MALVPVGMAPRQMRVNRCIFASIVSFDACITYKSPCSPDAYHDDGWFICNNHLIKRFKMSKMVLPIFDEDDNQFKMTIARHLVGNKERGIKRILIPSATNYQDVFNLNSMMQAEQLIFHLIYNNENAVNTICDNLKYTEGFTSNTQRVIHSVYATTKSILDTTNPNTFCSRVSRDELRFFDVTNARALRGGAGDQLFNNYSGFLQNLIRRAVAPEYLQIDTEELRFRNCATCIIDETGLVASVPDGPELYNPIRSSDIMRSQPNRLQIRNVLKFEGDTRELDRTLSGYEEYPTYVPLFLGYQIINSENNFLRNDFIPRANPNATLGGGAVAGPAPGVAGEAGGGIAV"
        ));
    }

    @Test 
    public void testValidTranslation5() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atggaaaaaaaaataactcctagcgaattggaacttaatgaatttataaaaattatcaacgaaatgagtggtattgatttaaccgataaaaaaaatatactagctttaaagttgaataaatttcttgaaggaactaatactaaaaatttttccgaatttttgggaaaattaaaaagcaatagacaacttaaacaagaaactttagattttgtaaccataggtgaaacttattttttaagagaattggctcaattgaaagaaataatttattatgccaaaagcttagaaaagagagtaaatatcctaagcgccccttgttcaagtggagaagaagtatattctttggcattattggctgcacagaattttattaaagatatgtatattttaggcgttgatattaattcaagtgtgattgaaaaagcaaaacttggaaaatatcaaggaagaactttacagcgattgagcgagagtgaaaaaagaaggttttttttagaaagcgaagataaattttatactattaataaaaatgagctttgtacttgtaaatttgaactttgcaatgtttttgaagaaaaattttcaagattgggaaaatttgatattatagcttctagaaatatgattatttattttgatcatgaatcaaaactaaaacttatggagaggtttcatagaattttaaatgataaaggaaggctttatgttggcaatgctgatttaattccagagactatttattttaaaaagatttctctccaagaggtgtttactatgaaaaagtataaattctaa",
            "MEKKITPSELELNEFIKIINEMSGIDLTDKKNILALKLNKFLEGTNTKNFSEFLGKLKSNRQLKQETLDFVTIGETYFLRELAQLKEIIYYAKSLEKRVNILSAPCSSGEEVYSLALLAAQNFIKDMYILGVDINSSVIEKAKLGKYQGRTLQRLSESEKRRFFLESEDKFYTINKNELCTCKFELCNVFEEKFSRLGKFDIIASRNMIIYFDHESKLKLMERFHRILNDKGRLYVGNADLIPETIYFKKISLQEVFTMKKYKF"
        ));
    }

    @Test 
    public void testValidTranslation6() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testValidTranslation(
            "atggagcggcgaacgggtgagtag",
            "MERRTGE"
        ));
    }
    
    @Test 
    public void testValidTranslation7() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        assertTrue(test.testValidTranslation(
            "cctcataaacggagacgatattttcggatttgatgagactaagctgggaatatgcgtttctgggataggtctttccggctttgaagtatacgacctgcttagagagaagtacaacatacaagtggagcttgccgacgggagctatatactggccatagtcagtttgggcgataccaggagcgatatcgatgcacttgtagcggcacttgaagacataatagcgagtcattccggcgggaagcgccaaatagagcaaccaatactaataaatccctacgtagtaatatcgcccagagaggcgttctatagccccaaacggacagtaaggcttgaggacgcggaaggggaggtaagcggagaatcgcttatgctctacccgcacggtatcccaatatttgcaatcggtgagaggataacgcgcgatatgatagattacatcaaattcctaaaaaaacaaagcgcagtgcttgttgggaccgaggacccgggcatagagcatataaaaatattgggaatgtaa",
           "LINGDDIFGFDETKLGICVSGIGLSGFEVYDLLREKYNIQVELADGSYILAIVSLGDTRSDIDALVAALEDIIASHSGGKRQIEQPILINPYVVISPREAFYSPKRTVRLEDAEGEVSGESLMLYPHGIPIFAIGERITRDMIDYIKFLKKQSAVLVGTEDPGIEHIKILGM"
       ));
    }
    
    @Test 
    public void testValidTranslationDegenerateStartCodon1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setFixDegenarateStartCodon(true);
        assertTrue(test.testValidTranslation(
            "aygcaaactttaggaatattagcaatagtagcattagtagtagcaggaataatagcaatagttgtgtggtctatagtaatcatagaatataggaaaatattaagacaaagaaaaatagacaggttacttaatagaataagtgaaagagcagaagacagtggcaatgagagtgaaggggatcaggaagaattgtcagctcttgtggaaatggggcatcatgctccttgggatgttgatgatctgtag",
            "MQTLGILAIVALVVAGIIAIVVWSIVIIEYRKILRQRKIDRLLNRISERAEDSGNESEGDQEELSALVEMGHHAPWDVDDL"));
    }
    
    @Test 
    public void testValidTranslationCodonException1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        test.setFixDegenarateStartCodon(true);
        test.addCodonException("tta", 'Q');
        test.addCodonException("act", 'Q');
        assertTrue(test.testValidTranslation(
            "atgcaaacttta",
            "MQQQ")); // MQTL
    }

    @Test 
    public void testValidTranslationCodonStartTwo1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        assertTrue(test.testValidTranslation(
            "aatcactcctcaaccaggtgttgacccgattgaagcatctgctgcaattgcaggtgagtcttcaacagctacttggacagttgtgtggactgatttattaacagcttgtgacttatacagagcgaaagcatatcgagtagatccagttccaaacgttgcagatcaatattttgcttacatagcttacgatattgatttatttgaagaaggttccattgcgaatttaactgcttcgattattggtaacgtttttgggtttaaagctgttaaagctcttcgtctagaagatatgcgtatgccaatagcttacctaaaaactttccaaggtcctgcaactggattgattgtagaacgtgagcgtatggataagttcggtagacctttcttaggtgctacagttaaacctaaattaggtttatctggcaaaaactacggaagagttgtatacgaaggcctaaaaggcggtcttgatttccttaaagatgatgaaaatattaactcacaaccatttatgcgttggagagaaagatttttatattctatggaaggtgtaaataaagcatctgcttctgctggcgaaattaaaggtcattaccttaacgtgacagctgcgacaatggaagatatgtatgagagagccgaattctctaaagaggttggtagtatcatctgtatgattgaccttgttattggttatactgcgattcaaagtatggcaatctgggctcgcaaacatgacatgattttacatttacatagagcaggtaattcaacttactctcgtcaaaaaaatcatggtatgaacttccgagttatttgcaaatggatgcgtatggctggtgtcgaccatattcacgcaggtacagttgtaggtaaacttgaaggagatcctttaatgattaaaggattctacaatactttacttgaaagtgaaacagatattaacctacctcaaggtctgttnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnttgattacttaggtgatgatgtagttcttcagtttggtggtggtacaattggacatcctgatggtatccaagcaggtgcaactgctaacagagtagcattagagtccatggttatggcaagaaatgagggccgtaactatgtagcagaaggcccacaaatcttaagggacgctgctaaaacttgtgggcctctacaaacagctttagatttatggaaagacattagttttaactatacttccacagatacagctgacttcgttgagactccaacagcaaacatctag",
            "ITPQPGVDPIEASAAIAGESSTATWTVVWTDLLTACDLYRAKAYRVDPVPNVADQYFAYIAYDIDLFEEGSIANLTASIIGNVFGFKAVKALRLEDMRMPIAYLKTFQGPATGLIVERERMDKFGRPFLGATVKPKLGLSGKNYGRVVYEGLKGGLDFLKDDENINSQPFMRWRERFLYSMEGVNKASASAGEIKGHYLNVTAATMEDMYERAEFSKEVGSIICMIDLVIGYTAIQSMAIWARKHDMILHLHRAGNSTYSRQKNHGMNFRVICKWMRMAGVDHIHAGTVVGKLEGDPLMIKGFYNTLLESETDINLPQGLXXXXXXXXXXXXXXXXXXXXXXXXXXXXXDYLGDDVVLQFGGGTIGHPDGIQAGATANRVALESMVMARNEGRNYVAEGPQILRDAAKTCGPLQTALDLWKDISFNYTSTDTADFVETPTANI"
        ));
    }

    @Test 
    public void testValidTranslationCodonStartTwo2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "ggagaattgctctgtcatcgaaggacacttgcagatactcttgatgttcaaaacgaggcccgaagatttccgagacctcagtttccccaaactcatcatgatcactgattacttgctgctcttccgggtctatgggctcgagagcctgaaggacctgttccccaacctcacggtcatccggggatcacgactgttctttaactacgcgctggtcatcttcgagatggttcacctcaaggaactcggcctctacaacctgatgaacatcacccggggttctgtccgcatcgagaagaacaatgagctctgttacttggccactatcgactggtcccgtatcctggattccgtggaggataataacatcgtgttgaacaaagatgacaacgaggagtgtggagacatctgtccgggtaccgcgaagggcaagaccaacagccccgccaccgtcatcaacgggcagtttgtcgaacgatgtaggactcatagtcactgccagaaag",
            "ENCSVIEGHLQILLMFKTRPEDFRDLSFPKLIMITDYLLLFRVYGLESLKDLFPNLTVIRGSRLFFNYALVIFEMVHLKELGLYNLMNITRGSVRIEKNNELCYLATIDWSRILDSVEDNNIVLNKDDNEECGDICPGTAKGKTNSPATVINGQFVERCRTHSHCQK"
        ));
    }

    @Test 
    public void testValidTranslationCodonStartTwo3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "agggctgaagctgccctcgaggacctggtctccaccattcgagtctgaagattctcagaagcacaaccagagtgagtatgaggattcggccggcgaatgctgctcctgtccaaagacagactctcagatcctgaaggagctggaggagtcctcgtttaggaagacgtttgaggattacctgcacaacgtggttttcgtccccag",
            "GLKLPSRTWSPPFESEDSQKHNQSEYEDSAGECCSCPKTDSQILKELEESSFRKTFEDYLHNVVFVP"
        ));
    }
    
    @Test 
    public void testValidTranslationPartialNoStartWithMethionine1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        test.setLeftPartial(true);
        assertTrue(test.testValidTranslation(
            "agctttgaccagcgagacctggctcaagatgatgctgtgtggctggagcatggccctctggagtccagtagcccagctgtggtggtggcagcagcccgtataggtattggtcatgcaggggagtggacacagaagcccttgcgcttctatgtccagggcagcccatgggtcagtgtggtagacagagtggctgaacagatggatcagctagcaaacagcctgctctga",
            "SFDQRDLAQDDAVWLEHGPLESSSPAVVVAAARIGIGHAGEWTQKPLRFYVQGSPWVSVVDRVAEQMDQLANSLL"
        ));         
    }
    
    @Test 
    public void testValidTranslationLeftAndRightPartial1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setLeftPartial(true);
        test.setRightPartial(true);
        assertTrue(test.testValidTranslation(
            "tgtgccacctgggattattataagaaactctttggcagtggaacaacacttgttgtcacagataaacaacttgatgc",
            "CATWDYYKKLFGSGTTLVVTDKQLDA"));
    }

    @Test 
    public void testValidTranslationLeftPartial1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        test.setLeftPartial(true);
        assertTrue(test.testValidTranslation(
            "gacgagatgctgcgcatgtaccacgcactgaaggaggcgctcagcatcatcggcaacatcaacacgaccaccgtcagcacgcccatgcccccgcccgtggacgactcctggctgcaggtgcagagcgtaccggccggacgcaggtcgcccacgtccagccccacgccgcagcgccgagcccccgccgtgcccccagcccggcccgggtcgcggggccctgctcctgggcctccgcctgctgggtccgccctggggggggcgccccccgtgccctccaggccgggggcttcccctgaccctttcggccctccccctcaggtgccctcgcgccccaaccgcgccccgcccggggtccccagccagccgatcgggtcaggcaagtccatcccgtcctga",
            "DEMLRMYHALKEALSIIGNINTTTVSTPMPPPVDDSWLQVQSVPAGRRSPTSSPTPQRRAPAVPPARPGSRGPAPGPPPAGSALGGAPPVPSRPGASPDPFGPPPQVPSRPNRAPPGVPSQPIGSGKSIPS"));
    }
    
    @Test 
    public void testInvalidTranslationNonRightPartialOneTrailingBase1() throws ValidationException {
        // The current implementation allows 3' partial non-
        // translated codons after the stop codon if the 
        // feature is 5'partial. A fixer for this would be to
        // trim the traling bases from the 3' end.
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "gcgcgccgctgtagccgctgatcgtggaattattggtggttatggtctaggtgccccctacggtttagctggtggttacggtttggaagttccttacggcttggctggatacgctgactaccgctaccccgctggtgcatgcggtatcgatgcttacggtggtattggtgaaggtaacgttgctgtcgctggtgagctgcccgtagctggtaccactgctgtcgctggtcaagtacctatcatgggcgctgtgaaattcggtggtgatgtctgcgctgctggttccgtatccatcgctggcaagtgcgcttgcggctgcggtgattacggttacggctacggattaggtgctccctacctgtactaaa",
                "Translator-16"
        ));        
    }
        
    @Test 
    public void testInvalidTranslationMoreThanOneStopCodonAtTheEnd1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
           "atgtttgaccagcgagacctggctcaagatgatgctgtgtggctggagcatggccctctggagtccagtagcccagctgtggtggtggcagcagcccgtataggtattggtcatgcaggggagtggacacagaagcccttgcgcttctatgtccagggcagcccatgggtcagtgtggtagacagagtggctgaacagatggatcagctagcaaacagcctgctctgatga",
                "Translator-13"
        ));
    }

    @Test 
    public void testInvalidTranslationMoreThanOneStopCodonAtTheEnd2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
           "atgtagtagtagtagtag",
                "Translator-13"
        ));
    }
    
    @Test 
    public void testInvalidTranslationInternalStopCodons1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
           "atgtagaaatag",
                "Translator-17"
        ));        
    }
    
    @Test 
    public void testInvalidTranslationNoStopCodon1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "tgtgccacctgggattattataagaaactctttggcagtggaacaacacttgttgtcacagataaacaacttgatgc",
                "Translator-15"
        ));
    }
    
    @Test 
    public void testInvalidTranslationNoStopCodon2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "atg",
                "Translator-15"
        ));
    }

    @Test 
    public void testInvalidTranslationNoStopCodon3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "atgaaa",
                "Translator-15"
        ));
    }

    @Test 
    public void testInvalidTranslationNoStartWithMethionine1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "agctttgaccagcgagacctggctcaagatgatgctgtgtggctggagcatggccctctggagtccagtagcccagctgtggtggtggcagcagcccgtataggtattggtcatgcaggggagtggacacagaagcccttgcgcttctatgtccagggcagcccatgggtcagtgtggtagacagagtggctgaacagatggatcagctagcaaacagcctgctctga",
                "Translator-18"
        ));
    }
    
    @Test 
    public void testInvalidTranslationNoStartWithMethionine2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "tgtgccacctgggattattataagaaactctttggcagtggaacaacacttgttgtcacagataaacaacttgatgcc",
                "Translator-18"
        ));
    }
    
    @Test 
    public void testInvalidTranslationNoStartWithMethionine3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "gacgagatgctgcgcatgtaccacgcactgaaggaggcgctcagcatcatcggcaacatcaacacgaccaccgtcagcacgcccatgcccccgcccgtggacgactcctggctgcaggtgcagagcgtaccggccggacgcaggtcgcccacgtccagccccacgccgcagcgccgagcccccgccgtgcccccagcccggcccgggtcgcggggccctgctcctgggcctccgcctgctgggtccgccctggggggggcgccccccgtgccctccaggccgggggcttcccctgaccctttcggccctccccctcaggtgccctcgcgccccaaccgcgccccgcccggggtccccagccagccgatcgggtcaggcaagtccatcccgtcctga",
                "Translator-18"
        ));
    }
  
    @Test 
    public void testInvalidTranslationNoStartWithMethionine4() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "ttttag",
                "Translator-18"
        ));
    }

    @Test 
    public void testInvalidTranslationNotMultipleofThree1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "atga",
                "Translator-11"
        ));
    }
    
    @Test 
    public void testInvalidTranslationCodonStartTwoNotPartial1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(2);
        assertTrue(test.testInvalidTranslation(
            "atg",
                "Translator-3"
        ));
    }
    
    @Test 
    public void testInvalidTranslationShorterThanThree1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        assertTrue(test.testInvalidTranslation(
            "a",
                "Translator-10"
        ));
    }
    
    @Test 
    public void testInvalidTranslationShorterThanThree2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "a",
                "Translator-1"
        ));
    }    

    @Test 
    public void testInvalidTranslationShorterThanThree3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        assertTrue(test.testInvalidTranslation(
            "at",
                "Translator-10"
        ));
    }    

    @Test 
    public void testInvalidTranslationShorterThanThree4() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "at",
                "Translator-1"
        ));
    }    

    @Test 
    public void testInvalidTranslationShorterThanThree5() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        assertTrue(test.testInvalidTranslation(
            "",
                "Translator-10"
        ));
    }  

    @Test 
    public void testInvalidTranslationShorterThanThree6() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "",
                "Translator-1"
        ));
    }  

    @Test 
    public void testInvalidTranslationShorterThanThree7() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setNonTranslating(true);
        assertTrue(test.testInvalidTranslation(
            "a",
                "Translator-10"
        ));
    }
    
    @Test 
    public void testInvalidTranslationShorterThanThree8() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setNonTranslating(true);
        assertTrue(test.testInvalidTranslation(
            "at",
                "Translator-10"
        ));
    }    

    @Test 
    public void testInvalidTranslationShorterThanThree9() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(2);
        test.setRightPartial(true);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "at",
                "Translator-4"
        ));
    }

    @Test 
    public void testInvalidStopCodonOnly1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        assertTrue(test.testInvalidTranslation(
            "tag",
                "Translator-12"
        ));
    }

    @Test 
    public void testInvalidTranslationCodonStartNotOneThreeThree1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(4);
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-2"
        ));        
    }

    @Test 
    public void testValidTranslationStopCodonRightPartialPseudo1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        test.setNonTranslating(true);
        test.addCodonException("gag", 'R');
        test.addTranslationException(7, 9, 'E');
        assertTrue(test.testValidTranslation(
            "atggagcggcgaacgggtgagtag",
            ""
        ));
    }

    @Test 
    public void testInvalidTranslationStopCodonRightPartial1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-14"
         ));
     }

    @Test 
    public void testInvalidTranslationTranslationException1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        test.setNonTranslating(true);
        test.addCodonException("gag", 'R');
        test.addTranslationException(6, 8, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-9"
        ));
    }

    @Test 
    public void testInvalidTranslationTranslationException2() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.setRightPartial(true);
        test.setNonTranslating(true);
        test.addCodonException("gag", 'R');
        test.addTranslationException(5, 7, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-9"
        ));
    }

    @Test 
    public void testInvalidTranslationTranslationException3() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        test.addTranslationException(1, 3, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-4"
        ));
    }

    @Test 
    public void testInvalidTranslationTranslationException4() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.addTranslationException(1, 2, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-8"
        ));
    }
 
    @Test 
    public void testInvalidTranslationTranslationException5() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.addTranslationException(1, 1, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-8"
        ));
    }

    @Test 
    public void testInvalidTranslationTranslationException6() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.addTranslationException(25, 27, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-6"
        ));
    }

    @Test 
    public void testInvalidTranslationTranslationException7() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        test.addTranslationException(23, 25, 'E');
        assertTrue(test.testInvalidTranslation(
            "atggagcggcgaacgggtgagtag",
                "Translator-6"
        ));
    }





    @Test
    public void testInvalidTranslationTranslationException8() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(11);
        test.setCodonStart(1);
        assertTrue(test.testInvalidTranslation(
            "atgnnnnnnnnnnnnnnnnntgagtag",
                "Translator-20"
        ));
    }
    
    @Test 
    public void testInvalidTranslationInvalidBase1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        boolean b = test.testInvalidTranslation(
                "***",
                "SequenceBasesCheck"
        );
        assertTrue(b);
    }
    
    @Test 
    public void testInvalidTranslationTooShort1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(3);
        test.setLeftPartial(true);
        test.setRightPartial(true);
        assertTrue(test.testInvalidTranslation(
            "ata",
                "Translator-1"
        ));
    }    

    @Test 
    public void testInvalidTranslationStopCodonOnly1() throws ValidationException {
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(2);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "ttaga",
                "Translator-12"
            ));
    }

    @Test 
    public void testInvalidTranslationStopCodonOnly2() throws ValidationException { 
        TranslatorTester test = new TranslatorTester();
        test.setTranslationTable(1);
        test.setCodonStart(1);
        test.setLeftPartial(true);
        assertTrue(test.testInvalidTranslation(
            "taga",
                "Translator-12"
        ));
    }
}
