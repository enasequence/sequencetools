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

import java.util.Date;

import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;

public class ReferenceFactory {

	public Reference createReference() {
		return new Reference();
	}
		
	public Reference createReference(Publication publication, 
			Integer referenceNumber) {
		return new Reference(publication, referenceNumber);
	}

	public Publication createPublication() {
		return new Publication();
	}
		
	public LocalRange createRange(Long beginPosition, 
			Long endPosition) {
		LocationFactory locationFactory = new LocationFactory();
		return locationFactory.createLocalRange(
				beginPosition, endPosition); 
	}

	public Article createArticle() {
		return new Article();
	}
	
	public Article createArticle(Publication publication) {
		return new Article(publication);
	}
		
	public Article createArticle(String title, String journal) {
		return new Article(title, journal);
	}

	public Book createBook() {
		return new Book();
	}

	public Book createBook(Publication publication) {
		return new Book(publication);
	}
		
	public Book createBook(String title, String bookTitle, String firstPage, 
			String lastPage, String publisher) {
		return new Book(title, bookTitle, firstPage, lastPage, publisher);
	}

	public Thesis createThesis() {
		return new Thesis();
	}

	public Thesis createThesis(Publication publication) {
		return new Thesis(publication);
	}
		
	public Thesis createThesis(String title, Date year, String institute) {
		return new Thesis(title, year, institute);
	}

	public Unpublished createUnpublished() {
		return new Unpublished();
	}

	public Unpublished createUnpublished(Publication publication) {
		return new Unpublished(publication);
	}
		
	public Unpublished createUnpublished(String title) {
		return new Unpublished(title);
	}

	public ElectronicReference createElectronicReference() {
		return new ElectronicReference();
	}

	public ElectronicReference createElectronicReference(Publication publication) {
		return new ElectronicReference(publication);
	}
		
	public ElectronicReference createElectronicReference(String title, 
			String text) {
		return new ElectronicReference(title, text);
	}

	public Submission createSubmission() {
		return new Submission();
	}

	public Submission createSubmission(Publication publication) {
		return new Submission(publication);
	}
		
	public Submission createSubmission(String title, Date day, 
			String submitterAddress) {
		return new Submission(title, day, submitterAddress);
	}

	public Patent createPatent() {
		return new Patent();
	}

	public Patent createPatent(Publication publication) {
		return new Patent(publication);
	}

	public Patent createPatent(String title, String patentOffice, 
			String patentNumber, String patentType, Integer sequenceNumber, 
			Date day) {
		return new Patent(title, patentOffice, patentNumber, 
				patentType, sequenceNumber, day);
	}

	public Person createPerson() {
		return new Person();
	}
	
	public Person createPerson(String surname) {
		return new Person(surname);
	}
		
	public Person createPerson(String surname, String firstName) {
		return new Person(surname, firstName);
	}

}
