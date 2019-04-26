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
package uk.ac.ebi.embl.api.entry;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public class XRef implements HasOrigin, Serializable, Comparable<XRef> {

	private static final long serialVersionUID = -2171849413762536576L;
	
	private Origin origin;
	private String database;
	private String primaryAccession;
	private String secondaryAccession;

	public XRef() {
	}	
	
	public XRef(String database, String primaryAccession) {
		this.database = database;
		this.primaryAccession = primaryAccession;
	}

	public XRef(String database, String primaryAccession,
			String secondaryAccession) {
		this(database, primaryAccession);
		this.secondaryAccession = secondaryAccession;
	}
	
	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}	
	
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getPrimaryAccession() {
		return primaryAccession;
	}

	public void setPrimaryAccession(String primaryAccession) {
		this.primaryAccession = primaryAccession;
	}

	public String getSecondaryAccession() {
		return secondaryAccession;
	}

	public void setSecondaryAccession(String secondaryAccession) {
		this.secondaryAccession = secondaryAccession;
	}
	
	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.database);
		builder.append(this.primaryAccession);
		builder.append(this.secondaryAccession);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof XRef) {
			final XRef other = (XRef) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.database, other.database);
			builder.append(this.primaryAccession, other.primaryAccession);
			builder.append(this.secondaryAccession, other.secondaryAccession);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("database", database);
		builder.append("primaryAccession", primaryAccession);
		builder.append("secondaryAccession", secondaryAccession);
		return builder.toString();
	}
	
	public int compareTo(XRef o) {
		final CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.database, o.database);
		builder.append(this.primaryAccession, o.primaryAccession);
		builder.append(this.secondaryAccession, o.secondaryAccession);
		return builder.toComparison();
	}	
	
	
}
