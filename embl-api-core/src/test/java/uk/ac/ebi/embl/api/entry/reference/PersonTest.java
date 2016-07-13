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

import org.junit.Before;
import org.junit.Test;

public class PersonTest {

	private Person person = new Person(null);
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPerson() {
		assertNull(person.getSurname());
		assertNull(person.getFirstName());
		person.setFirstName("firstname");
		person.setSurname("surname");
		assertEquals("firstname", person.getFirstName());
		assertEquals("surname", person.getSurname());
	}

	@Test
	public void testHashCode() {
		person.hashCode();
		new Person("surname").hashCode();
		new Person("surname", "firstname").hashCode();
	}
	
	@Test
	public void testEquals() {
		assertTrue(person.equals(person));
		assertTrue(person.equals(new Person()));
		Person person2 = new Person();
		person.setFirstName("firstname");
		assertFalse(person.equals(person2));
		person2.setFirstName("firstname");
		assertTrue(person.equals(person2));
		person.setSurname("surname");
		assertFalse(person.equals(person2));
		person2.setSurname("surname");
		assertTrue(person.equals(person2));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(person.equals(null));
		assertFalse(person.equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(person.toString());
		assertNotNull(new Person("sur", "first").toString());
	}

	@Test
	public void testCompareTo() {
		assertTrue(person.compareTo(person) == 0);
		assertTrue(person.compareTo(new Person()) == 0);
		Person person2 = new Person();
		person.setFirstName("firstname");
		assertTrue(person.compareTo(person2) > 0);
		person2.setFirstName("firstname");
		assertTrue(person.compareTo(person2) == 0);
		person.setSurname("surname");
		assertTrue(person.compareTo(person2) > 0);
		person2.setSurname("surname");
		assertTrue(person.compareTo(person2) == 0);
	}	
}
