/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

@Description("Added source qualifier {0} as qualifier {2} value matches the pattern {3}"
		+ "Qualifier \"environment_sample\" has been added to the \"Source\" feature as Organism is a Metagenome.")
@ExcludeScope(validationScope = { ValidationScope.NCBI })
public class SourceQualifierMissingFix extends EntryValidationCheck
{

	private static final String addEnvironmentalSampleForUnculturedOrg = "SourceQualifierMissingFix_1";
	private static final String addEnvironmentalSampleForMetagenomeOrg = "SourceQualifierMissingFix_2";
	private static final String metagenomeIsolationQualifierFix_ID = "SourceQualifierMissingFix_3";
	private static final String unculturedOrgIsolationQualifierFix_ID = "SourceQualifierMissingFix_3";
	private static final String lineageEnvironmentQualifierFix_ID = "SourceQualifierMissingFix_4";
	private static final String strainToIsolateFix = "SourceQualifierMissingFix_5";
	private static final String strainRemovalFix = "SourceQualifierMissingFix_6";
	private static final String METAGENOME_SOURCE_QUAL_REMOVED = "MetagenomeSourceQualifierRemoved";
	private static final String QUALIFIER_VALUE_CHANGE = "QualifierValueChange";


	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if (entry.getPrimarySourceFeature() == null || entry.getPrimarySourceFeature().getQualifiers().size()==0)
		{
			return result;
		}

		if(entry.getPrimarySourceFeature().getTaxId()!=null)//set the scientific name based on taxid
		{
            Taxon taxon=getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonById(entry.getPrimarySourceFeature().getTaxId());
            if(taxon!=null)
            entry.getPrimarySourceFeature().setScientificName(taxon.getScientificName());
		}
		
		String scientificName = entry.getPrimarySourceFeature().getScientificName();
		if(NumberUtils.isNumber(scientificName))
		{
			Taxon taxon=getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonById(new Long(scientificName));
			entry.getPrimarySourceFeature().setScientificName(taxon.getScientificName());
			entry.getPrimarySourceFeature().setTaxId(taxon.getTaxId());
		}
		boolean is_environment_sample_exists = entry.getPrimarySourceFeature().getQualifiers(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME).size() != 0;
		boolean is_isolation_source_exists = entry.getPrimarySourceFeature().getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).size() != 0;

		if (is_environment_sample_exists && is_isolation_source_exists)
		{
			return result;
		}

		if (scientificName != null)
		{
			boolean isSourceOrganismMetagenome = getEmblEntryValidationPlanProperty() != null
					&&  getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismMetagenome(scientificName);

			boolean hasMetagenomeSource = fixMetagenomeSource(entry.getPrimarySourceFeature(), scientificName);

			if((isSourceOrganismMetagenome || hasMetagenomeSource) && !is_environment_sample_exists) {

				entry.getPrimarySourceFeature().addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
				is_environment_sample_exists = true;
				reportMessage(	Severity.FIX,
						entry.getPrimarySourceFeature().getOrigin(),
						addEnvironmentalSampleForMetagenomeOrg);
			}

			Pattern pattern = Pattern.compile("^(uncultured).*");
			if (pattern.matcher(scientificName).matches() && !is_environment_sample_exists)
			{
				entry.getPrimarySourceFeature().addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
				is_environment_sample_exists = true;
				reportMessage(	Severity.FIX,
								entry.getPrimarySourceFeature().getOrigin(),
						addEnvironmentalSampleForUnculturedOrg);
			}
			else if (isSourceOrganismMetagenome && !is_isolation_source_exists)
				{

					String isolation_source_value = null;
					if (scientificName.contains("metagenome"))
					{
						isolation_source_value = scientificName.replace("metagenome","");
					}
					else
						if (scientificName.contains("metagenomes"))
						{
							isolation_source_value = scientificName.replace("metagenomes","");
						}

					isolation_source_value = isolation_source_value == null ||isolation_source_value.isEmpty() ? "unknown" : isolation_source_value.trim();

					entry.getPrimarySourceFeature().addQualifier(	Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME,
																	isolation_source_value);
					is_isolation_source_exists = true;
					reportMessage(	Severity.FIX,
									entry.getPrimarySourceFeature().getOrigin(),
									metagenomeIsolationQualifierFix_ID,
									isolation_source_value);

				}

			if(!is_environment_sample_exists)
			{
				Taxon taxon= getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonByScientificName(scientificName);
			
				if(taxon != null && taxon.getLineage() != null)
				{
					String lineage=taxon.getLineage();
					if(lineage.contains("environmental samples"))
					{
						entry.getPrimarySourceFeature().addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
						is_environment_sample_exists=true;
						reportMessage(	Severity.FIX,
										entry.getPrimarySourceFeature().getOrigin(),lineageEnvironmentQualifierFix_ID
										);
					}
				}
			}
			
		}

		if(!is_isolation_source_exists && is_environment_sample_exists) {
			entry.getPrimarySourceFeature().addQualifier(	Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, "unknown");
			reportMessage(	Severity.FIX,
					entry.getPrimarySourceFeature().getOrigin(),
					unculturedOrgIsolationQualifierFix_ID, "unknown");
		}

		if(is_environment_sample_exists && entry.getPrimarySourceFeature().getQualifiers(Qualifier.STRAIN_QUALIFIER_NAME).size() != 0) {
			entry.getPrimarySourceFeature().removeQualifier(Qualifier.STRAIN_QUALIFIER_NAME);
			if(entry.getPrimarySourceFeature().getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size() == 0) {
				entry.getPrimarySourceFeature().addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME,"unknown");
				reportMessage( Severity.FIX, entry.getPrimarySourceFeature().getOrigin(),strainRemovalFix);
			} else {
				reportMessage( Severity.FIX, entry.getPrimarySourceFeature().getOrigin(),strainToIsolateFix);
			}
		}

		return result;
	}

	private boolean fixMetagenomeSource(SourceFeature source, String organismScientificName) {

		List<Qualifier> metagenomeSourceQualifiers = source.getQualifiers(Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);

		if (metagenomeSourceQualifiers != null && !metagenomeSourceQualifiers.isEmpty() ) {

			if (metagenomeSourceQualifiers.size() == 1) {
				String metagenomeSourceScientificName = metagenomeSourceQualifiers.get(0).getValue();
				if (getEmblEntryValidationPlanProperty() != null) {
					Taxon taxon = getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonByScientificName(metagenomeSourceScientificName);
					if(taxon != null) {
						if (taxon.getScientificName().equalsIgnoreCase(organismScientificName)) {
							source.removeSingleQualifier(Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME);
							reportMessage(Severity.FIX, source.getOrigin(), METAGENOME_SOURCE_QUAL_REMOVED);
							return false;
						} else {
							if (!metagenomeSourceScientificName.equals(taxon.getScientificName())) {
								metagenomeSourceQualifiers.get(0).setValue(taxon.getScientificName());
								reportMessage(Severity.FIX, source.getOrigin(), QUALIFIER_VALUE_CHANGE, metagenomeSourceScientificName, taxon.getScientificName());
							}
						}
					}
				}

			}
			return true;
		}

		return false;
	}



}
