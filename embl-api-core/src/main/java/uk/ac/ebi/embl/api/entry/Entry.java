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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.HasOrigin;
import uk.ac.ebi.embl.api.validation.Origin;

public class Entry implements HasOrigin, Serializable, Comparable<Entry> {
	
	private static final long serialVersionUID = 6081010525908462117L;
	
	public enum Status
	{
		DRAFT(1),
		PRIVATE(2),
		CANCELLED(3),
		PUBLIC(4),
		SUPPRESSED(5),
		KILLED(6),
		TEMPORARY_SUPPRESSED(7),
		TEMPORARY_KILLED(8);
		private int statusId;
		
		Status(int statusId)
		{
			this.statusId = statusId;
		}
		
		public static Status getStatus(int statusId)
		{
			for(Status s:values())
			{
				if(statusId==s.getStatusId())
					return s;
    		}
			
			return null;
		}
		
		public int getStatusId()
		{
			return statusId;
		}
	}
 	
	
    /**
     * dataclass statics
     */
    public static final String EST_DATACLASS = "EST";
    public static final String WGS_DATACLASS = "WGS";
    public static final String GSS_DATACLASS = "GSS";
    public static final String STANDARD_DATACLASS = "STD";
    public static final String CON_DATACLASS = "CON";
    public static final String TPA_DATACLASS = "TPA";
    public static final String STS_DATACLASS = "STS";
    public static final String PRT_DATACLASS="PRT";
    public static final String PAT_DATACLASS="PAT";
    public static final String STD_DATACLASS="STD";
    public static final String HTG_DATACLASS="HTG";
    public static final String SET_DATACLASS="SET";
    public static final String TSA_DATACLASS="TSA";
    public static final String TPX_DATACLASS="TPX";
    public static final String HTC_DATACLASS="HTC";
    public static final int DEFAULT_MIN_GAP_LENGTH=10;
    /**
     * mol_type statics
     */
    public static final String PROTEIN = "protein";

	private String id;
	private Origin  origin;
	private String primaryAccession;
	private Integer version;
	private Date holdDate;
	private Date ffDate;
	private Status status;
	private Date firstPublic;
	private Date lastUpdated;
	private Integer firstPublicRelease;
	private Integer lastUpdatedRelease;
	private String dataClass;
	private String division;
	private Text description;
	private Text comment;
	private Sequence sequence;
	private String submitterAccession;
	private Integer submitterWgsVersion;
	private boolean deleteEntry;
	private long idLineSequenceLength;
	private List<Text> secondaryAccessions;
	private List<Text> keywords;
	private List<Text> projectAccessions;
	private List<Feature> features;
	private List<Reference> references;
	private List<XRef> xRefs;
	private List<Text> masterConAccessions;
	private List<Text> masterWgsAccessions;
	private List<Text> masterTpaAccessions;
	private List<Text> masterTsaAccessions;
	protected List<Assembly> assemblies;
	private boolean isAnnotationOnlyCON=false;
	private boolean isSingletonAgp=false;
	private boolean isNonExpandedCON=false;

	public Entry() {
		this.secondaryAccessions = new ArrayList<Text>();
		this.keywords = new ArrayList<Text>();
		this.projectAccessions = new ArrayList<Text>();
		this.references = new ArrayList<Reference>();
		this.features = new ArrayList<Feature>();
		this.xRefs = new ArrayList<XRef>();
		this.assemblies = new ArrayList<Assembly>();
        this.description = new Text();
        this.comment = new Text();
		this.masterConAccessions = new ArrayList<Text>();
		this.masterWgsAccessions = new ArrayList<Text>();
		this.masterTpaAccessions = new ArrayList<Text>();
		this.masterTsaAccessions = new ArrayList<Text>();
		this.deleteEntry=false;
		this.isNonExpandedCON=false;

    }
		
	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}
	
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPrimaryAccession() {
		return this.primaryAccession;
	}

	public void setPrimaryAccession(String primaryacc) {
		this.primaryAccession = primaryacc;
	}
	
	public String getDivision() {
		return this.division;
	}

	public void setDivision(String taxonomicDivision) {
		this.division = taxonomicDivision;
	}

	public String getDataClass() {
		return this.dataClass;
	}

	public void setDataClass(String dataClass) {
		this.dataClass = dataClass;
	}

	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer entryVersion) {
		this.version = entryVersion;
	}

	public Date getHoldDate() {
		return holdDate;
	}

	public void setHoldDate(Date holdDate) {
		this.holdDate = holdDate;
	}

	public Date getFFDate() {
		return ffDate;
	}

	public void setFFDate(Date ffDate) {
		this.ffDate = ffDate;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Text getDescription() {
		return this.description;
	}

	public void setDescription(Text description) {
		this.description = description;
	}

	public Text getComment() {
		return this.comment;
	}

	public void setComment(Text comment) {
		this.comment = comment;
	}
	
	public Date getFirstPublic() {
		return this.firstPublic;
	}

	public void setFirstPublic(Date firstPublic) {
		this.firstPublic = firstPublic;
	}
	
	public Date getLastUpdated() {
		return this.lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Integer getFirstPublicRelease() {
		return this.firstPublicRelease;
	}

	public void setFirstPublicRelease(Integer release) {
		this.firstPublicRelease = release;
	}
	
	public Integer getLastUpdatedRelease() {
		return this.lastUpdatedRelease;
	}

	public void setLastUpdatedRelease(Integer release) {
		this.lastUpdatedRelease = release;
	}
	
	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public String getSubmitterAccession() {
		return submitterAccession;
	}

	public void setSubmitterAccession(String submitterAccession) {
		this.submitterAccession = submitterAccession;
	}

	public Integer getSubmitterWgsVersion() {
		return submitterWgsVersion;
	}

	public void setSubmitterWgsVersion(Integer submitterWgsVersion) {
		this.submitterWgsVersion = submitterWgsVersion;
	}	
	
	public List<Text> getSecondaryAccessions() {
		return this.secondaryAccessions;
	}

	public boolean addSecondaryAccession(Text accession) {
		return this.secondaryAccessions.add(accession);
	}
	
	public boolean addSecondaryAccessions(Collection<Text> accessions) {
		if (accessions == null) {
			return false;
		}
		return this.secondaryAccessions.addAll(accessions);
	}
	
	public boolean removeSecondaryAccession(Text accession) {
		return this.secondaryAccessions.remove(accession);
	}
	
	public List<Text> getKeywords() {
		return this.keywords;
	}

	public boolean addKeyword(Text keyword) {
		return this.keywords.add(keyword);
	}
	
	public boolean addKeywords(Collection<Text> keywords) {
		if (keywords == null) {
			return false;
		}
		return this.keywords.addAll(keywords);
	}
	
	public boolean removeKeyword(Text keyword) {
		return this.keywords.remove(keyword);
	}

	public void removeKeywords() {
		this.keywords.clear();
	}
		
	public List<Text> getProjectAccessions() {
		return this.projectAccessions;
	}
	
	public boolean addProjectAccession(Text projectAccession) {
		return this.projectAccessions.add(projectAccession);
	}
	
	public boolean addProjectAccessions(Collection<Text> projectAccessions) {
		if (projectAccessions == null) {
			return false;
		}
		return this.projectAccessions.addAll(projectAccessions);
	}
	
	public boolean removeProjectAccession(Text projectAccession) {
		return this.projectAccessions.remove(projectAccession);
	}

	public void removeProjectAccessions() {
		this.projectAccessions.clear();
	}
	
	public List<Feature> getFeatures() {
		return this.features;
	}

    public void clearFeatures() {
       for (Feature f : features) {
          f.removeAllQualifiers();
       }
        features.clear();
    }

	@SuppressWarnings("unchecked")
	public <T extends Feature> List<T> getComplexFeatures(String name, List<T> features) {
		if (name == null) {
			return features;
		}
		for (Feature feature : this.features) {
			String featureName=feature.getName();
			if (feature != null && featureName != null
					&& featureName.equals(name)) {
				features.add((T)feature);
			}
		}
		return features;
	}	
		
	public boolean addFeature(Feature feature) {
		return this.features.add(feature);
	}
	
	public boolean addFeatures(Collection<Feature> features) {
		if (features == null) {
			return false;
		}
		return this.features.addAll(features);
	}
	
	public boolean removeFeature(Feature feature) {
		return this.features.remove(feature);
	}
	
	public List<Reference> getReferences() {
		return this.references;
	}

	public boolean addReference(Reference reference) {
		return this.references.add(reference);
	}

	public void addReference(int index, Reference reference) {
		this.references.add(index, reference);
	}

    /**
     * Sets the referenceNumbers on all the references based on their order
     */
    public void renumberReferences() {
        int count = 0;
        for (Reference reference : this.references) {
            reference.setReferenceNumber(count);
            count++;
        }
    }

    public boolean addReferences(Collection<Reference> references) {
		if (references == null) {
			return false;
		}
		return this.references.addAll(references);
	}
	
	public boolean removeReference(Reference reference) {
		return this.references.remove(reference);
	}	

	public void removeReferences() {
		this.references.clear();
	}	
		
	public List<XRef> getXRefs() {
		return this.xRefs;
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
	
	public List<Assembly> getAssemblies() {
		return this.assemblies;
	}

	public boolean addAssembly(Assembly assembly) {
		return this.assemblies.add(assembly);
	}

	public boolean addAssemblies(Collection<Assembly> assemblies) {
		if (assemblies == null) {
			return false;
		}
		return this.assemblies.addAll(assemblies);
	}

	public boolean removeAssembly(Assembly assembly) {
		return this.assemblies.remove(assembly);
	}	

	public void removeAssemblies() {
		this.assemblies.clear();
	}	
		
	public List<Text> getMasterConAccessions() {
		return this.masterConAccessions;
	}
	
	public boolean addMasterConAccession(Text accession) {
		return this.masterConAccessions.add(accession);
	}
	
	public boolean addMasterConAccessions(Collection<Text> accessions) {
		if (accessions == null) {
			return false;
		}
		return this.masterConAccessions.addAll(accessions);
	}
	
	public boolean removeMasterConAccession(Text accession) {
		return this.masterConAccessions.remove(accession);
	}

	public List<Text> getMasterWgsAccessions() {
		return this.masterWgsAccessions;
	}
	
	public boolean addMasterWgsAccession(Text accession) {
		return this.masterWgsAccessions.add(accession);
	}
	
	public boolean addMasterWgsAccessions(Collection<Text> accessions) {
		if (accessions == null) {
			return false;
		}
		return this.masterWgsAccessions.addAll(accessions);
	}
	
	public boolean removeMasterWgsAccession(Text accession) {
		return this.masterWgsAccessions.remove(accession);
	}

	public List<Text> getMasterTpaAccessions() {
		return this.masterTpaAccessions;
	}
	
	public boolean addMasterTpaAccession(Text accession) {
		return this.masterTpaAccessions.add(accession);
	}
	
	public boolean addMasterTpaAccessions(Collection<Text> accessions) {
		if (accessions == null) {
			return false;
		}
		return this.masterTpaAccessions.addAll(accessions);
	}
	
	public boolean removeMasterTpaAccession(Text accession) {
		return this.masterTpaAccessions.remove(accession);
	}
	
	public List<Text> getMasterTsaAccessions() {
		return this.masterTsaAccessions;
	}
	
	public boolean addMasterTsaAccession(Text accession) {
		return this.masterTsaAccessions.add(accession);
	}
	
	public boolean addMasterTsaAccessions(Collection<Text> accessions) {
		if (accessions == null) {
			return false;
		}
		return this.masterTsaAccessions.addAll(accessions);
	}
	
	public boolean removeMasterTsaAccession(Text accession) {
		return this.masterTsaAccessions.remove(accession);
	}
	
	public boolean isMaster() {
		return (this.masterConAccessions.size() > 0 ||
		        this.masterWgsAccessions.size() > 0 ||
		        this.masterTpaAccessions.size() > 0||
		        this.masterTsaAccessions.size() > 0);
	}
	
	public SourceFeature getPrimarySourceFeature () {
		List<SourceFeature> features = new ArrayList<SourceFeature>();
		getComplexFeatures(Feature.SOURCE_FEATURE_NAME, features);
		for (SourceFeature feature : features) {
			if (feature.isFocus()) return feature;
			if (feature.isTransgenic()) return feature;
	    }
		if (features.size() > 0) return features.get(0);
		return null;
	}	
	
	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
//		builder.append(this.id);
		builder.append(this.primaryAccession);
		builder.append(this.version);
		return builder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Entry) {
			final Entry other = (Entry) obj;
			final EqualsBuilder builder = new EqualsBuilder();
//			builder.append(this.id, other.id);
			builder.append(this.primaryAccession, other.primaryAccession);
			builder.append(this.version, other.version);
			builder.append(this.holdDate, other.holdDate);
			builder.append(this.status, other.status);
			builder.append(this.firstPublic, other.firstPublic);
			builder.append(this.lastUpdated, other.lastUpdated);
			builder.append(this.firstPublicRelease, other.firstPublicRelease);
			builder.append(this.lastUpdatedRelease, other.lastUpdatedRelease);
			builder.append(this.dataClass, other.dataClass);
			builder.append(this.division, other.division);
			builder.append(this.description.getText(), other.description.getText());
			builder.append(this.comment.getText(), other.comment.getText());
			builder.append(this.submitterAccession, other.submitterAccession);
			builder.append(this.submitterWgsVersion, other.submitterWgsVersion);
			return builder.isEquals();
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
        if(primaryAccession == null){
            return "No accession";
        }
		return primaryAccession;
	}

	public int compareTo(Entry o) {
		final CompareToBuilder builder = new CompareToBuilder();
//		builder.append(this.id, o.id);
		return builder.toComparison();
	}

	/*
	 * FIX MODE :if entry has to be deleted on any particular error from bulk
	 * file this method can be used, so that entry writer will stop writing that
	 * entry fixed file
	 */
	public void setDelete(boolean delete)
	{
		deleteEntry = delete;
	}

	/*
	 * FIX MODE :if its true,entry will not be written into the fixed file
	 */
	public boolean isDelete()
	{
		return deleteEntry;
	}

	public long getIdLineSequenceLength()
	{
		return idLineSequenceLength;
	}

	public void setIdLineSequenceLength(long idLineSequenceLength)
	{
		this.idLineSequenceLength = idLineSequenceLength;
	}
	
	public boolean isAnnotationOnlyCON()
	{
		return isAnnotationOnlyCON;
	}

	public void setAnnotationOnlyCON(boolean isAnnotationOnlyCON)
	{
		this.isAnnotationOnlyCON = isAnnotationOnlyCON;
	}
	
	public boolean isSingletonAgp() {
		return isSingletonAgp;
	}

	public void setSingletonAgp(boolean isSingletonAgp) {
		this.isSingletonAgp = isSingletonAgp;
	}
	
	public boolean isNonExpandedCON() {
		return isNonExpandedCON;
	}

	public void setNonExpandedCON(boolean isNonExpandedCON) {
		this.isNonExpandedCON = isNonExpandedCON;
	}
	
	public String getBiosampleId()
	{
		for(XRef xref:getXRefs())
		{
			if("BioSample".equals(xref.getDatabase()))
			{
				return xref.getPrimaryAccession();
			}
		}
		return null;
	}

	/**
	 * Release resources allocated in this bean. Can help the GC to better cleanup the heap
	 */
	public void close() {
      this.clearFeatures();
      this.removeAssemblies();
      this.removeKeywords();
      this.removeProjectAccessions();
      this.removeReferences();
	}
}
