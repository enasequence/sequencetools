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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Book;
import uk.ac.ebi.embl.api.entry.reference.ElectronicReference;
import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Thesis;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class JournalReaderTest extends GenbankReaderTest {

	public void testRead_Book() throws IOException {
		initLineReader(
				 "  JOURNAL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n" +
			     "            SPECIFIC EUKARYOTIC GENES:117-132;\n" +
			     "            Munksgaard, Copenhagen (1979).\n"
			);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Book);
		Book book = (Book)reference.getPublication();
		assertEquals(3, book.getEditors().size());
		assertEquals("Engberg", book.getEditors().get(0).getSurname());
		assertEquals("Klenow", book.getEditors().get(1).getSurname());
		assertEquals("Leick", book.getEditors().get(2).getSurname());
		assertEquals("J.", book.getEditors().get(0).getFirstName());
		assertEquals("H.", book.getEditors().get(1).getFirstName());
		assertEquals("V.", book.getEditors().get(2).getFirstName());
		assertEquals("SPECIFIC EUKARYOTIC GENES", book.getBookTitle());
		assertEquals("117", book.getFirstPage());
		assertEquals("132", book.getLastPage());
		assertEquals("Munksgaard, Copenhagen", book.getPublisher());
		assertEquals(FlatFileUtils.getYear("1979"), book.getYear());
	}	
	
	public void testRead_BookNoBookTitle() throws IOException {
		initLineReader(
				 "  JOURNAL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n" +
			     "            :117-132;\n" +
			     "            Munksgaard, Copenhagen (1979).\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.16", Severity.ERROR));
	}
	
	public void testRead_BookNoPublisher() throws IOException {
		initLineReader(
				 "  JOURNAL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n" +
			     "            SPECIFIC EUKARYOTIC GENES:117-132;\n" +
			     "            (1979).\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.19", Severity.ERROR));
	}	
	
	public void testRead_BookNoYear() throws IOException {
		initLineReader(
				 "  JOURNAL   (in) Engberg J., Klenow H.., Leick V. (Eds.);\n" +
			     "            SPECIFIC EUKARYOTIC GENES:117-132;\n" +
			     "            Munksgaard, Copenhagen ().\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.20", Severity.ERROR));
	}		
	
	public void testRead_ArticleWithComma() throws IOException {
		initLineReader(
	    		"  JOURNAL   J.  Biol. Chem. 273_II( 48_V ),\n" +
	    		"            322X81 -32Y287(1998)\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem.", article.getJournal());
		assertEquals("273_II",article.getVolume());
		assertEquals("48_V",article.getIssue());
		assertEquals("322X81", article.getFirstPage());
		assertEquals("32Y287", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());
	}	
	
	public void testReadArticleWithComma_NoIssue() throws IOException {
		initLineReader(
	    		"  JOURNAL   J.  Biol. Chem. 273_II,\n" +
	    		"            322X81 -32Y287(1998).\n"
	    );
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem.", article.getJournal());
		assertEquals("273_II",article.getVolume());
		assertNull(article.getIssue());
		assertEquals("322X81", article.getFirstPage());
		assertEquals("32Y287", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());
	}	

	public void testReadArticleWithComma_NoVolume() throws IOException {
		initLineReader(
	    		"  JOURNAL   J. Biol. Chem. ( 48_V ),\n" +
	    		"            322X81 -32Y287(1998).\n"
	    );
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem. ( 48_V )", article.getJournal());
		assertEquals(null, article.getIssue());
		assertEquals(null, article.getVolume());
		assertEquals("322X81", article.getFirstPage());
		assertEquals("32Y287", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());	}	

	public void testReadArticleWithComma_NoYear() throws IOException {
		initLineReader(
	    		"  JOURNAL   J.  Biol. Chem. 273_II( 48_V ),\n" +
	    		"            322X81 -32Y287 (2018).\n"
	    );
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem.", article.getJournal());
		assertEquals("273_II",article.getVolume());
		assertEquals("48_V",article.getIssue());
		assertEquals("322X81", article.getFirstPage());
		assertEquals("32Y287", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2018"), article.getYear());
	}

	public void testArticleNoVolIssue() throws IOException {
		//Note: it may be //journal first_page-last_page (year) but
		// we parse it as //journal (issue) first_page-last_page (year)
		initLineReader(
				"  JOURNAL   G3 (Bethesda) 0-0 (2018) In press\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("G3 (Bethesda)", article.getJournal());
		assertEquals("0-0",article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals(null, article.getFirstPage());
		assertEquals(null, article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2018"),article.getYear());
	}

	public void testArticleVolFirstpage() throws IOException {
		//journal vol, firstpage (year)
		initLineReader(
				"  JOURNAL   Infect. Genet. Evol. VOL4, 34 (2017)\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Infect. Genet. Evol.", article.getJournal());
		assertEquals("VOL4",article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals("34", article.getFirstPage());
		assertEquals(null, article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2017"),article.getYear());
	}

	public void testArticleVolPageRange() throws IOException {
		//journal vol, firstpage (year)
		initLineReader(
				"  JOURNAL   Infect. Genet. Evol. VOL4, 34-100 (2017)\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Infect. Genet. Evol.", article.getJournal());
		assertEquals("VOL4",article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals("34", article.getFirstPage());
		assertEquals("100", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2017"),article.getYear());
	}

	public void testArticleInvalidVol() throws IOException {

		initLineReader(
				"  JOURNAL   Infect. Genet. Evol. ( (VOL4) ), 34-100 (2017)\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Infect. Genet. Evol. ( (VOL4) )", article.getJournal());
		assertEquals(null,article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals("34", article.getFirstPage());
		assertEquals("100", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2017"),article.getYear());
	}

	public void testArticleInvalidPage() throws IOException {
		initLineReader(
				"  JOURNAL   Infect. Genet. Evol. 12 (234), 34-100-100 (2017)\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}

	public void testArticleOnlyVolAndIssue() throws IOException {
		//journal vol  firstpage-lastpage (year)
		initLineReader(
				"  JOURNAL   Magallat Albasrat Liabhat Nahlat Altamr 15 (1-2) (2016) In press\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Magallat Albasrat Liabhat Nahlat Altamr", article.getJournal());
		assertEquals("15",article.getVolume());
		assertEquals("1-2",article.getIssue());
		assertEquals(null, article.getFirstPage());
		assertEquals(null, article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2016"),article.getYear());
	}

	public void testArticleOnlyPageRange() throws IOException {
		//journal vol  firstpage-lastpage (year)
		initLineReader(
				"  JOURNAL   Biosci Microbiota Food Health 16-027 (2017)\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Biosci Microbiota Food Health", article.getJournal());
		assertEquals("16-027",article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals(null, article.getFirstPage());
		assertEquals(null, article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2017"),article.getYear());
	}

	public void testArticleWithCommaNoPage() throws IOException {
		initLineReader(
				"  JOURNAL   Magallat Albasrat Liabhat Nahlat Altamr, Germany (2016) In press\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Magallat Albasrat Liabhat Nahlat", article.getJournal());
		assertEquals("Altamr",article.getVolume());
		assertEquals(null,article.getIssue());
		assertEquals("Germany", article.getFirstPage());
		assertEquals(null, article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2016"),article.getYear());
	}

	public void testRead_ArticleWithCommaNoLastPage() throws IOException {
		initLineReader(
	    		"  JOURNAL   BMC Biochem. 3 (1), 19 (2002)\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("BMC Biochem.", article.getJournal());
		assertEquals("3",article.getVolume());
		assertEquals("1",article.getIssue());
		assertEquals("19", article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2002"), article.getYear());
	}	

	public void testRead_ArticleWithCommaNoLastPage2() throws IOException {
		initLineReader(
	    		"  JOURNAL   BMC Biochem. 3 (1), 19- (2002)\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("BMC Biochem.", article.getJournal());
		assertEquals("3",article.getVolume());
		assertEquals("1",article.getIssue());
		assertEquals("19", article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2002"), article.getYear());
	}	
	
	public void testRead_ArticleWithoutComma() throws IOException {
		initLineReader(
	    		"  JOURNAL   J.  Biol. Chem. 273_II( 48_V )\n" +
	    		"            (1998) In press\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem.", article.getJournal());
		assertEquals("273_II",article.getVolume());
		assertEquals("48_V",article.getIssue());
		assertNull(article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());
	}	

	public void testRead_ArticleWithoutComma_OnlyJournal() throws IOException {
		initLineReader(
	    		"  JOURNAL   Plant Dis. (1998) In press \n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Plant Dis.", article.getJournal());
		assertNull(article.getVolume());
		assertNull(article.getIssue());
		assertNull(article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());
	}	
	
	public void testRead_ArticleWithoutComma_NoIssue() throws IOException {
		initLineReader(
	    		"  JOURNAL   J.  Biol. Chem. 273_II\n" +
	    		"            (1998) In press\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("J. Biol. Chem.", article.getJournal());
		assertEquals("273_II",article.getVolume());
		assertNull(article.getIssue());
		assertNull(article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1998"), article.getYear());
	}
	
	public void testRead_ArticleWithoutComma_NoVolume() throws IOException {
		initLineReader(
	    		"  JOURNAL   Oncogene (1999) In press\n"
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
//		for (ValidationMessage message : result.getMessages()) {
//			System.out.println(message.getMessage());
//		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Oncogene", article.getJournal());
		assertNull(article.getVolume());
		assertNull(article.getIssue());
		assertNull(article.getFirstPage());
		assertNull(article.getLastPage());
		assertEquals(FlatFileUtils.getYear("1999"), article.getYear());
	}	

	public void testRead_ArticleIssueWithComma() throws IOException {
		initLineReader(
	    		"  JOURNAL   	Clin. Hemorheol. Microcirc. 23 (2,3,4), 251-257 (2000)\n" 
	    );
		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Article);
		Article article = (Article)reference.getPublication();
		assertEquals("Clin. Hemorheol. Microcirc.", article.getJournal());
		assertEquals("23",article.getVolume());
		assertEquals("2,3,4",article.getIssue());
		assertEquals("251", article.getFirstPage());
		assertEquals("257", article.getLastPage());
		assertEquals(FlatFileUtils.getYear("2000"), article.getYear());
	}	
		
	public void testRead_Patent() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: EP O238993-A 3 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Patent);
		Patent patent = (Patent)reference.getPublication();
		assertEquals("EP", patent.getPatentOffice());
		assertEquals("O238993", patent.getPatentNumber());
		assertEquals("A", patent.getPatentType());
		assertEquals(new Integer(3), patent.getSequenceNumber());
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), patent.getDay());
		assertTrue(patent.getApplicants().size() == 2);
		assertEquals("BAYER AG", patent.getApplicants().get(0));
		assertEquals("GOOGLE AG", patent.getApplicants().get(1));
	}	
		
	public void testRead_PreGrantPatent() throws IOException {
		initLineReader(
			    "  JOURNAL   Pre-Grant Patent: EP O238993-A 3 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Patent);
		Patent patent = (Patent)reference.getPublication();
		assertEquals("EP", patent.getPatentOffice());
		assertEquals("O238993", patent.getPatentNumber());
		assertEquals("A", patent.getPatentType());
		assertEquals(new Integer(3), patent.getSequenceNumber());
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), patent.getDay());
		assertTrue(patent.getApplicants().size() == 2);
		assertEquals("BAYER AG", patent.getApplicants().get(0));
		assertEquals("GOOGLE AG", patent.getApplicants().get(1));
	}	
	
	public void testRead_InvalidPatent() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: PCT #W097/25420 17-JUL-1997;\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Patent);
		Patent patent = (Patent)reference.getPublication();
		assertEquals("PCT #W097/25420 17-JUL-1997", patent.getPatentOffice());
		assertNull(patent.getPatentNumber());
		assertNull(patent.getPatentType());
		assertNull(patent.getSequenceNumber());
		assertNull(patent.getDay());
		assertTrue(patent.getApplicants().size() == 0);
	}		
	
	public void testRead_PatentNoPatentOffice() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: O238993-A 3 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.5", Severity.ERROR));
	}		
	
	public void testRead_PatentNoPatentNumber() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: EP -A 3 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.6", Severity.ERROR));
	}		

	public void testRead_PatentNoPatentType() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: EP O238993- 3 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.7", Severity.ERROR));
	}		

	public void testRead_PatentNoSequenceNumber() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: EP O238993-A 10-SEP-1998;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.8", Severity.ERROR));
	}		

	public void testRead_PatentNoDay() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: EP O238993-A 3;\n" +
				"            BAYER   AG  ;\n" +
				"            GOOGLE AG;\n"
		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("RL.9", Severity.ERROR));
	}		

	public void testRead_PatentNoApplicants() throws IOException {
		initLineReader(
			    "  JOURNAL   Patent: JP 2000297097-A 1 24-OCT-2000\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Patent);
		Patent patent = (Patent)reference.getPublication();
		assertEquals("JP", patent.getPatentOffice());
		assertEquals("2000297097", patent.getPatentNumber());
		assertEquals("A", patent.getPatentType());
		assertEquals(new Integer(1), patent.getSequenceNumber());
		assertEquals(FlatFileUtils.getDay("24-OCT-2000"), patent.getDay());
		assertTrue(patent.getApplicants().size() == 0);
	}	
		
	public void testRead_Thesis() throws IOException {
		initLineReader(
				"  JOURNAL   Thesis (1998) Universitaet Muenchen\n" +
				"            near  Berlin"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Thesis);
		Thesis thesis = (Thesis)reference.getPublication();
		assertEquals(FlatFileUtils.getYear("1998"), thesis.getYear());
		assertEquals(
				"Universitaet Muenchen near Berlin",			
				thesis.getInstitute());
	}	
	
	public void testRead_ThesisNoYear() throws IOException {
		initLineReader(
				"  JOURNAL   Thesis () Universitaet Muenchen\n" +
				"            near  Berlin"		
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Thesis);
		Thesis thesis = (Thesis)reference.getPublication();
		assertNull(thesis.getYear());
		assertEquals(
				"Universitaet Muenchen near Berlin",			
				thesis.getInstitute());
	}		
	
	public void testRead_ThesisNoInstitute() throws IOException {
		initLineReader(
				"  JOURNAL   Thesis (1998)"
		);		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Thesis);
		Thesis thesis = (Thesis)reference.getPublication();
		assertEquals(FlatFileUtils.getYear("1998"), thesis.getYear());
		assertNull(thesis.getInstitute());
	}		
	
	public void testRead_SubmissionWithPublication() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted (10-SEP-1998) Great\n" +
				"            Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n" +
				"            Berkeley, CA 94720, USA\n"
		);
		Reference reference = lineReader.getCache().getReference();
		Publication publication = lineReader.getCache().getPublication();
		publication.setTitle("title");
		publication.setConsortium("consortium");
		publication.addAuthor((new ReferenceFactory()).createPerson("surname", "firstName"));
		publication.addXRef((new EntryFactory()).createXRef("UniProtKB", "A00001"));  
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), submission.getDay());
		assertEquals(
				"Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",				
				submission.getSubmitterAddress());
		assertTrue(publication.equals(reference.getPublication()));
	}
	
	public void testRead_SubmissionWithoutPublication() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted (10-SEP-1998) to the INSDC.\n" +
				"            Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n" +
				"            Berkeley, CA 94720, USA\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), submission.getDay());
		assertEquals(
				"Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",				
				submission.getSubmitterAddress());
	}

    /**
     * Uses "to the EMBL/GenBank/DDBJ databases"
     *
     * @throws IOException
     */
	public void testRead_SubmissionWithoutPublicationOld() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted (10-SEP-1998) to the EMBL/GenBank/DDBJ databases.\n" +
				"            Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n" +
				"            Berkeley, CA 94720, USA\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), submission.getDay());
		assertEquals(
				"Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",
				submission.getSubmitterAddress());
	}
	
	public void testRead_SubmissionNoDate() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted ()\n" +
				"            Great Drosophila Genome  Center, Lawrence Berkeley Laboratory, MS 64-121,\n" +
				"            Berkeley, CA 94720, USA\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertNull(submission.getDay());
		assertEquals(
				"Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA",				
				submission.getSubmitterAddress());	}
	
	public void testRead_SubmissionNoAddress() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted (10-SEP-1998) to the INSDC.\n"
		);		
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), submission.getDay());
		assertNull(submission.getSubmitterAddress());
	}

    /**
     * uses the "to the EMBL/Genbank/DDBJ"
     *
     * @throws IOException
     */
	public void testRead_SubmissionNoAddressOld() throws IOException {
		initLineReader(
				"  JOURNAL   Submitted (10-SEP-1998) to the EMBL/GenBank/DDBJ databases.\n"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Submission);
		Submission submission = (Submission)reference.getPublication();
		assertEquals(FlatFileUtils.getDay("10-SEP-1998"), submission.getDay());
		assertNull(submission.getSubmitterAddress());
	}

	public void testRead_UnpublishedWithPublication() throws IOException {
		initLineReader(
				"  JOURNAL   Unpublished."

		);
		Reference reference = lineReader.getCache().getReference();
		Publication publication = lineReader.getCache().getPublication();		
		ReferenceFactory referenceFactory = new ReferenceFactory(); 
		EntryFactory entryFactory = new EntryFactory(); 
		publication.setTitle("title");
		publication.setConsortium("consortium");
		publication.addAuthor(referenceFactory.createPerson("surname", "firstName"));
		publication.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));  
		reference.setPublication(publication);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Unpublished);
		assertTrue(publication.equals(reference.getPublication()));
	}
	
	public void testRead_UnpublishedWithoutPublication() throws IOException {
		initLineReader(
				"  JOURNAL   Unpublished."

		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof Unpublished);
	}
	
	public void testRead_ElectronicReferenceWithPublication() throws IOException {
		initLineReader(
				"  JOURNAL   (er) dah dah"
		);
		Reference reference = lineReader.getCache().getReference();
		Publication publication = lineReader.getCache().getPublication();		
		ReferenceFactory referenceFactory = new ReferenceFactory(); 
		EntryFactory entryFactory = new EntryFactory(); 
		publication.setTitle("title");
		publication.setConsortium("consortium");
		publication.addAuthor(referenceFactory.createPerson("surname", "firstName"));
		publication.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));  
		reference.setPublication(publication);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof ElectronicReference);
		assertTrue(publication.equals(reference.getPublication()));
		assertEquals("dah dah", ((ElectronicReference)reference.getPublication()).getText());
	}
	
	public void testRead_ElectronicReferenceWithoutPublication() throws IOException {
		initLineReader(
				"  JOURNAL   (er) dah dah"
		);
		Reference reference = lineReader.getCache().getReference();
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(reference.getPublication());
		assertTrue(reference.getPublication() instanceof ElectronicReference);
		assertEquals("dah dah", ((ElectronicReference)reference.getPublication()).getText());
	}
	
/*	public void testRead_FormatError() throws IOException {
		initLineReader(
				"  JOURNAL   Blah blah"

		);
		ValidationResult result = (new JournalReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}*/
}
