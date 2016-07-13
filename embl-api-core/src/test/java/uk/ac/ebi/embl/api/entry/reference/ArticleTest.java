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
package uk.ac.ebi.embl.api.entry.reference;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.EntryFactory;

public class ArticleTest {

	private Article article;
	
	@Before
	public void setUp() throws Exception {
		article = new Article();
	}

	@Test
	public void testArticle() {
		assertNull(article.getJournal());
		assertNull(article.getFirstPage());
		assertNull(article.getLastPage());
		assertNull(article.getVolume());
		assertNull(article.getIssue());
		assertNull(article.getYear());
		article.setFirstPage("1");
		article.setLastPage("2");
		article.setIssue("issue");
		article.setVolume("volume");
		article.setJournal("journal");
		Date date = new Date();
		article.setYear(date);
		assertEquals("journal", article.getJournal());		
		assertEquals("1", article.getFirstPage());
		assertEquals("2", article.getLastPage());		
		assertEquals("volume", article.getVolume());
		assertEquals("issue", article.getIssue());		
		assertEquals(date, article.getYear());		
	}

	@Test
	public void testHashCode() {
		article.hashCode();
		new Article("t", "j").hashCode();
	}

	@Test
	public void testEquals() {
		assertTrue(article.equals(article));
		assertTrue(article.equals(new Article()));
		Article article2 = new Article();
		article.setTitle("title");
		assertFalse(article.equals(article2));
		article2.setTitle("title");
		assertTrue(article.equals(article2));
		article.setConsortium("consortium");
		assertFalse(article.equals(article2));
		article2.setConsortium("consortium");
		assertTrue(article.equals(article2));
		article.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertFalse(article.equals(article2));
		article2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(article.equals(article2));
		article.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertFalse(article.equals(article2));
		article2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(article.equals(article2));	
		article.setFirstPage("1");
		assertFalse(article.equals(article2));
		article2.setFirstPage("1");
		assertTrue(article.equals(article2));
		article.setLastPage("2");
		assertFalse(article.equals(article2));
		article2.setLastPage("2");
		assertTrue(article.equals(article2));
		article.setIssue("issue");
		assertFalse(article.equals(article2));
		article2.setIssue("issue");
		assertTrue(article.equals(article2));
		article.setVolume("volume");
		assertFalse(article.equals(article2));
		article2.setVolume("volume");
		assertTrue(article.equals(article2));
		article.setJournal("journal");
		assertFalse(article.equals(article2));
		article2.setJournal("journal");
		assertTrue(article.equals(article2));
		Date date = new Date();
		article.setYear(date);
		assertFalse(article.equals(article2));
		article2.setYear(date);
		assertTrue(article.equals(article2));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(article.equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(article.toString());
		assertNotNull(new Article("t", "j").toString());
	}
	
	@Test
	public void testCompareTo() {
		assertTrue(article.compareTo(article) == 0);
		assertTrue(article.compareTo(new Article()) == 0);
		Article article2 = new Article();
		article.setTitle("title");
		// null < not null
		assertTrue(article.compareTo(article2) > 0);
		article2.setTitle("title");
		assertTrue(article.compareTo(article2) == 0);
		article.setConsortium("consortium");
		assertTrue(article.compareTo(article2) > 0);
		article2.setConsortium("consortium");
		assertTrue(article.compareTo(article2) == 0);
		article.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(article.compareTo(article2) > 0);
		article2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(article.compareTo(article2) == 0);
		article.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(article.compareTo(article2) > 0);
		article2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(article.compareTo(article2) == 0);
		article.setFirstPage("1");
		assertTrue(article.compareTo(article2) > 0);
		article2.setFirstPage("1");
		assertTrue(article.compareTo(article2) == 0);
		article.setLastPage("2");
		assertTrue(article.compareTo(article2) > 0);
		article2.setLastPage("2");
		assertTrue(article.compareTo(article2) == 0);
		article.setIssue("issue");
		assertTrue(article.compareTo(article2) > 0);
		article2.setIssue("issue");
		assertTrue(article.compareTo(article2) == 0);
		article.setVolume("volume");
		assertTrue(article.compareTo(article2) > 0);
		article2.setVolume("volume");
		assertTrue(article.compareTo(article2) == 0);
		article.setJournal("journal");
		assertTrue(article.compareTo(article2) > 0);
		article2.setJournal("journal");
		assertTrue(article.compareTo(article2) == 0);
		Date date = new Date();
		article.setYear(date);
		assertTrue(article.compareTo(article2) > 0);
		article2.setYear(date);	
		assertTrue(article.compareTo(article2) == 0);
	}	
}
