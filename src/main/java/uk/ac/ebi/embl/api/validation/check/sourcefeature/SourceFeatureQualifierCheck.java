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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.QualifierHelper;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Description("Any of Qualifiers \"{0}\"   must exist in Source feature if there is an rRNA gene.")
@ExcludeScope( validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class SourceFeatureQualifierCheck extends EntryValidationCheck {

	private final static String DIFFERENT_ORGANISM_MESSAGE_ID = "SourceFeatureQualifierCheck2";
	private final static String MULTIPLE_FOCUS_MESSAGE_ID = "SourceFeatureQualifierCheck3";
	private final static String MULTIPLE_TRANSEGENIC_MESSAGE_ID = "SourceFeatureQualifierCheck4";
	private final static String FOCUS_TRANSEGENIC_EXCLUDE_MESSAGE_ID = "SourceFeatureQualifierCheck5";
	private final static String TRANSEGENIC_SOURCE_MESSAGE_ID = "SourceFeatureQualifierCheck6";
	private final static String NON_UNIQUE_ORGANISM_MESSAGE_ID = "SourceFeatureQualifierCheck7";
	private final static String NOT_SUBMITTABLE_ORGANISM_MESSAGE_ID = "SourceFeatureQualifierCheck8";
	private final static String ENV_SAMPLE_REQUIRED = "EnvSampleRequiredForMetagenome";
	private final static String MORE_THAN_ONE_METAGENOME_SOURCE = "MorethanOneMetagenome";
	private final static String INVALID_METAGENOME_SOURCE = "InvalidMetagenomeSource";
	private final static String FOCUS_MISPLACED_MESSAGE_ID = "FocusAllowedOnlyInPrimarySource";

	public SourceFeatureQualifierCheck() {
	}

	public ValidationResult check(Entry entry) {

		result = new ValidationResult();

		if (entry == null) {
			return result;
		}
		Collection<Feature> sources = SequenceEntryUtils.getFeatures(
				Feature.SOURCE_FEATURE_NAME, entry);

		if (sources == null) {
			return result;
		}
		int sourcesNumber = sources.size();

		if (sourcesNumber > 1) {
			// Multiple sourcefeatures with different organisms: one has to have
			// a /focus or /transgenic qualifiers
			if (isOrganismValueDifferent(sources)) {
				reportError(entry.getOrigin(), DIFFERENT_ORGANISM_MESSAGE_ID);
			}
		}
		
		int focus = 0;
		int transgenic = 0;
		ArrayList<String> organismValues = null;
		for (Feature feature : sources) {
			SourceFeature source = (SourceFeature) feature;

			checkMetagenomeSource(entry.getOrigin(), source);

			if(source !=null && source.getScientificName()!=null && source.getTaxId()==null && !isOrganismUnique(source.getScientificName()))
				reportError(entry.getOrigin(), NON_UNIQUE_ORGANISM_MESSAGE_ID,source.getScientificName());
			if(source!=null&&source.getScientificName()!=null)
			{
				boolean isOrganismSubmittable = getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismSubmittable(source.getScientificName());
				boolean isTaxidSubmittable=isOrganismSubmittable;
				boolean isAnyNameSubmittable=false;
				Long taxId = source.getTaxId();
				if(taxId!=null)		
					isTaxidSubmittable = getEmblEntryValidationPlanProperty().taxonHelper.get().isTaxidSubmittable(taxId);
				if(!isOrganismSubmittable && !isTaxidSubmittable)
				{
					isAnyNameSubmittable = getEmblEntryValidationPlanProperty().taxonHelper.get().isAnyNameSubmittable(source.getScientificName());
					 if(!isAnyNameSubmittable)
						 reportError(entry.getOrigin(),NOT_SUBMITTABLE_ORGANISM_MESSAGE_ID,source.getScientificName());
				}
			}
			if (SequenceEntryUtils.isQualifierAvailable(
					Qualifier.ORGANISM_QUALIFIER_NAME, feature)) {
                Qualifier orgQualifier = SequenceEntryUtils.getQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, feature);
                  if (orgQualifier.isValue()) {
                    organismValues.add(orgQualifier.getValue());
                    }
            }
			if(source.isFocus())
			{
				focus=source.getQualifiers(Qualifier.FOCUS_QUALIFIER_NAME).size();
			}
			if(source.isTransgenic())
				transgenic=source.getQualifiers(Qualifier.TRANSGENIC_QUALIFIER_NAME).size();
           }

		List<Qualifier> focusQuals = SequenceEntryUtils.getQualifiers(Qualifier.FOCUS_QUALIFIER_NAME, entry);
		if (focusQuals != null && focusQuals.size() > 0) {
			if (focusQuals.size() == 1) {
				//focus qualifier is allowed only in primary source feature
				if (null == entry.getPrimarySourceFeature() ||
						null == entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.FOCUS_QUALIFIER_NAME)) {
					reportError(entry.getOrigin(), FOCUS_MISPLACED_MESSAGE_ID);
				}
			} else {
				reportError(entry.getOrigin(), MULTIPLE_FOCUS_MESSAGE_ID);
			}
		}

		if (focus > 0 && transgenic > 0) {//focus not allowed when /transgenic is used
			reportError(entry.getOrigin(), FOCUS_TRANSEGENIC_EXCLUDE_MESSAGE_ID);
		}

		if (sources.size() < 2 && transgenic > 0) {
			//entries with /transgenic must have at least 2 source features
			reportError(entry.getOrigin(), TRANSEGENIC_SOURCE_MESSAGE_ID);
		}
		if (transgenic > 1) {
			//multiple /transgenic qualifiers not allowed
			reportError(entry.getOrigin(), MULTIPLE_TRANSEGENIC_MESSAGE_ID);
		}

		return result;
	}

	//ENA-2825
	private void checkMetagenomeSource(Origin origin, SourceFeature source) {
		List<Qualifier> metagenomeSourceQual = source.getQualifiers(Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);

		if(metagenomeSourceQual != null && !metagenomeSourceQual.isEmpty()) {
			Qualifier envSample = source.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
			if(envSample == null) {
				reportError(origin, ENV_SAMPLE_REQUIRED);
			}
			if(metagenomeSourceQual.size() > 1) {
				reportError(origin, MORE_THAN_ONE_METAGENOME_SOURCE);
			}
			String metegenomeSource = metagenomeSourceQual.get(0).getValue();
			if(metegenomeSource == null || (!metegenomeSource.contains("metagenome") && metegenomeSource.contains("Metagenome"))) {
				reportError(origin, INVALID_METAGENOME_SOURCE, metegenomeSource);
			}

			List<Taxon> taxon = getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonsByScientificName(metegenomeSource);

			if(taxon == null || taxon.isEmpty() || taxon.get(0).getTaxId() == 408169L || !getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismMetagenome(metegenomeSource)) {
				reportError(origin, INVALID_METAGENOME_SOURCE, metegenomeSource);
			}

		}
	}

	public boolean isOrganismValueDifferent(Collection<Feature> sources) {
		ArrayList<String> organismValues = new ArrayList<String>();

		for (Feature sourceFeature : sources) {
			SourceFeature feature = (SourceFeature) sourceFeature;
			if (feature.isFocus() || feature.isTransgenic())
				return false;

		}

		for (Feature sourceFeature : sources) {
			SourceFeature feature = (SourceFeature) sourceFeature;
			if (SequenceEntryUtils.isQualifierAvailable(
					Qualifier.ORGANISM_QUALIFIER_NAME, feature))
				organismValues.add(SequenceEntryUtils.getQualifierValue(
						Qualifier.ORGANISM_QUALIFIER_NAME, feature));

		}

		for (int j = organismValues.size() - 1; j >= 0; j--) {
			for (int k = 0; k < organismValues.size(); k++) {
				if (!organismValues.get(j).equals(organismValues.get(k))) {
					return true;
				}
			}
		}
		return false;

	}
	
	public boolean isOrganismUnique(String scientificName)
    {
		List<Taxon> taxons=getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonsByScientificName(scientificName);

    	if(taxons!=null&&taxons.size()>1)
        {
        	return false;
        }
        return true;
    }

}
