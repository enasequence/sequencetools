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
package uk.ac.ebi.embl.api.entry.feature;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.LocationComparator;
import uk.ac.ebi.embl.api.validation.Origin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Feature implements HasOrigin, Serializable, Comparable<Feature> {
	
	private static final long serialVersionUID = -13325880439686170L;
	
	public static final String CDS_FEATURE_NAME = "CDS";
	public static final String SIG_PEPTIDE_FEATURE_NAME = "sig_peptide";
	public static final String MAP_PEPTIDE_FEATURE_NAME = "map_peptide";
	public static final String MAT_PEPTIDE_FEATURE_NAME = "mat_peptide";
	public static final String TRANSIT_PEPTIDE_FEATURE_NAME = "transit_peptide";
	public static final String SOURCE_FEATURE_NAME = "source";
	public static final String OLD_SEQUENCE_FEATURE_NAME = "old_sequence";
	public static final String PRIMER_BIND_FEATURE_NAME = "primer_bind";
	public static final String EXON_FEATURE_NAME = "exon";
	public static final String INTRON_FEATURE_NAME = "intron";
	public static final String GENE_FEATURE_NAME = "gene";
	public static final String GAP_FEATURE_NAME = "gap";
	public static final String PROMOTER_FEATURE_NAME = "promoter";
    public static final String CONFLICT_FEATURE_NAME = "conflict";
    public static final String REPEAT_REGION = "repeat_region";
    public static final String MISC_DIFFERENCE_FEATURE = "misc_difference";
    public static final String VARIATION_FEATURE = "variation";
    public static final String MISC_FEATURE_NAME = "misc_feature";
    public static final String MISC_RNA_FEATURE_NAME = "misc_RNA";
    public static final String mRNA_FEATURE_NAME = "mRNA";
    public static final String rRNA_FEATURE_NAME = "rRNA";
    public static final String tRNA_FEATURE_NAME = "tRNA";
    public static final String tmRNA_FEATURE_NAME = "tmRNA";
    public static final String precursorRNA_FEATURE_NAME = "precursor_RNA";
    public static final String ncRNA_FEATURE_NAME = "ncRNA";
    public static final String OPERON_FEATURE_NAME = "operon";
    public static final String REGULATORY_FEATURE_NAME = "regulatory";
    public static final String PROPETIDE_FEATURE_NAME ="propeptide";

    
    public static final String ASSEMBLY_GAP_FEATURE_NAME = "assembly_gap";
    
    private Origin origin;
	private String id;
	private String name;
	private CompoundLocation<Location> locations;
	private List<Qualifier> qualifiers;
	private List<XRef> xRefs;

    protected Feature(String featureName) {
		this.name = featureName;		
		this.qualifiers = new ArrayList<Qualifier>();
		this.xRefs = new ArrayList<XRef>();
	}

	protected Feature(String featureName, boolean join) {
		this.name = featureName;
		
		if (join) {
			this.locations = new Join<Location>();
		} else {
			this.locations = new Order<Location>();
		}
		this.qualifiers = new ArrayList<Qualifier>();
		this.xRefs = new ArrayList<XRef>();
	}
	
	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}	
	
	public List<Qualifier> getQualifiers(String name) {
		List<Qualifier> result = new ArrayList<Qualifier>();
		if (name == null) {
			return result;
		}
		for (Qualifier qualifier : qualifiers) {
			if (qualifier != null && qualifier.getName() != null
					&& qualifier.getName().equals(name)) {
				result.add(qualifier);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Qualifier> List<T> getComplexQualifiers(String name, List<T> qualifiers) {
		if (name == null) {
			return qualifiers;
		}
		for (Qualifier qualifier : this.qualifiers) {
			if (qualifier != null && qualifier.getName() != null
					&& qualifier.getName().equals(name)) {
				qualifiers.add((T)qualifier);
			}
		}
		return qualifiers;
	}

	public Qualifier getSingleQualifier(String name) {
		List<Qualifier> qualifiers = getQualifiers(name);
		return (qualifiers == null || qualifiers.isEmpty())? null : qualifiers.get(0);
	}
		
	public String getSingleQualifierValue(String name) {
		Qualifier qualifier = getSingleQualifier(name);
		if (qualifier == null) {
			return null;
		}
		return qualifier.getValue();
	}

	public void setSingleQualifier(String name) {
		Qualifier qualifier = getSingleQualifier(name);
		if (qualifier == null) {
			QualifierFactory factory = new QualifierFactory();
			qualifier = factory.createQualifier(name);
			addQualifier(qualifier);
		}
		qualifier.setValue(null);
	}
		
	public void setSingleQualifierValue(String name, String value) {
		Qualifier qualifier = getSingleQualifier(name);
		if (qualifier == null) {
			QualifierFactory factory = new QualifierFactory();
			qualifier = factory.createQualifier(name);
			addQualifier(qualifier);
		}
		qualifier.setValue(value);
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

	public String getName() {
		return name;
	}

    /**
     * allow public access for fixer classes
     * @param name
     */
	public void setName(String name) {
		this.name = name;
	}

	public CompoundLocation<Location> getLocations() {
		return locations;
	}

	public void setLocations(CompoundLocation<Location> locations) {
		this.locations = locations;
	}
	
	public List<Qualifier> getQualifiers() {
		return Collections.unmodifiableList(this.qualifiers);
	}
	
	public boolean addQualifier(String name) {
		QualifierFactory factory = new QualifierFactory();
		return this.qualifiers.add(factory.createQualifier(name));
	}
		
	public boolean addQualifier(String name, String value) {
		QualifierFactory factory = new QualifierFactory();
		return this.qualifiers.add(factory.createQualifier(name, value));
	}

	public boolean addQualifier(Qualifier qualifier) {
		return this.qualifiers.add(qualifier);
	}
	
	public boolean addQualifiers(Collection<Qualifier> qualifiers) {
		if (qualifiers == null) {
			return false;
		}
		return this.qualifiers.addAll(qualifiers);
	}
	
	public boolean removeQualifier(Qualifier qualifier) {
		return this.qualifiers.remove(qualifier);
	}

	public boolean removeSingleQualifier(String name) {
		for (Qualifier qualifier: this.qualifiers) {
			if (qualifier.getName().equals(name)) {				
				return removeQualifier(qualifier);
			}
		}
		return false;

	}

	public void removeAllQualifiers() {
		qualifiers.clear();
	}	
	
	public boolean removeQualifiersWithValue(String qualifierName, String qualifierValue) {
		for (Qualifier qualifier: getQualifiers(qualifierName)) {
			if (qualifier.getValue().equals(qualifierValue)) {
				return removeQualifier(qualifier);
			}
		}
		return false;
	}

	public boolean removeQualifier(String qualifierName) {
		for (Qualifier qualifier: getQualifiers(qualifierName)) {
			return removeQualifier(qualifier);
		}
		return false;
	}
	public List<XRef> getXRefs() {
		return Collections.unmodifiableList(this.xRefs);
	}
	
	public boolean addXRef(XRef xRef) {
		return this.xRefs.add(xRef);
	}
	
	public boolean addXRefs(Collection<XRef> xRefs) {
		if (xRefs == null) {
			return false;
		}
		return this.xRefs.addAll(xRefs);
	}
	
	public boolean removeXRef(XRef xRef) {
		return this.xRefs.remove(xRef);
	}	

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.id);
		builder.append(this.name);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Feature) {
			final Feature other = (Feature) obj;
			final EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.id, other.id);
			builder.append(this.name, other.name);
			builder.append(this.locations, other.locations);
			builder.append(this.qualifiers, other.qualifiers);
			builder.append(this.xRefs, other.xRefs);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	public int compareTo(Feature o) {
		// The natural order of the features is the order in
		// which they should appear in the flat file.
		if (this.equals(o)) {
			return 0;
		}
		String name = this.getName();
		if (name == null) {
			name = "";
		}
		String otherName = o.getName();
		if (otherName == null) {
			otherName = "";
		}
		// Source features go before other features.
		if (name.equals(Feature.SOURCE_FEATURE_NAME) &&
			!otherName.equals(Feature.SOURCE_FEATURE_NAME)) {
				return -1;				
		}
		if (otherName.equals(Feature.SOURCE_FEATURE_NAME) &&
			!name.equals(Feature.SOURCE_FEATURE_NAME)) {
				return 1;
		}

		Long minPosition = this.getLocations().getMinPosition();
		Long maxPosition = this.getLocations().getMaxPosition();
		
		Long otherMinPosition = o.getLocations().getMinPosition();
		Long otherMaxPosition = o.getLocations().getMaxPosition();
		
		// Order features with smaller minimum positions first.
		if (!minPosition.equals(otherMinPosition)) {
			return minPosition.compareTo(otherMinPosition);
		}

		// Order largest maximum positions first.
		return otherMaxPosition.compareTo(maxPosition);
	}		
	
	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("id", id);
		builder.append("name", name);
		builder.append("locations", locations);
		builder.append("qualifiers", qualifiers);
		builder.append("xRefs", xRefs);
		return builder.toString();
	}

    /**
     * The feature name and location as a string - handy for text summary
     * @return
     */
    public String getTextDescription() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(" ");
        List<Location> locationList = new ArrayList<Location>(locations.getLocations());
        Collections.sort(locationList, new LocationComparator(LocationComparator.START_LOCATION));
        for(Location location : locationList) {
            builder.append(" ");
            builder.append(location.getBeginPosition());
            builder.append("-");
            builder.append(location.getEndPosition());
        }

        return builder.toString();
    }
    
	public Long getLength()
	{
		Long minPosition = null;
		Long maxPosition = null;
		if (getLocations() != null)
		{
			maxPosition = getLocations().getMaxPosition();
			minPosition = getLocations().getMinPosition();
		}
		if (maxPosition != null && minPosition != null)
		{
			return maxPosition - minPosition+1;
		}
		
		return null;
		
	}
}
