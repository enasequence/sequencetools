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

public class Patent extends Publication	implements Comparable<Patent>{
	
	private static final long serialVersionUID = 1783711866778315255L;
	
	private String patentOffice;
	private String patentNumber;
	private String patentType;
	private Integer sequenceNumber;
	private Date day;
	private List<String> applicants;
	
	protected Patent() {
		this.applicants = new ArrayList<String>();
	}

	protected Patent(Publication publication) {
		this();
		if(publication != null) {
			setTitle(publication.getTitle());
			setConsortium(publication.getConsortium());
			addAuthors(publication.getAuthors());
			addXRefs(publication.getXRefs());
		}
	}
		
	protected Patent(String title, String patentOffice, String patentNumber, 
			String patentType, Integer sequenceNumber, Date day) {
		this();
		setTitle(title);		
		this.patentOffice = patentOffice;
		this.patentNumber = patentNumber;
		this.patentType = patentType;
		this.sequenceNumber = sequenceNumber;
		this.day = day;
	}

	public String getPatentOffice() {
		return patentOffice;
	}

	public void setPatentOffice(String patentOffice) {
		this.patentOffice = patentOffice;
	}

	public String getPatentNumber() {
		return patentNumber;
	}

	public void setPatentNumber(String patentNumber) {
		this.patentNumber = patentNumber;
	}

	public String getPatentType() {
		return patentType;
	}

	public void setPatentType(String patentType) {
		this.patentType = patentType;
	}
	
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public List<String> getApplicants() {
		return Collections.unmodifiableList(this.applicants);
	}

	public boolean addApplicant(String applicant) {
		return this.applicants.add(applicant);
	}
	
	public boolean addApplicants(Collection<String> applicant) {
		if (applicant == null) {
			return false;
		}
		return this.applicants.addAll(applicant);
	}
	
	public boolean removeApplicant(String applicant) {
		return this.applicants.remove(applicant);	
	}
	
	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.appendSuper(super.hashCode());
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Patent) {
			final Patent other = (Patent) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.appendSuper(super.equals(other));
			builder.append(this.patentOffice, other.patentOffice);
			builder.append(this.patentNumber, other.patentNumber);
			builder.append(this.patentType, other.patentType);
			builder.append(this.sequenceNumber, other.sequenceNumber);
			builder.append(this.day, other.day);
			builder.append(this.applicants, other.applicants);			
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		
		builder.append("patentOffice", patentOffice);
		builder.append("patentNumber", patentNumber);
		builder.append("patentType", patentType);
		builder.append("day", day);
		builder.append("applicants", applicants);
		return builder.toString();
	}	
	
	public int compareTo(Patent o) {
		final Patent other = o;
		final CompareToBuilder builder = new CompareToBuilder();
		builder.appendSuper(super.compareTo(other));
		builder.append(this.patentOffice, other.patentOffice);
		builder.append(this.patentNumber, other.patentNumber);
		builder.append(this.patentType, other.patentType);
		builder.append(this.sequenceNumber, other.sequenceNumber);
		builder.append(this.day, other.day);
		String[] thisApplicants = this.applicants.toArray(new String[this.applicants.size()]);
		String[] otherApplicants = other.applicants.toArray(new String[this.applicants.size()]); 
		builder.append(thisApplicants, otherApplicants);
		return builder.toComparison();
	}
}
