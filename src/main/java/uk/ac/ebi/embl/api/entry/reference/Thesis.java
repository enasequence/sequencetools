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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Thesis extends Publication implements Comparable<Thesis> {
	
	private static final long serialVersionUID = 6743707173965018448L;
	
	private Date year;
	private String institute;

	protected Thesis() {
	}
	
	protected Thesis(Publication publication) {
		this();
		if(publication != null) {
			setTitle(publication.getTitle());
			setConsortium(publication.getConsortium());
			addAuthors(publication.getAuthors());
			addXRefs(publication.getXRefs());
		}
	}
		
	protected Thesis(String title, Date year, String institute) {
		this();
		setTitle(title);
		this.year = year;
		this.institute = institute;
	}

	public String getInstitute() {
		return institute;
	}

	public void setInstitute(String institute) {
		this.institute = institute;
	}

	public Date getYear() {
		return year;
	}

	public void setYear(Date year) {
		this.year = year;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.appendSuper(super.hashCode());
		builder.append(this.institute);
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Thesis) {
			final Thesis other = (Thesis) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.appendSuper(super.equals(other));
			builder.append(this.institute, other.institute);
			builder.append(this.year, other.year);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("institute", institute);
		builder.append("year", year);
		return builder.toString();
	}	
	
	public int compareTo(Thesis o) {
		final Thesis other = o;
		final CompareToBuilder builder = new CompareToBuilder();
		builder.appendSuper(super.compareTo(other));
		builder.append(this.institute, other.institute);
		builder.append(this.year, other.year);
		return builder.toComparison();
	}		
}
