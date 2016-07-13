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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Book extends Publication implements Comparable<Book> {
	
	private static final long serialVersionUID = 1783711866778315255L;
	
	private String bookTitle;
	private String firstPage;
	private String lastPage;
	private String publisher;
	private Date year;
	private List<Person> editors;

	protected Book() {
		this.editors = new ArrayList<Person>();
	}

	protected Book(Publication publication) {
		this();
		if(publication != null) {
			setTitle(publication.getTitle());
			setConsortium(publication.getConsortium());
			addAuthors(publication.getAuthors());
			addXRefs(publication.getXRefs());
		}
	}
		
	protected Book(String title) {
		this();
		setTitle(title);
	}
	
	protected Book(String title, String bookTitle, String firstPage, 
			String lastPage, String publisher) {
		this();
		setTitle(title);
		this.bookTitle = bookTitle;
		this.firstPage = firstPage;
		this.lastPage = lastPage;
		this.publisher = publisher;
	}
	
	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public String getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	public String getLastPage() {
		return lastPage;
	}

	public void setLastPage(String lastPage) {
		this.lastPage = lastPage;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Date getYear() {
		return year;
	}

	public void setYear(Date year) {
		this.year = year;
	}

	public List<Person> getEditors() {
		return Collections.unmodifiableList(this.editors);
	}

	public boolean addEditor(Person editor) {
		return this.editors.add(editor);
	}
	
	public boolean addEditors(Collection<Person> editors) {
		if (editors == null) {
			return false;
		}
		return this.editors.addAll(editors);
	}
	
	public boolean removeEditor(Person editor) {
		return this.editors.remove(editor);	
	}
	
	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.appendSuper(super.hashCode());
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Book) {
			final Book other = (Book) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.appendSuper(super.equals(other));
			builder.append(this.bookTitle, other.bookTitle);
			builder.append(this.firstPage, other.firstPage);
			builder.append(this.lastPage, other.lastPage);
			builder.append(this.publisher, other.publisher);
			builder.append(this.year, other.year);
			builder.append(this.editors, other.editors);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("bookTitle", bookTitle);
		builder.append("firstPage", firstPage);
		builder.append("lastPage", lastPage);
		builder.append("publisher", publisher);
		builder.append("year", year);
		builder.append("editors", editors);
		return builder.toString();
	}	
	
	public int compareTo(Book o) {
		final Book other = (Book) o;
		final CompareToBuilder builder = new CompareToBuilder();
		builder.appendSuper(super.compareTo(other));
		builder.append(this.bookTitle, other.bookTitle);
		builder.append(this.firstPage, other.firstPage);
		builder.append(this.lastPage, other.lastPage);
		builder.append(this.publisher, other.publisher);
		builder.append(this.year, other.year);
		Person[] thisEditors = this.editors.toArray(new Person[this.editors.size()]);
		Person[] otherEditors = other.editors.toArray(new Person[other.editors.size()]);
		builder.append(thisEditors, otherEditors);
		return builder.toComparison();
	}
}
