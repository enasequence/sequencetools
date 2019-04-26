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

public class Submission extends Publication implements Comparable<Submission> {
	
	private static final long serialVersionUID = 2331121591169041723L;
	
	private Date day;
	private String submitterAddress;
	
	protected Submission() {
	}

	protected Submission(Publication publication) {
		this();
		if(publication != null) {
			setTitle(publication.getTitle());
			setConsortium(publication.getConsortium());
			addAuthors(publication.getAuthors());
			addXRefs(publication.getXRefs());
		}
	}
	
	protected Submission(String title, Date day, String submitterAddress) {
		this();
		setTitle(title);
		this.day = day;
		this.submitterAddress = submitterAddress;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public String getSubmitterAddress() {
		return submitterAddress;
	}

	public void setSubmitterAddress(String submitterAddress) {
		this.submitterAddress = submitterAddress;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.appendSuper(super.hashCode());
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Submission) {
			final Submission other = (Submission) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.appendSuper(super.equals(other));
			builder.append(this.day, other.day);
			builder.append(this.submitterAddress, other.submitterAddress);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("day", day);
		builder.append("submitterAddress", submitterAddress);
		return builder.toString();
	}
	
	public int compareTo(Submission o) {
		final Submission other = o;
		final CompareToBuilder builder = new CompareToBuilder();
		builder.appendSuper(super.compareTo(other));
		builder.append(this.day, other.day);
		builder.append(this.submitterAddress, other.submitterAddress);
		return builder.toComparison();
	}
}
