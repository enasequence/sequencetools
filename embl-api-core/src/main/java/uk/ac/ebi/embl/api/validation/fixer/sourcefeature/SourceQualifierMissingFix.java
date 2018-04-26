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

import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

@Description("Added source qualifier {0} as qualifier {2} value matches the pattern {3}"
		+ "Qualifier \"environment_sample\" has been added to the \"Source\" feature as Organism is a Metagenome.")
@RemoteExclude
public class SourceQualifierMissingFix extends EntryValidationCheck
{

	private static final String unculturedPatternEnvironmentQualifierFix_ID = "SourceQualifierMissingFix_1";
	private static final String metagenomeEnvironmentQualifierFix_ID = "SourceQualifierMissingFix_2";
	private static final String metagenomeIsolationQualifierFix_ID = "SourceQualifierMissingFix_3";
	private static final String lineageEnvironmentQualifierFix_ID = "SourceQualifierMissingFix_4";



	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if (entry.getPrimarySourceFeature() == null||entry.getPrimarySourceFeature().getQualifiers().size()==0)
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
			Pattern pattern = Pattern.compile("^(uncultured).*");
			if (pattern.matcher(scientificName).matches() && !is_environment_sample_exists)
			{
				entry.getPrimarySourceFeature().addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
				is_environment_sample_exists=true;
				reportMessage(	Severity.FIX,
								entry.getPrimarySourceFeature().getOrigin(),
								unculturedPatternEnvironmentQualifierFix_ID);
			}
			else
				if (getEmblEntryValidationPlanProperty() != null && getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismMetagenome(scientificName))
				{
					if (!is_environment_sample_exists)
					{
						entry.getPrimarySourceFeature().addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
						is_environment_sample_exists=true;
						reportMessage(	Severity.FIX,
										entry.getPrimarySourceFeature().getOrigin(),
										metagenomeEnvironmentQualifierFix_ID);
					}

					if (is_environment_sample_exists && !is_isolation_source_exists)
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
						reportMessage(	Severity.FIX,
										entry.getPrimarySourceFeature().getOrigin(),
										metagenomeIsolationQualifierFix_ID,
										isolation_source_value);
					}
				}
			if(!is_environment_sample_exists)
			{
				Taxon taxon= getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonByScientificName(scientificName);
			
				if(taxon!=null)
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

		return result;
	}

}
