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

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.EntryFactory;

public class BookTest {

	private Book book;
	
	@Before
	public void setUp() throws Exception {
		book = new Book();
	}

	@Test
	public void testBook() {
		assertNull(book.getBookTitle());
		assertNull(book.getFirstPage());
		assertNull(book.getLastPage());
		assertNull(book.getPublisher());
		assertNull(book.getYear());
		assertTrue(book.getEditors().isEmpty());
		book.setBookTitle("bookTitle");
		book.setFirstPage("1");
		book.setLastPage("2");
		book.setPublisher("publisher");
		Date date = new Date();
		book.setYear(date);
		assertEquals("bookTitle", book.getBookTitle());
		assertEquals("1", book.getFirstPage());
		assertEquals("2", book.getLastPage());
		assertEquals("publisher", book.getPublisher());
		assertTrue(book.getEditors().isEmpty());		
	}

	@Test
	public void testGetEditors() {
		assertEquals(0, book.getEditors().size());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetEditors_UnmodifiableList() {
		book.getEditors().add(null);
	}	

	@Test
	public void testAddEditor() {
		Person person = new Person("X");
		assertTrue(book.addEditor(person));
		assertEquals(person, book.getEditors().get(0));
	}

	@Test
	public void testAddEditors() {
		Person person = new Person("X");
		assertTrue(book.addEditors(Arrays.asList(person)));
		assertEquals(person, book.getEditors().get(0));
	}

	@Test
	public void testAddEditors_Null() {
		assertFalse(book.addEditors(null));
		assertTrue(book.getEditors().isEmpty());
	}
	
	@Test
	public void testRemoveEditor() {
		Person personX = new Person("X");
		Person personY = new Person("Y");
		assertFalse(book.removeEditor(personX));
		
		book.addEditor(personX);
		assertEquals(1, book.getEditors().size());
		
		assertFalse(book.removeEditor(personY));
		assertEquals(1, book.getEditors().size());
		
		assertTrue(book.removeEditor(personX));
		assertEquals(0, book.getEditors().size());
	}

	@Test
	public void testHashCode() {
		book.hashCode();
		new Book("title").hashCode();
		new Book("title", "book-title", "1", "3", "pub").hashCode();
	}

	@Test
	public void testEquals() {
		assertTrue(book.equals(book));
		assertTrue(book.equals(new Book()));
		Book book2 = new Book();
		book.setTitle("title");
		assertFalse(book.equals(book2));
		book2.setTitle("title");
		assertTrue(book.equals(book2));
		book.setConsortium("consortium");
		assertFalse(book.equals(book2));
		book2.setConsortium("consortium");
		assertTrue(book.equals(book2));
		book.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertFalse(book.equals(book2));
		book2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(book.equals(book2));
		book.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertFalse(book.equals(book2));
		book2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(book.equals(book2));			
		Date date = new Date();
		book.setBookTitle("bookTitle");
		assertFalse(book.equals(book2));
		book2.setBookTitle("bookTitle");
		assertTrue(book.equals(book2));
		book.setFirstPage("1");
		assertFalse(book.equals(book2));
		book2.setFirstPage("1");
		assertTrue(book.equals(book2));
		book.setLastPage("2");
		assertFalse(book.equals(book2));
		book2.setLastPage("2");
		assertTrue(book.equals(book2));
		book.setPublisher("publisher");
		assertFalse(book.equals(book2));
		book2.setPublisher("publisher");
		assertTrue(book.equals(book2));
		book.setYear(date);
		assertFalse(book.equals(book2));
		book2.setYear(date);
		assertTrue(book.equals(book2));
		book.addEditor((new ReferenceFactory()).createPerson("surname", "firstName"));
		assertFalse(book.equals(book2));
		book2.addEditor((new ReferenceFactory()).createPerson("surname", "firstName"));
		assertTrue(book.equals(book2));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(book.equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(book.toString());
		assertNotNull(new Book("x").toString());
		assertNotNull(new Book("t", "b-t", "1", "3", "p").toString());
	}
	
	@Test
	public void testCompareTo() {
		assertTrue(book.compareTo(book) == 0);
		assertTrue(book.compareTo(new Book()) == 0);
		Book book2 = new Book();
		book.setTitle("title");
		// null < not null
		assertTrue(book.compareTo(book2) > 0);
		book2.setTitle("title");
		assertTrue(book.compareTo(book2) == 0);
		book.setConsortium("consortium");
		assertTrue(book.compareTo(book2) > 0);
		book2.setConsortium("consortium");
		assertTrue(book.compareTo(book2) == 0);
		book.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(book.compareTo(book2) > 0);
		book2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(book.compareTo(book2) == 0);
		book.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(book.compareTo(book2) > 0);
		book2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(book.compareTo(book2) == 0);
		Date date = new Date();
		book.setBookTitle("bookTitle");
		assertTrue(book.compareTo(book2) > 0);
		book2.setBookTitle("bookTitle");
		assertTrue(book.compareTo(book2) == 0);
		book.setFirstPage("1");
		assertTrue(book.compareTo(book2) > 0);
		book2.setFirstPage("1");
		assertTrue(book.compareTo(book2) == 0);
		book.setLastPage("2");
		assertTrue(book.compareTo(book2) > 0);
		book2.setLastPage("2");
		assertTrue(book.compareTo(book2) == 0);
		book.setPublisher("publisher");
		assertTrue(book.compareTo(book2) > 0);
		book2.setPublisher("publisher");
		assertTrue(book.compareTo(book2) == 0);
		book.setYear(date);
		assertTrue(book.compareTo(book2) > 0);
		book2.setYear(date);
		assertTrue(book.compareTo(book2) == 0);
		book.addEditor((new ReferenceFactory()).createPerson("surname", "firstName"));
		assertTrue(book.compareTo(book2) > 0);
		book2.addEditor((new ReferenceFactory()).createPerson("surname", "firstName"));
		assertTrue(book.compareTo(book2) == 0);		
	}
}
