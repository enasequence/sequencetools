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
package uk.ac.ebi.embl.api.entry.location;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class LocalBase extends Base implements LocalLocation, Serializable {
	
	private static final long serialVersionUID = -4973041663951278963L;

	protected LocalBase(Long position, boolean complement) {
		super(position, complement);
	}
	
	protected LocalBase(Long position) {
		super(position);
	}	
			
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof LocalBase) {
			final LocalBase other = (LocalBase) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.getBeginPosition(), other.getBeginPosition());
			builder.append(this.getEndPosition(), other.getEndPosition());
			builder.append(this.isComplement(), other.isComplement());
			return builder.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.getBeginPosition());
        builder.append(this.getEndPosition());
        builder.append(this.isComplement());
        return builder.toHashCode();
    }
    
	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("beginPosition", getBeginPosition());
		builder.append("endPosition", getEndPosition());
		builder.append("complement", isComplement());
		return builder.toString();
	}
}
