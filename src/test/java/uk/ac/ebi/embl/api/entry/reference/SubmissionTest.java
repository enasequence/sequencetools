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

public class SubmissionTest {

	private Submission submission;
	
	@Before
	public void setUp() throws Exception {
		submission = new Submission(null, null, null);
	}

	@Test
	public void testSubmission() {
		assertNull(submission.getDay());
		assertNull(submission.getSubmitterAddress());		
		Date date = new Date(); 
		submission.setDay(date);
		submission.setSubmitterAddress("address");
		assertEquals(date, submission.getDay());		
		assertEquals("address", submission.getSubmitterAddress());		
	}

	@Test
	public void testHashCode() {
		submission.hashCode();
		new Submission("t", new Date(), "a").hashCode();
	}


	@Test
	public void testEquals() {
		assertTrue(submission.equals(submission));
		assertTrue(submission.equals(new Submission()));
		Submission submission2 = new Submission();
		submission.setTitle("title");
		assertFalse(submission.equals(submission2));
		submission2.setTitle("title");
		assertTrue(submission.equals(submission2));
		submission.setConsortium("consortium");
		assertFalse(submission.equals(submission2));
		submission2.setConsortium("consortium");
		assertTrue(submission.equals(submission2));
		submission.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertFalse(submission.equals(submission2));
		submission2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(submission.equals(submission2));
		submission.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertFalse(submission.equals(submission2));
		submission2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(submission.equals(submission2));	
		submission.setSubmitterAddress("submitterAddress");
		assertFalse(submission.equals(submission2));
		submission2.setSubmitterAddress("submitterAddress");
		assertTrue(submission.equals(submission2));	
		submission.setDay(new Date());
		assertFalse(submission.equals(submission2));
		submission2.setDay(new Date());
		assertTrue(submission.equals(submission2));	
	}
	
	@Test
	public void testEquals_WrongObject() {
		assertFalse(submission.equals(new String()));
		assertFalse(submission.equals(null));
	}
	
	@Test
	public void testToString() {
		assertNotNull(submission.toString());
		assertNotNull(new Submission("t", new Date(), "a").toString());
	}
	
	@Test
	public void testCompareTo() {
		assertTrue(submission.compareTo(submission) == 0);
		assertTrue(submission.compareTo(new Submission()) == 0);
		Submission submission2 = new Submission();
		submission.setTitle("title");
		// null < not null
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.setTitle("title");
		assertTrue(submission.compareTo(submission2) == 0);
		submission.setConsortium("consortium");
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.setConsortium("consortium");
		assertTrue(submission.compareTo(submission2) == 0);
		submission.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(submission.compareTo(submission2) == 0);
		submission.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(submission.compareTo(submission2) == 0);
		submission.setSubmitterAddress("submitterAddress");
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.setSubmitterAddress("submitterAddress");
		assertTrue(submission.compareTo(submission2) == 0);
		submission.setDay(new Date());
		assertTrue(submission.compareTo(submission2) > 0);
		submission2.setDay(new Date());
		assertTrue(submission.compareTo(submission2) == 0);
	}	
}
