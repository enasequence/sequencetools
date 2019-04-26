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
package uk.ac.ebi.embl.api.entry.sequence;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;



public class SequenceAccession implements Serializable, 
		Comparable<SequenceAccession> {
	
	private static final long serialVersionUID = -4945791550055963506L;

	private static final String SEPARATOR = ".";
	private static final String SEPARATOR_PATTERN = "\\.";
	
	private String accession;
	private Integer version;
	
	public SequenceAccession(String value) {
		this.accession = parseAccession(value);
		this.version = parseVersion(value);
	}

	public SequenceAccession(String accession, Integer version) {
		this.accession = accession;
		this.version = version;
	}

	public static String parseAccession(String value) {
		if (StringUtils.isEmpty(value)) {
			throw new AccessionFormatException("Improper accession value: " + 
					value);
		}
		String[] values = value.split(SEPARATOR_PATTERN);
		if (values.length > 0 && StringUtils.isNotEmpty(values[0])) {
			return values[0];
		}
		throw new AccessionFormatException("Improper accession value: " + 
				value);
	}
	
	public static Integer parseVersion(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String[] values = value.split(SEPARATOR_PATTERN);
		if (values.length > 1) {
			try {
				int intValue = Integer.parseInt(values[1]);
				if (intValue < 0) {
					throw new AccessionFormatException("Version must be " +
							"a positive number: " + intValue);
				}
				return intValue;
			} catch (NumberFormatException e) {
				throw new AccessionFormatException();
			}
		}
		return null;
	}
	
	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.accession);
		builder.append(this.version);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SequenceAccession) {
			final SequenceAccession other = (SequenceAccession) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.accession, other.accession);
			builder.append(this.version, other.version);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotEmpty(accession)) {
			builder.append(accession);
		}
		if (version != null) {
			builder.append(SEPARATOR).append(version);
		}
		return builder.toString();
	}
	
	public int compareTo(SequenceAccession o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.accession, o.accession);
		builder.append(this.version, o.version);
		return builder.toComparison();
	}	

}
