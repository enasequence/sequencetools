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

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public class Reference implements HasOrigin, Serializable, Comparable<Reference> {
	
	private static final long serialVersionUID = -4700988799848365167L;
	
	private Origin origin;
	private String id;
	private Publication publication;
	private Integer referenceNumber;
	private String comment;
	private CompoundLocation<LocalRange> locations;
	private boolean isAuthorExists;
	private boolean isLocationExists;
	private boolean isNumberExists;
	
	public boolean isAuthorExists()
	{
		return isAuthorExists;
	}

	public void setAuthorExists(boolean isAuthorExists)
	{
		this.isAuthorExists = isAuthorExists;
	}

	public boolean isLocationExists()
	{
		return isLocationExists;
	}

	public void setLocationExists(boolean isLocationExists)
	{
		this.isLocationExists = isLocationExists;
	}

	public boolean isNumberExists()
	{
		return isNumberExists;
	}

	public void setNumberExists(boolean isNumberExists)
	{
		this.isNumberExists = isNumberExists;
	}

	protected Reference() {
		this.locations = new Order<LocalRange>();
	}

	protected Reference(Publication publication, Integer referenceNumber) {
		this.locations = new Order<LocalRange>();
		this.publication = publication;
		this.referenceNumber = referenceNumber;
		this.isAuthorExists=false;
		this.isLocationExists=false;
		this.isNumberExists=false;
	}
	
	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}	

	public String getId() {
		return id;
	}

	public void setId(Object id) {
		if (id != null) {
			this.id = id.toString();
		} else {
			this.id = null;
		}
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	public Integer getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(Integer referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public CompoundLocation<LocalRange> getLocations() {
		return locations;
	}

	public void setLocations(CompoundLocation<LocalRange> locations) {
		this.locations = locations;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.id);
		builder.append(this.referenceNumber);
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Reference) {
			final Reference other = (Reference) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.referenceNumber, other.referenceNumber);
			builder.append(this.publication, other.publication);
			builder.append(this.comment, other.comment);
			builder.append(this.locations, other.locations);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("id", id);
		builder.append("referenceNumber", referenceNumber);
		builder.append("publication", publication);
		builder.append("comment", comment);
		builder.append("locations", locations);
		return builder.toString();
	}	
	
	public int compareTo(Reference o) {
		final Reference other = o;
		final CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.referenceNumber, other.referenceNumber);
		builder.append(this.publication, other.publication);
		builder.append(this.comment, other.comment);
		// TODO builder.append(this.locations, other.locations);
		return builder.toComparison();
	}
}
