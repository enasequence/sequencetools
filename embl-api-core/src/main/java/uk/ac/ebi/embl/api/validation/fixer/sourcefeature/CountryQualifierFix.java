/*******************************************************************************
 * Copyright 2012-2013 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@CheckDataSet(dataSetNames={"feature-regex-groups.tsv"})
@Description("\"{0}\" qualifier value \"{1}\" is invalid, a note has been added.")
@ExcludeScope(validationScope={ValidationScope.ARRAYEXPRESS, ValidationScope.ASSEMBLY_CHROMOSOME, ValidationScope.ASSEMBLY_CONTIG, ValidationScope.ASSEMBLY_MASTER, ValidationScope.ASSEMBLY_SCAFFOLD, ValidationScope.ASSEMBLY_TRANSCRIPTOME, ValidationScope.EGA, ValidationScope.EMBL, ValidationScope.EMBL, ValidationScope.EMBL_TEMPLATE, ValidationScope.EPO, ValidationScope.EPO_PEPTIDE, ValidationScope.INSDC})
@RemoteExclude
public class CountryQualifierFix extends FeatureValidationCheck
{
	private final static String COUNTRY_QUALIFIER_VALUE_FIX_ID = "CountryQualifierFix_1";

	QualifierFactory qualifierFactory = new QualifierFactory();


	//private DataSet valuesSet;

	private Set<String> countries = new HashSet<>();

    public CountryQualifierFix() { }

	CountryQualifierFix(DataSet qualifierValueSet)
	{
		//this.valuesSet = qualifierValueSet;
	}

/*	public void init() {

    	valuesSet = GlobalDataSets.getDataSet("feature-regex-groups.tsv");

		if (valuesSet != null) {
			for (DataRow regexpRow : valuesSet.getRows()) {
				if (regexpRow.getString(0).equals("country")) {
					Stream.of(regexpRow.getStringArray(3)).forEach(country -> countries.add(country.trim().toLowerCase()));
					break;
				}
			}
		} else {
			throw new IllegalArgumentException("Failed to set qualifier values in CountryQualifierFix!");
		}
	}*/

	private Set<String> getCountries() {
		Set<String> countries = new HashSet<>();
		DataSet valuesSet = GlobalDataSets.getDataSet("feature-regex-groups.tsv");

		if (valuesSet != null) {
			for (DataRow regexpRow : valuesSet.getRows()) {
				if (regexpRow.getString(0).equals("country")) {
					Stream.of(regexpRow.getStringArray(3)).forEach(country -> countries.add(country.trim().toLowerCase()));
					break;
				}
			}
		} else {
			throw new IllegalArgumentException("Failed to set qualifier values in CountryQualifierFix!");
		}
		return countries;
	}

	/*@Override
	public void setPopulated() {
		init();
		super.setPopulated();
	}*/


	public ValidationResult check(Feature feature) {
		result = new ValidationResult();
		Set<String> countries = getCountries();
		if (null != feature && feature instanceof SourceFeature) {

			SourceFeature source = (SourceFeature) feature;
			List<Qualifier> countryQualifiers = source.getQualifiers(Qualifier.COUNTRY_QUALIFIER_NAME);
			for (Qualifier countryQualifier : countryQualifiers) {
				String countryQualifierValue = countryQualifier.getValue();

				if (!countries.contains(countryQualifierValue.trim().toLowerCase())) {
					source.removeQualifier(countryQualifier);
					source.addQualifier(qualifierFactory.createQualifier(Qualifier.NOTE_QUALIFIER_NAME, countryQualifierValue));
					reportMessage(Severity.FIX, countryQualifier.getOrigin(), COUNTRY_QUALIFIER_VALUE_FIX_ID, Qualifier.COUNTRY_QUALIFIER_NAME, countryQualifier.getValue());
				}
			}
	    }

		return result;

	}

}
