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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.OrganismQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.taxonomy.HasTaxon;
import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.api.translation.TranslationTable;

import java.io.Serializable;

public class SourceFeature extends Feature implements HasTaxon, Serializable {

	private static final long serialVersionUID = -4327924654143517534L;
	String anticodonMessage =null;
	protected SourceFeature() {
		super(SOURCE_FEATURE_NAME, false);
	}
	
	public synchronized Taxon getTaxon() {
		OrganismQualifier organismQualifier =
			(OrganismQualifier)getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
		if (organismQualifier == null) {
            organismQualifier = (new QualifierFactory()).createOrganismQualifier(null);
			addQualifier(organismQualifier);
		}
		return organismQualifier.getTaxon();
	}

	public synchronized void setTaxon(Taxon taxon) {
		OrganismQualifier organismQualifier =
			(OrganismQualifier)getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
		if (organismQualifier == null) {
			organismQualifier = (new QualifierFactory()).createOrganismQualifier(null);
            if(taxon != null){
                organismQualifier.setValue(taxon.getScientificName());
            }
			addQualifier(organismQualifier);
		}
		organismQualifier.setTaxon(taxon);		
	}

	public Long getTaxId() {
		return getTaxon().getTaxId();
	}

	public void setTaxId(Long taxId) {
		getTaxon().setTaxId(taxId);
	}

	public String getScientificName() {
		if(getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME)==null)
		{
			return null;
		}
		return getTaxon().getScientificName();
	}

	public void setScientificName(String scientificName) {
		getTaxon().setScientificName(scientificName);
	}

	public String getCommonName() {
		return getTaxon().getCommonName();
	}	
	
	public void setCommonName(String commonName) {
		getTaxon().setCommonName(commonName);
	}	
	
	public boolean isFocus() {
		return (getSingleQualifier(Qualifier.FOCUS_QUALIFIER_NAME) != null);
	}

	public void setFocus(boolean focus) {
		if (focus) {
			setSingleQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		} else {
			removeSingleQualifier(Qualifier.FOCUS_QUALIFIER_NAME);
		}
	}

	public boolean isTransgenic() {
		return (getSingleQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME) != null);
	}

	public void setTransgenic(boolean transgenic) {
		if (transgenic) {
			setSingleQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME);
		} else {
			removeSingleQualifier(Qualifier.TRANSGENIC_QUALIFIER_NAME);
		}
	}

	public Integer getTranslationTable() {
		Taxon taxon = getTaxon();
		if (taxon == null) {
			return null;
		}
		String organelle = getSingleQualifierValue("organelle");
		if (StringUtils.equals(organelle, "mitochondrion")
				|| StringUtils.equals(organelle, "mitochondrion:kinetoplast")) {
			return taxon.getMitochondrialGeneticCode();
		} else if (StringUtils.contains(organelle, "plastid")) {
			return TranslationTable.PLASTID_TRANSLATION_TABLE;
		} else {
			return taxon.getGeneticCode();
		}
	}
	
	public void setMasterLocation()
	{
		Order<Location>featureLocation = new Order<Location>();
		LocationFactory locationFactory=new LocationFactory();
		featureLocation.addLocation(locationFactory.createLocalRange(1l, 1l));
		setLocations(featureLocation);
	}	

	public String getAntiCodonMessage()
	{
		return anticodonMessage;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SourceFeature) {
			final EqualsBuilder builder = new EqualsBuilder();
			builder.appendSuper(super.equals(obj));
			return builder.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		return builder.toString();
	}
}
